package com.uit.buddy.repository.social.projection;

import java.time.LocalDateTime;

public interface ReactionProjection {
  String getMssv();

  String getFullName();

  String getAvatarUrl();

  LocalDateTime getReactedAt();
}
