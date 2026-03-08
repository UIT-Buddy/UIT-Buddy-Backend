package com.uit.buddy.dto.request.social;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CreatePostRequest(
                @NotBlank(message = "Title is required") @Size(max = 255, message = "Title must not exceed 255 characters") @Schema(description = "Post title", example = "My first post", type = "string") String title,

                @Schema(description = "Post content", example = "This is my post content", type = "string") String content,

                @Schema(description = "Post image file", type = "string", format = "binary") MultipartFile image,

                @Schema(description = "Post video file", type = "string", format = "binary") MultipartFile video) {
}
