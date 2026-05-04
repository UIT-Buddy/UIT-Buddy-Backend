package com.uit.buddy.dto.response.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uit.buddy.dto.base.CursorPageResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    @JsonProperty("unread_notification")
    private long unreadCount;
    private CursorPageResponse.PagingInfo paging;
}
