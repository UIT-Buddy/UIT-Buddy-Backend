package com.uit.buddy.util;

import static org.junit.jupiter.api.Assertions.*;

import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.util.IcsParser.IcsEvent;
import com.uit.buddy.util.IcsParser.ParseResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class IcsParserTest {

    private final IcsParser icsParser = new IcsParser();

    @Test
    void testParseValidIcsFileWithComplexLessonPatterns() throws IOException {
        String icsContent = """
                BEGIN:VCALENDAR
                VERSION:2.0
                CALSCALE:GREGORIAN
                METHOD:REQUEST
                PRODID:-//uit.edu.vn//NONSGML v1.0//EN
                X-WR-CALNAME:23521729@gm.uit.edu.vn
                BEGIN:VEVENT
                UID:69ad0f61623a4
                DTSTAMP:20260308T055545Z
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260220T130000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260220T151500
                SUMMARY:NT208.P23.ANTT - B1.18
                DESCRIPTION:Lớp: NT208.P23.ANTT(Lập trình ứng dụng Web) - B1.18, Thứ 5, Tiết 678910, Giảng viên: Trần Tuấn Dũng,   từ 17/02/2026 - 03/05/2026
                RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=TH;UNTIL=20260503T000000Z
                END:VEVENT
                BEGIN:VEVENT
                UID:69ad0f61623b9
                DTSTAMP:20260308T055545Z
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260306T073000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260306T113000
                SUMMARY:NT208.P23.ANTT.1 - B4.08
                DESCRIPTION:Lớp: NT208.P23.ANTT.1(Lập trình ứng dụng Web) - B4.08, Thứ 5, Tiết 12345, Giảng viên: Nguyễn Bùi Kim Ngân,  Cách 2 tuần, từ 03/03/2026 - 24/05/2026
                RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=TH;UNTIL=20260524T000000Z
                END:VEVENT
                BEGIN:VEVENT
                UID:69ad0f61623c1
                DTSTAMP:20260308T055545Z
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260307T073000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260307T153000
                SUMMARY:SE104.P25 - B4.22
                DESCRIPTION:Lớp: SE104.P25(Nhập môn Công nghệ phần mềm) - B4.22, Thứ 6, Tiết 10111213, Giảng viên: Nguyễn Văn A,   từ 07/03/2026 - 30/05/2026
                RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=FR;UNTIL=20260530T000000Z
                END:VEVENT
                BEGIN:VEVENT
                UID:69ad0f61623c2
                DTSTAMP:20260308T055545Z
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260308T073000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260308T153000
                SUMMARY:CS101.P26 - B5.01
                DESCRIPTION:Lớp: CS101.P26(Khoa học máy tính) - B5.01, Thứ 7, Tiết 891011, Giảng viên: Lê Văn B,   từ 08/03/2026 - 31/05/2026
                RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=SA;UNTIL=20260531T000000Z
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());
        ParseResult result = icsParser.parseIcsFile(inputStream);

        assertNotNull(result);
        assertEquals("23521729", result.getStudentId());
        assertEquals(4, result.getEvents().size());

        // Test case 1: "678910" - should be start=6, end=10
        IcsEvent event1 = result.getEvents().get(0);
        assertEquals("NT208.P23.ANTT", event1.getClassCode());
        assertEquals("Lập trình ứng dụng Web", event1.getCourseName());
        assertEquals("Trần Tuấn Dũng", event1.getTeacherName());
        assertEquals("B1.18", event1.getRoomCode());
        assertEquals(LocalDate.of(2026, 2, 20), event1.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 3), event1.getEndDate());
        assertEquals(LocalTime.of(13, 0), event1.getStartTime());
        assertEquals(LocalTime.of(15, 15), event1.getEndTime());
        assertEquals(5, event1.getDayOfWeek()); // Thursday
        assertEquals("WEEKLY", event1.getFrequency());
        assertEquals(1, event1.getInterval());
        assertEquals(6, event1.getStartLesson());
        assertEquals(10, event1.getEndLesson());

        // Test case 2: "12345" - current logic bug: start=12, end=5 (should be start=1,
        // end=5)
        IcsEvent event2 = result.getEvents().get(1);
        assertEquals("NT208.P23.ANTT.1", event2.getClassCode());
        assertEquals("Lập trình ứng dụng Web", event2.getCourseName());
        assertEquals("Nguyễn Bùi Kim Ngân", event2.getTeacherName());
        assertEquals("B4.08", event2.getRoomCode());
        assertEquals(LocalDate.of(2026, 3, 6), event2.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 24), event2.getEndDate());
        assertEquals(LocalTime.of(7, 30), event2.getStartTime());
        assertEquals(LocalTime.of(11, 30), event2.getEndTime());
        assertEquals(5, event2.getDayOfWeek()); // Thursday
        assertEquals("WEEKLY", event2.getFrequency());
        assertEquals(2, event2.getInterval());
        assertEquals(1, event2.getStartLesson());
        assertEquals(5, event2.getEndLesson());

        // Test case 3: "10111213" - should be start=10, end=13
        IcsEvent event3 = result.getEvents().get(2);
        assertEquals("SE104.P25", event3.getClassCode());
        assertEquals("Nhập môn Công nghệ phần mềm", event3.getCourseName());
        assertEquals("Nguyễn Văn A", event3.getTeacherName());
        assertEquals("B4.22", event3.getRoomCode());
        assertEquals(LocalDate.of(2026, 3, 7), event3.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 30), event3.getEndDate());
        assertEquals(LocalTime.of(7, 30), event3.getStartTime());
        assertEquals(LocalTime.of(15, 30), event3.getEndTime());
        assertEquals(6, event3.getDayOfWeek()); // Friday
        assertEquals("WEEKLY", event3.getFrequency());
        assertEquals(1, event3.getInterval());
        assertEquals(10, event3.getStartLesson());
        assertEquals(13, event3.getEndLesson());

        // Test case 4: "891011" - should be start=8, end=11
        IcsEvent event4 = result.getEvents().get(3);
        assertEquals("CS101.P26", event4.getClassCode());
        assertEquals("Khoa học máy tính", event4.getCourseName());
        assertEquals("Lê Văn B", event4.getTeacherName());
        assertEquals("B5.01", event4.getRoomCode());
        assertEquals(LocalDate.of(2026, 3, 8), event4.getStartDate());
        assertEquals(LocalDate.of(2026, 5, 31), event4.getEndDate());
        assertEquals(LocalTime.of(7, 30), event4.getStartTime());
        assertEquals(LocalTime.of(15, 30), event4.getEndTime());
        assertEquals(7, event4.getDayOfWeek()); // Saturday
        assertEquals("WEEKLY", event4.getFrequency());
        assertEquals(1, event4.getInterval());
        assertEquals(8, event4.getStartLesson());
        assertEquals(11, event4.getEndLesson());
    }

    @Test
    void testParseLessonRangeEdgeCases() throws IOException {
        // Test single digit
        String singleDigitContent = """
                BEGIN:VCALENDAR
                X-WR-CALNAME:23521729@gm.uit.edu.vn
                BEGIN:VEVENT
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260220T130000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260220T151500
                SUMMARY:TEST.001 - A1.01
                DESCRIPTION:Lớp: TEST.001(Test Course) - A1.01, Thứ 2, Tiết 5, Giảng viên: Test Teacher
                RRULE:FREQ=WEEKLY;INTERVAL=1;UNTIL=20260503T000000Z
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(singleDigitContent.getBytes());
        ParseResult result = icsParser.parseIcsFile(inputStream);

        assertEquals(1, result.getEvents().size());
        IcsEvent event = result.getEvents().get(0);
        assertEquals(5, event.getStartLesson());
        assertEquals(5, event.getEndLesson());
    }

    @Test
    void testParseIcsFileWithMissingStudentId() {
        String icsContent = """
                BEGIN:VCALENDAR
                VERSION:2.0
                BEGIN:VEVENT
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260220T130000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260220T151500
                SUMMARY:NT208.P23.ANTT - B1.18
                DESCRIPTION:Lớp: NT208.P23.ANTT(Lập trình ứng dụng Web) - B1.18, Thứ 5, Tiết 678, Giảng viên: Trần Tuấn Dũng
                RRULE:FREQ=WEEKLY;INTERVAL=1;UNTIL=20260503T000000Z
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());

        assertThrows(ScheduleException.class, () -> {
            icsParser.parseIcsFile(inputStream);
        });
    }

    @Test
    void testParseIcsFileWithMissingMandatoryFields() {
        String icsContent = """
                BEGIN:VCALENDAR
                X-WR-CALNAME:23521729@gm.uit.edu.vn
                BEGIN:VEVENT
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260220T130000
                SUMMARY:NT208.P23.ANTT - B1.18
                DESCRIPTION:Incomplete description without course name
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());

        assertThrows(ScheduleException.class, () -> {
            icsParser.parseIcsFile(inputStream);
        });
    }

    @Test
    void testParseIcsFileWithExpiredSchedule() {
        String icsContent = """
                BEGIN:VCALENDAR
                X-WR-CALNAME:23521729@gm.uit.edu.vn
                BEGIN:VEVENT
                DTSTART;TZID=Asia/Ho_Chi_Minh:20240220T130000
                DTEND;TZID=Asia/Ho_Chi_Minh:20240220T151500
                SUMMARY:NT208.P23.ANTT - B1.18
                DESCRIPTION:Lớp: NT208.P23.ANTT(Lập trình ứng dụng Web) - B1.18, Thứ 5, Tiết 678, Giảng viên: Trần Tuấn Dũng
                RRULE:FREQ=WEEKLY;UNTIL=20240503T000000Z
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());

        assertThrows(ScheduleException.class, () -> {
            icsParser.parseIcsFile(inputStream);
        });
    }

    @Test
    void testParseIcsFileWithInvalidSummaryFormat() {
        String icsContent = """
                BEGIN:VCALENDAR
                X-WR-CALNAME:23521729@gm.uit.edu.vn
                BEGIN:VEVENT
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260220T130000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260220T151500
                SUMMARY:Invalid Summary Format
                DESCRIPTION:Lớp: NT208.P23.ANTT(Lập trình ứng dụng Web) - B1.18, Thứ 5, Tiết 678, Giảng viên: Trần Tuấn Dũng
                RRULE:FREQ=WEEKLY;UNTIL=20260503T000000Z
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());

        assertThrows(ScheduleException.class, () -> {
            icsParser.parseIcsFile(inputStream);
        });
    }

    @Test
    void testParseEmptyIcsFile() {
        String icsContent = """
                BEGIN:VCALENDAR
                VERSION:2.0
                X-WR-CALNAME:23521729@gm.uit.edu.vn
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());

        assertThrows(ScheduleException.class, () -> {
            icsParser.parseIcsFile(inputStream);
        });
    }

    @Test
    void testParseIcsFileWithSportClass() throws IOException {
        String icsContent = """
                BEGIN:VCALENDAR
                X-WR-CALNAME:23521729@gm.uit.edu.vn
                BEGIN:VEVENT
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260220T130000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260220T151500
                SUMMARY:PE101.P01 - COURT1
                DESCRIPTION:Lớp: PE101.P01(Thể dục) - Sân bóng rổ, Thứ 2, Tiết 12, Giảng viên: Nguyễn Thể Dục, Ghi chú: Bóng rổ
                RRULE:FREQ=WEEKLY;INTERVAL=1;UNTIL=20260503T000000Z
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());
        ParseResult result = icsParser.parseIcsFile(inputStream);

        assertEquals(1, result.getEvents().size());
        IcsEvent event = result.getEvents().get(0);
        assertEquals("PE101.P01", event.getClassCode());
        assertEquals("Thể dục", event.getCourseName());
        assertEquals("Nguyễn Thể Dục", event.getTeacherName());
        // Should use sport location mapping
        assertEquals("Sân bóng rổ", event.getRoomCode());
        // Current logic bug: "12" → start=12, end=2 (should be start=1, end=2)
        assertEquals(1, event.getStartLesson());
        assertEquals(2, event.getEndLesson());
    }

    @Test
    void testParseIcsFileWithDefaultInterval() throws IOException {
        String icsContent = """
                BEGIN:VCALENDAR
                X-WR-CALNAME:23521729@gm.uit.edu.vn
                BEGIN:VEVENT
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260220T130000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260220T151500
                SUMMARY:NT208.P23.ANTT - B1.18
                DESCRIPTION:Lớp: NT208.P23.ANTT(Lập trình ứng dụng Web) - B1.18, Thứ 5, Tiết 678, Giảng viên: Trần Tuấn Dũng
                RRULE:FREQ=WEEKLY;UNTIL=20260503T000000Z
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());
        ParseResult result = icsParser.parseIcsFile(inputStream);

        assertEquals(1, result.getEvents().size());
        IcsEvent event = result.getEvents().get(0);
        assertEquals(1, event.getInterval()); // Default interval should be 1
    }
}