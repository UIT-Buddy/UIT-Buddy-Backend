package com.uit.buddy.exception.social;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SocialErrorCode {
    POST_NOT_FOUND("SOCIAL_001", "Post not found", HttpStatus.NOT_FOUND),
    NOT_INCLUDE_BOTH_TYPES("SOCIAL_002", "Just 1 video or 1 image, not both", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_FOUND("SOCIAL_003", "Comment not found", HttpStatus.NOT_FOUND),
    UNAUTHORIZED("SOCIAL_004", "Unauthorized", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
