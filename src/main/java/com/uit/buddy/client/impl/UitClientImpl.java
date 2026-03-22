package com.uit.buddy.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.client.AbstractBaseClient;
import com.uit.buddy.client.UitClient;
import com.uit.buddy.client.validator.MoodleResponseValidator;
import com.uit.buddy.constant.MoodleApiConstants;
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
    private final Executor executor;

    public UitClientImpl(@Qualifier("moodleClient") RestClient restClient, ObjectMapper objectMapper,
            @Value("${app.uit.moodle-server-path}") String moodleServerPath,
            @Value("${app.uit.rest-format}") String restFormat, MoodleResponseValidator moodleResponseValidator,
            @Qualifier("uploadExecutor") Executor executor) {
        super(restClient, objectMapper);
        this.moodleServerPath = moodleServerPath;
        this.restFormat = restFormat;
        this.moodleResponseValidator = moodleResponseValidator;
        this.executor = executor;
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
    public Map<String, List<CourseDetailResponse>> getCourseContents(String wstoken) {
        SiteInfoResponse siteInfo = fetchSiteInfo(wstoken);
        Long userId = siteInfo.userid();
        List<EnrolledCourseResponse> enrolledCourseResponses = getUserCourses(wstoken, userId);
        Map<String, String> coursesInSemester = getCourseSemesters(enrolledCourseResponses);

        List<CompletableFuture<Map.Entry<String, List<CourseDetailResponse>>>> futures = new ArrayList<>();

        for (Map.Entry<String, String> entry : coursesInSemester.entrySet()) {
            String courseId = entry.getKey();
            String courseName = entry.getValue();

            futures.add(CompletableFuture.supplyAsync(() -> {
                List<CourseDetailResponse> details = getAllCourseDetail(wstoken, courseId);
                return Map.entry(courseName, details);
            }, executor));
        }

        return futures.stream().map(CompletableFuture::join)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<CourseDetailResponse> getAllCourseDetail(String wstoken, String courseId) {
        Map<String, String> queryParams = buildCourseContentsParams(wstoken, courseId);
        List<CourseDetailResponse> details = getList(moodleServerPath,
                new ParameterizedTypeReference<List<CourseDetailResponse>>() {
                }, queryParams, null);
        return details;
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

    private Map<String, String> getCourseSemesters(List<EnrolledCourseResponse> courses) {
        Map<String, String> coursesInSemester = new HashMap<>();
        for (EnrolledCourseResponse course : courses) {
            if (verifySemester(course.startDate())) {
                coursesInSemester.put(course.id(), course.shortName());
            }
        }
        return coursesInSemester;
    }

    private boolean verifySemester(String startDate) {
        long courseTs = Long.parseLong(startDate);
        long nowTs = Instant.now().getEpochSecond();

        var courseDate = Instant.ofEpochSecond(courseTs).atZone(ZoneId.systemDefault());

        var nowDate = Instant.ofEpochSecond(nowTs).atZone(ZoneId.systemDefault());

        int courseYear = courseDate.getYear();
        int nowYear = nowDate.getYear();

        int courseMonth = courseDate.getMonthValue();
        int nowMonth = nowDate.getMonthValue();

        int courseSemester = (courseMonth <= 6) ? 1 : 2;
        int currentSemester = (nowMonth <= 6) ? 1 : 2;

        return courseYear == nowYear && courseSemester == currentSemester;
    }

}
