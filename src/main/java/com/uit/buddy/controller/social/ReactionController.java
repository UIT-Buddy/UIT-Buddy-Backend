package com.uit.buddy.controller.social;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CursorPageResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.response.social.UserReactionResponse;
import com.uit.buddy.service.social.ReactionService;
import com.uit.buddy.util.CursorUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reaction", description = "Post reaction management APIs")
public class ReactionController extends AbstractBaseController {

    private final ReactionService reactionService;

    @PostMapping("/posts/{postId}/like")
    @Operation(summary = "Like/Unlike post", description = "Toggle like on a post")
    public ResponseEntity<SuccessResponse> togglePostLike(
            @PathVariable UUID postId,
            @AuthenticationPrincipal String mssv) {

        log.info("[Reaction Controller] Toggling like on post: {} by mssv: {}", postId, mssv);

        boolean isLiked = reactionService.togglePostLike(postId, mssv);
        String message = isLiked ? "Post liked successfully" : "Post unliked successfully";

        return success(message);
    }

    @GetMapping("/{postId}/reactions")
    @Operation(summary = "Get post reactions", description = "Get cursor-paginated list of users who liked the post")
    public ResponseEntity<CursorPageResponse<UserReactionResponse>> getPostReactions(@PathVariable UUID postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal String mssv) {
        log.info("[Post Controller] Getting reactions for post: {} with cursor: {}, limit: {}", postId, cursor, limit);

        List<UserReactionResponse> reactions = reactionService.getPostReactions(postId, mssv, cursor, limit);

        return cursorPaging(
                "Post reactions retrieved successfully",
                reactions,
                limit,
                reaction -> CursorUtils.encode(reaction.reactedAt(), UUID.fromString(reaction.user().mssv())));
    }
}