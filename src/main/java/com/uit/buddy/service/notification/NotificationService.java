package com.uit.buddy.service.notification;

import com.uit.buddy.event.social.PostCommentedEvent;
import com.uit.buddy.event.social.PostLikedEvent;
import com.uit.buddy.event.social.PostSharedEvent;

public interface NotificationService {
    void createPostLikeNotification(PostLikedEvent event);

    void createPostCommentNotification(PostCommentedEvent event);

    void createPostShareNotification(PostSharedEvent event);
}
