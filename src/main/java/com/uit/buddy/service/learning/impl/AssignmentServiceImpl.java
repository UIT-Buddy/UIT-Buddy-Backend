package com.uit.buddy.service.learning.impl;

import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.entity.learning.StudentTask;
import com.uit.buddy.mapper.schedule.ScheduleMapper;
import com.uit.buddy.repository.learning.StudentTaskRepository;
import com.uit.buddy.service.learning.AssignmentService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AssignmentServiceImpl implements AssignmentService {

    private final StudentTaskRepository studentTaskRepository;
    private final ScheduleMapper scheduleMapper;
    private final Executor executor;

    public AssignmentServiceImpl(StudentTaskRepository studentTaskRepository, ScheduleMapper scheduleMapper,
            @Qualifier("fetchExecutor") Executor executor) {
        this.studentTaskRepository = studentTaskRepository;
        this.scheduleMapper = scheduleMapper;
        this.executor = executor;
    }

    @Override
    public List<CourseContentResponse> getDeadlineWithMssv(String mssv, Integer month, Integer year) {
        return fetchDeadlineAsync(mssv, month, year).join();
    }

    private CompletableFuture<List<CourseContentResponse>> fetchDeadlineAsync(String mssv, Integer month,
            Integer year) {
        return CompletableFuture.supplyAsync(() -> {
            List<CourseContentResponse> listCourseContent = new ArrayList<>();
            List<StudentTask> listStudentTask = month != null && year != null
                    ? studentTaskRepository.findDeadlineTasksByMssv(mssv, month, year)
                    : studentTaskRepository.findDeadlineTasksByMssv(mssv);
            for (StudentTask task : listStudentTask) {
                listCourseContent.add(scheduleMapper.toCourseContentResponse(task));
            }
            return listCourseContent;
        }, executor);
    }

}
