package com.uit.buddy.dto.request.social;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePostRequest(

        @Schema(description = "Post image file", format = "binary") List<MultipartFile> images,

        @Schema(description = "Post video file", format = "binary") List<MultipartFile> videos) {
}
