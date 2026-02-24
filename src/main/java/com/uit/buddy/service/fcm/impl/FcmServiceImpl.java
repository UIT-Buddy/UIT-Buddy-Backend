package com.uit.buddy.service.fcm.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.uit.buddy.dto.request.fcm.FcmNotificationRequest;
import com.uit.buddy.service.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class FcmServiceImpl implements FcmService {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public void sendPushNotification(FcmNotificationRequest request) {
        Notification notification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.message())
                .setImage(request.image())
                .build();

        Message message = Message.builder()
                .setToken(request.targetToken())
                .setNotification(notification)
                .putAllData(request.toDataMap())
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("[FCM Service] Successfully sent message. ID: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("[FCM Service] Failed to send push notification: {}", e.getMessagingErrorCode(), e);
        }
    }
}