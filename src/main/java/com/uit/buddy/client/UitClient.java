package com.uit.buddy.client;

import com.uit.buddy.dto.response.client.AssignmentDetailResponse;
import com.uit.buddy.dto.response.client.CourseDetailResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import java.util.List;
import java.util.Map;

public interface UitClient {
    SiteInfoResponse fetchSiteInfo(String wstoken);

    List<EnrolledCourseResponse> getUserCourses(String wstoken, Long userId);

    List<CourseDetailResponse> getAllCourseDetail(String wstoken, String courseId);

    AssignmentDetailResponse getCourseAssignments(String wstoken, String assignmentId);

    /**
     * Batch-fetch submission statuses for multiple assignment IDs in parallel. Uses the moodleAssignments circuit
     * breaker — returns null for any assignment whose circuit is open. Calls are executed in parallel using
     * CompletableFuture.allOf to maximise throughput.
     *
     * @param wstoken
     *            the decrypted Moodle token
     * @param assignmentIds
     *            list of assignment IDs to fetch
     *
     * @return map of assignmentId → response (null if circuit open or call failed)
     */
    Map<String, AssignmentDetailResponse> getAssignmentsInfo(String wstoken, List<String> assignmentIds);
}
