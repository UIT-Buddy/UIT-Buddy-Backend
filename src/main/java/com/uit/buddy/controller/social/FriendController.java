package com.uit.buddy.controller.social;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CursorPageResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.social.RespondFriendRequestRequest;
import com.uit.buddy.dto.request.social.SendFriendRequestRequest;
import com.uit.buddy.dto.response.social.FriendshipResponse;
import com.uit.buddy.dto.response.social.PendingFriendRequestResponse;
import com.uit.buddy.dto.response.social.SentFriendRequestResponse;
import com.uit.buddy.service.social.FriendService;
import com.uit.buddy.util.CursorUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Friend", description = "Friend management APIs")
public class FriendController extends AbstractBaseController {

    private final FriendService friendService;

    @PostMapping("/requests")
    @Operation(summary = "Toggle friend request", description = "Send a friend request if not sent, or cancel if already sent")
    public ResponseEntity<SuccessResponse> toggleFriendRequest(@Valid @RequestBody SendFriendRequestRequest request,
            @AuthenticationPrincipal String mssv) {
        log.info("[Friend Controller] Toggling friend request from {} to {}", mssv, request.receiverMssv());
        boolean isSent = friendService.toggleFriendRequest(mssv, request);
        String message = isSent ? "Friend request sent successfully" : "Friend request cancelled successfully";
        return success(message);
    }

    @PutMapping("/requests/{mssvReceiver}")
    @Operation(summary = "Respond to friend request", description = "Accept or reject a friend request")
    public ResponseEntity<SuccessResponse> respondToFriendRequest(@PathVariable String mssvReceiver ,
            @Valid @RequestBody RespondFriendRequestRequest request, @AuthenticationPrincipal String mssvSender) {
        log.info("[Friend Controller] Responding to friend request {} by {} with action: {}", mssvReceiver, mssvSender,
                request.action());
        friendService.respondToFriendRequest(mssvSender, mssvReceiver, request);
        return success("Friend request responded successfully");
    }

    @DeleteMapping("/{friendMssv}")
    @Operation(summary = "Unfriend", description = "Remove a friend from your friends list")
    public ResponseEntity<SuccessResponse> unfriend(@PathVariable String friendMssv,
            @AuthenticationPrincipal String mssv) {
        log.info("[Friend Controller] Unfriending {} by {}", friendMssv, mssv);
        friendService.unfriend(mssv, friendMssv);
        return success("Unfriended successfully");
    }

    @GetMapping("/requests/pending")
    @Operation(summary = "Get pending friend requests", description = "Get all pending friend requests received by the user")
    public ResponseEntity<CursorPageResponse<PendingFriendRequestResponse>> getPendingRequests(
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal String mssv) {
        log.info("[Friend Controller] Getting pending requests for {}", mssv);
        List<PendingFriendRequestResponse> requests = friendService.getPendingRequests(mssv, cursor, limit);
        return cursorPaging("Pending requests retrieved successfully", requests, limit,
                req -> CursorUtils.encode(req.createdAt(), req.id()));
    }

    @GetMapping("/requests/sent")
    @Operation(summary = "Get sent friend requests", description = "Get all pending friend requests sent by the user")
    public ResponseEntity<CursorPageResponse<SentFriendRequestResponse>> getSentRequests(
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal String mssv) {
        log.info("[Friend Controller] Getting sent requests for {}", mssv);
        List<SentFriendRequestResponse> requests = friendService.getSentRequests(mssv, cursor, limit);
        return cursorPaging("Sent requests retrieved successfully", requests, limit,
                req -> CursorUtils.encode(req.createdAt(), req.id()));
    }

    @GetMapping
    @Operation(summary = "Get friends list", description = "Get all friends of the user")
    public ResponseEntity<CursorPageResponse<FriendshipResponse>> getFriends(
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal String mssv) {
        log.info("[Friend Controller] Getting friends for {}", mssv);
        List<FriendshipResponse> friends = friendService.getFriends(mssv, cursor, limit);
        return cursorPaging("Friends retrieved successfully", friends, limit,
                friend -> CursorUtils.encode(friend.createdAt(), friend.id()));
    }
}
