package com.uit.buddy.client;

import com.uit.buddy.dto.response.client.CourseDetailResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import java.util.List;

public interface UitClient {
    SiteInfoResponse fetchSiteInfo(String wstoken);

    List<EnrolledCourseResponse> getUserCourses(String wstoken, Long userId);

    List<CourseDetailResponse> getAllCourseDetail(String wstoken, String courseId);
}
