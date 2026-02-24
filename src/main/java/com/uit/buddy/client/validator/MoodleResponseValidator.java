package com.uit.buddy.client.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.dto.response.client.MoodleErrorResponse;
import com.uit.buddy.exception.client.ExternalClientErrorCode;
import com.uit.buddy.exception.client.ExternalClientException;

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

        try {
            MoodleErrorResponse errorResponse = objectMapper.convertValue(response, MoodleErrorResponse.class);

            if (hasError(errorResponse)) {
                handleMoodleError(errorResponse);
            }
        } catch (ExternalClientException e) {
            throw e;
        } catch (Exception e) {
            log.debug("[Moodle Validation] Could not validate response for errors: {}", e.getMessage());
        }
    }

    private boolean hasError(MoodleErrorResponse errorResponse) {
        return errorResponse.exception() != null || errorResponse.errorcode() != null;
    }

    private void handleMoodleError(MoodleErrorResponse errorResponse) {
        String errorMessage = String.format("Moodle error: %s - %s",
                errorResponse.errorcode(),
                errorResponse.message());
        log.error("[Moodle Error] {}", errorMessage);

        ExternalClientErrorCode errorCode = mapMoodleErrorCode(errorResponse.errorcode());
        throw new ExternalClientException(errorCode, errorMessage);
    }

    private ExternalClientErrorCode mapMoodleErrorCode(String moodleErrorCode) {
        if (moodleErrorCode == null) {
            return ExternalClientErrorCode.INVALID_RESPONSE;
        }

        return switch (moodleErrorCode) {
            case "invalidtoken", "invalidlogin" -> ExternalClientErrorCode.UNAUTHORIZED_REQUEST;
            case "accessexception", "nopermission" -> ExternalClientErrorCode.FORBIDDEN_REQUEST;
            case "invalidparameter", "invalidrecord" -> ExternalClientErrorCode.BAD_REQUEST;
            default -> ExternalClientErrorCode.INVALID_RESPONSE;
        };
    }
}
