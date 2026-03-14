package com.uit.buddy.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.exception.client.ExternalClientErrorCode;
import com.uit.buddy.exception.client.ExternalClientException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

@Slf4j
public abstract class AbstractBaseClient {

  protected final RestClient restClient;
  protected final ObjectMapper objectMapper;

  protected AbstractBaseClient(RestClient restClient, ObjectMapper objectMapper) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
  }

  protected void validateResponse(Object response) {}

  protected <T> T get(
      String path, Class<T> responseType, Map<String, String> queryParams, HttpHeaders headers) {
    T response =
        restClient
            .get()
            .uri(
                uriBuilder -> {
                  uriBuilder.path(path);
                  if (queryParams != null) queryParams.forEach(uriBuilder::queryParam);
                  return uriBuilder.build();
                })
            .headers(
                h -> {
                  if (headers != null) h.putAll(headers);
                })
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
            .body(responseType);

    validateResponse(response);
    return response;
  }

  protected <T> T post(String path, Object body, Class<T> responseType, HttpHeaders headers) {
    String jsonBody = serializeBody(body);

    T response =
        restClient
            .post()
            .uri(path)
            .contentType(MediaType.APPLICATION_JSON)
            .headers(
                h -> {
                  if (headers != null) h.putAll(headers);
                })
            .body(jsonBody)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
            .body(responseType);

    validateResponse(response);
    return response;
  }

  protected void delete(String path, HttpHeaders headers) {
    restClient
        .delete()
        .uri(path)
        .headers(
            h -> {
              if (headers != null) h.putAll(headers);
            })
        .retrieve()
        .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
        .toBodilessEntity();
  }

  protected <T> List<T> getList(
      String path,
      ParameterizedTypeReference<List<T>> typeReference,
      Map<String, String> queryParams,
      HttpHeaders headers) {
    List<T> response =
        restClient
            .get()
            .uri(
                uriBuilder -> {
                  uriBuilder.path(path);
                  if (queryParams != null) queryParams.forEach(uriBuilder::queryParam);
                  return uriBuilder.build();
                })
            .headers(
                h -> {
                  if (headers != null) h.putAll(headers);
                  h.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
            .body(typeReference);

    validateResponse(response);
    return response;
  }

  private String serializeBody(Object body) {
    try {
      return objectMapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      log.error("[External Call] Serialization failed: {}", e.getMessage());
      throw new ExternalClientException(
          ExternalClientErrorCode.BAD_REQUEST, "Failed to serialize request body");
    }
  }

  private void handleErrorResponse(HttpRequest request, ClientHttpResponse response)
      throws IOException {
    HttpStatusCode status = response.getStatusCode();
    String responseBody = readBody(response);

    log.error(
        "[External Call Error] {} {} - Status: {} - Body: {}",
        request.getMethod(),
        request.getURI(),
        status,
        responseBody);

    String errorMessage = mapErrorMessage(status, responseBody);
    throw mapToException(status, errorMessage);
  }

  private String readBody(ClientHttpResponse response) {
    try {
      byte[] bodyBytes = response.getBody().readAllBytes();
      return (bodyBytes.length > 0) ? new String(bodyBytes, StandardCharsets.UTF_8) : "";
    } catch (IOException e) {
      return "Could not read response body";
    }
  }

  private String mapErrorMessage(HttpStatusCode status, String responseBody) {
    if (status.value() == 400 && responseBody != null && !responseBody.isEmpty()) {
      return responseBody;
    }
    return switch (status.value()) {
      case 401 -> "Unauthorized access to external service";
      case 403 -> "Access forbidden to external service";
      case 404 -> "External resource not found";
      default -> "External service error: " + status;
    };
  }

  private ExternalClientException mapToException(HttpStatusCode status, String message) {
    ExternalClientErrorCode errorCode =
        switch (status.value()) {
          case 400 -> ExternalClientErrorCode.BAD_REQUEST;
          case 401 -> ExternalClientErrorCode.UNAUTHORIZED_REQUEST;
          case 403 -> ExternalClientErrorCode.FORBIDDEN_REQUEST;
          case 404 -> ExternalClientErrorCode.NOT_FOUND;
          default -> ExternalClientErrorCode.EXTERNAL_SERVICE_ERROR;
        };
    return new ExternalClientException(errorCode, message);
  }
}
