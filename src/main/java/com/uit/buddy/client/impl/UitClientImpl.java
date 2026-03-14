package com.uit.buddy.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.client.AbstractBaseClient;
import com.uit.buddy.client.UitClient;
import com.uit.buddy.client.validator.MoodleResponseValidator;
import com.uit.buddy.constant.MoodleApiConstants;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public UitClientImpl(
      @Qualifier("moodleClient") RestClient restClient,
      ObjectMapper objectMapper,
      @Value("${app.uit.moodle-server-path}") String moodleServerPath,
      @Value("${app.uit.rest-format}") String restFormat,
      MoodleResponseValidator moodleResponseValidator) {
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
    Map<String, String> queryParams =
        buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_SITE_INFO);
    SiteInfoResponse response = get(moodleServerPath, SiteInfoResponse.class, queryParams, null);
    return response;
  }

  @Override
  public List<EnrolledCourseResponse> getUserCourses(String wstoken, Long userId) {
    Map<String, String> queryParams =
        buildBaseParams(wstoken, MoodleApiConstants.FUNCTION_GET_USERS_COURSES);
    queryParams.put(MoodleApiConstants.PARAM_USERID, String.valueOf(userId));

    List<EnrolledCourseResponse> response =
        getList(
            moodleServerPath,
            new ParameterizedTypeReference<List<EnrolledCourseResponse>>() {},
            queryParams,
            null);

    return response;
  }

  private Map<String, String> buildBaseParams(String wstoken, String function) {
    Map<String, String> params = new HashMap<>();
    params.put(MoodleApiConstants.PARAM_WSTOKEN, wstoken);
    params.put(MoodleApiConstants.PARAM_WSFUNCTION, function);
    params.put(MoodleApiConstants.PARAM_MOODLEWSRESTFORMAT, restFormat);
    return params;
  }
}
