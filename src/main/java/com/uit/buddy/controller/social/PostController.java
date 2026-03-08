package com.uit.buddy.controller.social;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.PostResponse;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.social.SocialErrorCode;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import com.uit.buddy.service.social.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Post", description = "Post management APIs")
public class PostController extends AbstractBaseController {

    private final PostService postService;
    private final CloudinaryService cloudinaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new post", description = "Create a new post with optional image and video")
    public ResponseEntity<SingleResponse<PostResponse>> createPost(
            @Valid @ModelAttribute CreatePostRequest request,
            @AuthenticationPrincipal String mssv) {
        log.info("[Post Controller] Creating post for mssv: {}", mssv);
        validateMediaFiles(request.image(), request.video());

        PostResponse response = postService.createPost(mssv, request, request.image(), request.video());
        return successSingle(response, "Post created successfully");
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update a post", description = "Update post title and content (files cannot be updated)")
    public ResponseEntity<SingleResponse<PostResponse>> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal String mssv) {
        log.info("[Post Controller] Updating post: {} by mssv: {}", postId, mssv);
        PostResponse response = postService.updatePost(postId, mssv, request);
        return successSingle(response, "Post updated successfully");
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post", description = "Delete a post and its associated media")
    public ResponseEntity<SuccessResponse> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal String mssv) {

        log.info("[Post Controller] Deleting post: {} by mssv: {}", postId, mssv);

        postService.deletePost(postId, mssv);
        return success("Post deleted successfully");
    }

    private void validateMediaFiles(MultipartFile image, MultipartFile video) {
        boolean hasImage = image != null && !image.isEmpty();
        boolean hasVideo = video != null && !video.isEmpty();

        if (hasImage && hasVideo) {
            throw new SocialException(SocialErrorCode.NOT_INCLUDE_BOTH_TYPES);
        }

        if (hasImage) {
            cloudinaryService.validateFile(image, FileType.IMAGE);
        }

        if (hasVideo) {
            cloudinaryService.validateFile(video, FileType.VIDEO);
        }
    }
}
