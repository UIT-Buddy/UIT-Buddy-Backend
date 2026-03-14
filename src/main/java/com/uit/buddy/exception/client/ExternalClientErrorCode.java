package com.uit.buddy.exception.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalClientErrorCode {
  // Connection errors
  CONNECTION_TIMEOUT(
      "CLIENT_001", "Connection timeout to external service", HttpStatus.GATEWAY_TIMEOUT),
  CONNECTION_REFUSED(
      "CLIENT_002", "Connection refused by external service", HttpStatus.BAD_GATEWAY),
  SERVICE_UNAVAILABLE(
      "CLIENT_003", "External service is unavailable", HttpStatus.SERVICE_UNAVAILABLE),

  // Request errors
  BAD_REQUEST("CLIENT_004", "Invalid request to external service", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED_REQUEST(
      "CLIENT_005", "Unauthorized request to external service", HttpStatus.UNAUTHORIZED),
  FORBIDDEN_REQUEST("CLIENT_006", "Forbidden request to external service", HttpStatus.FORBIDDEN),
  NOT_FOUND("CLIENT_007", "Resource not found on external service", HttpStatus.NOT_FOUND),

  // Response errors
  INVALID_RESPONSE("CLIENT_008", "Invalid response from external service", HttpStatus.BAD_GATEWAY),
  RESPONSE_PARSING_ERROR(
      "CLIENT_009", "Failed to parse response from external service", HttpStatus.BAD_GATEWAY),

  // Generic errors
  EXTERNAL_SERVICE_ERROR("CLIENT_010", "External service error", HttpStatus.BAD_GATEWAY),
  UNKNOWN_ERROR(
      "CLIENT_011",
      "Unknown error occurred while calling external service",
      HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
