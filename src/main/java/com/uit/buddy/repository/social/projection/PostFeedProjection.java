package com.uit.buddy.repository.social.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PostFeedProjection {
    UUID getId();

    String getTitle();

    String getContent();

    String getMedias();

    Long getLikeCount();

    Long getCommentCount();

    Long getShareCount();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getAuthorMssv();

    String getAuthorFullName();

    String getAuthorAvatarUrl();

    String getAuthorHomeClassCode();

    boolean isLiked();
}