package com.uit.buddy.service.fcm;

import java.util.List;

import com.uit.buddy.dto.request.fcm.FcmNotificationRequest;

public interface FcmService {
    void syncDeviceToken(String mssv, String fcmToken);

    void sendPushNotification(FcmNotificationRequest request);

    void sendMulticastNotification(List<String> tokens, String notificationId,
            String title, String body, String type, String dataId);
}