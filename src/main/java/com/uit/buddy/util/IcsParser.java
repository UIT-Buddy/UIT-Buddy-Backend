package com.uit.buddy.util;

import com.uit.buddy.constant.IcsConstants;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IcsParser {

    @Data
    public static class IcsEvent {
        private String classCode;
        private String courseName;
        private String teacherName;
        private String roomCode;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private Integer dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer startLesson;
        private Integer endLesson;
        private String frequency;
        private Integer interval;
    }

    @Data
    public static class ParseResult {
        private String studentId;
        private List<IcsEvent> events;
    }

    public ParseResult parseIcsFile(InputStream inputStream) throws IOException {
        List<IcsEvent> events = new ArrayList<>();
        String content = preprocessIcs(inputStream);
        String[] lines = content.split("\n");

        IcsEvent currentEvent = null;
        LocalDateTime fileLatestEndDate = LocalDateTime.MIN;
        String extractedStudentId = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            if (line.startsWith(IcsConstants.X_WR_CALNAME)) {
                extractedStudentId = extractStudentIdFromEmail(line.substring(IcsConstants.X_WR_CALNAME.length()));
                continue;
            }

            if (line.equals(IcsConstants.BEGIN_VEVENT)) {
                currentEvent = new IcsEvent();
            } else if (line.equals(IcsConstants.END_VEVENT) && currentEvent != null) {
                if (isValidEvent(currentEvent)) {
                    events.add(currentEvent);
                    if (currentEvent.getEndDateTime().isAfter(fileLatestEndDate)) {
                        fileLatestEndDate = currentEvent.getEndDateTime();
                    }
                }
                currentEvent = null;
            } else if (currentEvent != null) {
                parseEventProperty(line, currentEvent);
            }
        }

        // expired
        if (!events.isEmpty() && fileLatestEndDate.isBefore(LocalDateTime.now())) {
            throw new ScheduleException(ScheduleErrorCode.EXPIRED_SCHEDULE);
        }

        ParseResult result = new ParseResult();
        result.setStudentId(extractedStudentId);
        result.setEvents(events);
        return result;
    }

    // solving Line-Folding in ICS parsing
    private String preprocessIcs(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(" ") || line.startsWith("\t")) {
                    sb.append(line.substring(1));
                } else {
                    sb.append("\n").append(line);
                }
            }
        }
        return sb.toString();
    }

    private String extractStudentIdFromEmail(String value) {
        Pattern p = Pattern.compile(IcsConstants.STUDENT_ID_PATTERN);
        Matcher m = p.matcher(value);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    private void parseEventProperty(String line, IcsEvent event) {
        String[] parts = line.split(":", 2);
        if (parts.length < 2)
            return;

        String key = parts[0];
        String value = parts[1];

        if (key.startsWith(IcsConstants.SUMMARY)) {
            // VD: NT208.P23.ANTT - P. B1.18
            Pattern pattern = Pattern.compile(IcsConstants.SUMMARY_PATTERN);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                event.setClassCode(matcher.group(1));
                event.setRoomCode(matcher.group(2));
            }
        } else if (key.startsWith(IcsConstants.DESCRIPTION)) {
            parseDescription(value, event);
        } else if (key.startsWith(IcsConstants.DTSTART)) {
            event.setStartDateTime(parseDateTime(value));
            event.setStartTime(event.getStartDateTime().toLocalTime());
            event.setDayOfWeek(event.getStartDateTime().getDayOfWeek().getValue());
        } else if (key.startsWith(IcsConstants.DTEND)) {
            // Tạm thời set endDateTime là kết thúc buổi học đầu tiên
            event.setEndDateTime(parseDateTime(value));
            event.setEndTime(event.getEndDateTime().toLocalTime());
        } else if (key.startsWith(IcsConstants.RRULE)) {
            // Parse RRULE for frequency, interval, and until date
            parseRRule(value, event);
        }
    }

    private void parseDescription(String description, IcsEvent event) {
        // Làm sạch ký tự &nbsp; (u00a0) thường có trong file UIT
        String cleanDesc = description.replace("\u00a0", " ");

        // Parse course name từ (...)
        Pattern coursePattern = Pattern.compile(IcsConstants.COURSE_NAME_PATTERN);
        Matcher courseMatcher = coursePattern.matcher(cleanDesc);
        if (courseMatcher.find()) {
            event.setCourseName(courseMatcher.group(1));
        }

        // Parse teacher name
        Pattern teacherPattern = Pattern.compile(IcsConstants.TEACHER_PATTERN);
        Matcher teacherMatcher = teacherPattern.matcher(cleanDesc);
        if (teacherMatcher.find()) {
            event.setTeacherName(teacherMatcher.group(1).trim());
        }

        // Parse lesson numbers từ "Tiết 678" hoặc "Tiết 123" hoặc "Tiết 678910"
        Pattern lessonPattern = Pattern.compile(IcsConstants.LESSON_PATTERN);
        Matcher lessonMatcher = lessonPattern.matcher(cleanDesc);
        if (lessonMatcher.find()) {
            String lessons = lessonMatcher.group(1);
            log.debug("[ICS Parser] Found lesson pattern: {}", lessons);

            if (lessons.length() >= 2) {
                int startLesson = Character.getNumericValue(lessons.charAt(0));
                int endLesson;
                if (lessons.length() >= 5 && lessons.endsWith("10")) {
                    endLesson = 10;
                } else {
                    endLesson = Character.getNumericValue(lessons.charAt(lessons.length() - 1));
                }

                event.setStartLesson(startLesson);
                event.setEndLesson(endLesson);
                log.debug("[ICS Parser] Parsed lessons: start={}, end={}", startLesson, endLesson);
            }
        } else {
            log.warn("[ICS Parser] No lesson pattern found in description: {}", cleanDesc);
        }

        // Parse room code đặc biệt cho môn thể dục
        parseSpecialRoomCode(cleanDesc, event);
    }

    private void parseSpecialRoomCode(String description, IcsEvent event) {
        // Nếu là môn thể dục và có ghi chú về môn thể thao
        if (event.getClassCode() != null && event.getClassCode().startsWith(IcsConstants.PE_PREFIX)) {
            Pattern sportPattern = Pattern.compile(IcsConstants.NOTE_PATTERN);
            Matcher sportMatcher = sportPattern.matcher(description);
            if (sportMatcher.find()) {
                String sport = sportMatcher.group(1).trim();
                if (!sport.isEmpty() && !sport.toLowerCase().contains("học từ")) {
                    event.setRoomCode(IcsConstants.COURT_PREFIX + sport);
                    return;
                }
            }
            // fallback
            for (String sport : IcsConstants.SPORTS) {
                if (description.toLowerCase().contains(sport.toLowerCase())) {
                    event.setRoomCode(IcsConstants.COURT_PREFIX + sport);
                    return;
                }
            }
        }
    }

    private void parseRRule(String rrule, IcsEvent event) {
        // Parse FREQ (frequency)
        Pattern freqPattern = Pattern.compile(IcsConstants.FREQ_PATTERN);
        Matcher freqMatcher = freqPattern.matcher(rrule);
        if (freqMatcher.find()) {
            event.setFrequency(freqMatcher.group(1));
        }

        // Parse INTERVAL
        Pattern intervalPattern = Pattern.compile(IcsConstants.INTERVAL_PATTERN);
        Matcher intervalMatcher = intervalPattern.matcher(rrule);
        if (intervalMatcher.find()) {
            event.setInterval(Integer.parseInt(intervalMatcher.group(1)));
        } else {
            // Default interval is 1 if not specified
            event.setInterval(1);
        }

        // Parse UNTIL (end date)
        Pattern untilPattern = Pattern.compile(IcsConstants.UNTIL_PATTERN);
        Matcher untilMatcher = untilPattern.matcher(rrule);
        if (untilMatcher.find()) {
            event.setEndDateTime(parseDateTime(untilMatcher.group(1)));
        }
    }

    private LocalDateTime parseDateTime(String value) {
        // Xử lý các format: 20250220T130000 hoặc 20250503T000000Z
        String cleanValue = value.replace("Z", "");
        return LocalDateTime.parse(cleanValue, DateTimeFormatter.ofPattern(IcsConstants.DATETIME_PATTERN));
    }

    private boolean isValidEvent(IcsEvent event) {
        return event.getClassCode() != null && event.getStartDateTime() != null;
    }
}
