package com.uit.buddy.mapper.home;

import com.uit.buddy.dto.response.home.HomepageResponse;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.entity.learning.TemporaryDeadline;
import com.uit.buddy.enums.TimeUnit;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface HomeMapper {

    @Mapping(target = "studentName", source = "studentName")
    @Mapping(target = "todayClass", source = "todayClassCount")
    @Mapping(target = "unreadNotificationCount", source = "unreadCount")
    @Mapping(target = "incomingCourse", source = "incomingClass")
    @Mapping(target = "totalDealineCount", source = "totalDeadlineCount")
    @Mapping(target = "incomingDeadlines", source = "deadlines")
    @Mapping(target = "paging", source = "paging")
    HomepageResponse toHomepageResponse(String studentName, int todayClassCount, int unreadCount,
            SubjectClass incomingClass, int totalDeadlineCount, List<TemporaryDeadline> deadlines,
            HomepageResponse.PagingMetadata paging);

    @Mapping(target = "remainingTime", source = "startTime", qualifiedByName = "calculateRemainingMinutes")
    @Mapping(target = "studentsInClass", constant = "0") // Default if not available
    @Mapping(target = "courseCode", source = "courseCode")
    @Mapping(target = "courseName", source = "course.courseName")
    @Mapping(target = "roomCode", source = "roomCode")
    @Mapping(target = "lecturerName", source = "teacherName")
    HomepageResponse.IncomingClass toIncomingClass(SubjectClass subjectClass);

    @Mapping(target = "deadlineName", source = "deadlineName")
    @Mapping(target = "remainingTime", source = "dueDate", qualifiedByName = "calculateRemainingTime")
    @Mapping(target = "dueDate", source = "dueDate")
    HomepageResponse.IncomingDeadline toIncomingDeadline(TemporaryDeadline deadline);

    @Named("calculateRemainingMinutes")
    default int calculateRemainingMinutes(LocalTime startTime) {
        if (startTime == null)
            return 0;
        LocalTime now = LocalTime.now();
        if (now.isAfter(startTime))
            return 0;
        return (int) Duration.between(now, startTime).toMinutes();
    }

    @Named("calculateRemainingTime")
    default HomepageResponse.IncomingDeadline.RemainingTime calculateRemainingTime(LocalDateTime dueDate) {
        if (dueDate == null) {
            return new HomepageResponse.IncomingDeadline.RemainingTime(0, TimeUnit.MINUTE);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dueDate)) {
            return new HomepageResponse.IncomingDeadline.RemainingTime(0, TimeUnit.MINUTE);
        }

        Duration duration = Duration.between(now, dueDate);
        long minutes = duration.toMinutes();

        if (minutes >= 10080) { // 7 days * 1440 minutes
            return new HomepageResponse.IncomingDeadline.RemainingTime((int) (minutes / 10080), TimeUnit.WEEK);
        } else if (minutes >= 1440) { // 24 hours * 60 minutes
            return new HomepageResponse.IncomingDeadline.RemainingTime((int) (minutes / 1440), TimeUnit.DAY);
        } else if (minutes >= 60) {
            return new HomepageResponse.IncomingDeadline.RemainingTime((int) (minutes / 60), TimeUnit.HOUR);
        } else {
            return new HomepageResponse.IncomingDeadline.RemainingTime((int) minutes, TimeUnit.MINUTE);
        }
    }
}
