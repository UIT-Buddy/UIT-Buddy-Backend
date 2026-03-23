package com.uit.buddy.dto.response.notification;

import com.uit.buddy.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String title;
    private String content;
    private NotificationType type;
    private String dataId;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
