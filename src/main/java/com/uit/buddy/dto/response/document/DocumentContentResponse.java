package com.uit.buddy.dto.response.document;

import com.uit.buddy.dto.response.social.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentContentResponse {

    private UUID id;
    private String fileName;
    private String content;
    private Long version;
    private UserSummary owner;
    private UserSummary lastEditor;
    private Instant lastEditedAt;
    private List<UserSummary> collaborators;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
