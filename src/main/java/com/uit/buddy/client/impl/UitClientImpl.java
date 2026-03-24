package com.uit.buddy.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.client.AbstractBaseClient;
import com.uit.buddy.client.UitClient;
import com.uit.buddy.client.validator.MoodleResponseValidator;
import com.uit.buddy.constant.MoodleApiConstants;
import com.uit.buddy.dto.response.client.AssignmentDetailResponse;
import com.uit.buddy.dto.response.client.CourseDetailResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UitClientImpl extends AbstractBaseClient implements UitClient {

    private final String moodleServerPath;
    private final String restFormat;
    private final MoodleResponseValidator moodleResponseValidator;

    public UitClientImpl(@Qualifier("moodleClient") RestClient restClient, ObjectMapper objectMapper,
            @Value("${app.uit.moodle-server-path}") String moodleServerPath,
            @Value("${app.uit.rest-format}") String restFormat, MoodleResponseValidator moodleResponseValidator) {
        super(restClient, objectMapper);
        this.moodleServerPath = moodleServerPath;
        this.restFormat = restFormat;
        this.moodleResponseValidator = moodleResponseValidator;
    }

    @Override
    protected void validateResponse(Object response) {
        moodleResponseValidator.validate(response);
    }

    @Override
    public SiteInfoResponse fetchSiteInfo(String wstoken) {
        Map<String, String> queryParams = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_SITE_INFO);
        SiteInfoResponse response = get(moodleServerPath, SiteInfoResponse.class, queryParams, null);
        return response;
    }

    @Override
    public List<EnrolledCourseResponse> getUserCourses(String wstoken, Long userId) {
        Map<String, String> queryParams = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_USERS_COURSES);
        queryParams.put(MoodleApiConstants.PARAM_USERID, String.valueOf(userId));

        List<EnrolledCourseResponse> response = getList(moodleServerPath,
                new ParameterizedTypeReference<List<EnrolledCourseResponse>>() {
                }, queryParams, null);
        return response;
    }

    @Override
    public List<CourseDetailResponse> getAllCourseDetail(String wstoken, String courseId) {
        Map<String, String> queryParams = buildCourseContentsParams(wstoken, courseId);
        List<CourseDetailResponse> details = getList(moodleServerPath,
                new ParameterizedTypeReference<List<CourseDetailResponse>>() {
                }, queryParams, null);
        return details;
    }

    @Override
    public AssignmentDetailResponse getCourseAssignments(String wstoken, String assignmentId) {
        Map<String, String> queryParams = buildAssignmentSubmissionsParams(wstoken, assignmentId);
        AssignmentDetailResponse response = get(moodleServerPath, AssignmentDetailResponse.class, queryParams, null);
        return response;
    }

    private Map<String, String> buildBaseParams(String wstoken, String function) {
        Map<String, String> params = new HashMap<>();
        params.put(MoodleApiConstants.PARAM_WSTOKEN, wstoken);
        params.put(MoodleApiConstants.PARAM_WSFUNCTION, function);
        params.put(MoodleApiConstants.PARAM_MOODLEWSRESTFORMAT, restFormat);
        return params;
    }

    private Map<String, String> buildCourseContentsParams(String wstoken, String courseId) {
        Map<String, String> params = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_COURSE_CONTENTS);
        params.put(MoodleApiConstants.PARAM_COURSEID, courseId);
        return params;
    }

    private Map<String, String> buildAssignmentSubmissionsParams(String wstoken, String assignmentId) {
        Map<String, String> params = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_ASSIGNMENT_SUBMISSIONS);
        params.put(MoodleApiConstants.PARAM_ASSIGNMENTID, assignmentId);
        return params;
    }

}
