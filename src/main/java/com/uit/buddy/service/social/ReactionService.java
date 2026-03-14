package com.uit.buddy.service.social;

import com.uit.buddy.dto.response.social.UserReactionResponse;
import java.util.List;
import java.util.UUID;

public interface ReactionService {
  boolean togglePostLike(UUID postId, String mssv);

  List<UserReactionResponse> getPostReactions(UUID postId, String mssv, String cursor, int limit);
}
