package com.uit.buddy.mapper.home;

import com.uit.buddy.dto.response.home.HomepageResponse;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.entity.learning.TemporaryDeadline;
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
    @Mapping(target = "remainingTime", source = "dueDate", qualifiedByName = "calculateRemainingMinutesDateTime")
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

    @Named("calculateRemainingMinutesDateTime")
    default int calculateRemainingMinutesDateTime(LocalDateTime dueDate) {
        if (dueDate == null)
            return 0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(dueDate))
            return 0;
        return (int) Duration.between(now, dueDate).toMinutes();
    }
}
