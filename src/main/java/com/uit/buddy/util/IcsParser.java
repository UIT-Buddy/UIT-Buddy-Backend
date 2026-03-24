package com.uit.buddy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.uit.buddy.constant.IcsConstants;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IcsParser {


    @Data
    public static class IcsEvent {
        private String classCode;
        private String courseName;
        private String teacherName;
        private String roomCode;
        private LocalDate startDate;
        private LocalDate endDate;
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
        LocalDate fileLatestEndDate = LocalDate.MIN;
        String extractedStudentId = null;

        for (String line1 : lines) {
            String line = line1.trim();
            if (line.isEmpty())
                continue;
            if (line.startsWith(IcsConstants.X_WR_CALNAME)) {
                extractedStudentId = extractStudentIdFromEmail(line.substring(IcsConstants.X_WR_CALNAME.length()));
                if (extractedStudentId == null) {
                    log.error("[ICS Parser] Failed to extract Student ID from header line: {}", line);
                    throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_FORMAT);
                }
                continue;
            }
            if (line.equals(IcsConstants.BEGIN_VEVENT)) {
                currentEvent = new IcsEvent();
            } else if (line.equals(IcsConstants.END_VEVENT) && currentEvent != null) {
                validateStrictEvent(currentEvent);

                events.add(currentEvent);
                if (currentEvent.getEndDate().isAfter(fileLatestEndDate)) {
                    fileLatestEndDate = currentEvent.getEndDate();
                }
                currentEvent = null;
            } else if (currentEvent != null) {
                parseEventProperty(line, currentEvent);
            }
        }

        if (extractedStudentId == null) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_FORMAT);
        }

        if (fileLatestEndDate.isBefore(LocalDate.now())) {
            throw new ScheduleException(ScheduleErrorCode.EXPIRED_SCHEDULE);
        }

        ParseResult result = new ParseResult();
        result.setStudentId(extractedStudentId);
        result.setEvents(events);
        return result;
    }

    private void parseEventProperty(String line, IcsEvent event) {
        int colonIndex = line.indexOf(':');
        if (colonIndex == -1)
            return;

        String keyPart = line.substring(0, colonIndex);
        String value = line.substring(colonIndex + 1);
        String rawKey = keyPart.split(";")[0];

        switch (rawKey) {
        case IcsConstants.SUMMARY -> {
            Matcher m = IcsConstants.SUMMARY_PATTERN.matcher(value);
            if (m.find()) {
                event.setClassCode(m.group(1));
                String rawRoom = m.group(2).trim();
                String cleanRoom = rawRoom.replaceAll("^(?i)P\\.\\s*", "");
                event.setRoomCode(cleanRoom);
            }
        }
        case IcsConstants.DESCRIPTION -> parseDescriptionStrict(value, event);
        case IcsConstants.DTSTART -> {
            LocalDateTime dt = parseDateTime(value);
            if (event.getStartDate() == null) {
                event.setStartDate(dt.toLocalDate());
            }
            event.setStartTime(dt.toLocalTime());
            event.setDayOfWeek(dt.getDayOfWeek().getValue() + 1);
            log.info("[ICS Parser] Parsed DTSTART: {} -> startDate={}", value, event.getStartDate());
        }
        case IcsConstants.DTEND -> event.setEndTime(parseDateTime(value).toLocalTime());
        case IcsConstants.RRULE -> parseRRule(value, event);
        }
    }

    private void parseDescriptionStrict(String description, IcsEvent event) {
        String cleanDesc = description.replace("\\,", ",").replace("\\;", ";").replace("\u00a0", " ")
                .replaceAll("\\s+", " ").trim();
        final String normalizedDesc = cleanDesc
            .replaceAll("\\s+,", ",")
            .replaceAll(",\\s*,", ",")
            .replaceAll("\\s*--", " --")
            .replaceAll("\\s+", " ")
            .trim();

        // Prefer the course name in parentheses to avoid matching the class code first.
        Matcher courseNameMatcher = Pattern.compile("\\(([^)]+)\\)").matcher(normalizedDesc);
        if (courseNameMatcher.find()) {
            event.setCourseName(courseNameMatcher.group(1).trim());
        } else {
            matchAndSet(IcsConstants.COURSE_NAME_PATTERN, normalizedDesc, event::setCourseName);
        }
        matchAndSet(IcsConstants.TEACHER_PATTERN, normalizedDesc, event::setTeacherName);

        Matcher lessonMatcher = IcsConstants.LESSON_PATTERN.matcher(normalizedDesc);
        if (lessonMatcher.find()) {
            parseLessonRange(lessonMatcher.group(1).trim(), event);
        }

        IcsConstants.SPORT_LOCATION_MAP.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()))
                .filter(entry -> normalizedDesc.toLowerCase().contains(entry.getKey().toLowerCase())).findFirst()
                .ifPresent(entry -> event.setRoomCode(entry.getValue()));
    }

    private void parseLessonRange(String nums, IcsEvent event) {
        if (nums == null || nums.isEmpty())
            return;

        List<Integer> lessons = new ArrayList<>();
        int i = 0;
        while (i < nums.length()) {
            // case: 8 9 10 11 12 13 14 15
            if (i + 1 < nums.length()) {
                int twoDigits = Integer.parseInt(nums.substring(i, i + 2));
                if (twoDigits >= 10 && twoDigits <= 15) {
                    int firstDigit = Character.getNumericValue(nums.charAt(i));
                    int secondDigit = Character.getNumericValue(nums.charAt(i + 1));

                    // case: 123 12 34
                    if (secondDigit == firstDigit + 1 && (i + 2 >= nums.length()
                            || Character.getNumericValue(nums.charAt(i + 2)) == secondDigit + 1)) {
                        lessons.add(firstDigit);
                        i++;
                        continue;
                    }

                    // case 10 11 12
                    lessons.add(twoDigits);
                    i += 2;
                    continue;
                }
            }
            // case: 1 2 3
            lessons.add(Character.getNumericValue(nums.charAt(i)));
            i++;
        }

        if (!lessons.isEmpty()) {
            event.setStartLesson(lessons.get(0));
            event.setEndLesson(lessons.get(lessons.size() - 1));
        }
    }

    private void validateStrictEvent(IcsEvent event) {
        if (event.getClassCode() == null || event.getCourseName() == null || event.getStartDate() == null
                || event.getEndDate() == null || event.getStartTime() == null || event.getEndTime() == null
                || event.getStartLesson() == null || event.getEndLesson() == null) {

            log.error("[ICS Parser] Missing mandatory field in event: {}", event);
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_FORMAT);
        }
    }

    private void matchAndSet(Pattern pattern, String input, Consumer<String> action) {
        Matcher m = pattern.matcher(input);
        if (m.find() && m.groupCount() >= 1) {
            for (int i = 1; i <= m.groupCount(); i++) {
                String value = m.group(i);
                if (value != null && !value.isBlank()) {
                    action.accept(value.trim());
                    break;
                }
            }
        }
    }

    private void parseRRule(String rrule, IcsEvent event) {
        matchAndSet(IcsConstants.FREQ_PATTERN, rrule, event::setFrequency);
        Matcher intM = IcsConstants.INTERVAL_PATTERN.matcher(rrule);
        event.setInterval(intM.find() ? Integer.valueOf(intM.group(1)) : 1);
        Matcher untilM = IcsConstants.UNTIL_PATTERN.matcher(rrule);
        if (untilM.find() && event.getEndDate() == null) {
            String untilToken = untilM.group();
            String untilValue = untilToken.substring(untilToken.indexOf('=') + 1);
            event.setEndDate(parseDateTime(untilValue).toLocalDate());
            log.info("[ICS Parser] Parsed UNTIL date: {}", event.getEndDate());
        }
    }

    private String preprocessIcs(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(" ") || line.startsWith("\t")) {
                    if (sb.length() > 0)
                        sb.append(line.substring(1));
                } else {
                    if (sb.length() > 0)
                        sb.append("\n");
                    sb.append(line);
                }
            }
        }
        return sb.toString();
    }

    private LocalDateTime parseDateTime(String value) {
        String cleanValue = value.substring(value.lastIndexOf(':') + 1).replace("Z", "");
        try {
            if (cleanValue.length() == 8)
                return LocalDate.parse(cleanValue, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay();
            return LocalDateTime.parse(cleanValue, DateTimeFormatter.ofPattern(IcsConstants.DATETIME_PATTERN));
        } catch (Exception e) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_FORMAT);
        }
    }

    private String extractStudentIdFromEmail(String value) {
        Matcher m = IcsConstants.STUDENT_ID_PATTERN.matcher(value);
        return m.find() ? m.group(1) : null;
    }
}
