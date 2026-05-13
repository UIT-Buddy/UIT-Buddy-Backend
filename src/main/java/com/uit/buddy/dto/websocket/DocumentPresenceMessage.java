package com.uit.buddy.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPresenceMessage {

    private UUID documentId;
    private List<ActiveUser> activeUsers;
    private Integer totalActive;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveUser {
        private String mssv;
        private String name;
        private String avatarUrl;
    }
}
