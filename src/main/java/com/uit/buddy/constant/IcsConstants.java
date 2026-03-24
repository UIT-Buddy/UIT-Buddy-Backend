package com.uit.buddy.constant;

import java.util.Map;
import java.util.regex.Pattern;

public final class IcsConstants {

    private IcsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // REGEX PATTERNS
    public static final Pattern SUMMARY_PATTERN = Pattern.compile("^([A-Z0-9.]+)\\s*-\\s*(.+)$");
    public static final Pattern COURSE_NAME_PATTERN = Pattern
            .compile("\\(([^)]+)\\)|\\b([A-Z]{2,}\\d{3}(?:\\.[A-Z0-9]+){1,2})\\b");
    public static final Pattern TEACHER_PATTERN = Pattern.compile("Giảng viên:\\s*([^,]+)");
    public static final Pattern LESSON_PATTERN = Pattern.compile("Tiết\\s*([0-9]+)");
    public static final Pattern NOTE_PATTERN = Pattern.compile("Ghi chú:\\s*([^,]+)");
    public static final Pattern STUDENT_ID_PATTERN = Pattern.compile("(\\d{8})");
    public static final Pattern UNTIL_PATTERN = Pattern.compile("UNTIL=(\\d{8}T\\d{6}Z?)");
    public static final Pattern FREQ_PATTERN = Pattern.compile("FREQ=([A-Z]+)");
    public static final Pattern INTERVAL_PATTERN = Pattern.compile("INTERVAL=(\\d+)");

    // SPORT KEYWORDS
    public static final String[] SPORTS = { "Bơi lội", "Pickleball", "Bóng đá", "Bóng rổ", "Bóng bàn", "Cầu lông",
            "Tennis", "Võ thuật", "Thể dục" };

    public static final Map<String, String> SPORT_LOCATION_MAP = Map.of("Bơi lội", "Hồ bơi ĐHQG", "Pickleball",
            "Sân Pickleball", "Bóng đá", "Sân bóng đá", "Bóng rổ", "Sân bóng rổ", "Bóng bàn", "Nhà thi đấu");

    public static final String PE_PREFIX = "PE";
    public static final String COURT_PREFIX = "Sân ";

    // DATE TIME PATTERNS
    public static final String DATETIME_PATTERN = "yyyyMMdd'T'HHmmss";

    // ICS PROPERTIES
    public static final String X_WR_CALNAME = "X-WR-CALNAME:";
    public static final String BEGIN_VEVENT = "BEGIN:VEVENT";
    public static final String END_VEVENT = "END:VEVENT";
    public static final String SUMMARY = "SUMMARY";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String DTSTART = "DTSTART";
    public static final String DTEND = "DTEND";
    public static final String RRULE = "RRULE";
}