package com.uit.buddy.service.fcm;

import com.uit.buddy.dto.request.fcm.FcmNotificationRequest;

public interface FcmService {
    void sendPushNotification(FcmNotificationRequest request);
}