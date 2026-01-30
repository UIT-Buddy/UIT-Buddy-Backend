package com.uit.buddy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.dto.base.ErrorResponse;
import com.uit.buddy.exception.auth.AuthErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Check if there's a custom error code in request attributes (set by filter)
        String errorCode = (String) request.getAttribute("errorCode");
        String errorMessage = (String) request.getAttribute("errorMessage");

        // If no error code from filter, determine based on auth header presence
        if (errorCode == null) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || authHeader.isBlank()) {
                // No token provided
                errorCode = AuthErrorCode.TOKEN_MISSING.getCode();
                errorMessage = AuthErrorCode.TOKEN_MISSING.getMessage();
            } else {
                // Token exists but invalid (filter didn't catch it)
                errorCode = AuthErrorCode.TOKEN_INVALID.getCode();
                errorMessage = AuthErrorCode.TOKEN_INVALID.getMessage();
            }
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .statusCode(HttpServletResponse.SC_UNAUTHORIZED)
                .message(errorMessage)
                .errorCode(errorCode)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
