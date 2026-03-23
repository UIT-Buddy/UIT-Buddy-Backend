package com.uit.buddy.client;

import com.uit.buddy.dto.response.client.CourseDetailResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import java.util.List;
import java.util.Map;

public interface UitClient {
    SiteInfoResponse fetchSiteInfo(String wstoken);

    List<EnrolledCourseResponse> getUserCourses(String wstoken, Long userId);

    Map<String, List<CourseDetailResponse>> getCourseContents(String wstoken);
}
