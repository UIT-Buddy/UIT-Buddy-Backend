package com.uit.buddy.client;

import com.uit.buddy.exception.client.ExternalClientErrorCode;
import com.uit.buddy.exception.client.ExternalClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractBaseClient {

    protected final RestTemplate restTemplate;
    protected final String baseUrl;

    protected AbstractBaseClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    protected <T> T get(String path, Class<T> responseType, Map<String, String> queryParams, HttpHeaders headers) {
        String url = buildUrl(path, queryParams);
        return executeRequest(url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    protected <T> T post(String path, Object body, Class<T> responseType, HttpHeaders headers) {
        String url = buildUrl(path, null);
        return executeRequest(url, HttpMethod.POST, new HttpEntity<>(body, headers), responseType);
    }

    protected void delete(String path, HttpHeaders headers) {
        String url = buildUrl(path, null);
        executeRequest(url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }

    protected <T> List<T> getList(String path, Class<T> elementType, Map<String, String> queryParams,
            HttpHeaders headers) {
        String url = buildUrl(path, queryParams);
        @SuppressWarnings("unchecked")
        Class<T[]> arrayType = (Class<T[]>) java.lang.reflect.Array.newInstance(elementType, 0).getClass();
        T[] array = executeRequest(url, HttpMethod.GET, new HttpEntity<>(headers), arrayType);
        return array != null ? List.of(array) : List.of();
    }

    private <T> T executeRequest(String url, HttpMethod method, HttpEntity<?> entity, Class<T> responseType) {
        try {
            log.info("[External Call] {} to: {}", method, url);

            HttpHeaders headers = new HttpHeaders();
            if (entity.getHeaders() != null) {
                headers.putAll(entity.getHeaders());
            }
            if (headers.getAccept().isEmpty()) {
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            }

            HttpEntity<?> finalEntity = new HttpEntity<>(entity.getBody(), headers);

            ResponseEntity<T> response = restTemplate.exchange(url, method, finalEntity, responseType);
            T body = response.getBody();
            validateResponse(body);
            return body;
        } catch (HttpClientErrorException e) {
            log.error("[External Call Error] {} to {} - Client error: {}", method, url, e.getStatusCode());
            throw mapClientError(e);
        } catch (HttpServerErrorException e) {
            log.error("[External Call Error] {} to {} - Server error: {}", method, url, e.getStatusCode());
            throw new ExternalClientException(ExternalClientErrorCode.EXTERNAL_SERVICE_ERROR,
                    "External service error: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.error("[External Call Error] {} to {} - Connection error", method, url);
            throw mapConnectionError(e);
        } catch (ExternalClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("[External Call Error] {} to {} - Unexpected error: {}", method, url, e.getMessage());
            throw new ExternalClientException(ExternalClientErrorCode.UNKNOWN_ERROR, "Unexpected error", e);
        }
    }

    private String buildUrl(String path, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl).path(path);
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }
        return builder.toUriString();
    }

    private ExternalClientException mapClientError(HttpClientErrorException e) {
        return switch (e.getStatusCode().value()) {
            case 400 -> new ExternalClientException(ExternalClientErrorCode.BAD_REQUEST, "Bad request", e);
            case 401 -> new ExternalClientException(ExternalClientErrorCode.UNAUTHORIZED_REQUEST, "Unauthorized", e);
            case 403 -> new ExternalClientException(ExternalClientErrorCode.FORBIDDEN_REQUEST, "Forbidden", e);
            case 404 -> new ExternalClientException(ExternalClientErrorCode.NOT_FOUND, "Not found", e);
            default -> new ExternalClientException(ExternalClientErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Client error: " + e.getStatusCode(), e);
        };
    }

    private ExternalClientException mapConnectionError(ResourceAccessException e) {
        Throwable cause = e.getCause();
        if (cause instanceof SocketTimeoutException) {
            return new ExternalClientException(ExternalClientErrorCode.CONNECTION_TIMEOUT, "Connection timeout", e);
        } else if (cause instanceof ConnectException) {
            return new ExternalClientException(ExternalClientErrorCode.CONNECTION_REFUSED, "Connection refused", e);
        }
        return new ExternalClientException(ExternalClientErrorCode.SERVICE_UNAVAILABLE, "Service unavailable", e);
    }

    protected void validateResponse(Object response) {

    }
}
