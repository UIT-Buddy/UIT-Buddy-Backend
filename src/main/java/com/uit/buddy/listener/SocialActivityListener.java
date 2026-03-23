package com.uit.buddy.listener;

import com.uit.buddy.event.social.CommentLikedEvent;
import com.uit.buddy.event.social.FriendRequestAcceptedEvent;
import com.uit.buddy.event.social.FriendRequestReceivedEvent;
import com.uit.buddy.event.social.PostCommentedEvent;
import com.uit.buddy.event.social.PostLikedEvent;
import com.uit.buddy.event.social.PostSharedEvent;
import com.uit.buddy.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialActivityListener {

    private final NotificationService notificationService;

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostLikedEvent(PostLikedEvent event) {
        log.info("[Event Listener] Processing post like notification for post: {}", event.postId());
        notificationService.createPostLikeNotification(event);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostCommentedEvent(PostCommentedEvent event) {
        log.info("[Event Listener] Processing post comment notification for post: {}", event.postId());
        notificationService.createPostCommentNotification(event);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePostSharedEvent(PostSharedEvent event) {
        log.info("[Event Listener] Processing post share notification for post: {}", event.originalPostId());
        notificationService.createPostShareNotification(event);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequestReceivedEvent(FriendRequestReceivedEvent event) {
        log.info("[Event Listener] Processing friend request notification for receiver: {}", event.receiverMssv());
        notificationService.createFriendRequestNotification(event);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequestAcceptedEvent(FriendRequestAcceptedEvent event) {
        log.info("[Event Listener] Processing friend request accepted notification for sender: {}", event.senderMssv());
        notificationService.createFriendRequestAcceptedNotification(event);
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentLikedEvent(CommentLikedEvent event) {
        log.info("[Event Listener] Processing comment like notification for comment: {}", event.commentId());
        notificationService.createCommentLikeNotification(event);
    }
}
