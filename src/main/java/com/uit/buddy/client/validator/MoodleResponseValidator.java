package com.uit.buddy.client.validator;

import static com.uit.buddy.constant.MoodleApiConstants.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.dto.response.client.MoodleErrorResponse;
import com.uit.buddy.exception.client.ExternalClientErrorCode;
import com.uit.buddy.exception.client.ExternalClientException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MoodleResponseValidator {

  private final ObjectMapper objectMapper;

  public void validate(Object response) {
    if (response == null) {
      return;
    }

    if (response instanceof Map<?, ?> map) {

      if (map.containsKey(KEY_EXCEPTION) || map.containsKey(KEY_ERROR_CODE)) {
        try {
          MoodleErrorResponse errorResponse =
              objectMapper.convertValue(response, MoodleErrorResponse.class);
          handleMoodleError(errorResponse);
        } catch (IllegalArgumentException e) {
          log.debug(
              "[Moodle Validation] Response contains error keys but is not a standard MoodleErrorResponse");
        }
      }
    }
  }

  private void handleMoodleError(MoodleErrorResponse errorResponse) {
    String errorMessage =
        String.format("Moodle error: %s - %s", errorResponse.errorcode(), errorResponse.message());
    log.error("[Moodle Error] {}", errorMessage);

    ExternalClientErrorCode errorCode = mapMoodleErrorCode(errorResponse.errorcode());
    throw new ExternalClientException(errorCode, errorMessage);
  }

  private ExternalClientErrorCode mapMoodleErrorCode(String moodleErrorCode) {
    if (moodleErrorCode == null) {
      return ExternalClientErrorCode.INVALID_RESPONSE;
    }

    return switch (moodleErrorCode) {
      case ERROR_INVALID_TOKEN, ERROR_INVALID_LOGIN -> ExternalClientErrorCode.UNAUTHORIZED_REQUEST;
      case ERROR_ACCESS_EXCEPTION, ERROR_NO_PERMISSION -> ExternalClientErrorCode.FORBIDDEN_REQUEST;
      case ERROR_INVALID_PARAMETER, ERROR_INVALID_RECORD -> ExternalClientErrorCode.BAD_REQUEST;
      default -> ExternalClientErrorCode.INVALID_RESPONSE;
    };
  }
}
