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
import com.uit.buddy.exception.client.ExternalClientErrorCode;
import com.uit.buddy.exception.client.ExternalClientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class UitClientImpl extends AbstractBaseClient implements UitClient {

    private final String moodleServerPath;
    private final String restFormat;
    private final MoodleResponseValidator moodleResponseValidator;
    private final MoodleRateLimiter rateLimiter;
    private final AtomicReference<SiteInfoResponse> siteInfoCache = new AtomicReference<>();
    private final ApplicationContext applicationContext;

    public UitClientImpl(@Qualifier("moodleClient") RestClient restClient, ObjectMapper objectMapper,
            @Value("${app.uit.moodle-server-path}") String moodleServerPath,
            @Value("${app.uit.rest-format}") String restFormat, MoodleResponseValidator moodleResponseValidator,
            MoodleRateLimiter rateLimiter, ApplicationContext applicationContext) {
        super(restClient, objectMapper);
        this.moodleServerPath = moodleServerPath;
        this.restFormat = restFormat;
        this.moodleResponseValidator = moodleResponseValidator;
        this.rateLimiter = rateLimiter;
        this.applicationContext = applicationContext;
    }

    @Override
    protected void validateResponse(Object response) {
        moodleResponseValidator.validate(response);
    }

    @Retryable(retryFor = { ExternalClientException.class,
            RestClientException.class }, maxAttemptsExpression = "${moodle.retry.max-attempts:3}", backoff = @Backoff(delayExpression = "${moodle.retry.delay-ms:1000}", multiplierExpression = "${moodle.retry.multiplier:2}"))
    @CircuitBreaker(name = "moodle", fallbackMethod = "fallbackGetSiteInfo")
    @Override
    public SiteInfoResponse fetchSiteInfo(String wstoken) {
        try {
            rateLimiter.acquire();
            Map<String, String> queryParams = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_SITE_INFO);
            SiteInfoResponse response = get(moodleServerPath, SiteInfoResponse.class, queryParams, null);
            siteInfoCache.set(response);
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter permit", e);
        } finally {
            rateLimiter.release();
        }
    }

    public SiteInfoResponse fallbackGetSiteInfo(String wstoken, Throwable t) {
        log.warn("Circuit breaker OPEN for Moodle. Returning stale cache.", t);
        return siteInfoCache.get();
    }

    @Retryable(retryFor = { ExternalClientException.class,
            RestClientException.class }, maxAttemptsExpression = "${moodle.retry.max-attempts:3}", backoff = @Backoff(delayExpression = "${moodle.retry.delay-ms:1000}", multiplierExpression = "${moodle.retry.multiplier:2}"))
    @Override
    public List<EnrolledCourseResponse> getUserCourses(String wstoken, Long userId) {
        try {
            rateLimiter.acquire();
            Map<String, String> queryParams = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_USERS_COURSES);
            queryParams.put(MoodleApiConstants.PARAM_USERID, String.valueOf(userId));

            return getList(moodleServerPath, new ParameterizedTypeReference<List<EnrolledCourseResponse>>() {
            }, queryParams, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter permit", e);
        } finally {
            rateLimiter.release();
        }
    }

    @Retryable(retryFor = { ExternalClientException.class,
            RestClientException.class }, maxAttemptsExpression = "${moodle.retry.max-attempts:3}", backoff = @Backoff(delayExpression = "${moodle.retry.delay-ms:1000}", multiplierExpression = "${moodle.retry.multiplier:2}"))
    @Override
    public List<CourseDetailResponse> getAllCourseDetail(String wstoken, String courseId) {
        try {
            rateLimiter.acquire();
            Map<String, String> queryParams = buildCourseContentsParams(wstoken, courseId);
            return getList(moodleServerPath, new ParameterizedTypeReference<List<CourseDetailResponse>>() {
            }, queryParams, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter permit", e);
        } finally {
            rateLimiter.release();
        }
    }

    /**
     * Outer wrapper: acquires/releases rate-limiter permit around the entire call so that circuit-open calls (which
     * bypass the method body entirely) do NOT leak permits.
     */
    @Override
    public AssignmentDetailResponse getCourseAssignments(String wstoken, String assignmentId) {
        try {
            rateLimiter.acquire();
            return doGetCourseAssignments(wstoken, assignmentId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for rate limiter permit", e);
        } finally {
            rateLimiter.release();
        }
    }

    /**
     * Inner method holds the resilience annotations. When the circuit is OPEN the fallback fires immediately — without
     * running this method body — so acquire() is never called and no permit leaks.
     */
    @Retryable(retryFor = { ExternalClientException.class,
            RestClientException.class }, maxAttemptsExpression = "${moodle.retry.max-attempts:3}", backoff = @Backoff(delayExpression = "${moodle.retry.delay-ms:1000}", multiplierExpression = "${moodle.retry.multiplier:2}"))
    @CircuitBreaker(name = "moodleAssignments", fallbackMethod = "fallbackGetCourseAssignments")
    private AssignmentDetailResponse doGetCourseAssignments(String wstoken, String assignmentId) {
        Map<String, String> queryParams = buildAssignmentSubmissionsParams(wstoken, assignmentId);
        return get(moodleServerPath, AssignmentDetailResponse.class, queryParams, null);
    }

    public AssignmentDetailResponse fallbackGetCourseAssignments(String wstoken, String assignmentId, Throwable t) {
        log.warn("[UitClient] Circuit breaker OPEN for getCourseAssignments (assignmentId={}): {}", assignmentId,
                t.getMessage());
        return null;
    }

    /**
     * Batch-fetch submission statuses for multiple assignments in parallel. Each call fires independently through the
     * Spring proxy so @CircuitBreaker + @Retryable interceptors fire per-call. Calls that hit an open circuit or fail
     * return null so the caller can fall back to date-only inference.
     */
    @Override
    public Map<String, AssignmentDetailResponse> getAssignmentsInfo(String wstoken, List<String> assignmentIds) {
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            return Map.of();
        }

        // Obtain the AOP proxy so interceptors (CircuitBreaker, Retryable) fire on each call
        UitClient uitClientProxy = applicationContext.getBean(UitClient.class);

        List<CompletableFuture<Map.Entry<String, AssignmentDetailResponse>>> futures = new java.util.ArrayList<>();
        for (String id : assignmentIds) {
            String assignmentId = id;
            CompletableFuture<Map.Entry<String, AssignmentDetailResponse>> f = CompletableFuture.supplyAsync(() -> {
                AssignmentDetailResponse resp = uitClientProxy.getCourseAssignments(wstoken, assignmentId);
                return Map.entry(assignmentId, resp);
            }, ForkJoinPool.commonPool()).exceptionally(ex -> {
                log.debug("[UitClient] getCourseAssignments exception for id={}: {}", assignmentId, ex.getMessage());
                return Map.entry(assignmentId, (AssignmentDetailResponse) null);
            });
            futures.add(f);
        }

        Map<String, AssignmentDetailResponse> results = new HashMap<>();
        for (CompletableFuture<Map.Entry<String, AssignmentDetailResponse>> future : futures) {
            try {
                Map.Entry<String, AssignmentDetailResponse> entry = future.join();
                results.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                // Already handled in exceptionally above; safe to skip
            }
        }

        return results;
    }

    @Recover
    public SiteInfoResponse recoverSiteInfo(ExternalClientException e, String wstoken) {
        log.warn("[UitClient] All retries exhausted for fetchSiteInfo: {}", e.getMessage());
        throw e;
    }

    @Recover
    public SiteInfoResponse recoverSiteInfoFromRestClient(RestClientException e, String wstoken) {
        log.warn("[UitClient] All retries exhausted for fetchSiteInfo due to RestClient error: {}", e.getMessage());
        throw new ExternalClientException(ExternalClientErrorCode.EXTERNAL_SERVICE_ERROR,
                "External service error during fetchSiteInfo", e);
    }

    @Recover
    public List<EnrolledCourseResponse> recoverGetUserCourses(ExternalClientException e, String wstoken, Long userId) {
        log.warn("[UitClient] All retries exhausted for getUserCourses: {}", e.getMessage());
        throw e;
    }

    @Recover
    public List<EnrolledCourseResponse> recoverGetUserCoursesFromParseError(HttpMessageNotReadableException e,
            String wstoken, Long userId) {
        log.warn("[UitClient] All retries exhausted for getUserCourses due to parse error: {}", e.getMessage());
        throw new ExternalClientException(ExternalClientErrorCode.RESPONSE_PARSING_ERROR,
                "Failed to parse Moodle enrolled courses response", e);
    }

    @Recover
    public List<EnrolledCourseResponse> recoverGetUserCoursesFromRestClient(RestClientException e, String wstoken,
            Long userId) {
        log.warn("[UitClient] All retries exhausted for getUserCourses due to RestClient error: {}", e.getMessage());
        throw new ExternalClientException(ExternalClientErrorCode.EXTERNAL_SERVICE_ERROR,
                "External service error during getUserCourses", e);
    }

    @Recover
    public List<CourseDetailResponse> recoverGetAllCourseDetail(ExternalClientException e, String wstoken,
            String courseId) {
        log.warn("[UitClient] All retries exhausted for getAllCourseDetail: {}", e.getMessage());
        throw e;
    }

    @Recover
    public List<CourseDetailResponse> recoverGetAllCourseDetailFromRestClient(RestClientException e, String wstoken,
            String courseId) {
        log.warn("[UitClient] All retries exhausted for getAllCourseDetail due to RestClient error: {}",
                e.getMessage());
        throw new ExternalClientException(ExternalClientErrorCode.EXTERNAL_SERVICE_ERROR,
                "External service error during getAllCourseDetail", e);
    }

    @Recover
    public AssignmentDetailResponse recoverGetCourseAssignments(ExternalClientException e, String wstoken,
            String assignmentId) {
        log.warn("[UitClient] All retries exhausted for getCourseAssignments: {}", e.getMessage());
        throw e;
    }

    @Recover
    public AssignmentDetailResponse recoverGetCourseAssignmentsFromRestClient(RestClientException e, String wstoken,
            String assignmentId) {
        log.warn("[UitClient] All retries exhausted for getCourseAssignments due to RestClient error: {}",
                e.getMessage());
        throw new ExternalClientException(ExternalClientErrorCode.EXTERNAL_SERVICE_ERROR,
                "External service error during getCourseAssignments", e);
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
