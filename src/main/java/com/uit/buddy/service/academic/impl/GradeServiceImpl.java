package com.uit.buddy.service.academic.impl;

import com.uit.buddy.dto.response.academic.SemesterGradesResponse;
import com.uit.buddy.entity.academic.Grade;
import com.uit.buddy.entity.academic.Semester;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.grade.GradeErrorCode;
import com.uit.buddy.exception.grade.GradeException;
import com.uit.buddy.mapper.academic.GradeMapper;
import com.uit.buddy.repository.academic.CurriculumCourseRepository;
import com.uit.buddy.repository.academic.GradeRepository;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.GradeService;
import com.uit.buddy.util.GradePdfParser;
import com.uit.buddy.util.GradePdfParser.CourseGradeExtract;
import com.uit.buddy.util.GradePdfParser.ParsedGradeData;
import com.uit.buddy.util.GradePdfParser.SemesterMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final SemesterRepository semesterRepository;
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
            throw new GradeException(GradeErrorCode.INVALID_FILE,
                    "MSSV in PDF does not match authenticated user");
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
            Integer resolvedCredits = curriculumMetadata != null && curriculumMetadata.credits() != null
                    && curriculumMetadata.credits() > 0
                            ? curriculumMetadata.credits()
                            : parsedCredits;

            Grade grade = Grade.builder()
                    .mssv(mssv)
                    .semesterCode(semesterCode)
                    .courseCode(courseGrade.courseCode())
                    .courseName(courseGrade.courseName())
                    .credits(resolvedCredits)
                    .courseType(curriculumMetadata != null ? curriculumMetadata.categoryCode() : null)
                    .processGrade(normalizeGrade(courseGrade.processGrade()))
                    .midtermGrade(normalizeGrade(courseGrade.midtermGrade()))
                    .finalGrade(normalizeGrade(courseGrade.finalGrade()))
                    .labGrade(normalizeGrade(courseGrade.labGrade()))
                    .totalGrade(normalizeGrade(courseGrade.totalGrade()))
                    .build();

            Optional<Grade> existing = gradeRepository.findByMssvAndCourseCodeAndSemesterCode(
                    mssv, courseGrade.courseCode(), semesterCode);

            if (existing.isPresent()) {
                grade.setId(existing.get().getId());
            }

            gradeRepository.save(grade);
            savedCount++;
        }

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
            throw new GradeException(GradeErrorCode.INVALID_FILE,
                    "Cannot determine semester from PDF summary");
        }

        SemesterMetrics metrics = metricsOpt.get();
        return determineSemester(metrics.semesterNumber(), metrics.yearStart(), metrics.yearEnd());
    }

    private String determineSemester(Integer semesterNumber, String yearStart, String yearEnd) {
        if (semesterNumber == null || yearStart == null || yearEnd == null) {
            throw new GradeException(GradeErrorCode.INVALID_FILE,
                    "Cannot determine semester info (hoc ky/nam hoc) from PDF");
        }

        Optional<Semester> semester = semesterRepository.findBySemesterNumberAndYearStartAndYearEnd(
                String.valueOf(semesterNumber),
                yearStart,
                yearEnd);

        if (semester.isPresent()) {
            return semester.get().getSemesterCode();
        }

        throw new GradeException(GradeErrorCode.INVALID_FILE,
                "Semester not found in system: HK" + semesterNumber + " " +
                        yearStart + "-" + yearEnd);
    }

    @Override
    @Transactional(readOnly = true)
    public SemesterGradesResponse getGradesBySemester(String mssv, String semesterCode) {
        log.info("Getting grades for student {} in semester {}", mssv, semesterCode);

        List<Grade> grades = gradeRepository.findByMssvAndSemesterCode(mssv, semesterCode);
        Semester semester = semesterRepository.findById(semesterCode)
                .orElseThrow(() -> new GradeException(GradeErrorCode.INVALID_FILE, "Semester not found"));

        return buildSemesterGradesResponse(semester, grades);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SemesterGradesResponse> getAllGrades(String mssv) {
        log.info("Getting all grades for student {}", mssv);

        List<Grade> allGrades = gradeRepository.findByMssv(mssv);

        Map<String, List<Grade>> gradesBySemester = allGrades.stream()
                .collect(Collectors.groupingBy(Grade::getSemesterCode));

        Set<String> semesterCodes = gradesBySemester.keySet();

        List<Semester> semesters = semesterRepository.findAllById(semesterCodes);

        return semesters.stream()
                .sorted(Comparator.comparing(Semester::getStartDate))
                .map(semester -> buildSemesterGradesResponse(
                        semester,
                        gradesBySemester.getOrDefault(semester.getSemesterCode(), Collections.emptyList())))
                .collect(Collectors.toList());
    }

    private SemesterGradesResponse buildSemesterGradesResponse(Semester semester, List<Grade> grades) {
        return gradeMapper.toSemesterGradesResponse(semester, grades);
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
            metadata = curriculumCourseRepository
                    .findGradeCourseMetadata(candidate, majorCode, academicStartYear)
                    .map(value -> new CurriculumMetadata(value.getCredits(), value.getCategoryCode()))
                    .orElse(null);
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

        return BigDecimal.valueOf(grade.doubleValue())
                .setScale(1, RoundingMode.HALF_UP)
                .floatValue();
    }

    private record CurriculumMetadata(Integer credits, String categoryCode) {
    }
}
