package com.uit.buddy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.dto.base.ErrorResponse;
import com.uit.buddy.exception.auth.AuthErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    String message = (String) request.getAttribute("auth_error");
    if (message == null) message = AuthErrorCode.UNAUTHORIZED.getMessage();

    ErrorResponse errorResponse =
        new ErrorResponse(
            HttpServletResponse.SC_UNAUTHORIZED, message, AuthErrorCode.UNAUTHORIZED.getCode());

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
