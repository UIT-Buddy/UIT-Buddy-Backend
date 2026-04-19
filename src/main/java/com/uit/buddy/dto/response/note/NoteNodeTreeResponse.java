package com.uit.buddy.dto.response.note;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteNodeTreeResponse {
    private UUID id;
    private UUID parentId;
    private String name;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<NoteNodeTreeResponse> children = new ArrayList<>();

    @Builder.Default
    private List<NoteSummaryResponse> notes = new ArrayList<>();
}
