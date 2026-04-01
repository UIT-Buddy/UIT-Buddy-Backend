package com.uit.buddy.dto.request.document;

import com.uit.buddy.enums.DocumentResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UnshareResourceRequest(
        @NotNull(message = "Resource type is required") DocumentResourceType resourceType,
        @NotNull(message = "Resource ID is required") UUID resourceId,
        @NotBlank(message = "Target MSSV is required") String targetMssv) {
}
