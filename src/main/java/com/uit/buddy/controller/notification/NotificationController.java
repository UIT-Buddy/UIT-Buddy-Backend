package com.uit.buddy.controller.notification;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CursorPageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.response.notification.NotificationListResponse;
import com.uit.buddy.dto.response.notification.NotificationResponse;
import com.uit.buddy.dto.response.notification.UnreadCountResponse;
import com.uit.buddy.service.notification.NotificationService;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController extends AbstractBaseController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get notifications", description = "Get paginated notifications with unread count")
    public ResponseEntity<SingleResponse<NotificationListResponse>> getNotifications(
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal String mssv) {

        log.info("[GET /api/notifications] Getting notifications with cursor: {}, limit: {}", cursor, limit);

        List<NotificationResponse> notifications = notificationService.getNotifications(mssv, cursor, limit);
        long unreadCount = notificationService.getUnreadCount(mssv);

        boolean hasMore = notifications.size() > limit;
        List<NotificationResponse> pagedData = hasMore ? notifications.subList(0, limit) : notifications;

        String nextCursor = null;
        if (!pagedData.isEmpty() && hasMore) {
            NotificationResponse lastItem = pagedData.get(pagedData.size() - 1);
            nextCursor = CursorUtils.encode(lastItem.getCreatedAt(), lastItem.getId());
        }

        NotificationListResponse response = NotificationListResponse.builder().notifications(pagedData)
                .unreadCount(unreadCount).paging(new CursorPageResponse.PagingInfo(nextCursor, hasMore, limit)).build();

        return successSingle(response, "Notifications retrieved successfully");
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications")
    public ResponseEntity<SingleResponse<UnreadCountResponse>> getUnreadCount(@AuthenticationPrincipal String mssv) {
        log.info("[GET /api/notifications/unread-count] Getting unread count for mssv: {}", mssv);
        long count = notificationService.getUnreadCount(mssv);
        UnreadCountResponse response = new UnreadCountResponse(count);
        return successSingle(response, "Unread count retrieved successfully");
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    public ResponseEntity<SuccessResponse> markAsRead(@PathVariable UUID notificationId,
            @AuthenticationPrincipal String mssv) {
        log.info("[PUT /api/notifications/{notificationId}/read] Marking notification {} as read for mssv: {}",
                notificationId, mssv);
        notificationService.markAsRead(notificationId, mssv);
        return success("Notification marked as read");
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read")
    public ResponseEntity<SuccessResponse> markAllAsRead(@AuthenticationPrincipal String mssv) {
        log.info("[PUT /api/notifications/read-all] Marking all notifications as read for mssv: {}", mssv);
        notificationService.markAllAsRead(mssv);
        return success("All notifications marked as read");
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a notification")
    public ResponseEntity<SuccessResponse> deleteNotification(@PathVariable UUID notificationId,
            @AuthenticationPrincipal String mssv) {
        log.info("[DELETE /api/notifications/{notificationId}] Deleting notification {} for mssv: {}", notificationId,
                mssv);
        notificationService.deleteNotification(notificationId, mssv);
        return success("Notification deleted successfully");
    }
}
