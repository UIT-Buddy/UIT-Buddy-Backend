package com.uit.buddy.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.uit.buddy.dto.base.ErrorResponse;
import com.uit.buddy.exception.system.SystemErrorCode;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        log.warn("Base exception: {} - {}", ex.getCode(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(ex.getHttpStatus().value(), ex.getMessage(), ex.getCode());

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

        String firstError = errors.values().stream().findFirst().orElse(SystemErrorCode.VALIDATION_ERROR.getMessage());

        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), firstError,
                SystemErrorCode.VALIDATION_ERROR.getCode());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DataAccessException ex) {
        log.error("Database error: ", ex);

        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                SystemErrorCode.DATABASE_ERROR.getMessage(), SystemErrorCode.DATABASE_ERROR.getCode());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {} for {}", ex.getMethod(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(SystemErrorCode.METHOD_NOT_ALLOWED.getHttpStatus().value(),
                SystemErrorCode.METHOD_NOT_ALLOWED.getMessage(), SystemErrorCode.METHOD_NOT_ALLOWED.getCode());

        return ResponseEntity.status(SystemErrorCode.METHOD_NOT_ALLOWED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException ex) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponse response = new ErrorResponse(SystemErrorCode.RESOURCE_NOT_FOUND.getHttpStatus().value(),
                SystemErrorCode.RESOURCE_NOT_FOUND.getMessage(), SystemErrorCode.RESOURCE_NOT_FOUND.getCode());

        return ResponseEntity.status(SystemErrorCode.RESOURCE_NOT_FOUND.getHttpStatus()).body(response);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(TransactionSystemException ex) {
        Throwable cause = ex.getRootCause();

        if (cause instanceof BaseException) {
            BaseException baseEx = (BaseException) cause;
            log.warn("Transaction wrapped exception: {} - {}", baseEx.getCode(), baseEx.getMessage());

            ErrorResponse response = new ErrorResponse(baseEx.getHttpStatus().value(), baseEx.getMessage(),
                    baseEx.getCode());

            return ResponseEntity.status(baseEx.getHttpStatus()).body(response);
        }

        log.error("Transaction error: ", ex);
        ErrorResponse response = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                SystemErrorCode.INTERNAL_ERROR.getMessage(), SystemErrorCode.INTERNAL_ERROR.getCode());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        log.warn("Binding validation failed: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(SystemErrorCode.INVALID_PARAMETER.getHttpStatus().value(),
                SystemErrorCode.INVALID_PARAMETER.getMessage(), SystemErrorCode.INVALID_PARAMETER.getCode());

        return ResponseEntity.status(SystemErrorCode.INVALID_PARAMETER.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {
        log.warn("Multipart parsing error: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(SystemErrorCode.MULTIPART_ERROR.getHttpStatus().value(),
                SystemErrorCode.MULTIPART_ERROR.getMessage(), SystemErrorCode.MULTIPART_ERROR.getCode());

        return ResponseEntity.status(SystemErrorCode.MULTIPART_ERROR.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingPart(MissingServletRequestPartException ex) {
        log.warn("Missing request part: {}", ex.getRequestPartName());

        ErrorResponse response = new ErrorResponse(SystemErrorCode.INVALID_PARAMETER.getHttpStatus().value(),
                SystemErrorCode.INVALID_PARAMETER.getMessage(), SystemErrorCode.INVALID_PARAMETER.getCode());

        return ResponseEntity.status(SystemErrorCode.INVALID_PARAMETER.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        ErrorResponse response = new ErrorResponse(SystemErrorCode.INTERNAL_ERROR.getHttpStatus().value(),
                SystemErrorCode.INTERNAL_ERROR.getMessage(), SystemErrorCode.INTERNAL_ERROR.getCode());

        return ResponseEntity.status(SystemErrorCode.INTERNAL_ERROR.getHttpStatus()).body(response);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<ErrorResponse> handleThreadException(InterruptedException ex) {
        log.error("Unexpected error: ", ex);

        ErrorResponse response = new ErrorResponse(SystemErrorCode.INTERNAL_ERROR.getHttpStatus().value(),
                SystemErrorCode.INTERNAL_ERROR.getMessage(), SystemErrorCode.INTERNAL_ERROR.getCode());

        return ResponseEntity.status(SystemErrorCode.INTERNAL_ERROR.getHttpStatus()).body(response);
    }
}
