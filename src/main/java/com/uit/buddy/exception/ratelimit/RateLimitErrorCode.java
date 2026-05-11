package com.uit.buddy.exception.ratelimit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RateLimitErrorCode {
    RATE_LIMIT_EXCEEDED("RL_001", "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),
    PERMIT_TIMEOUT("RL_002", "Timeout waiting for rate limiter permit", HttpStatus.REQUEST_TIMEOUT),
    PERMIT_INTERRUPTED("RL_003", "Interrupted while waiting for rate limiter permit", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
