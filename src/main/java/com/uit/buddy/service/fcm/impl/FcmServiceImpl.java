package com.uit.buddy.service.fcm.impl;

import com.google.firebase.messaging.*;
import com.uit.buddy.dto.request.fcm.FcmNotificationRequest;
import com.uit.buddy.exception.fcm.FcmErrorCode;
import com.uit.buddy.exception.fcm.FcmException;
import com.uit.buddy.repository.user.DeviceTokenRepository;
import com.uit.buddy.service.fcm.FcmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    Notification notification =
        Notification.builder()
            .setTitle(request.title())
            .setBody(request.message())
            .setImage(request.image())
            .build();

    Message message =
        Message.builder()
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

  @Override
  public void sendMulticastNotification(
      List<String> tokens,
      String notificationId,
      String title,
      String body,
      String type,
      String dataId) {
    if (tokens == null || tokens.isEmpty()) return;

    // MulticastMessage vẫn dùng được để build data chung cho tất cả tokens
    MulticastMessage message =
        MulticastMessage.builder()
            .addAllTokens(tokens)
            .setNotification(Notification.builder().setTitle(title).setBody(body).build())
            .putData("id", notificationId)
            .putData("type", type)
            .putData("dataId", dataId)
            .build();

    try {
      // SỬ DỤNG sendEachForMulticast thay cho sendMulticast bị deprecated
      BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

      log.info(
          "[FCM Service] Sent notification. Success: {}, Failure: {}",
          response.getSuccessCount(),
          response.getFailureCount());

      // BEST PRACTICE: Dọn dẹp token rác ngay lập tức nếu gửi thất bại
      if (response.getFailureCount() > 0) {
        handleMulticastFailures(response, tokens);
      }

    } catch (FirebaseMessagingException e) {
      log.error("[FCM Service] Fatal error sending multicast notification", e);
    }
  }

  /** Logic xử lý các token bị lỗi (không tồn tại, hết hạn) sau khi gửi multicast */
  private void handleMulticastFailures(BatchResponse response, List<String> tokens) {
    List<SendResponse> responses = response.getResponses();
    List<String> invalidTokens = new java.util.ArrayList<>();

    for (int i = 0; i < responses.size(); i++) {
      if (!responses.get(i).isSuccessful()) {
        MessagingErrorCode code = responses.get(i).getException().getMessagingErrorCode();
        // Nếu token không còn hiệu lực (UNREGISTERED) hoặc sai định dạng
        if (code == MessagingErrorCode.UNREGISTERED
            || code == MessagingErrorCode.INVALID_ARGUMENT) {
          invalidTokens.add(tokens.get(i));
        }
      }
    }

    if (!invalidTokens.isEmpty()) {
      log.info("[FCM Service] Removing {} invalid tokens from DB", invalidTokens.size());
      deviceTokenRepository.deleteAllByFcmTokenIn(
          invalidTokens); // Cần thêm method này vào Repository
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
