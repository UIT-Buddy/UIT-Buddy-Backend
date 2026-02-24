package com.uit.buddy.client.impl;

import com.uit.buddy.client.AbstractBaseClient;
import com.uit.buddy.client.UitClient;
import com.uit.buddy.client.validator.MoodleResponseValidator;
import com.uit.buddy.constant.MoodleApiConstants;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UitClientImpl extends AbstractBaseClient implements UitClient {

    private final String moodleServerPath;
    private final String restFormat;
    private final MoodleResponseValidator moodleResponseValidator;

    public UitClientImpl(
            RestTemplate restTemplate,
            @Value("${app.uit.api-url}") String baseUrl,
            @Value("${app.uit.moodle-server-path}") String moodleServerPath,
            @Value("${app.uit.rest-format}") String restFormat,
            MoodleResponseValidator moodleResponseValidator) {
        super(restTemplate, baseUrl);
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

        return get(moodleServerPath, SiteInfoResponse.class, queryParams, null);
    }

    @Override
    public List<EnrolledCourseResponse> getUserCourses(String wstoken, Long userId) {
        Map<String, String> queryParams = buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_USERS_COURSES);
        queryParams.put("userid", String.valueOf(userId));

        return getList(moodleServerPath, EnrolledCourseResponse.class, queryParams, null);
    }

    private Map<String, String> buildBaseParams(String wstoken, String function) {
        Map<String, String> params = new HashMap<>();
        params.put("wstoken", wstoken);
        params.put("wsfunction", function);
        params.put("moodlewsrestformat", restFormat);
        return params;
    }
}