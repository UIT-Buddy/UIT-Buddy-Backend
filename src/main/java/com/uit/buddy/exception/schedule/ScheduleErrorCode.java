package com.uit.buddy.exception.schedule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ScheduleErrorCode {

    EXPIRED_SCHEDULE("SCH001", "Schedule expired", HttpStatus.BAD_REQUEST),
    INVALID_OWNER("SCH002", "You do not own this schedule", HttpStatus.FORBIDDEN),
    INVALID_FILE_TYPE("SCH003", "Only .ics files are allowed", HttpStatus.BAD_REQUEST),
    COURSE_NOT_FOUND("SCH004", "Course in schedule not found, suspicious schedule", HttpStatus.NOT_FOUND),
    SEMESTER_NOT_FOUND("SCH005", "Semester in schedule not found, suspicious schedule", HttpStatus.NOT_FOUND),
    INVALID_FILE_FORMAT("SCH006", "Fake ics file, you have to put the true one", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
