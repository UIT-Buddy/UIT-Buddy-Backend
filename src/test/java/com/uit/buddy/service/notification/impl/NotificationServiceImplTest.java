package com.uit.buddy.service.notification.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.uit.buddy.dto.response.notification.NotificationResponse;
import com.uit.buddy.entity.notification.Notification;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.entity.user.UserSetting;
import com.uit.buddy.enums.NotificationType;
import com.uit.buddy.event.social.CommentLikedEvent;
import com.uit.buddy.event.social.PostCommentedEvent;
import com.uit.buddy.event.social.PostLikedEvent;
import com.uit.buddy.event.social.PostSharedEvent;
import com.uit.buddy.exception.notification.NotificationException;
import com.uit.buddy.mapper.notification.NotificationMapper;
import com.uit.buddy.repository.notification.NotificationRepository;
import com.uit.buddy.repository.user.DeviceTokenRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.repository.user.UserSettingRepository;
import com.uit.buddy.service.fcm.FcmService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private DeviceTokenRepository deviceTokenRepository;
    @Mock
    private FcmService fcmService;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private UserSettingRepository userSettingRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private String actorMssv;
    private String receiverMssv;
    private UUID postId;
    private UUID commentId;
    private Student receiver;
    private UserSetting userSetting;

    @BeforeEach
    void setUp() {
        actorMssv = "22100001";
        receiverMssv = "22100002";
        postId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        receiver = new Student();
        receiver.setMssv(receiverMssv);
        receiver.setFullName("Receiver Name");

        userSetting = new UserSetting();
        userSetting.setMssv(receiverMssv);
        userSetting.setEnableNotification(true);

        // Mock save to return notification with ID set
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
            return n;
        });
    }

    @Test
    void shouldCreatePostLikeNotificationWhenEnabled() {
        PostLikedEvent event = new PostLikedEvent(actorMssv, "Actor Name", receiverMssv, postId, "Test content");

        when(notificationRepository.findByMssvAndTypeAndDataId(receiverMssv, "POST_LIKE", postId.toString()))
                .thenReturn(null);
        when(studentRepository.getReferenceById(receiverMssv)).thenReturn(receiver);
        when(userSettingRepository.findById(receiverMssv)).thenReturn(Optional.of(userSetting));
        when(deviceTokenRepository.findAllTokensByMssv(receiverMssv)).thenReturn(List.of("token1"));

        notificationService.createPostLikeNotification(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(fcmService).sendMulticastNotification(any(), any(), any(), any(), eq("POST_LIKE"),
                eq(postId.toString()));
    }

    @Test
    void shouldNotSendFcmWhenNotificationDisabled() {
        PostLikedEvent event = new PostLikedEvent(actorMssv, "Actor Name", receiverMssv, postId, "Test content");
        userSetting.setEnableNotification(false);

        when(notificationRepository.findByMssvAndTypeAndDataId(receiverMssv, "POST_LIKE", postId.toString()))
                .thenReturn(null);
        when(studentRepository.getReferenceById(receiverMssv)).thenReturn(receiver);
        when(userSettingRepository.findById(receiverMssv)).thenReturn(Optional.of(userSetting));

        notificationService.createPostLikeNotification(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(fcmService, never()).sendMulticastNotification(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldDefaultToEnabledWhenUserSettingNotFound() {
        PostLikedEvent event = new PostLikedEvent(actorMssv, "Actor Name", receiverMssv, postId, "Test content");

        when(notificationRepository.findByMssvAndTypeAndDataId(receiverMssv, "POST_LIKE", postId.toString()))
                .thenReturn(null);
        when(studentRepository.getReferenceById(receiverMssv)).thenReturn(receiver);
        when(userSettingRepository.findById(receiverMssv)).thenReturn(Optional.empty());
        when(deviceTokenRepository.findAllTokensByMssv(receiverMssv)).thenReturn(List.of("token1"));

        notificationService.createPostLikeNotification(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(fcmService).sendMulticastNotification(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldAggregatePostLikeNotifications() {
        PostLikedEvent event = new PostLikedEvent(actorMssv, "New Actor", receiverMssv, postId, "Test content");

        Notification existingNotification = createMockNotification();
        existingNotification.setContent("Old Actor đã thích bài viết của bạn");
        existingNotification.setDataId(postId.toString());

        when(notificationRepository.findByMssvAndTypeAndDataId(receiverMssv, "POST_LIKE", postId.toString()))
                .thenReturn(existingNotification);
        when(userSettingRepository.findById(receiverMssv)).thenReturn(Optional.of(userSetting));
        when(deviceTokenRepository.findAllTokensByMssv(receiverMssv)).thenReturn(List.of("token1"));

        notificationService.createPostLikeNotification(event);

        verify(notificationRepository).save(existingNotification);
        assertThat(existingNotification.getContent()).contains("New Actor");
        verify(fcmService).sendMulticastNotification(any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldCreatePostCommentNotification() {
        PostCommentedEvent event = new PostCommentedEvent(actorMssv, "Actor Name", receiverMssv, postId, commentId,
                "Nice post!");

        when(studentRepository.getReferenceById(receiverMssv)).thenReturn(receiver);
        when(userSettingRepository.findById(receiverMssv)).thenReturn(Optional.of(userSetting));
        when(deviceTokenRepository.findAllTokensByMssv(receiverMssv)).thenReturn(List.of("token1"));

        notificationService.createPostCommentNotification(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(fcmService).sendMulticastNotification(any(), any(), any(), any(), eq("POST_COMMENT"),
                eq(postId.toString()));
    }

    @Test
    void shouldCreatePostShareNotification() {
        PostSharedEvent event = new PostSharedEvent(actorMssv, "Actor Name", receiverMssv, postId, UUID.randomUUID());

        when(notificationRepository.findByMssvAndTypeAndDataId(receiverMssv, "POST_SHARE", postId.toString()))
                .thenReturn(null);
        when(studentRepository.getReferenceById(receiverMssv)).thenReturn(receiver);
        when(userSettingRepository.findById(receiverMssv)).thenReturn(Optional.of(userSetting));
        when(deviceTokenRepository.findAllTokensByMssv(receiverMssv)).thenReturn(List.of("token1"));

        notificationService.createPostShareNotification(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(fcmService).sendMulticastNotification(any(), any(), any(), any(), eq("POST_SHARE"),
                eq(postId.toString()));
    }

    @Test
    void shouldCreateCommentLikeNotification() {
        CommentLikedEvent event = new CommentLikedEvent(actorMssv, "Actor Name", receiverMssv, commentId, postId);

        when(notificationRepository.findByMssvAndTypeAndDataId(receiverMssv, "COMMENT_LIKE", commentId.toString()))
                .thenReturn(null);
        when(studentRepository.getReferenceById(receiverMssv)).thenReturn(receiver);
        when(userSettingRepository.findById(receiverMssv)).thenReturn(Optional.of(userSetting));
        when(deviceTokenRepository.findAllTokensByMssv(receiverMssv)).thenReturn(List.of("token1"));

        notificationService.createCommentLikeNotification(event);

        verify(notificationRepository).save(any(Notification.class));
        verify(fcmService).sendMulticastNotification(any(), any(), any(), any(), eq("COMMENT_LIKE"),
                eq(commentId.toString()));
    }

    @Test
    void shouldDeleteNotificationSuccessfully() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = createMockNotification();
        ReflectionTestUtils.setField(notification, "id", notificationId);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        notificationService.deleteNotification(notificationId, receiverMssv);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNotOwnedNotification() {
        UUID notificationId = UUID.randomUUID();
        Notification notification = createMockNotification();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.deleteNotification(notificationId, "22100999"))
                .isInstanceOf(NotificationException.class);

        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentNotification() {
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.deleteNotification(notificationId, receiverMssv))
                .isInstanceOf(NotificationException.class);

        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void shouldGetUnreadCount() {
        when(notificationRepository.countByStudentMssvAndIsReadFalse(receiverMssv)).thenReturn(5L);

        long count = notificationService.getUnreadCount(receiverMssv);

        assertThat(count).isEqualTo(5L);
        verify(notificationRepository).countByStudentMssvAndIsReadFalse(receiverMssv);
    }

    @Test
    void shouldMarkAsRead() {
        UUID notificationId = UUID.randomUUID();

        notificationService.markAsRead(notificationId, receiverMssv);

        verify(notificationRepository).markAsRead(notificationId, receiverMssv);
    }

    @Test
    void shouldMarkAllAsRead() {
        notificationService.markAllAsRead(receiverMssv);

        verify(notificationRepository).markAllAsRead(receiverMssv);
    }

    @Test
    void shouldGetNotifications() {
        Notification notification = createMockNotification();

        when(notificationRepository.findByMssvWithCursor(eq(receiverMssv), eq(null), eq(11)))
                .thenReturn(List.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(mock(NotificationResponse.class));

        List<NotificationResponse> result = notificationService.getNotifications(receiverMssv, null, 10);

        assertThat(result).hasSize(1);
        verify(notificationRepository).findByMssvWithCursor(eq(receiverMssv), eq(null), eq(11));
    }

    private Notification createMockNotification() {
        Notification notification = Notification.builder().student(receiver).title("Test").content("Test content")
                .type(NotificationType.SOCIAL).isRead(false).build();
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        return notification;
    }
}
