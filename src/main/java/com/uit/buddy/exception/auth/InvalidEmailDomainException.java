package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class InvalidEmailDomainException extends AuthException {
    public InvalidEmailDomainException() {
        super(
                AuthErrorCode.INVALID_EMAIL_DOMAIN,
                "Email phải thuộc domain @gm.uit.edu.vn",
                HttpStatus.BAD_REQUEST.value());
    }

    public InvalidEmailDomainException(String allowedDomain) {
        super(
                AuthErrorCode.INVALID_EMAIL_DOMAIN,
                String.format("Email phải thuộc domain %s", allowedDomain),
                HttpStatus.BAD_REQUEST.value());
    }
}
