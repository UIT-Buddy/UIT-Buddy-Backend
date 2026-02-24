package com.uit.buddy.exception;

import com.uit.buddy.dto.base.ErrorResponse;
import com.uit.buddy.exception.system.SystemErrorCode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(BaseException.class)
        public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
                log.warn("Base exception: {} - {}", ex.getCode(), ex.getMessage());

                ErrorResponse response = new ErrorResponse(
                                ex.getHttpStatus().value(),
                                ex.getMessage(),
                                ex.getCode());

                return ResponseEntity.status(ex.getHttpStatus()).body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                String firstError = errors.values().stream().findFirst()
                                .orElse(SystemErrorCode.VALIDATION_ERROR.getMessage());

                ErrorResponse response = new ErrorResponse(
                                HttpStatus.BAD_REQUEST.value(),
                                firstError,
                                SystemErrorCode.VALIDATION_ERROR.getCode());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(DataAccessException.class)
        public ResponseEntity<ErrorResponse> handleDatabaseException(DataAccessException ex) {
                log.error("Database error: ", ex);

                ErrorResponse response = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                SystemErrorCode.DATABASE_ERROR.getMessage(),
                                SystemErrorCode.DATABASE_ERROR.getCode());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
                log.warn("Method not supported: {} for {}", ex.getMethod(), ex.getMessage());

                String message = String.format("HTTP method %s is not supported for this endpoint", ex.getMethod());

                ErrorResponse response = new ErrorResponse(
                                HttpStatus.METHOD_NOT_ALLOWED.value(),
                                message,
                                SystemErrorCode.METHOD_NOT_ALLOWED.getCode());

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
        }

        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
                log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

                String message = String.format("Endpoint not found: %s %s", ex.getHttpMethod(), ex.getRequestURL());

                ErrorResponse response = new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                message,
                                SystemErrorCode.RESOURCE_NOT_FOUND.getCode());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
                log.error("Unexpected error: ", ex);

                ErrorResponse response = new ErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                SystemErrorCode.INTERNAL_ERROR.getMessage(),
                                SystemErrorCode.INTERNAL_ERROR.getCode());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
}
