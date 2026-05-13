package com.uit.buddy.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            log.info("[WebSocket] Received STOMP command: {}", accessor.getCommand());

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                log.info("[WebSocket] Processing CONNECT frame");
                String authHeader = accessor.getFirstNativeHeader("Authorization");
                log.info("[WebSocket] Authorization header present: {}", authHeader != null);

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);

                    try {
                        if (jwtUtils.validateToken(token)) {
                            String mssv = jwtUtils.getMssvFromToken(token);

                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    mssv, null, null);

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            accessor.setUser(authentication);

                            log.info("[WebSocket] User {} authenticated successfully", mssv);
                        } else {
                            log.warn("[WebSocket] Invalid JWT token");
                        }
                    } catch (Exception e) {
                        log.error("[WebSocket] Error validating token: {}", e.getMessage());
                    }
                } else {
                    log.warn("[WebSocket] No Authorization header found in CONNECT frame");
                }
            }
        }

        return message;
    }
}
