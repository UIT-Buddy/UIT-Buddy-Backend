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
    INVALID_FILE_FORMAT("SCH006", "Fake ics file, you have to put the true one", HttpStatus.BAD_REQUEST),
    INVALID_MONTH("SCH007", "Invalid month value, must be between 1 and 12", HttpStatus.BAD_REQUEST),
    INVALID_FILTER_WITH_DEADLINES("SCH008", "Year is required", HttpStatus.BAD_REQUEST),
    INVALID_FILTER_WITH_CALENDAR("SCH009", "Semester and year are required", HttpStatus.BAD_REQUEST),
    ICS_FILE_NOT_FOUND("SCH009", "User have not uploaded .ics schedule file", HttpStatus.BAD_REQUEST),
    ICS_UPLOADED("SCH010", "User upload a duplicated .ics file", HttpStatus.BAD_REQUEST),
    INVALID_DEADLINE_INPUT("SCH011", "Deadline name and due date are required", HttpStatus.BAD_REQUEST),
    STUDENT_CLASS_NOT_FOUND_FOR_COURSE("SCH012", "No class mapping found for this course", HttpStatus.NOT_FOUND),
    CLASS_NOT_FOUND("SCH011", "Class in schedule not found", HttpStatus.NOT_FOUND),
    INVALID_EXERCISE_NAME("SCH012", "Exercise name is required", HttpStatus.NOT_FOUND),
    INVALID_DUE_TIME("SCH013", "Due time is required", HttpStatus.NOT_FOUND),
    ASSIGNMENT_NOT_EXIST("SCH014", "Assignment not exist", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
