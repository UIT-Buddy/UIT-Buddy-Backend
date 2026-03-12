package com.uit.buddy.controller.social;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CreatedResponse;
import com.uit.buddy.dto.base.CursorPageResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.social.CreateCommentRequest;
import com.uit.buddy.dto.request.social.UpdateCommentRequest;
import com.uit.buddy.dto.response.social.CommentResponse;
import com.uit.buddy.service.social.CommentService;
import com.uit.buddy.util.CursorUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comment", description = "Comment management APIs")
public class CommentController extends AbstractBaseController {

    private final CommentService commentService;

    @PostMapping("/{postId}")
    @Operation(summary = "Create comment", description = "Create a new comment on a post")
    public ResponseEntity<CreatedResponse<Void>> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal String mssv) {

        log.info("[Comment Controller] Creating comment on post: {} by mssv: {}", postId, mssv);

        commentService.createComment(postId, mssv, request);
        return created("Comment created successfully");
    }

    @GetMapping("/{postId}/comments")
    @Operation(summary = "Get post comments", description = "Get cursor-paginated list of comments for the post")
    public ResponseEntity<CursorPageResponse<CommentResponse>> getPostComments(
            @PathVariable UUID postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal String mssv) {

        log.info("[Post Controller] Getting comments for post: {} with cursor: {}, limit: {}", postId, cursor, limit);

        List<CommentResponse> comments = commentService.getPostComments(postId, mssv, cursor, limit);

        return cursorPaging(
                "Post comments retrieved successfully",
                comments,
                limit,
                comment -> CursorUtils.encode(comment.createdAt(), comment.id()));
    }

    @PostMapping("/{commentId}/replies")
    @Operation(summary = "Reply to comment", description = "Create a reply to an existing comment")
    public ResponseEntity<CreatedResponse<Void>> replyToComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal String mssv) {

        log.info("[Comment Controller] Creating reply to comment: {} by mssv: {}", commentId, mssv);

        commentService.replyToComment(commentId, mssv, request);
        return created("Reply created successfully");
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Update comment", description = "Update comment content (only by author)")
    public ResponseEntity<SuccessResponse> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal String mssv) {

        log.info("[Comment Controller] Updating comment: {} by mssv: {}", commentId, mssv);

        commentService.updateComment(commentId, mssv, request);
        return success("Comment updated successfully");
    }

    @GetMapping("/comments/{commentId}/replies")
    @Operation(summary = "Get comment replies", description = "Get cursor-paginated list of replies for a specific comment")
    public ResponseEntity<CursorPageResponse<CommentResponse>> getCommentReplies(
            @PathVariable UUID commentId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal String mssv) {

        log.info("[Comment] Getting replies for comment: {}", commentId);

        List<CommentResponse> replies = commentService.getCommentReplies(commentId, mssv, cursor, limit);

        return cursorPaging(
                "Comment replies retrieved successfully",
                replies,
                limit,
                reply -> CursorUtils.encode(reply.createdAt(), reply.id()));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete comment", description = "Delete comment and its replies (only by author)")
    public ResponseEntity<SuccessResponse> deleteComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal String mssv) {

        log.info("[Comment Controller] Deleting comment: {} by mssv: {}", commentId, mssv);

        commentService.deleteComment(commentId, mssv);
        return success("Comment deleted successfully");
    }

    @PostMapping("/{commentId}/like")
    @Operation(summary = "Like/Unlike comment", description = "Toggle like on a comment")
    public ResponseEntity<SuccessResponse> toggleCommentLike(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal String mssv) {

        log.info("[Comment Controller] Toggling like on comment: {} by mssv: {}", commentId, mssv);

        boolean isLiked = commentService.toggleCommentLike(commentId, mssv);
        String message = isLiked ? "Comment liked successfully" : "Comment unliked successfully";

        return success(message);
    }
}