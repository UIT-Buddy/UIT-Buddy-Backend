package com.uit.buddy.service.notification.impl;

import com.uit.buddy.dto.response.notification.NotificationResponse;
import com.uit.buddy.entity.notification.Notification;
import com.uit.buddy.enums.NotificationTemplate;
import com.uit.buddy.event.social.CommentLikedEvent;
import com.uit.buddy.event.social.FriendRequestAcceptedEvent;
import com.uit.buddy.event.social.FriendRequestReceivedEvent;
import com.uit.buddy.event.social.PostCommentedEvent;
import com.uit.buddy.event.social.PostLikedEvent;
import com.uit.buddy.event.social.PostSharedEvent;
import com.uit.buddy.exception.notification.NotificationErrorCode;
import com.uit.buddy.exception.notification.NotificationException;
import com.uit.buddy.mapper.notification.NotificationMapper;
import com.uit.buddy.repository.notification.NotificationRepository;
import com.uit.buddy.repository.user.DeviceTokenRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.repository.user.UserSettingRepository;
import com.uit.buddy.service.fcm.FcmService;
import com.uit.buddy.service.notification.NotificationService;
import com.uit.buddy.util.CursorUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;
    private final StudentRepository studentRepository;
    private final NotificationMapper notificationMapper;
    private final UserSettingRepository userSettingRepository;

    @Override
    @Transactional
    public void createPostLikeNotification(PostLikedEvent event) {
        NotificationTemplate template = NotificationTemplate.POST_LIKE;
        processAggregatedNotification(event.receiverMssv(), template.getTitle(), event.actorName(), template,
                event.postId().toString(), "thích");
    }

    @Override
    @Transactional
    public void createPostCommentNotification(PostCommentedEvent event) {
        NotificationTemplate template = NotificationTemplate.POST_COMMENT;
        processNotification(event.receiverMssv(), template.getTitle(),
                template.formatContent(event.actorName(), event.commentContent()), template, event.postId().toString());
    }

    @Override
    @Transactional
    public void createPostShareNotification(PostSharedEvent event) {
        NotificationTemplate template = NotificationTemplate.POST_SHARE;
        processAggregatedNotification(event.receiverMssv(), template.getTitle(), event.actorName(), template,
                event.originalPostId().toString(), "chia sẻ");
    }

    @Override
    @Transactional
    public void createFriendRequestNotification(FriendRequestReceivedEvent event) {
        NotificationTemplate template = NotificationTemplate.FRIEND_REQUEST_RECEIVED;
        processNotification(event.receiverMssv(), template.getTitle(), template.formatContent(event.senderName()),
                template, event.requestId().toString());
    }

    @Override
    @Transactional
    public void createFriendRequestAcceptedNotification(FriendRequestAcceptedEvent event) {
        NotificationTemplate template = NotificationTemplate.FRIEND_REQUEST_ACCEPTED;
        processNotification(event.senderMssv(), template.getTitle(), template.formatContent(event.accepterName()),
                template, event.requestId().toString());
    }

    @Override
    @Transactional
    public void createCommentLikeNotification(CommentLikedEvent event) {
        NotificationTemplate template = NotificationTemplate.COMMENT_LIKE;
        processAggregatedNotification(event.receiverMssv(), template.getTitle(), event.actorName(), template,
                event.commentId().toString(), "thích");
    }

    @Override
    @Transactional
    public void createNearDeadlineNotification(String mssv, String deadlineName, String dataId) {
        NotificationTemplate template = NotificationTemplate.REMINDER;
        processNotification(mssv, template.getTitle(), "Deadline '" + deadlineName + "' sẽ đến hạn trong vòng 24 giờ.",
                template, dataId);
    }

    @Override
    @Transactional
    public void createOverdueDeadlineNotification(String mssv, String deadlineName, String dataId) {
        NotificationTemplate template = NotificationTemplate.ACADEMIC;
        processNotification(mssv, template.getTitle(), "Deadline '" + deadlineName + "' đã quá hạn.", template, dataId);
    }

    @Override
    @Transactional
    public void createNewDeadlineNotification(String mssv, String deadlineName, String dataId) {
        NotificationTemplate template = NotificationTemplate.ACADEMIC;
        processNotification(mssv, template.getTitle(), "Bạn có deadline mới: '" + deadlineName + "'.", template,
                dataId);
    }

    private void processAggregatedNotification(String receiverMssv, String title, String actorName,
            NotificationTemplate type, String dataId, String action) {

        Notification existingNotification = notificationRepository.findByMssvAndTypeAndDataId(receiverMssv, type,
                dataId);

        String content;
        if (existingNotification != null) {
            int currentCount = extractCountFromContent(existingNotification.getContent());
            int newCount = currentCount + 1;

            // Update content with new count
            content = formatAggregatedContent(actorName, newCount, action);
            existingNotification.setContent(content);
            existingNotification.setIsRead(false);
            existingNotification.setUpdatedAt(LocalDateTime.now());

            notificationRepository.save(existingNotification);
            log.info("[Notification Service] Updated aggregated notification for receiver: {}, type: {}, count: {}",
                    receiverMssv, type.name(), newCount);

            sendFcmIfEnabled(receiverMssv, existingNotification.getId().toString(), title, content, type.name(),
                    dataId);
        } else {
            content = String.format(NotificationTemplate.MSG_SINGLE, actorName, action);
            processNotification(receiverMssv, title, content, type, dataId);
        }
    }

    private void processNotification(String receiverMssv, String title, String content, NotificationTemplate type,
            String dataId) {

        Notification notification = Notification.builder().student(studentRepository.getReferenceById(receiverMssv))
                .title(title).content(content).type(type).dataId(dataId).isRead(false).build();
        notificationRepository.save(notification);

        sendFcmIfEnabled(receiverMssv, notification.getId().toString(), title, content, type.name(), dataId);
    }

    private void sendFcmIfEnabled(String mssv, String notificationId, String title, String content, String type,
            String dataId) {
        boolean isEnabled = userSettingRepository.findById(mssv).map(setting -> setting.isEnableNotification())
                .orElse(true);

        if (!isEnabled) {
            log.info("[Notification Service] FCM disabled for user: {}", mssv);
            return;
        }

        List<String> tokens = deviceTokenRepository.findAllTokensByMssv(mssv);
        if (!tokens.isEmpty()) {
            fcmService.sendMulticastNotification(tokens, notificationId, title, content, type, dataId);
        }
    }

    private int extractCountFromContent(String content) {
        Matcher matcher = Pattern.compile(NotificationTemplate.COUNT_EXTRACT_REGEX).matcher(content);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1)) + 1;
            } catch (NumberFormatException e) {
                log.warn("[Notification Service] Parse error: {}", content);
            }
        }
        return 1;
    }

    private String formatAggregatedContent(String latestActor, int totalCount, String action) {
        if (totalCount <= 1) {
            return String.format(NotificationTemplate.MSG_SINGLE, latestActor, action);
        }
        return String.format(NotificationTemplate.MSG_MULTIPLE, latestActor, totalCount - 1, action);
    }

    @Override
    public List<NotificationResponse> getNotifications(String mssv, String cursor, int limit) {
        LocalDateTime cursorTime = null;
        if (cursor != null && !cursor.isBlank()) {
            var cursorContents = CursorUtils.decode(cursor);
            cursorTime = cursorContents.timestamp();
        }

        List<Notification> notifications = notificationRepository.findByMssvWithCursor(mssv, cursorTime, limit + 1);
        return notifications.stream().limit(limit).map(notificationMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, String mssv) {
        notificationRepository.markAsRead(notificationId, mssv);
    }

    @Override
    @Transactional
    public void markAllAsRead(String mssv) {
        notificationRepository.markAllAsRead(mssv);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, String mssv) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getStudent().getMssv().equals(mssv)) {
            throw new NotificationException(NotificationErrorCode.FORBIDDEN);
        }

        notificationRepository.delete(notification);
    }

    @Override
    public long getUnreadCount(String mssv) {
        return notificationRepository.countByStudentMssvAndIsReadFalse(mssv);
    }
}
