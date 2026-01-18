package com.uit.buddy.controller.base;

import com.uit.buddy.dto.base.DetailedErrorResponse;
import com.uit.buddy.dto.base.ErrorResponse;
import com.uit.buddy.dto.base.PageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ResponseFactory {

        public <T> SingleResponse<T> createSingleResponse(
                        HttpStatus status,
                        String message,
                        T data) {
                return SingleResponse.<T>builder()
                                .statusCode(status.value())
                                .message(message)
                                .data(data)
                                .build();
        }

        public <T> PageResponse<T> createPageResponse(
                        HttpStatus status,
                        String message,
                        List<T> data,
                        int page,
                        int size,
                        long totalElements,
                        int totalPages) {
                return PageResponse.<T>builder()
                                .statusCode(status.value())
                                .message(message)
                                .data(data)
                                .paging(new PageResponse.PagingResponse(page, size, totalElements, totalPages))
                                .build();
        }

        public <T> ResponseEntity<SingleResponse<T>> successSingle(
                        T data,
                        String message) {
                return ResponseEntity.ok(
                                createSingleResponse(HttpStatus.OK, message, data));
        }

        public ResponseEntity<SuccessResponse> success(String message) {
                SuccessResponse response = SuccessResponse.builder()
                                .statusCode(HttpStatus.OK.value())
                                .message(message)
                                .build();
                return ResponseEntity.ok(response);
        }

        public ResponseEntity<SuccessResponse> success(HttpStatus status, String message) {
                SuccessResponse response = SuccessResponse.builder()
                                .statusCode(status.value())
                                .message(message)
                                .build();
                return ResponseEntity.status(status).body(response);
        }

        public ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
                ErrorResponse response = ErrorResponse.builder()
                                .statusCode(status.value())
                                .message(message)
                                .build();
                return ResponseEntity.status(status).body(response);
        }

        public ResponseEntity<DetailedErrorResponse> detailedError(
                        HttpStatus status,
                        String message,
                        Map<String, String> items) {
                DetailedErrorResponse response = DetailedErrorResponse.builder()
                                .statusCode(status.value())
                                .message(message)
                                .items(items)
                                .build();
                return ResponseEntity.status(status).body(response);
        }
}
