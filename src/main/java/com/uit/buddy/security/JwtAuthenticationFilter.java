package com.uit.buddy.security;

import com.uit.buddy.entity.user.User;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.repository.user.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip if no auth header or not Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String mssv = jwtUtils.extractMssv(jwt);

            if (mssv != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByMssv(mssv).orElse(null);

                if (user != null) {
                    JwtUserDetails userDetails = new JwtUserDetails(user);

                    if (jwtUtils.validateAccessToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        // Token invalid
                        request.setAttribute("errorCode", AuthErrorCode.TOKEN_INVALID.getCode());
                        request.setAttribute("errorMessage", AuthErrorCode.TOKEN_INVALID.getMessage());
                    }
                } else {
                    // User not found
                    request.setAttribute("errorCode", AuthErrorCode.TOKEN_INVALID.getCode());
                    request.setAttribute("errorMessage", AuthErrorCode.TOKEN_INVALID.getMessage());
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token expired
            request.setAttribute("errorCode", AuthErrorCode.TOKEN_EXPIRED.getCode());
            request.setAttribute("errorMessage", AuthErrorCode.TOKEN_EXPIRED.getMessage());
            logger.error("JWT token expired: " + e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            // Malformed token
            request.setAttribute("errorCode", AuthErrorCode.TOKEN_INVALID.getCode());
            request.setAttribute("errorMessage", AuthErrorCode.TOKEN_INVALID.getMessage());
            logger.error("Invalid JWT token: " + e.getMessage());
        } catch (Exception e) {
            // Other errors
            request.setAttribute("errorCode", AuthErrorCode.TOKEN_INVALID.getCode());
            request.setAttribute("errorMessage", AuthErrorCode.TOKEN_INVALID.getMessage());
            logger.error("Cannot set user authentication: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
