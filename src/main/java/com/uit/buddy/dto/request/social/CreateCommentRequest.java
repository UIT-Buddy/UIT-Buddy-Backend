package com.uit.buddy.dto.request.social;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
    @NotBlank(message = "The content of comment must be not empty")
        @Size(max = 500, message = "Not to much, not exceed 500 characters")
        String content) {}
