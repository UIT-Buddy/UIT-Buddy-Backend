package com.uit.buddy.controller.social;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CreatedResponse;
import com.uit.buddy.dto.base.CursorPageResponse;
import com.uit.buddy.dto.base.PageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.service.social.PostService;
import com.uit.buddy.util.CursorUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Post", description = "Post management APIs")
public class PostController extends AbstractBaseController {

    private final PostService postService;

    @GetMapping
    @Operation(summary = "Get post feed", description = "Get paginated post feed with cursor-based pagination")
    public ResponseEntity<CursorPageResponse<PostFeedResponse>> getPostFeed(
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal String mssv) {

        log.info("[Post Controller] Getting post feed with cursor: {}, limit: {}", cursor, limit);

        List<PostFeedResponse> postList = postService.getPostFeed(mssv, cursor, limit);

        return cursorPaging("Post feed retrieved successfully", postList, limit,
                post -> CursorUtils.encode(post.createdAt(), post.id()));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get post detail", description = "Get detailed post information with comments and reactions")
    public ResponseEntity<SingleResponse<PostDetailResponse>> getPostDetail(@PathVariable UUID postId,
            @AuthenticationPrincipal String mssv) {
        log.info("[Post Controller] Getting post detail: {}", postId);
        PostDetailResponse response = postService.getPostDetail(postId, mssv);
        return successSingle(response, "Post detail retrieved successfully");
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update a post", description = "Update post title and content (files cannot be updated)")
    public ResponseEntity<SuccessResponse> updatePost(@PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request, @AuthenticationPrincipal String mssv) {
        log.info("[Post Controller] Updating post: {} by mssv: {}", postId, mssv);
        postService.updatePost(postId, mssv, request);
        return success("Post updated successfully");
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post", description = "Delete a post and its associated media")
    public ResponseEntity<SuccessResponse> deletePost(@PathVariable UUID postId, @AuthenticationPrincipal String mssv) {

        log.info("[Post Controller] Deleting post: {} by mssv: {}", postId, mssv);

        postService.deletePost(postId, mssv);
        return success("Post deleted successfully");
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts", description = "Search posts with keyword and filter")
    public ResponseEntity<PageResponse<PostFeedResponse>> searchPostByKeywordAndFilters(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal String mssv) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<PostFeedResponse> responses = postService.searchPost(keyword, mssv, pageable);
        return paging(responses, "Search posts with keyword and filter successfully");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create post", description = "Create post with multiple media types")
    public ResponseEntity<CreatedResponse<Void>> uploadPost(
            @RequestParam("title") @NotBlank(message = "Title is required") @Size(min = 1, max = 255, message = "Title must not exceed 255 characters and at least 1 character") String title,
            @RequestParam("content") @NotBlank(message = "Content is required") @Size(max = 500, message = "Content must not exceed 500 characters") String content,
            @ModelAttribute CreatePostRequest request, @AuthenticationPrincipal String mssv) {
        postService.createPost(mssv, title, content, request);
        return created("Upload posts successfully with multimedia");
    }
}
