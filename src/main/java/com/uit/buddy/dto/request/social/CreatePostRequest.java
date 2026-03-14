package com.uit.buddy.dto.request.social;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record CreatePostRequest(@Schema(description = "Post image file", format = "binary") List<MultipartFile> images,
        @Schema(description = "Post video file", format = "binary") List<MultipartFile> videos) {
}
