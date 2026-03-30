package com.uit.buddy.constant;

public final class ScheduleConstant {

    private ScheduleConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Deadline Labels and Status
    public static final String DUE_DATE_LABEL = "Due:";
    public static final String SUBMITTED_STATUS = "submitted";
    public static final String UNKNOWN_CLASS_CODE = "unknown";

    // Time Constants (in milliseconds and minutes)
    public static final long SCRAPE_DEADLINE_INTERVAL = 600000; // 10 minutes
    public static final long PUSH_NOTIFICATION_INTERVAL = 30000; // 30 seconds
    public static final long PING_MOODLE_INTERVAL = 900000; // 15 minutes
    public static final long GAP_PER_STUDENT_PING_MOODLE = 1000; // 1 second gap between processing each student to avoid overwhelming Moodle
    // Time Buffers (in hours and minutes)
    public static final int NEAR_DEADLINE_HOURS = 24; // 24 hours before due
    public static final long NEAR_DEADLINE_BUFFER_MINUTES = NEAR_DEADLINE_HOURS * 60; // 1440 minutes
    public static final long OVERDUE_BUFFER_MINUTES = 12; // 12 minutes after overdue

    // Class Status Determination
    public static final int DOT_COUNT_FOR_LAB_CLASS = 2; // classCode with 2+ dots indicates lab class

    // Key Normalization
    public static final String EMPTY_NORMALIZED = "";
}
