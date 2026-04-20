package com.uit.buddy.service.academic.impl;

import com.uit.buddy.dto.response.academic.AcademicSummaryResponse;
import com.uit.buddy.dto.response.academic.GradeResponse;
import com.uit.buddy.dto.response.academic.SemesterGradesResponse;
import com.uit.buddy.entity.academic.AcademicSummary;
import com.uit.buddy.entity.academic.Grade;
import com.uit.buddy.entity.academic.Semester;
import com.uit.buddy.entity.academic.SemesterSummary;
import com.uit.buddy.enums.CourseCategoryCode;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.grade.GradeErrorCode;
import com.uit.buddy.exception.grade.GradeException;
import com.uit.buddy.mapper.academic.GradeMapper;
import com.uit.buddy.repository.academic.AcademicSummaryRepository;
import com.uit.buddy.repository.academic.CurriculumCourseRepository;
import com.uit.buddy.repository.academic.GradeRepository;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.academic.SemesterSummaryRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.GradeService;
import com.uit.buddy.util.GradePdfParser;
import com.uit.buddy.util.GradePdfParser.AcademicMetrics;
import com.uit.buddy.util.GradePdfParser.CourseGradeExtract;
import com.uit.buddy.util.GradePdfParser.ParsedGradeData;
import com.uit.buddy.util.GradePdfParser.SemesterMetrics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SemesterRepository semesterRepository;
    private final SemesterSummaryRepository semesterSummaryRepository;
    private final AcademicSummaryRepository academicSummaryRepository;
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final StudentRepository studentRepository;
    private final GradeMapper gradeMapper;
    private final GradePdfParser gradePdfParser;

    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");
    private static final Pattern ENG_NUMERIC_CODE_PATTERN = Pattern.compile("^ENG(\\d{1,2})$");
    private static final Pattern EN_NUMERIC_CODE_PATTERN = Pattern.compile("^EN(\\d{1,3})$");

    @Override
    @Transactional
    public String importGradePdf(String mssv, MultipartFile gradeFile) {
        log.info("Importing grade PDF for student {}", mssv);

        ParsedGradeData parsedData = gradePdfParser.parse(gradeFile);
        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new GradeException(GradeErrorCode.STUDENT_NOT_FOUND));

        if (parsedData.studentMssv() == null || !parsedData.studentMssv().equals(mssv)) {
            throw new GradeException(GradeErrorCode.INVALID_FILE, "MSSV in PDF does not match authenticated user");
        }

        String fallbackSemesterCode = determineSemester(parsedData.semesterMetrics());
        Map<String, String> semesterCodeCache = new HashMap<>();
        Map<String, CurriculumMetadata> curriculumMetadataCache = new HashMap<>();
        String majorCode = resolveMajorCode(student);
        Integer academicStartYear = resolveAcademicStartYear(student);

        int savedCount = 0;
        for (CourseGradeExtract courseGrade : parsedData.courseGrades()) {
            String semesterCode = resolveSemesterCode(courseGrade, fallbackSemesterCode, semesterCodeCache);
            CurriculumMetadata curriculumMetadata = resolveCurriculumMetadata(courseGrade.courseCode(), majorCode,
                    academicStartYear, curriculumMetadataCache);

            Integer parsedCredits = courseGrade.credits() != null ? courseGrade.credits() : 0;

            Grade grade = Grade.builder().mssv(mssv).semesterCode(semesterCode).courseCode(courseGrade.courseCode())
                    .courseName(courseGrade.courseName()).credits(parsedCredits)
                    .courseType(curriculumMetadata != null ? curriculumMetadata.categoryCode() : null)
                    .processGrade(normalizeGrade(courseGrade.processGrade()))
                    .midtermGrade(normalizeGrade(courseGrade.midtermGrade()))
                    .finalGrade(normalizeGrade(courseGrade.finalGrade()))
                    .labGrade(normalizeGrade(courseGrade.labGrade()))
                    .totalGrade(normalizeGrade(courseGrade.totalGrade())).build();

            Optional<Grade> existing = gradeRepository.findByMssvAndCourseCodeAndSemesterCode(mssv,
                    courseGrade.courseCode(), semesterCode);

            if (existing.isPresent()) {
                grade.setId(existing.get().getId());
            }

            gradeRepository.save(grade);
            savedCount++;
        }

        synchronizeSemesterSummaries(student);
        synchronizeAcademicSummary(student, parsedData.academicMetrics());

        log.info("Successfully imported {} grades for student {}", savedCount, mssv);
        return String.format("Imported %d courses successfully", savedCount);
    }

    private String resolveSemesterCode(CourseGradeExtract courseGrade, String fallbackSemesterCode,
            Map<String, String> semesterCodeCache) {
        if (courseGrade.semesterNumber() == null || courseGrade.yearStart() == null || courseGrade.yearEnd() == null) {
            return fallbackSemesterCode;
        }

        String key = courseGrade.semesterNumber() + "-" + courseGrade.yearStart() + "-" + courseGrade.yearEnd();
        if (semesterCodeCache.containsKey(key)) {
            return semesterCodeCache.get(key);
        }

        String semesterCode = determineSemester(courseGrade.semesterNumber(), courseGrade.yearStart(),
                courseGrade.yearEnd());
        semesterCodeCache.put(key, semesterCode);
        return semesterCode;
    }

    private String determineSemester(Optional<SemesterMetrics> metricsOpt) {
        if (metricsOpt.isEmpty()) {
            throw new GradeException(GradeErrorCode.INVALID_FILE, "Cannot determine semester from PDF summary");
        }

        SemesterMetrics metrics = metricsOpt.get();
        return determineSemester(metrics.semesterNumber(), metrics.yearStart(), metrics.yearEnd());
    }

    private String determineSemester(Integer semesterNumber, String yearStart, String yearEnd) {
        if (semesterNumber == null || yearStart == null || yearEnd == null) {
            throw new GradeException(GradeErrorCode.INVALID_FILE,
                    "Cannot determine semester info (hoc ky/nam hoc) from PDF");
        }

        Optional<Semester> semester = semesterRepository
                .findBySemesterNumberAndYearStartAndYearEnd(String.valueOf(semesterNumber), yearStart, yearEnd);

        if (semester.isPresent()) {
            return semester.get().getSemesterCode();
        }

        throw new GradeException(GradeErrorCode.INVALID_FILE,
                "Semester not found in system: HK" + semesterNumber + " " + yearStart + "-" + yearEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public SemesterGradesResponse getGradesBySemester(String mssv, String semesterCode) {
        log.info("Getting grades for student {} in semester {}", mssv, semesterCode);

        List<Grade> grades = gradeRepository.findByMssvAndSemesterCode(mssv, semesterCode);
        Semester semester = semesterRepository.findById(semesterCode)
                .orElseThrow(() -> new GradeException(GradeErrorCode.INVALID_FILE, "Semester not found"));
        SemesterSummary summary = semesterSummaryRepository.findByMssvAndSemesterCode(mssv, semesterCode).orElse(null);
        Integer accumulatedCredits = summary != null && summary.getAccumulatedCredits() != null
                ? summary.getAccumulatedCredits()
                : calculateAccumulatedCreditsUntilSemester(mssv, semester);

        return buildSemesterGradesResponse(semester, grades, summary, accumulatedCredits);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemesterGradesResponse> getAllGrades(String mssv) {
        log.info("Getting all grades for student {}", mssv);

        List<Grade> allGrades = gradeRepository.findByMssv(mssv);
        Map<String, SemesterSummary> summaryBySemesterCode = semesterSummaryRepository.findByMssv(mssv).stream()
                .collect(Collectors.toMap(SemesterSummary::getSemesterCode, value -> value, (left, right) -> left));

        Map<String, List<Grade>> gradesBySemester = allGrades.stream()
                .collect(Collectors.groupingBy(Grade::getSemesterCode));

        Set<String> semesterCodes = gradesBySemester.keySet();

        List<Semester> semesters = semesterRepository.findAllById(semesterCodes);
        List<Semester> sortedSemesters = semesters.stream().sorted(Comparator.comparing(Semester::getStartDate))
                .toList();

        List<SemesterGradesResponse> responses = new ArrayList<>();
        int runningAccumulatedCredits = 0;

        for (Semester semester : sortedSemesters) {
            List<Grade> semesterGrades = gradesBySemester.getOrDefault(semester.getSemesterCode(),
                    Collections.emptyList());
            SemesterSummary semesterSummary = summaryBySemesterCode.get(semester.getSemesterCode());

            Integer totalCredits = resolveTotalCredits(semesterGrades, semesterSummary);
            Integer accumulatedCredits = semesterSummary != null ? semesterSummary.getAccumulatedCredits() : null;
            if (accumulatedCredits == null) {
                runningAccumulatedCredits += totalCredits;
                accumulatedCredits = runningAccumulatedCredits;
            } else {
                runningAccumulatedCredits = accumulatedCredits;
            }

            responses.add(buildSemesterGradesResponse(semester, semesterGrades, semesterSummary, accumulatedCredits));
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicSummaryResponse getAcademicSummary(String mssv) {
        AcademicSummary summary = academicSummaryRepository.findByMssv(mssv).orElse(null);
        if (summary == null) {
            return AcademicSummaryResponse.builder().attemptedCredits(0).accumulatedCredits(0).attemptedGpa(0F)
                    .accumulatedGpa(0F).majorProgress(0F).accumulatedGeneralCredits(0)
                    .accumulatedFoundationCredits(0).accumulatedMajorCredits(0).accumulatedElectiveCredits(0)
                    .accumulatedGraduationCredits(0).build();
        }

        return AcademicSummaryResponse.builder()
                .attemptedCredits(summary.getAttemptedCredits() != null ? summary.getAttemptedCredits() : 0)
                .accumulatedCredits(summary.getAccumulatedCredits() != null ? summary.getAccumulatedCredits() : 0)
                .attemptedGpa(summary.getAttemptedGpa() != null ? summary.getAttemptedGpa() : 0F)
                .accumulatedGpa(summary.getAccumulatedGpa() != null ? summary.getAccumulatedGpa() : 0F)
                .majorProgress(summary.getMajorProgress() != null ? summary.getMajorProgress() : 0F)
                .accumulatedGeneralCredits(
                        summary.getAccumulatedGeneralCredits() != null ? summary.getAccumulatedGeneralCredits() : 0)
                .accumulatedFoundationCredits(summary.getAccumulatedFoundationCredits() != null
                        ? summary.getAccumulatedFoundationCredits()
                        : 0)
                .accumulatedMajorCredits(
                        summary.getAccumulatedMajorCredits() != null ? summary.getAccumulatedMajorCredits() : 0)
                .accumulatedElectiveCredits(
                        summary.getAccumulatedElectiveCredits() != null ? summary.getAccumulatedElectiveCredits() : 0)
                .accumulatedGraduationCredits(summary.getAccumulatedGraduationCredits() != null
                        ? summary.getAccumulatedGraduationCredits()
                        : 0)
                .build();
    }

    private SemesterGradesResponse buildSemesterGradesResponse(Semester semester, List<Grade> grades,
            SemesterSummary semesterSummary, Integer accumulatedCredits) {
        List<GradeResponse> gradeResponses = grades.stream().map(gradeMapper::toResponse).collect(Collectors.toList());

        Integer totalCredits = resolveTotalCredits(grades, semesterSummary);
        Float gradeScale10 = resolveGradeScale10(grades, semesterSummary);
        Float gradeScale4 = resolveGradeScale4(gradeScale10, semesterSummary);
        Map<CourseCategoryCode, Integer> totalCreditsByCategory = resolveCreditsByCategory(grades, semesterSummary);

        return SemesterGradesResponse.builder().semesterCode(semester.getSemesterCode()).totalCredits(totalCredits)
                .accumulatedCredits(accumulatedCredits != null ? accumulatedCredits : 0)
                .averageGradeScale10(gradeScale10).averageGradeScale4(gradeScale4)
                .totalCreditsByCategory(totalCreditsByCategory).grades(gradeResponses).build();
    }

    private Integer calculateAccumulatedCreditsUntilSemester(String mssv, Semester targetSemester) {
        List<Grade> allGrades = gradeRepository.findByMssv(mssv);
        if (allGrades.isEmpty()) {
            return 0;
        }

        Map<String, List<Grade>> gradesBySemester = allGrades.stream()
                .collect(Collectors.groupingBy(Grade::getSemesterCode));

        Map<String, SemesterSummary> summaryBySemesterCode = semesterSummaryRepository.findByMssv(mssv).stream()
                .collect(Collectors.toMap(SemesterSummary::getSemesterCode, value -> value, (left, right) -> left));

        List<Semester> semesters = semesterRepository.findAllById(gradesBySemester.keySet());

        return semesters.stream().filter(semester -> isOnOrBeforeTargetSemester(semester, targetSemester))
                .mapToInt(semester -> resolveTotalCredits(
                        gradesBySemester.getOrDefault(semester.getSemesterCode(), Collections.emptyList()),
                        summaryBySemesterCode.get(semester.getSemesterCode())))
                .sum();
    }

    private boolean isOnOrBeforeTargetSemester(Semester semester, Semester targetSemester) {
        if (semester.getStartDate() != null && targetSemester.getStartDate() != null) {
            return !semester.getStartDate().isAfter(targetSemester.getStartDate());
        }

        return Objects.equals(semester.getSemesterCode(), targetSemester.getSemesterCode());
    }

    private Integer resolveTotalCredits(List<Grade> grades, SemesterSummary semesterSummary) {
        Integer totalCredits = semesterSummary != null && semesterSummary.getTermCredits() != null
                ? semesterSummary.getTermCredits()
                : calculateTotalCredits(grades);
        return totalCredits != null ? totalCredits : 0;
    }

    private Float resolveGradeScale10(List<Grade> grades, SemesterSummary semesterSummary) {
        Float gradeScale10 = semesterSummary != null && semesterSummary.getTermGpa() != null
                ? roundToTwoDecimals(semesterSummary.getTermGpa())
                : roundToTwoDecimals(calculateWeightedAverage(grades));
        return gradeScale10 != null ? gradeScale10 : 0F;
    }

    private Float resolveGradeScale4(Float gradeScale10, SemesterSummary semesterSummary) {
        if (semesterSummary != null && semesterSummary.getTermGpaScale4() != null) {
            return roundToTwoDecimals(semesterSummary.getTermGpaScale4());
        }

        return convertGradeScale10ToScale4(gradeScale10);
    }

    private Float convertGradeScale10ToScale4(Float gradeScale10) {
        float normalized = gradeScale10 != null ? gradeScale10 : 0F;
        return roundToTwoDecimals((normalized / 10F) * 4F);
    }

    private Map<CourseCategoryCode, Integer> resolveCreditsByCategory(List<Grade> grades,
            SemesterSummary semesterSummary) {
        if (hasCompleteCategoryCache(semesterSummary)) {
            return buildCategoryMapFromSummary(semesterSummary);
        }

        return calculateCreditsByCategory(grades);
    }

    private boolean hasCompleteCategoryCache(SemesterSummary semesterSummary) {
        return semesterSummary != null && semesterSummary.getTermDcCredits() != null
                && semesterSummary.getTermCsnnCredits() != null && semesterSummary.getTermCsnCredits() != null
                && semesterSummary.getTermCnCredits() != null && semesterSummary.getTermTottnCredits() != null
                && semesterSummary.getTermTcCredits() != null;
    }

    private Map<CourseCategoryCode, Integer> buildCategoryMapFromSummary(SemesterSummary semesterSummary) {
        Map<CourseCategoryCode, Integer> creditsByCategory = new LinkedHashMap<>();
        creditsByCategory.put(CourseCategoryCode.DC, semesterSummary.getTermDcCredits());
        creditsByCategory.put(CourseCategoryCode.CSNN, semesterSummary.getTermCsnnCredits());
        creditsByCategory.put(CourseCategoryCode.CSN, semesterSummary.getTermCsnCredits());
        creditsByCategory.put(CourseCategoryCode.CN, semesterSummary.getTermCnCredits());
        creditsByCategory.put(CourseCategoryCode.TOTTN, semesterSummary.getTermTottnCredits());
        creditsByCategory.put(CourseCategoryCode.TC, semesterSummary.getTermTcCredits());
        return creditsByCategory;
    }

    private Map<CourseCategoryCode, Integer> calculateCreditsByCategory(List<Grade> grades) {
        Map<CourseCategoryCode, Integer> creditsByCategory = new LinkedHashMap<>();
        for (CourseCategoryCode categoryCode : CourseCategoryCode.values()) {
            creditsByCategory.put(categoryCode, 0);
        }

        for (Grade grade : grades) {
            if (grade.getCredits() == null || grade.getCourseType() == null || grade.getCourseType().isBlank()) {
                continue;
            }

            CourseCategoryCode categoryCode = CourseCategoryCode.valueOf(grade.getCourseType());
            creditsByCategory.merge(categoryCode, grade.getCredits(), Integer::sum);
        }

        return creditsByCategory;
    }

    private void synchronizeSemesterSummaries(Student student) {
        String mssv = student.getMssv();
        List<Grade> allGrades = gradeRepository.findByMssv(mssv);
        Map<String, List<Grade>> gradesBySemester = allGrades.stream()
                .collect(Collectors.groupingBy(Grade::getSemesterCode));

        if (gradesBySemester.isEmpty()) {
            return;
        }

        List<Semester> sortedSemesters = semesterRepository.findAllById(gradesBySemester.keySet()).stream()
                .sorted(Comparator.comparing(Semester::getStartDate)).toList();

        Set<String> resolvedSemesterCodes = sortedSemesters.stream().map(Semester::getSemesterCode)
                .collect(Collectors.toSet());
        gradesBySemester.keySet().stream().filter(code -> !resolvedSemesterCodes.contains(code))
                .forEach(code -> log.warn("Skipping semester summary sync because semester {} was not found", code));

        int accumulatedCredits = 0;

        for (Semester semester : sortedSemesters) {
            String semesterCode = semester.getSemesterCode();
            List<Grade> grades = gradesBySemester.getOrDefault(semesterCode, Collections.emptyList());

            Integer termCredits = calculateTotalCredits(grades);
            Float termGpa = roundToTwoDecimals(calculateWeightedAverage(grades));
            Float termGpaScale4 = convertGradeScale10ToScale4(termGpa != null ? termGpa : 0F);
            Map<CourseCategoryCode, Integer> creditsByCategory = calculateCreditsByCategory(grades);

            accumulatedCredits += termCredits != null ? termCredits : 0;

            SemesterSummary summary = semesterSummaryRepository.findByMssvAndSemesterCode(mssv, semesterCode)
                    .orElseGet(() -> SemesterSummary.builder().student(student).semester(semester).build());

            summary.setTermCredits(termCredits);
            summary.setTermGpa(termGpa);
            summary.setTermGpaScale4(termGpaScale4);
            summary.setAccumulatedCredits(accumulatedCredits);
            summary.setTermDcCredits(creditsByCategory.get(CourseCategoryCode.DC));
            summary.setTermCsnnCredits(creditsByCategory.get(CourseCategoryCode.CSNN));
            summary.setTermCsnCredits(creditsByCategory.get(CourseCategoryCode.CSN));
            summary.setTermCnCredits(creditsByCategory.get(CourseCategoryCode.CN));
            summary.setTermTottnCredits(creditsByCategory.get(CourseCategoryCode.TOTTN));
            summary.setTermTcCredits(creditsByCategory.get(CourseCategoryCode.TC));
            semesterSummaryRepository.save(summary);
        }
    }

    private void synchronizeAcademicSummary(Student student, Optional<AcademicMetrics> academicMetricsOpt) {
        String mssv = student.getMssv();
        List<Grade> allGrades = gradeRepository.findByMssv(mssv);
        Map<CourseCategoryCode, Integer> totalCreditsByCategory = calculateCreditsByCategory(allGrades);
        String majorCode = resolveMajorCode(student);
        Integer academicStartYear = resolveAcademicStartYear(student);
        Integer totalCreditsRequired = resolveTotalCreditsRequired(majorCode, academicStartYear);

        Integer totalCredits = calculateTotalCredits(allGrades);
        Float weightedGpa = roundToTwoDecimals(calculateWeightedAverage(allGrades));
        AcademicMetrics academicMetrics = academicMetricsOpt.orElse(null);

        Integer attemptedCredits = academicMetrics != null && academicMetrics.attemptedCredits() != null
                ? academicMetrics.attemptedCredits()
                : totalCredits;
        Integer accumulatedCredits = academicMetrics != null && academicMetrics.accumulatedCredits() != null
                ? academicMetrics.accumulatedCredits()
                : totalCredits;
        Float attemptedGpa = academicMetrics != null && academicMetrics.attemptedGpa() != null
                ? roundToTwoDecimals(academicMetrics.attemptedGpa())
                : weightedGpa;
        Float accumulatedGpa = academicMetrics != null && academicMetrics.accumulatedGpa() != null
                ? roundToTwoDecimals(academicMetrics.accumulatedGpa())
                : weightedGpa;

        AcademicSummary summary = academicSummaryRepository.findByMssv(mssv)
                .orElseGet(() -> AcademicSummary.builder().student(student).build());

        summary.setAttemptedCredits(attemptedCredits != null ? attemptedCredits : 0);
        summary.setAccumulatedCredits(accumulatedCredits != null ? accumulatedCredits : 0);
        summary.setAttemptedGpa(attemptedGpa != null ? attemptedGpa : 0F);
        summary.setAccumulatedGpa(accumulatedGpa != null ? accumulatedGpa : 0F);
        summary.setAccumulatedGeneralCredits(totalCreditsByCategory.get(CourseCategoryCode.DC));
        summary.setAccumulatedFoundationCredits(totalCreditsByCategory.get(CourseCategoryCode.CSNN)
                + totalCreditsByCategory.get(CourseCategoryCode.CSN));
        summary.setAccumulatedMajorCredits(totalCreditsByCategory.get(CourseCategoryCode.CN));
        summary.setAccumulatedElectiveCredits(totalCreditsByCategory.get(CourseCategoryCode.TC));
        summary.setAccumulatedGraduationCredits(totalCreditsByCategory.get(CourseCategoryCode.TOTTN));
        summary.setMajorProgress(calculateMajorProgress(summary.getAccumulatedCredits(), totalCreditsRequired));
        academicSummaryRepository.save(summary);
    }

    private Integer resolveTotalCreditsRequired(String majorCode, Integer academicStartYear) {
        if (majorCode == null || majorCode.isBlank() || academicStartYear == null) {
            return null;
        }

        return curriculumCourseRepository.findTotalCreditsRequiredByMajorAndYear(majorCode, academicStartYear)
                .orElse(null);
    }

    private Float calculateMajorProgress(Integer accumulatedCredits, Integer totalCreditsRequired) {
        if (accumulatedCredits == null || totalCreditsRequired == null || totalCreditsRequired <= 0) {
            return 0F;
        }

        float progress = Math.min((float) accumulatedCredits / totalCreditsRequired, 1F);
        return roundToTwoDecimals(progress);
    }

    private Integer calculateTotalCredits(List<Grade> grades) {
        return grades.stream().filter(grade -> grade.getCredits() != null).mapToInt(Grade::getCredits).sum();
    }

    private Float calculateWeightedAverage(List<Grade> grades) {
        double totalWeightedGrade = 0D;
        int totalCredits = 0;

        for (Grade grade : grades) {
            if (grade.getTotalGrade() == null || grade.getCredits() == null) {
                continue;
            }

            totalWeightedGrade += grade.getTotalGrade() * grade.getCredits();
            totalCredits += grade.getCredits();
        }

        if (totalCredits == 0) {
            return null;
        }

        return (float) (totalWeightedGrade / totalCredits);
    }

    private Float roundToTwoDecimals(Float value) {
        if (value == null) {
            return null;
        }

        return BigDecimal.valueOf(value.doubleValue()).setScale(2, RoundingMode.HALF_UP).floatValue();
    }

    private CurriculumMetadata resolveCurriculumMetadata(String courseCode, String majorCode, Integer academicStartYear,
            Map<String, CurriculumMetadata> cache) {
        if (courseCode == null || courseCode.isBlank() || majorCode == null || majorCode.isBlank()
                || academicStartYear == null) {
            return null;
        }

        String normalizedCourseCode = courseCode.trim().toUpperCase(Locale.ROOT);
        if (cache.containsKey(normalizedCourseCode)) {
            return cache.get(normalizedCourseCode);
        }

        List<String> lookupCandidates = buildCourseCodeLookupCandidates(normalizedCourseCode);
        CurriculumMetadata metadata = null;

        for (String candidate : lookupCandidates) {
            metadata = curriculumCourseRepository.findGradeCourseMetadata(candidate, majorCode, academicStartYear)
                    .map(value -> new CurriculumMetadata(value.getCredits(), value.getCategoryCode())).orElse(null);
            if (metadata != null) {
                break;
            }
        }

        for (String candidate : lookupCandidates) {
            cache.put(candidate, metadata);
        }
        return metadata;
    }

    private List<String> buildCourseCodeLookupCandidates(String courseCode) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(courseCode);

        Matcher engMatcher = ENG_NUMERIC_CODE_PATTERN.matcher(courseCode);
        if (engMatcher.matches()) {
            int level = Integer.parseInt(engMatcher.group(1));
            candidates.add(String.format(Locale.ROOT, "EN%03d", level));
        }

        Matcher enMatcher = EN_NUMERIC_CODE_PATTERN.matcher(courseCode);
        if (enMatcher.matches()) {
            int level = Integer.parseInt(enMatcher.group(1));
            candidates.add(String.format(Locale.ROOT, "ENG%02d", level));
        }

        return new ArrayList<>(candidates);
    }

    private String resolveMajorCode(Student student) {
        if (student.getHomeClass() != null && student.getHomeClass().getMajorCode() != null
                && !student.getHomeClass().getMajorCode().isBlank()) {
            return student.getHomeClass().getMajorCode();
        }

        String homeClassCode = student.getHomeClassCode();
        if (homeClassCode != null && homeClassCode.length() >= 4) {
            return homeClassCode.substring(0, 4);
        }

        return null;
    }

    private Integer resolveAcademicStartYear(Student student) {
        if (student.getHomeClass() != null && student.getHomeClass().getAcademicYear() != null) {
            Matcher matcher = YEAR_PATTERN.matcher(student.getHomeClass().getAcademicYear());
            if (matcher.find()) {
                return Integer.valueOf(matcher.group(1));
            }
        }

        String mssv = student.getMssv();
        if (mssv != null && mssv.length() >= 2 && mssv.substring(0, 2).chars().allMatch(Character::isDigit)) {
            return 2000 + Integer.parseInt(mssv.substring(0, 2));
        }

        return null;
    }

    private Float normalizeGrade(Float grade) {
        if (grade == null) {
            return null;
        }

        return BigDecimal.valueOf(grade.doubleValue()).setScale(1, RoundingMode.HALF_UP).floatValue();
    }

    private record CurriculumMetadata(Integer credits, String categoryCode) {
    }
}
