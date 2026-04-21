package com.uit.buddy.constant;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class GradeConstants {

    private static final Map<String, Pattern> WHOLE_WORD_PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Pattern> FLOAT_AFTER_KEYWORD_PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Pattern> INTEGER_AFTER_KEYWORD_PATTERN_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Pattern> TEXT_AFTER_KEYWORD_PATTERN_CACHE = new ConcurrentHashMap<>();

    private GradeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final Pattern COURSE_CODE_PATTERN = Pattern.compile("\\b([A-Z]{2,}[0-9]{2,}[A-Z0-9]*)\\b");
    public static final Pattern NUMBER_PATTERN = Pattern.compile("(?<!\\d)(\\d{1,2}(?:[\\.,]\\d{1,2})?)(?!\\d)");
    public static final Pattern SEMESTER_INFO_PATTERN = Pattern
            .compile("(?:hk|hoc ky)\\s*([123])[^0-9]{0,20}(20\\d{2})\\s*[-/]\\s*((?:20)?\\d{2})");
    public static final Pattern STUDENT_ID_WITH_LABEL_PATTERN = Pattern
            .compile("(?:mssv|ma\\s*sv|ma\\s*so\\s*sv)\\s*[:\\-]?\\s*(\\d{8})", Pattern.CASE_INSENSITIVE);
    public static final Pattern TERM_SUMMARY_LINE_PATTERN = Pattern.compile(
            "trung\\s*binh\\s*hoc\\s*ky\\s*(\\d{1,2})[^0-9]{0,20}(\\d{1,2}(?:[\\.,]\\d{1,2})?)",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern CREDITS_INTEGER_TOKEN_PATTERN = Pattern.compile("\\b(\\d{1,2})\\b");
    public static final Pattern COURSE_NAME_TRAILING_NOISE_PATTERN = Pattern
            .compile("(?:\\s+\\d{1,2}(?:[\\.,]\\d{1,2})?){2,}$");
    public static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{M}");
    public static final Pattern MULTI_WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public static final float MIN_GRADE = 0F;
    public static final float MAX_GRADE = 10F;
    public static final int KEYWORD_VALUE_DISTANCE = 30;

    public static final List<String> TERM_GPA_KEYWORDS = List.of("diem trung binh hoc ky");
    public static final List<String> TERM_CREDITS_KEYWORDS = List.of("so tin chi hoc ky", "tin chi hoc ky");
    public static final String TERM_RANK_KEYWORD = "xep loai hoc ky";

    public static final List<String> ATTEMPTED_GPA_KEYWORDS = List.of("diem trung binh chung");
    public static final List<String> ACCUMULATED_GPA_KEYWORDS = List.of("diem trung binh chung tich luy");
    public static final List<String> ATTEMPTED_CREDITS_KEYWORDS = List.of("so tin chi da hoc");
    public static final List<String> ACCUMULATED_CREDITS_KEYWORDS = List.of("so tin chi tich luy");

    // Grade section markers
    public static final String SEMESTER_START_PATTERN_TEXT = ".*hoc ky.*\\d+.*";
    public static final List<String> SEMESTER_END_KEYWORDS = List.of("Trung bình học kỳ", "trung binh hoc ky");

    public static Pattern wholeWordPattern(String keyword) {
        return WHOLE_WORD_PATTERN_CACHE.computeIfAbsent(keyword,
                key -> Pattern.compile("\\b" + Pattern.quote(key) + "\\b"));
    }

    public static Pattern floatAfterKeywordPattern(String keyword) {
        return FLOAT_AFTER_KEYWORD_PATTERN_CACHE.computeIfAbsent(keyword, key -> Pattern
                .compile(Pattern.quote(key) + "[^0-9]{0," + KEYWORD_VALUE_DISTANCE + "}(\\d{1,2}(?:[\\.,]\\d{1,2})?)"));
    }

    public static Pattern integerAfterKeywordPattern(String keyword) {
        return INTEGER_AFTER_KEYWORD_PATTERN_CACHE.computeIfAbsent(keyword,
                key -> Pattern.compile(Pattern.quote(key) + "[^0-9]{0," + KEYWORD_VALUE_DISTANCE + "}(\\d{1,3})"));
    }

    public static Pattern textAfterKeywordPattern(String keyword) {
        return TEXT_AFTER_KEYWORD_PATTERN_CACHE.computeIfAbsent(keyword,
                key -> Pattern.compile(Pattern.quote(key) + "\\s*[:\\-]?\\s*([^\\r\\n]+)"));
    }
}
