package com.uit.buddy.util;

import com.uit.buddy.constant.GradeConstants;
import com.uit.buddy.exception.grade.GradeErrorCode;
import com.uit.buddy.exception.grade.GradeException;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

@Component
@Slf4j
public class GradePdfParser {

    // Parse grade PDF file and extract all information
    public ParsedGradeData parse(MultipartFile gradeFile) {
        byte[] pdfBytes = readPdfBytes(gradeFile);
        String rawText = extractTextFromPdf(pdfBytes);
        String normalizedText = normalize(rawText);
        String studentMssv = extractStudentMssv(normalizedText);
        if (studentMssv == null) {
            throw new GradeException(GradeErrorCode.INVALID_FILE, "Cannot extract student MSSV from PDF");
        }

        List<CourseGradeExtract> courseGrades = extractCourseGradesFromTables(pdfBytes);
        if (courseGrades.isEmpty()) {
            throw new GradeException(GradeErrorCode.INVALID_FILE, "Cannot detect grade table structure in PDF");
        }
        courseGrades = enrichCourseSemestersFromRawText(rawText, courseGrades);

        Optional<SemesterMetrics> semesterMetrics = extractSemesterMetrics(normalizedText);
        Optional<AcademicMetrics> academicMetrics = extractAcademicMetrics(normalizedText);

        return new ParsedGradeData(rawText, studentMssv, courseGrades, semesterMetrics, academicMetrics);
    }

    private byte[] readPdfBytes(MultipartFile gradeFile) {
        try {
            return gradeFile.getBytes();
        } catch (IOException ex) {
            throw new GradeException(GradeErrorCode.INVALID_FILE_TYPE, "Unable to read PDF content");
        }
    }

    // Extract text content from PDF file (for metadata extraction only)
    private String extractTextFromPdf(byte[] pdfBytes) {
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException ex) {
            throw new GradeException(GradeErrorCode.INVALID_FILE_TYPE, "Unable to parse PDF content");
        }
    }

    private List<CourseGradeExtract> extractCourseGradesFromTables(byte[] pdfBytes) {
        List<CourseGradeExtract> results = new ArrayList<>();

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm spreadsheetExtractionAlgorithm = new SpreadsheetExtractionAlgorithm();
            BasicExtractionAlgorithm basicExtractionAlgorithm = new BasicExtractionAlgorithm();
            HeaderMapping lastKnownMapping = null;

            PageIterator pages = extractor.extract();
            while (pages.hasNext()) {
                Page page = pages.next();

                int beforePageParse = results.size();
                for (Table table : spreadsheetExtractionAlgorithm.extract(page)) {
                    HeaderMapping used = parseTable(table, results, lastKnownMapping);
                    if (used != null) {
                        lastKnownMapping = used;
                    }
                }

                if (results.size() == beforePageParse) {
                    for (Table table : basicExtractionAlgorithm.extract(page)) {
                        HeaderMapping used = parseTable(table, results, lastKnownMapping);
                        if (used != null) {
                            lastKnownMapping = used;
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new GradeException(GradeErrorCode.INVALID_FILE_TYPE, "Unable to parse PDF table content");
        }

        return results;
    }

    private List<CourseGradeExtract> enrichCourseSemestersFromRawText(String rawText,
            List<CourseGradeExtract> courseGrades) {
        Map<String, Deque<SemesterMetrics>> semesterByCourseCode = buildCourseSemesterTimeline(rawText);
        if (semesterByCourseCode.isEmpty()) {
            return courseGrades;
        }

        List<CourseGradeExtract> enriched = new ArrayList<>(courseGrades.size());
        int reassignedCount = 0;

        for (CourseGradeExtract course : courseGrades) {
            if (course.semesterNumber() != null && course.yearStart() != null && course.yearEnd() != null) {
                enriched.add(course);
                continue;
            }

            Deque<SemesterMetrics> timeline = semesterByCourseCode.get(course.courseCode());
            if (timeline == null || timeline.isEmpty()) {
                enriched.add(course);
                continue;
            }

            SemesterMetrics mappedSemester = timeline.pollFirst();
            reassignedCount++;

            enriched.add(new CourseGradeExtract(
                    course.courseCode(),
                    course.courseName(),
                    course.credits(),
                    course.processGrade(),
                    course.midtermGrade(),
                    course.finalGrade(),
                    course.labGrade(),
                    course.totalGrade(),
                    mappedSemester.semesterNumber(),
                    mappedSemester.yearStart(),
                    mappedSemester.yearEnd()));
        }

        if (log.isInfoEnabled()) {
            log.info("Enriched semester info for {} course rows from raw PDF timeline", reassignedCount);
        }

        return enriched;
    }

    private Map<String, Deque<SemesterMetrics>> buildCourseSemesterTimeline(String rawText) {
        Map<String, Deque<SemesterMetrics>> timeline = new HashMap<>();
        String[] lines = rawText.split("\\R");
        SemesterMetrics currentSemester = null;

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            SemesterMetrics semesterFromLine = extractSemesterMetricsFromLine(line);
            if (semesterFromLine != null) {
                currentSemester = semesterFromLine;
                continue;
            }

            if (currentSemester == null) {
                continue;
            }

            Matcher codeMatcher = GradeConstants.COURSE_CODE_PATTERN.matcher(line.toUpperCase(Locale.ROOT));
            Set<String> seenCodesInLine = new HashSet<>();
            while (codeMatcher.find()) {
                String code = codeMatcher.group(1);
                if (!seenCodesInLine.add(code)) {
                    continue;
                }

                timeline.computeIfAbsent(code, ignored -> new ArrayDeque<>())
                        .addLast(currentSemester);
            }
        }

        return timeline;
    }

    private HeaderMapping parseTable(Table table, List<CourseGradeExtract> results, HeaderMapping fallbackMapping) {
        List<List<RectangularTextContainer>> rows = table.getRows();
        if (rows == null || rows.isEmpty()) {
            return fallbackMapping;
        }

        HeaderDetection header = detectHeader(rows);
        HeaderMapping mapping;
        int dataStartRow;

        if (header != null) {
            mapping = header.mapping();
            dataStartRow = header.dataStartRow();
        } else if (fallbackMapping != null) {
            mapping = fallbackMapping;
            dataStartRow = 0;
            if (log.isDebugEnabled()) {
                log.debug("Parsing table without explicit header using previous header mapping");
            }
        } else {
            return null;
        }

        SemesterMetrics currentSemester = null;
        MutableCourseRow pendingCourse = null;

        for (int i = dataStartRow; i < rows.size(); i++) {
            List<String> cells = rowToTexts(rows.get(i));
            String mergedRowText = joinNonBlank(cells);
            String normalizedRow = normalize(mergedRowText);
            if (normalizedRow.isBlank()) {
                continue;
            }

            if (normalizedRow.matches(GradeConstants.SEMESTER_START_PATTERN_TEXT)) {
                if (pendingCourse != null) {
                    results.add(pendingCourse.toRecord());
                    pendingCourse = null;
                }
                currentSemester = extractSemesterMetricsFromLine(mergedRowText);
                continue;
            }

            if (isSemesterSummaryRow(normalizedRow)) {
                if (pendingCourse != null) {
                    results.add(pendingCourse.toRecord());
                    pendingCourse = null;
                }
                continue;
            }

            String courseCode = extractCourseCode(cells, mapping.codeIndex(), mergedRowText);
            if (courseCode == null) {
                if (pendingCourse != null && isContinuationRow(cells, mapping)) {
                    pendingCourse.appendName(safeCell(cells, mapping.nameIndex()));
                }
                continue;
            }

            if (pendingCourse != null) {
                results.add(pendingCourse.toRecord());
            }

            pendingCourse = buildCourseRow(cells, mapping, courseCode, currentSemester);
        }

        if (pendingCourse != null) {
            results.add(pendingCourse.toRecord());
        }

        return mapping;
    }

    private HeaderDetection detectHeader(List<List<RectangularTextContainer>> rows) {
        int scanLimit = Math.min(rows.size(), 16);
        for (int i = 0; i < scanLimit; i++) {
            List<String> current = rowToTexts(rows.get(i));
            List<String> next = i + 1 < rows.size() ? rowToTexts(rows.get(i + 1)) : Collections.emptyList();

            HeaderMapping mapping = buildHeaderMapping(current, next);
            if (!mapping.isValid()) {
                continue;
            }

            int dataStartRow = i + (mapping.usesSubHeader() ? 2 : 1);
            if (log.isInfoEnabled()) {
                log.info("Detected grade table columns via Tabula: {}", mapping);
            }
            return new HeaderDetection(mapping, dataStartRow);
        }

        return null;
    }

    private HeaderMapping buildHeaderMapping(List<String> currentRow, List<String> nextRow) {
        int maxColumns = Math.max(currentRow.size(), nextRow.size());
        boolean usesSubHeader = false;

        HeaderIndex code = findHeaderIndex(currentRow, this::isCourseCodeHeaderCell);
        if (code.index() == -1) {
            code = findHeaderIndex(nextRow, this::isCourseCodeHeaderCell);
            if (code.index() != -1) {
                usesSubHeader = true;
            }
        }

        HeaderIndex name = findHeaderIndex(currentRow, this::isCourseNameHeaderCell);
        if (name.index() == -1) {
            name = findHeaderIndex(nextRow, this::isCourseNameHeaderCell);
            if (name.index() != -1) {
                usesSubHeader = true;
            }
        }

        HeaderIndex credits = findHeaderIndex(currentRow, this::isCreditsHeaderCell);
        if (credits.index() == -1) {
            credits = findHeaderIndex(nextRow, this::isCreditsHeaderCell);
            if (credits.index() != -1) {
                usesSubHeader = true;
            }
        }

        HeaderIndex process = findHeaderIndex(currentRow, this::isProcessHeaderCell);
        if (process.index() == -1) {
            process = findHeaderIndex(nextRow, this::isProcessHeaderCell);
            if (process.index() != -1) {
                usesSubHeader = true;
            }
        }

        HeaderIndex midterm = findHeaderIndex(currentRow, this::isMidtermHeaderCell);
        if (midterm.index() == -1) {
            midterm = findHeaderIndex(nextRow, this::isMidtermHeaderCell);
            if (midterm.index() != -1) {
                usesSubHeader = true;
            }
        }

        HeaderIndex lab = findHeaderIndex(currentRow, this::isLabHeaderCell);
        if (lab.index() == -1) {
            lab = findHeaderIndex(nextRow, this::isLabHeaderCell);
            if (lab.index() != -1) {
                usesSubHeader = true;
            }
        }

        HeaderIndex fin = findHeaderIndex(currentRow, this::isFinalHeaderCell);
        if (fin.index() == -1) {
            fin = findHeaderIndex(nextRow, this::isFinalHeaderCell);
            if (fin.index() != -1) {
                usesSubHeader = true;
            }
        }

        HeaderIndex total = findHeaderIndex(currentRow, this::isTotalHeaderCell);
        if (total.index() == -1) {
            total = findHeaderIndex(nextRow, this::isTotalHeaderCell);
            if (total.index() != -1) {
                usesSubHeader = true;
            }
        }

        int creditsIndex = credits.index();
        int processIndex = process.index();
        int midtermIndex = midterm.index();
        int labIndex = lab.index();
        int finalIndex = fin.index();
        int totalIndex = total.index();

        // Guard against accidental match with "Ten hoc phan" column.
        if (totalIndex == name.index() || totalIndex == code.index()) {
            totalIndex = -1;
        }

        if (creditsIndex != -1 && totalIndex != -1 && totalIndex <= creditsIndex) {
            totalIndex = -1;
        }

        if (finalIndex != -1 && totalIndex != -1 && totalIndex <= finalIndex) {
            totalIndex = -1;
        }

        if (creditsIndex != -1) {
            if (processIndex == -1 && creditsIndex + 1 < maxColumns) {
                processIndex = creditsIndex + 1;
            }
            if (midtermIndex == -1 && creditsIndex + 2 < maxColumns) {
                midtermIndex = creditsIndex + 2;
            }
            if (labIndex == -1 && creditsIndex + 3 < maxColumns) {
                labIndex = creditsIndex + 3;
            }
            if (finalIndex == -1 && creditsIndex + 4 < maxColumns) {
                finalIndex = creditsIndex + 4;
            }
            if (totalIndex == -1 && creditsIndex + 5 < maxColumns) {
                totalIndex = creditsIndex + 5;
            }
        }

        return new HeaderMapping(code.index(), name.index(), creditsIndex, processIndex, midtermIndex, labIndex,
                finalIndex, totalIndex, usesSubHeader);
    }

    private HeaderIndex findHeaderIndex(List<String> row, java.util.function.Predicate<String> predicate) {
        for (int i = 0; i < row.size(); i++) {
            String normalizedCell = normalize(row.get(i));
            if (predicate.test(normalizedCell)) {
                return new HeaderIndex(i);
            }
        }
        return new HeaderIndex(-1);
    }

    private boolean isCourseCodeHeaderCell(String cell) {
        return containsWord(cell, "ma") && containsWord(cell, "hp");
    }

    private boolean isCourseNameHeaderCell(String cell) {
        return containsWord(cell, "ten") && containsWord(cell, "hoc") && containsWord(cell, "phan");
    }

    private boolean isCreditsHeaderCell(String cell) {
        return containsWord(cell, "tin") && containsWord(cell, "chi");
    }

    private boolean isProcessHeaderCell(String cell) {
        return containsWord(cell, "qt") || (containsWord(cell, "qua") && containsWord(cell, "trinh"));
    }

    private boolean isMidtermHeaderCell(String cell) {
        return containsWord(cell, "gk") || (containsWord(cell, "giua") && containsWord(cell, "ky"));
    }

    private boolean isLabHeaderCell(String cell) {
        return containsWord(cell, "th") || (containsWord(cell, "thuc") && containsWord(cell, "hanh"));
    }

    private boolean isFinalHeaderCell(String cell) {
        return containsWord(cell, "ck") || (containsWord(cell, "cuoi") && containsWord(cell, "ky"));
    }

    private boolean isTotalHeaderCell(String cell) {
        if (containsWord(cell, "ma") && containsWord(cell, "hp")) {
            return false;
        }

        if (containsWord(cell, "diem") && containsWord(cell, "hp")) {
            return true;
        }

        if (containsWord(cell, "diem") && containsWord(cell, "hoc") && containsWord(cell, "phan")) {
            return true;
        }

        return "hp".equals(cell.trim());
    }

    private boolean containsWord(String text, String keyword) {
        if (text == null || text.isBlank()) {
            return false;
        }
        java.util.regex.Pattern pattern = GradeConstants.wholeWordPattern(keyword);
        return pattern.matcher(text).find();
    }

    private List<String> rowToTexts(List<RectangularTextContainer> row) {
        List<String> cells = new ArrayList<>(row.size());
        for (RectangularTextContainer cell : row) {
            cells.add(collapseWhitespace(cell == null ? "" : cell.getText()));
        }
        return cells;
    }

    private String joinNonBlank(List<String> cells) {
        StringBuilder joined = new StringBuilder();
        for (String cell : cells) {
            if (cell == null || cell.isBlank()) {
                continue;
            }
            if (joined.length() > 0) {
                joined.append(' ');
            }
            joined.append(cell.trim());
        }
        return joined.toString();
    }

    private String extractCourseCode(List<String> cells, int codeIndex, String mergedRow) {
        String codeCell = safeCell(cells, codeIndex).toUpperCase(Locale.ROOT);
        String code = extractCourseCodeFromText(codeCell);
        if (code != null) {
            return code;
        }

        return extractCourseCodeFromText(mergedRow.toUpperCase(Locale.ROOT));
    }

    private String extractCourseCodeFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher matcher = GradeConstants.COURSE_CODE_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private boolean isSemesterSummaryRow(String normalizedRow) {
        for (String keyword : GradeConstants.SEMESTER_END_KEYWORDS) {
            if (normalizedRow.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private boolean isContinuationRow(List<String> cells, HeaderMapping mapping) {
        String merged = joinNonBlank(cells);
        if (extractCourseCodeFromText(merged.toUpperCase(Locale.ROOT)) != null) {
            return false;
        }

        String nameCell = safeCell(cells, mapping.nameIndex());
        if (nameCell.isBlank()) {
            return false;
        }

        boolean hasCredits = parseCreditsCell(safeCell(cells, mapping.creditsIndex())) > 0;
        boolean hasGrades = parseGradeCell(safeCell(cells, mapping.processIndex())) != null
                || parseGradeCell(safeCell(cells, mapping.midtermIndex())) != null
                || parseGradeCell(safeCell(cells, mapping.labIndex())) != null
                || parseGradeCell(safeCell(cells, mapping.finalIndex())) != null
                || parseGradeCell(safeCell(cells, mapping.totalIndex())) != null;

        return !hasCredits && !hasGrades;
    }

    private MutableCourseRow buildCourseRow(List<String> cells, HeaderMapping mapping, String courseCode,
            SemesterMetrics currentSemester) {
        String courseName = cleanCourseName(safeCell(cells, mapping.nameIndex()));
        if (courseName.isBlank()) {
            courseName = courseCode;
        }

        Integer credits = parseCreditsCell(safeCell(cells, mapping.creditsIndex()));
        Float process = parseGradeCell(safeCell(cells, mapping.processIndex()));
        Float midterm = parseGradeCell(safeCell(cells, mapping.midtermIndex()));
        Float lab = parseGradeCell(safeCell(cells, mapping.labIndex()));
        Float fin = parseGradeCell(safeCell(cells, mapping.finalIndex()));
        Float total = parseGradeCell(safeCell(cells, mapping.totalIndex()));

        return new MutableCourseRow(courseCode, courseName, credits, process, midterm, fin, lab, total,
                currentSemester);
    }

    private Integer parseCreditsCell(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        Matcher matcher = GradeConstants.CREDITS_INTEGER_TOKEN_PATTERN.matcher(text);
        while (matcher.find()) {
            try {
                int value = Integer.parseInt(matcher.group(1));
                if (value >= 1 && value <= 6) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private Float parseGradeCell(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher matcher = GradeConstants.NUMBER_PATTERN.matcher(text);
        while (matcher.find()) {
            try {
                float value = Float.parseFloat(matcher.group(1).replace(',', '.'));
                if (value < GradeConstants.MIN_GRADE || value > GradeConstants.MAX_GRADE) {
                    continue;
                }
                return roundToOneDecimal(value);
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }

    private String cleanCourseName(String name) {
        String normalized = collapseWhitespace(name);
        Matcher tailMatcher = GradeConstants.COURSE_NAME_TRAILING_NOISE_PATTERN.matcher(normalized);
        if (tailMatcher.find()) {
            normalized = normalized.substring(0, tailMatcher.start()).trim();
        }
        return normalized;
    }

    private String safeCell(List<String> cells, int index) {
        if (index < 0 || index >= cells.size()) {
            return "";
        }
        return cells.get(index) == null ? "" : cells.get(index).trim();
    }

    private String collapseWhitespace(String text) {
        if (text == null) {
            return "";
        }
        return GradeConstants.MULTI_WHITESPACE_PATTERN
                .matcher(text.replace('\n', ' ').replace('\r', ' '))
                .replaceAll(" ")
                .trim();
    }

    private Float roundToOneDecimal(Float value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10f) / 10f;
    }

    private SemesterMetrics extractSemesterMetricsFromLine(String semesterLine) {
        String normalizedLine = normalize(semesterLine);
        Matcher semesterMatcher = GradeConstants.SEMESTER_INFO_PATTERN.matcher(normalizedLine);
        if (!semesterMatcher.find()) {
            return null;
        }

        Integer semesterNumber = null;
        try {
            semesterNumber = Integer.parseInt(semesterMatcher.group(1));
        } catch (NumberFormatException ignored) {
        }

        String yearStart = semesterMatcher.group(2);
        String yearEnd = normalizeAcademicYearEnd(yearStart, semesterMatcher.group(3));
        return new SemesterMetrics(semesterNumber, yearStart, yearEnd, null, null, null);
    }

    // Extract semester summary metrics (semester number, year, GPA, credits, rank)
    private Optional<SemesterMetrics> extractSemesterMetrics(String normalizedText) {
        Float termGpa = null;
        Integer termCredits = null;

        Matcher termSummaryMatcher = GradeConstants.TERM_SUMMARY_LINE_PATTERN.matcher(normalizedText);
        if (termSummaryMatcher.find()) {
            try {
                termCredits = Integer.parseInt(termSummaryMatcher.group(1));
            } catch (NumberFormatException ignored) {
            }
            try {
                termGpa = Float.parseFloat(termSummaryMatcher.group(2));
            } catch (NumberFormatException ignored) {
            }
        }

        if (termGpa == null) {
            termGpa = findFloatAfterKeywords(normalizedText, GradeConstants.TERM_GPA_KEYWORDS);
        }
        if (termCredits == null) {
            termCredits = findIntegerAfterKeywords(normalizedText, GradeConstants.TERM_CREDITS_KEYWORDS);
        }
        String termRank = findTextAfterKeyword(normalizedText, GradeConstants.TERM_RANK_KEYWORD);

        Integer semesterNumber = null;
        String yearStart = null;
        String yearEnd = null;

        Matcher semesterMatcher = GradeConstants.SEMESTER_INFO_PATTERN.matcher(normalizedText);
        if (semesterMatcher.find()) {
            try {
                semesterNumber = Integer.parseInt(semesterMatcher.group(1));
            } catch (NumberFormatException ignored) {
            }
            yearStart = semesterMatcher.group(2);
            yearEnd = normalizeAcademicYearEnd(yearStart, semesterMatcher.group(3));
        }

        if (termGpa == null && termCredits == null && termRank == null && semesterNumber == null) {
            return Optional.empty();
        }

        return Optional.of(new SemesterMetrics(semesterNumber, yearStart, yearEnd, termGpa, termCredits, termRank));
    }

    // Extract cumulative academic metrics (attempted/accumulated credits and GPA)
    private Optional<AcademicMetrics> extractAcademicMetrics(String normalizedText) {
        Float attemptedGpa = findFloatAfterKeywords(normalizedText, GradeConstants.ATTEMPTED_GPA_KEYWORDS);
        Float accumulatedGpa = findFloatAfterKeywords(normalizedText, GradeConstants.ACCUMULATED_GPA_KEYWORDS);
        Integer attemptedCredits = findIntegerAfterKeywords(normalizedText, GradeConstants.ATTEMPTED_CREDITS_KEYWORDS);
        Integer accumulatedCredits = findIntegerAfterKeywords(normalizedText,
                GradeConstants.ACCUMULATED_CREDITS_KEYWORDS);

        if (attemptedGpa == null && accumulatedGpa == null && attemptedCredits == null && accumulatedCredits == null) {
            return Optional.empty();
        }

        return Optional.of(new AcademicMetrics(attemptedCredits, accumulatedCredits, attemptedGpa, accumulatedGpa));
    }

    // Extract student MSSV from PDF text (only use labeled pattern)
    private String extractStudentMssv(String normalizedText) {
        Matcher matcher = GradeConstants.STUDENT_ID_WITH_LABEL_PATTERN.matcher(normalizedText);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Find Float value after one of the given keywords
    private Float findFloatAfterKeywords(String normalizedText, List<String> keywords) {
        for (String keyword : keywords) {
            Matcher matcher = GradeConstants.floatAfterKeywordPattern(keyword).matcher(normalizedText);
            if (matcher.find()) {
                try {
                    return Float.parseFloat(matcher.group(1).replace(',', '.'));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    // Find Integer value after one of the given keywords
    private Integer findIntegerAfterKeywords(String normalizedText, List<String> keywords) {
        for (String keyword : keywords) {
            Matcher matcher = GradeConstants.integerAfterKeywordPattern(keyword).matcher(normalizedText);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }

    // Find text after a keyword
    private String findTextAfterKeyword(String normalizedText, String keyword) {
        Matcher matcher = GradeConstants.textAfterKeywordPattern(keyword).matcher(normalizedText);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    // Normalize text: remove Vietnamese accents, lowercase, normalize whitespace
    private String normalize(String text) {
        String noAccents = GradeConstants.DIACRITICS_PATTERN
                .matcher(Normalizer.normalize(text, Normalizer.Form.NFD))
                .replaceAll("")
                .replace('Đ', 'D')
                .replace('đ', 'd');
        return GradeConstants.MULTI_WHITESPACE_PATTERN.matcher(noAccents.toLowerCase(Locale.ROOT))
                .replaceAll(" ");
    }

    private String normalizeAcademicYearEnd(String yearStart, String rawYearEnd) {
        if (rawYearEnd == null || rawYearEnd.isBlank()) {
            return rawYearEnd;
        }

        String trimmed = rawYearEnd.trim();
        if (trimmed.length() == 4) {
            return trimmed;
        }

        if (trimmed.length() == 2 && yearStart != null && yearStart.length() == 4) {
            return yearStart.substring(0, 2) + trimmed;
        }

        return trimmed;
    }

    private record HeaderIndex(int index) {
    }

    private record HeaderMapping(int codeIndex, int nameIndex, int creditsIndex, int processIndex, int midtermIndex,
            int labIndex, int finalIndex, int totalIndex, boolean usesSubHeader) {
        private boolean isValid() {
            return codeIndex >= 0 && nameIndex >= 0 && creditsIndex >= 0
                    && processIndex >= 0 && midtermIndex >= 0 && labIndex >= 0
                    && finalIndex >= 0 && totalIndex >= 0;
        }
    }

    private record HeaderDetection(HeaderMapping mapping, int dataStartRow) {
    }

    private static final class MutableCourseRow {
        private final String courseCode;
        private String courseName;
        private final Integer credits;
        private final Float processGrade;
        private final Float midtermGrade;
        private final Float finalGrade;
        private final Float labGrade;
        private final Float totalGrade;
        private final Integer semesterNumber;
        private final String yearStart;
        private final String yearEnd;

        private MutableCourseRow(String courseCode, String courseName, Integer credits, Float processGrade,
                Float midtermGrade, Float finalGrade, Float labGrade, Float totalGrade,
                SemesterMetrics semesterMetrics) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.credits = credits;
            this.processGrade = processGrade;
            this.midtermGrade = midtermGrade;
            this.finalGrade = finalGrade;
            this.labGrade = labGrade;
            this.totalGrade = totalGrade;
            this.semesterNumber = semesterMetrics != null ? semesterMetrics.semesterNumber() : null;
            this.yearStart = semesterMetrics != null ? semesterMetrics.yearStart() : null;
            this.yearEnd = semesterMetrics != null ? semesterMetrics.yearEnd() : null;
        }

        private void appendName(String moreName) {
            if (moreName == null || moreName.isBlank()) {
                return;
            }
            this.courseName = GradeConstants.MULTI_WHITESPACE_PATTERN
                    .matcher(this.courseName + " " + moreName)
                    .replaceAll(" ")
                    .trim();
        }

        private CourseGradeExtract toRecord() {
            return new CourseGradeExtract(courseCode, courseName, credits, processGrade, midtermGrade,
                    finalGrade, labGrade, totalGrade, semesterNumber, yearStart, yearEnd);
        }
    }

    public record ParsedGradeData(String rawText, String studentMssv, List<CourseGradeExtract> courseGrades,
            Optional<SemesterMetrics> semesterMetrics, Optional<AcademicMetrics> academicMetrics) {
    }

    public record CourseGradeExtract(String courseCode, String courseName, Integer credits, Float processGrade,
            Float midtermGrade, Float finalGrade, Float labGrade, Float totalGrade, Integer semesterNumber,
            String yearStart, String yearEnd) {
    }

    public record SemesterMetrics(Integer semesterNumber, String yearStart, String yearEnd, Float termGpa,
            Integer termCredits, String termRank) {
    }

    public record AcademicMetrics(Integer attemptedCredits, Integer accumulatedCredits, Float attemptedGpa,
            Float accumulatedGpa) {
    }
}
