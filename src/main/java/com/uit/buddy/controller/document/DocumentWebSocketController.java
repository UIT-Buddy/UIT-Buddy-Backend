package com.uit.buddy.controller.document;

import com.uit.buddy.service.document.DocumentCollaborationService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DocumentWebSocketController {

    private final DocumentCollaborationService documentCollaborationService;

    @MessageMapping("/document/{documentId}/join")
    public void joinDocument(@DestinationVariable UUID documentId, StompHeaderAccessor headerAccessor) {

        Principal user = headerAccessor.getUser();
        String mssv = user != null ? user.getName() : null;

        log.info("[WS] User {} joining document {}", mssv, documentId);

        if (mssv == null) {
            log.warn("[WS] Unauthenticated user attempted to join document {}", documentId);
            return;
        }

        documentCollaborationService.joinDocument(documentId, mssv);
    }

    @MessageMapping("/document/{documentId}/leave")
    public void leaveDocument(@DestinationVariable UUID documentId, StompHeaderAccessor headerAccessor) {

        Principal user = headerAccessor.getUser();
        String mssv = user != null ? user.getName() : null;

        log.info("[WS] User {} leaving document {}", mssv, documentId);

        if (mssv == null) {
            log.warn("[WS] Unauthenticated user attempted to leave document {}", documentId);
            return;
        }

        documentCollaborationService.leaveDocument(documentId, mssv);
    }
}
