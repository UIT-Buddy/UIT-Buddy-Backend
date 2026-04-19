package com.uit.buddy.dto.request.document;

import com.uit.buddy.enums.DocumentResourceType;
import com.uit.buddy.enums.ShareTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ShareResourceViaMessageRequest(
        @NotNull(message = "Resource type is required") DocumentResourceType resourceType,
        @NotNull(message = "Resource ID is required") UUID resourceId,
        @NotBlank(message = "Receiver ID is required") String receiverId,
        @NotNull(message = "Receiver type is required") ShareTargetType receiverType,
        @Size(max = 500, message = "Not too much, not exceed 500 characters") String content) {
}
