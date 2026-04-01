package com.uit.buddy.dto.request.document;

import com.uit.buddy.enums.AccessRole;
import com.uit.buddy.enums.DocumentResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ShareResourceRequest(
        @NotNull(message = "Resource type is required") DocumentResourceType resourceType,
        @NotNull(message = "Resource ID is required") UUID resourceId,
        @NotBlank(message = "Target MSSV is required") String targetMssv,
        @NotNull(message = "Access role is required") AccessRole accessRole) {
}
