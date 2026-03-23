package com.uit.buddy.service.notification;

import com.uit.buddy.dto.response.notification.NotificationResponse;
import com.uit.buddy.event.social.FriendRequestAcceptedEvent;
import com.uit.buddy.event.social.FriendRequestReceivedEvent;
import com.uit.buddy.event.social.PostCommentedEvent;
import com.uit.buddy.event.social.PostLikedEvent;
import com.uit.buddy.event.social.PostSharedEvent;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void createPostLikeNotification(PostLikedEvent event);

    void createPostCommentNotification(PostCommentedEvent event);

    void createPostShareNotification(PostSharedEvent event);

    void createFriendRequestNotification(FriendRequestReceivedEvent event);

    void createFriendRequestAcceptedNotification(FriendRequestAcceptedEvent event);

    List<NotificationResponse> getNotifications(String mssv, String cursor, int limit);

    void markAsRead(UUID notificationId, String mssv);

    void markAllAsRead(String mssv);

    void deleteNotification(UUID notificationId, String mssv);

    long getUnreadCount(String mssv);
}
