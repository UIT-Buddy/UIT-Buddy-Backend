package com.uit.buddy.event.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record DeadlineEvent(
    String deadlineName,
    String courseCode,
    boolean isPersonal,
    LocalTime dueTime,
    LocalDate dueDate
) {}
