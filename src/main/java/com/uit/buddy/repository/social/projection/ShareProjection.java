package com.uit.buddy.repository.social.projection;

import java.time.LocalDateTime;

public interface ShareProjection {
    String getMssv();

    String getFullName();

    String getAvatarUrl();

    LocalDateTime getSharedAt();
}
