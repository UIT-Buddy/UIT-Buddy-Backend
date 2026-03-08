package com.uit.buddy.dto.request.social;

import jakarta.validation.constraints.Size;

public record UpdatePostRequest(
                @Size(max = 255, message = "Title must not exceed 255 characters") String title,

                String content) {
}
