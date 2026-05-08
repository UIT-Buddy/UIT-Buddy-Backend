package com.uit.buddy.controller.chat;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.chat.ChatRequest;
import com.uit.buddy.dto.response.chat.ChatResponse;
import com.uit.buddy.service.chat.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI Chat API")
public class ChatController extends AbstractBaseController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Send a message to the AI")
    public ResponseEntity<SingleResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.processChat(request);
        return successSingle(response, "Success");
    }
}
