package com.uit.buddy.scheduler;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScheduleScheduler {
    private final ScheduleService scheduleService;
    private final StudentRepository studentRepository;
    private final Executor executor;

    public ScheduleScheduler(ScheduleService scheduleService, StudentRepository studentRepository,
            @Qualifier("fetchExecutor") Executor executor) {
        this.scheduleService = scheduleService;
        this.studentRepository = studentRepository;
        this.executor = executor;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void scrapeAllDeadlineOfStudent() {
        List<String> listOfMssv = studentRepository.findMssvAll();
        List<CompletableFuture<Void>> futures = listOfMssv.stream()
                .map(mssv -> CompletableFuture.runAsync(() -> scheduleService.fetchDeadline(mssv, null, null,
                        org.springframework.data.domain.Pageable.unpaged()), executor))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.debug("[Schedule Scheduler] Refreshed deadlines for {} students", listOfMssv.size());
    }
}
