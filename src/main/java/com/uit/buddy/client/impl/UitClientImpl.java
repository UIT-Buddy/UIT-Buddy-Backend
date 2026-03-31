package com.uit.buddy.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.client.AbstractBaseClient;
import com.uit.buddy.client.UitClient;
import com.uit.buddy.client.validator.MoodleResponseValidator;
import com.uit.buddy.config.MoodleRateLimiter;
import com.uit.buddy.constant.MoodleApiConstants;
import com.uit.buddy.dto.response.client.AssignmentDetailResponse;
import com.uit.buddy.dto.response.client.CourseDetailResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import com.uit.buddy.exception.client.ExternalClientException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class UitClientImpl extends AbstractBaseClient implements UitClient {

    private final String moodleServerPath;
    private final String restFormat;
    private final MoodleResponseValidator moodleResponseValidator;
    private final MoodleRateLimiter rateLimiter;

    public UitClientImpl(@Qualifier("moodleClient") RestClient restClient, ObjectMapper objectMapper,
            @Value("${app.uit.moodle-server-path}") String moodleServerPath,
            @Value("${app.uit.rest-format}") String restFormat, MoodleResponseValidator moodleResponseValidator,
            MoodleRateLimiter rateLimiter) {
        super(restClient, objectMapper);
        this.moodleServerPath = moodleServerPath;
        this.restFormat = restFormat;
        this.moodleResponseValidator = moodleResponseValidator;
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void validateResponse(Object response) {
        moodleResponseValidator.validate(response);
    }

    @Retryable(
            retryFor = ExternalClientException.class,
            maxAttemptsExpression = "${moodle.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${moodle.retry.delay-ms:1000}", multiplierExpression = "${moodle.retry.multiplier:2}"))
    @Override
    public SiteInfoResponse fetchSiteInfo(String wstoken) {
        try {
            rateLimiter.acquire();
            Map<String, String> queryParams = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_SITE_INFO);
            return get(moodleServerPath, SiteInfoResponse.class, queryParams, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter permit", e);
        } finally {
            rateLimiter.release();
        }
    }

    @Retryable(
            retryFor = ExternalClientException.class,
            maxAttemptsExpression = "${moodle.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${moodle.retry.delay-ms:1000}", multiplierExpression = "${moodle.retry.multiplier:2}"))
    @Override
    public List<EnrolledCourseResponse> getUserCourses(String wstoken, Long userId) {
        try {
            rateLimiter.acquire();
            Map<String, String> queryParams = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_USERS_COURSES);
            queryParams.put(MoodleApiConstants.PARAM_USERID, String.valueOf(userId));

            return getList(moodleServerPath, new ParameterizedTypeReference<List<EnrolledCourseResponse>>() {},
                    queryParams, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter permit", e);
        } finally {
            rateLimiter.release();
        }
    }

    @Retryable(
            retryFor = ExternalClientException.class,
            maxAttemptsExpression = "${moodle.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${moodle.retry.delay-ms:1000}", multiplierExpression = "${moodle.retry.multiplier:2}"))
    @Override
    public List<CourseDetailResponse> getAllCourseDetail(String wstoken, String courseId) {
        try {
            rateLimiter.acquire();
            Map<String, String> queryParams = buildCourseContentsParams(wstoken, courseId);
            return getList(moodleServerPath, new ParameterizedTypeReference<List<CourseDetailResponse>>() {},
                    queryParams, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter permit", e);
        } finally {
            rateLimiter.release();
        }
    }

    @Retryable(
            retryFor = ExternalClientException.class,
            maxAttemptsExpression = "${moodle.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${moodle.retry.delay-ms:1000}", multiplierExpression = "${moodle.retry.multiplier:2}"))
    @Override
    public AssignmentDetailResponse getCourseAssignments(String wstoken, String assignmentId) {
        try {
            rateLimiter.acquire();
            Map<String, String> queryParams = buildAssignmentSubmissionsParams(wstoken, assignmentId);
            return get(moodleServerPath, AssignmentDetailResponse.class, queryParams, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter permit", e);
        } finally {
            rateLimiter.release();
        }
    }

    @Recover
    public SiteInfoResponse recoverSiteInfo(ExternalClientException e, String wstoken) {
        log.warn("[UitClient] All retries exhausted for fetchSiteInfo: {}", e.getMessage());
        throw e;
    }

    @Recover
    public List<EnrolledCourseResponse> recoverGetUserCourses(ExternalClientException e, String wstoken, Long userId) {
        log.warn("[UitClient] All retries exhausted for getUserCourses: {}", e.getMessage());
        throw e;
    }

    @Recover
    public List<CourseDetailResponse> recoverGetAllCourseDetail(ExternalClientException e, String wstoken, String courseId) {
        log.warn("[UitClient] All retries exhausted for getAllCourseDetail: {}", e.getMessage());
        throw e;
    }

    @Recover
    public AssignmentDetailResponse recoverGetCourseAssignments(ExternalClientException e, String wstoken,
            String assignmentId) {
        log.warn("[UitClient] All retries exhausted for getCourseAssignments: {}", e.getMessage());
        throw e;
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
