package com.uit.buddy.service.fcm.impl;

import com.google.firebase.messaging.*;
import com.uit.buddy.dto.request.fcm.FcmNotificationRequest;
import com.uit.buddy.exception.fcm.FcmErrorCode;
import com.uit.buddy.exception.fcm.FcmException;
import com.uit.buddy.repository.user.DeviceTokenRepository;
import com.uit.buddy.service.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmServiceImpl implements FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final DeviceTokenRepository deviceTokenRepository;

    @Override
    public void syncDeviceToken(String mssv, String fcmToken) {
        if (fcmToken == null || fcmToken.isBlank()) {
            throw new FcmException(FcmErrorCode.INVALID_FCM_TOKEN);
        }
        log.info("[FCM Service] Syncing device token for student: {}", mssv);
        deviceTokenRepository.upsertToken(mssv, fcmToken);
    }

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
            handleFirebaseError(e, request.targetToken());
        }
    }

    private void handleFirebaseError(FirebaseMessagingException e, String token) {
        MessagingErrorCode code = e.getMessagingErrorCode();
        log.error("[FCM Service] Firebase error [{}]: {}", code, e.getMessage());

        if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
            log.info("[FCM Service] Cleaning up invalid token from DB: {}", token);
            deviceTokenRepository.deleteByFcmToken(token);
        }
    }
}