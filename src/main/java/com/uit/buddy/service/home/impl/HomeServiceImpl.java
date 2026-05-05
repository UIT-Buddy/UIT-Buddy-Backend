package com.uit.buddy.service.home.impl;

import com.uit.buddy.dto.response.home.HomepageResponse;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.entity.learning.TemporaryDeadline;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.home.HomeMapper;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.academic.StudentSubjectClassRepository;
import com.uit.buddy.repository.learning.TemporaryDeadlineRepository;
import com.uit.buddy.repository.notification.NotificationRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.home.HomeService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeServiceImpl implements HomeService {

    private final StudentRepository studentRepository;
    private final NotificationRepository notificationRepository;
    private final SemesterRepository semesterRepository;
    private final StudentSubjectClassRepository studentSubjectClassRepository;
    private final TemporaryDeadlineRepository temporaryDeadlineRepository;
    private final HomeMapper homeMapper;

    @Override
    public HomepageResponse getHomepageData(String mssv, Pageable pageable) {
        log.info("[Home Service] Aggregating homepage data for mssv: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        int todayValue = LocalDate.now().getDayOfWeek().getValue() + 1;
        final int finalTodayValue = todayValue;

        List<SubjectClass> todayClasses = studentSubjectClassRepository.findSubjectsByMssv(mssv).stream()
                .map(StudentSubjectClass::getSubjectClass)
                .filter(sc -> sc.getDayOfWeek() != null && sc.getDayOfWeek() == finalTodayValue).toList();

        long unreadCount = notificationRepository.countUnreadByMssv(mssv);

        // LocalTime nowTime = LocalTime.now();
        LocalTime nowTime = LocalTime.of(17, 30);
        SubjectClass incomingClass = todayClasses.stream()
                .filter(sc -> sc.getStartTime() != null && sc.getEndTime() != null && sc.getEndTime().isAfter(nowTime))
                .min(Comparator.comparing(SubjectClass::getStartTime)).orElse(null);

        LocalDateTime nowDateTime = LocalDateTime.now();

        // Fetch paginated deadlines
        Page<TemporaryDeadline> deadlinePage = temporaryDeadlineRepository.findUpcomingDeadlinesPaginated(mssv,
                nowDateTime, pageable);

        long totalElements = temporaryDeadlineRepository.countUpcomingDeadlines(mssv, nowDateTime);

        HomepageResponse.PagingMetadata paging = new HomepageResponse.PagingMetadata(deadlinePage.getNumber(),
                deadlinePage.getTotalPages(), deadlinePage.getTotalElements());

        return homeMapper.toHomepageResponse(student.getFullName(), todayClasses.size(), (int) unreadCount,
                incomingClass, (int) totalElements, deadlinePage.getContent(), paging, nowTime);
    }
}
