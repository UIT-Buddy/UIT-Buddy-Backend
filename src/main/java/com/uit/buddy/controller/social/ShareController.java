package com.uit.buddy.controller.social;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CursorPageResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.social.SharePostRequest;
import com.uit.buddy.dto.response.social.UserShareResponse;
import com.uit.buddy.enums.ShareType;
import com.uit.buddy.service.social.ShareService;
import com.uit.buddy.util.CursorUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shares")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Share", description = "Post sharing management APIs")
public class ShareController extends AbstractBaseController {

    private final ShareService shareService;

    @PostMapping("/posts/{postId}")
    @Operation(summary = "Share post", description = "Share a post to profile or via message")
    public ResponseEntity<SuccessResponse> sharePost(@PathVariable UUID postId,
            @RequestParam(defaultValue = "PROFILE") ShareType type,
            @RequestBody(required = false) SharePostRequest request, @AuthenticationPrincipal String mssv) {

        log.info("[Share Controller] Sharing post: {} by mssv: {} with type: {}", postId, mssv, type);

        shareService.sharePost(postId, mssv, type, request);

        return success("Post shared successfully");
    }

    @GetMapping("/{postId}/shares")
    @Operation(summary = "Get post shares", description = "Get cursor-paginated list of users who shared the post")
    public ResponseEntity<CursorPageResponse<UserShareResponse>> getPostShares(@PathVariable UUID postId,
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal String mssv) {

        log.info("[Post Controller] Getting shares for post: {} with cursor: {}, limit: {}", postId, cursor, limit);

        List<UserShareResponse> shares = shareService.getPostShares(postId, mssv, cursor, limit);

        return cursorPaging("Post shares retrieved successfully", shares, limit,
                share -> CursorUtils.encode(share.sharedAt(), UUID.fromString(share.user().mssv())));
    }
}
