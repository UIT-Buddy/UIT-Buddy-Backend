package com.uit.buddy.service.fcm;

import com.uit.buddy.dto.request.fcm.FcmNotificationRequest;

public interface FcmService {
    void syncDeviceToken(String mssv, String fcmToken);

    void sendPushNotification(FcmNotificationRequest request);
}