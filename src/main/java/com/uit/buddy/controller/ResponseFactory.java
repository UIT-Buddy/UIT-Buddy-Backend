package com.uit.buddy.controller;

import com.uit.buddy.dto.base.CreatedResponse;
import com.uit.buddy.dto.base.CursorPageResponse;
import com.uit.buddy.dto.base.ErrorResponse;
import com.uit.buddy.dto.base.PageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseFactory {

  public <T> SingleResponse<T> createSingleResponse(HttpStatus status, String message, T data) {
    return new SingleResponse<>(status.value(), message, data);
  }

  public <T> PageResponse<T> createPageResponse(
      HttpStatus status,
      String message,
      List<T> data,
      int page,
      int size,
      long totalElements,
      int totalPages) {
    return new PageResponse<>(
        status.value(),
        message,
        data,
        new PageResponse.PagingInfo(page, size, totalElements, totalPages));
  }

  public <T> CursorPageResponse<T> createCursorPageResponse(
      HttpStatus status, String message, List<T> data, String cursor, boolean hasMore, int limit) {
    return new CursorPageResponse<>(
        status.value(), message, data, new CursorPageResponse.PagingInfo(cursor, hasMore, limit));
  }

  public <T> ResponseEntity<SingleResponse<T>> successSingle(T data, String message) {
    return ResponseEntity.ok(createSingleResponse(HttpStatus.OK, message, data));
  }

  public ResponseEntity<SuccessResponse> success(String message) {
    SuccessResponse response = new SuccessResponse(HttpStatus.OK.value(), message);
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<SuccessResponse> success(HttpStatus status, String message) {
    SuccessResponse response = new SuccessResponse(status.value(), message);
    return ResponseEntity.status(status).body(response);
  }

  public <T> ResponseEntity<CreatedResponse<T>> created(T data, String message) {
    CreatedResponse<T> response = new CreatedResponse<>(HttpStatus.CREATED.value(), message, data);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  public ResponseEntity<CreatedResponse<Void>> created(String message) {
    CreatedResponse<Void> response =
        new CreatedResponse<>(HttpStatus.CREATED.value(), message, null);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  public ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
    ErrorResponse response = new ErrorResponse(status.value(), message, null);
    return ResponseEntity.status(status).body(response);
  }

  public ResponseEntity<ErrorResponse> error(HttpStatus status, String message, String errorCode) {
    ErrorResponse response = new ErrorResponse(status.value(), message, errorCode);
    return ResponseEntity.status(status).body(response);
  }
}
