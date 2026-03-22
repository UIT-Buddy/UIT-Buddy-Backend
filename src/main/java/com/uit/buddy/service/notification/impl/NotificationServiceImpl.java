package com.uit.buddy.service.notification.impl;

import com.uit.buddy.entity.notification.Notification;
import com.uit.buddy.enums.NotificationTemplate;
import com.uit.buddy.enums.NotificationType;
import com.uit.buddy.event.social.FriendRequestAcceptedEvent;
import com.uit.buddy.event.social.FriendRequestReceivedEvent;
import com.uit.buddy.event.social.PostCommentedEvent;
import com.uit.buddy.event.social.PostLikedEvent;
import com.uit.buddy.event.social.PostSharedEvent;
import com.uit.buddy.repository.notification.NotificationRepository;
import com.uit.buddy.repository.user.DeviceTokenRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.fcm.FcmService;
import com.uit.buddy.service.notification.NotificationService;
import java.util.List;
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

    @Override
    @Transactional
    public void createPostLikeNotification(PostLikedEvent event) {
        NotificationTemplate template = NotificationTemplate.POST_LIKE;
        processNotification(event.receiverMssv(), template.getTitle(), template.formatContent(event.actorName()),
                template.getType(), event.postId().toString());
    }

    @Override
    @Transactional
    public void createPostCommentNotification(PostCommentedEvent event) {
        NotificationTemplate template = NotificationTemplate.POST_COMMENT;
        processNotification(event.receiverMssv(), template.getTitle(),
                template.formatContent(event.actorName(), event.commentContent()), template.getType(),
                event.postId().toString());
    }

    @Override
    @Transactional
    public void createPostShareNotification(PostSharedEvent event) {
        NotificationTemplate template = NotificationTemplate.POST_SHARE;
        processNotification(event.receiverMssv(), template.getTitle(), template.formatContent(event.actorName()),
                template.getType(), event.originalPostId().toString());
    }

    @Override
    @Transactional
    public void createFriendRequestNotification(FriendRequestReceivedEvent event) {
        NotificationTemplate template = NotificationTemplate.FRIEND_REQUEST_RECEIVED;
        processNotification(
                event.receiverMssv(),
                template.getTitle(),
                template.formatContent(event.senderName()),
                template.getType(),
                event.requestId().toString());
    }

    @Override
    @Transactional
    public void createFriendRequestAcceptedNotification(FriendRequestAcceptedEvent event) {
        NotificationTemplate template = NotificationTemplate.FRIEND_REQUEST_ACCEPTED;
        processNotification(
                event.senderMssv(),
                template.getTitle(),
                template.formatContent(event.accepterName()),
                template.getType(),
                event.requestId().toString());
    }

    private void processNotification(String receiverMssv, String title, String content, String type, String dataId) {

        Notification notification = Notification.builder().student(studentRepository.getReferenceById(receiverMssv))
                .title(title).content(content).type(NotificationType.SOCIAL).isRead(false)
                .build();
        notificationRepository.save(notification);

        List<String> tokens = deviceTokenRepository.findAllTokensByMssv(receiverMssv);

        if (!tokens.isEmpty()) {
            fcmService.sendMulticastNotification(tokens, notification.getId().toString(), title, content, type, dataId);
        }
    }
}
