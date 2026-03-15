package com.uit.buddy.util;

import static org.junit.jupiter.api.Assertions.*;

import com.uit.buddy.util.IcsParser.IcsEvent;
import com.uit.buddy.util.IcsParser.ParseResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IcsParserTest {

    private final IcsParser icsParser = new IcsParser();

    @Test
    void testParseRRuleWithIntervalAndFrequency() throws IOException {
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
                SUMMARY:NT208.P23.ANTT - P. B1.18
                DESCRIPTION:Lớp: NT208.P23.ANTT(Lập trình ứng dụng Web) - P. B1.18, Thứ 5, Tiết 678, Giảng viên: Trần Tuấn Dũng,   từ 17/02/2026 - 03/05/2026
                RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=TH;UNTIL=20260503T000000Z
                END:VEVENT
                BEGIN:VEVENT
                UID:69ad0f61623b9
                DTSTAMP:20260308T055545Z
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260306T073000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260306T113000
                SUMMARY:NT208.P23.ANTT.1 - P. B4.08
                DESCRIPTION:Lớp: NT208.P23.ANTT.1(Lập trình ứng dụng Web) - P. B4.08, Thứ 5, Tiết 12345, Giảng viên: Nguyễn Bùi Kim Ngân,  Cách 2 tuần, từ 03/03/2026 - 24/05/2026
                RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=TH;UNTIL=20260524T000000Z
                END:VEVENT
                BEGIN:VEVENT
                UID:69ad0f61623c1
                DTSTAMP:20260308T055545Z
                DTSTART;TZID=Asia/Ho_Chi_Minh:20260307T073000
                DTEND;TZID=Asia/Ho_Chi_Minh:20260307T153000
                SUMMARY:SE104.P25 - P. B4.22
                DESCRIPTION:Lớp: SE104.P25(Nhập môn Công nghệ phần mềm) - P. B4.22, Thứ 6, Tiết 678910, Giảng viên: Nguyễn Văn A,   từ 07/03/2026 - 30/05/2026
                RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=FR;UNTIL=20260530T000000Z
                END:VEVENT
                END:VCALENDAR
                """;

        InputStream inputStream = new ByteArrayInputStream(icsContent.getBytes());
        ParseResult result = icsParser.parseIcsFile(inputStream);

        assertNotNull(result);
        assertEquals("23521729", result.getStudentId());
        assertEquals(3, result.getEvents().size());

        // Test first event (INTERVAL=1, Tiết 678)
        IcsEvent event1 = result.getEvents().get(0);
        assertEquals("NT208.P23.ANTT", event1.getClassCode());
        assertEquals("WEEKLY", event1.getFrequency());
        assertEquals(1, event1.getInterval());
        assertEquals(6, event1.getStartLesson());
        assertEquals(8, event1.getEndLesson());

        // Test second event (INTERVAL=2, Tiết 12345)
        IcsEvent event2 = result.getEvents().get(1);
        assertEquals("NT208.P23.ANTT.1", event2.getClassCode());
        assertEquals("WEEKLY", event2.getFrequency());
        assertEquals(2, event2.getInterval());
        assertEquals(1, event2.getStartLesson());
        assertEquals(5, event2.getEndLesson());

        // Test third event (Tiết 678910 - end lesson should be 10)
        IcsEvent event3 = result.getEvents().get(2);
        assertEquals("SE104.P25", event3.getClassCode());
        assertEquals("WEEKLY", event3.getFrequency());
        assertEquals(1, event3.getInterval());
        assertEquals(6, event3.getStartLesson());
        assertEquals(10, event3.getEndLesson());
    }
}
