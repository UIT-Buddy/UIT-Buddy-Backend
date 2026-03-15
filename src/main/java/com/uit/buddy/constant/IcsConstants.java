package com.uit.buddy.constant;

import java.util.List;

public final class IcsConstants {

    private IcsConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // REGEX PATTERNS
    public static final String SUMMARY_PATTERN = "([A-Z0-9.]+)\\s*-\\s*P\\.\\s*([A-Z0-9.]+)";
    public static final String COURSE_NAME_PATTERN = "\\(([^)]+)\\)";
    public static final String TEACHER_PATTERN = "Giảng viên:\\s*([^,]+)";
    public static final String LESSON_PATTERN = "Tiết\\s*(\\d+)";
    public static final String NOTE_PATTERN = "Ghi chú:\\s*([^,]+)";
    public static final String STUDENT_ID_PATTERN = "(\\d{8})";
    public static final String UNTIL_PATTERN = "UNTIL=(\\d{8}T\\d{6}Z?)";
    public static final String FREQ_PATTERN = "FREQ=([A-Z]+)";
    public static final String INTERVAL_PATTERN = "INTERVAL=(\\d+)";

    // SPORT KEYWORDS
    public static final List<String> SPORTS = List.of("Pickleball", "Bóng đá", "Bóng rổ", "Bóng bàn", "Bơi lội");

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