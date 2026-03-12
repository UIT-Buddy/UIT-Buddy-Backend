package com.uit.buddy.repository.social.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CommentProjection {
    UUID getId();

    String getContent();

    Long getLikeCount();

    Long getReplyCount();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    UUID getParentId();

    String getMssv();

    String getFullName();

    String getAvatarUrl();

    boolean isLiked();
}