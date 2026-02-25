package com.uit.buddy.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.exception.client.ExternalClientErrorCode;
import com.uit.buddy.exception.client.ExternalClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractBaseClient {

    protected final RestClient restClient;
    protected final ObjectMapper objectMapper;

    protected AbstractBaseClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    protected <T> T get(String path, Class<T> responseType, Map<String, String> queryParams, HttpHeaders headers) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    if (queryParams != null)
                        queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .headers(h -> {
                    if (headers != null)
                        h.putAll(headers);
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .body(responseType);
    }

    protected <T> T post(String path, Object body, Class<T> responseType, HttpHeaders headers) {
        log.debug("[HTTP POST] URL: {}, Body: {}, Body Class: {}",
                path, body, body != null ? body.getClass().getName() : "null");

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(body);
            log.debug("[HTTP POST] Serialized JSON body: {}", jsonBody);
        } catch (JsonProcessingException e) {
            log.error("[HTTP POST] Failed to serialize body: {}", e.getMessage());
            throw new ExternalClientException(ExternalClientErrorCode.BAD_REQUEST,
                    "Failed to serialize request body");
        }

        return restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    if (headers != null) {
                        h.putAll(headers);
                        log.debug("[HTTP POST] Headers: {}", headers);
                    }
                })
                .body(jsonBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .body(responseType);
    }

    protected void delete(String path, HttpHeaders headers) {
        restClient.delete()
                .uri(path)
                .headers(h -> {
                    if (headers != null)
                        h.putAll(headers);
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .toBodilessEntity();
    }

    protected <T> List<T> getList(String path,
            ParameterizedTypeReference<List<T>> typeReference,
            Map<String, String> queryParams,
            HttpHeaders headers) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(path);
                    if (queryParams != null)
                        queryParams.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .headers(h -> {
                    if (headers != null)
                        h.putAll(headers);
                    h.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .body(typeReference);
    }

    private void handleErrorResponse(org.springframework.http.HttpRequest request,
            org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
        HttpStatusCode status = response.getStatusCode();

        // Read response body for detailed error information
        String responseBody = "";
        try {
            byte[] bodyBytes = response.getBody().readAllBytes();
            if (bodyBytes != null && bodyBytes.length > 0) {
                responseBody = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                log.error("[External Call Error] {} {} - Status: {} - Response Body: {}",
                        request.getMethod(), request.getURI(), status, responseBody);
            } else {
                log.error("[External Call Error] {} {} - Status: {} - Empty response body",
                        request.getMethod(), request.getURI(), status);
            }
        } catch (Exception e) {
            log.error("[External Call Error] {} {} - Status: {} - Failed to read body: {}",
                    request.getMethod(), request.getURI(), status, e.getMessage());
        }

        String errorMessage = switch (status.value()) {
            case 400 -> {
                if (responseBody != null && !responseBody.isEmpty()) {
                    yield responseBody;
                }
                yield "Bad request from external service";
            }
            case 401 -> "Unauthorized access to external service";
            case 403 -> "Access forbidden to external service";
            case 404 -> "External resource not found";
            default -> "External service error: " + status;
        };

        throw switch (status.value()) {
            case 400 -> new ExternalClientException(ExternalClientErrorCode.BAD_REQUEST, errorMessage);
            case 401 -> new ExternalClientException(ExternalClientErrorCode.UNAUTHORIZED_REQUEST, errorMessage);
            case 403 -> new ExternalClientException(ExternalClientErrorCode.FORBIDDEN_REQUEST, errorMessage);
            case 404 -> new ExternalClientException(ExternalClientErrorCode.NOT_FOUND, errorMessage);
            default -> new ExternalClientException(ExternalClientErrorCode.EXTERNAL_SERVICE_ERROR, errorMessage);
        };
    }

    protected void validateResponse(Object response) {

    }
}