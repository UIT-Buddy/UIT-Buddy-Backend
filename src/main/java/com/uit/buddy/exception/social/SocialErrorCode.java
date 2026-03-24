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
    UNAUTHORIZED("SOCIAL_004", "Unauthorized", HttpStatus.UNAUTHORIZED),
    CANNOT_FRIEND_YOURSELF("SOCIAL_005", "Cannot send friend request to yourself", HttpStatus.BAD_REQUEST),
    ALREADY_FRIENDS("SOCIAl_006", "You have already friended this person", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_ALREADY_EXISTS("SOCIAL_007", "You have sent friend request to this person already",
            HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_NOT_FOUND("SOCIAL_008", "Friend request not found", HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_ALREADY_RESPONDED("SOCIAL_009", "You have responded this request already", HttpStatus.BAD_REQUEST),
    NOT_FRIENDS("SOCIAL_010", "Not be friends to unfriend", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
