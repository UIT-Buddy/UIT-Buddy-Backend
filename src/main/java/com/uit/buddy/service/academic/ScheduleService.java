package com.uit.buddy.service.academic;

import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.dto.response.schedule.ScheduleResponse;
import java.util.List;

public interface ScheduleService {
    List<ScheduleResponse> uploadSchedule(String mssv, UploadScheduleRequest request);

    DeadlineResponse fetchDeadlinesFromMoodle(String mssv);
}
