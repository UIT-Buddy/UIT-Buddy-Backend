package com.uit.buddy.controller.notification;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.fcm.FcmNotificationRequest;
import com.uit.buddy.service.fcm.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "Endpoints for testing fcm")
public class NotificationController extends AbstractBaseController {

    private final FcmService fcmService;

    @PostMapping("/send")
    @Operation(summary = "Send Push Notification", description = "Send a push notification to a specific device token")
    public ResponseEntity<SingleResponse<String>> sendNotification(@Valid @RequestBody FcmNotificationRequest request) {

        log.info("[Notification Controller] Received request to send push notification");
        fcmService.sendPushNotification(request);

        return successSingle("Notification request submitted", "Push notification sent successfully!");
    }
}
