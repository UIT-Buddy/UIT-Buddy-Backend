package com.uit.buddy.service.social;

import java.util.List;
import java.util.UUID;

import com.uit.buddy.dto.response.social.UserReactionResponse;

public interface ReactionService {
    boolean togglePostLike(UUID postId, String mssv);

    List<UserReactionResponse> getPostReactions(UUID postId, String mssv, String cursor, int limit);
}
