package com.uit.buddy.service.academic;

import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;

public interface ScheduleService {
    void uploadSchedule(String mssv, UploadScheduleRequest request);

    DeadlineResponse fetchDeadlinesFromMoodle(String mssv);
}
