package com.uit.buddy.dto.request.document;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDocumentContentRequest {

    @NotNull(message = "Content cannot be null")
    private String content;

    @NotNull(message = "Version is required for optimistic locking")
    private Long version;
}
