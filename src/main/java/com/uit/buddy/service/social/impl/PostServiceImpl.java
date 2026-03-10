package com.uit.buddy.service.social.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.uit.buddy.util.CursorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.social.SocialErrorCode;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import com.uit.buddy.service.social.PostService;
import com.uit.buddy.mapper.social.PostMapper;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final StudentRepository studentRepository;
    private final PostMapper postMapper;
    private final CloudinaryService cloudinaryService;
    @Value("${post.limit-upload-images}")
    private int limitNumberOfImages;
    @Value("${post.limit-upload-videos}")
    private int limitNumberOfVideos;
    @Override
    @Transactional
    public PostDetailResponse createPost(String mssv, String title, String content, CreatePostRequest request) {
        log.info("[Post Service] Create post for mssv: {}", mssv);
        validateLimitImagesAndVideos(request.images(), request.videos());
        Student author = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(
                        UserErrorCode.STUDENT_NOT_FOUND,
                        "Student not found"));

        Post post = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();

        Post savedPost = postRepository.save(post);
        handleMediaUpload(request.images(), request.videos(), savedPost);

        savedPost = postRepository.save(post);
        log.info("[Post Service] Post saved successfully with ID: {}", post.getId());
        return postMapper.toPostDetailResponse(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostFeedResponse> getPostFeed(String mssv, String cursor, int limit) {
        LocalDateTime cursorTime = null;
        UUID cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorId = contents.id();
        }

        // Lấy thêm 1 record để kiểm tra hasMore
        return postRepository.findFeed(mssv, cursorTime, cursorId, limit + 1)
                .stream()
                .map(postMapper::toPostFeedResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(UUID postId, String mssv) {
        return postRepository.findDetailWithStatus(postId, mssv)
                .map(postMapper::toPostDetailResponseFromProjection)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));
    }

    @Override
    @Transactional
    public PostDetailResponse updatePost(UUID postId, String mssv, UpdatePostRequest request) {
        log.info("[Post Service] Updating post: {}", postId);

        Post post = getPostAndValidateOwner(postId, mssv);

        if (request.title() != null && !request.title().isBlank()) {
            post.setTitle(request.title());
        }

        if (request.content() != null) {
            post.setContent(request.content());
        }

        postRepository.save(post);
        log.info("[Post Service] Successfully updated post: {}", postId);

        return getPostDetail(postId, mssv);
    }

    @Override
    @Transactional
    public void deletePost(UUID postId, String mssv) {
        Post post = getPostAndValidateOwner(postId, mssv);
        cloudinaryService.deletePostMedia(postId.toString());
        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostFeedResponse> searchPost(String keyword, String mssv, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return postRepository.findAllPosts(mssv, pageable)
                    .map(postMapper::toPostFeedResponse);
        }
        return postRepository.searchPostFull(keyword, mssv, pageable)
                .map(postMapper::toPostFeedResponse);
    }

    private Post getPostAndValidateOwner(UUID postId, String mssv) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.getAuthor().getMssv().equals(mssv)) {
            log.warn("[Post Service] Unauthorized access attempt by student {}", mssv);
            throw new AuthException(AuthErrorCode.PERMISSION_DENIED, "You do not have permission");
        }
        return post;
    }

    private void handleMediaUpload(List<MultipartFile> images, List<MultipartFile> videos, Post post) {
        if(images == null && videos == null)
        {
            post.setMedias(List.of());
            return;
        }
        String publicId = post.getId().toString();
        post.setMedias(cloudinaryService.uploadMultiMedia(images, videos, publicId));
    }
    private void validateLimitImagesAndVideos(List<MultipartFile> images, List<MultipartFile> videos)
    {
        System.out.println(limitNumberOfImages);
        System.out.println(limitNumberOfVideos);
        if(images != null && images.size() > limitNumberOfImages)
        {
            throw new UserException(UserErrorCode.REACH_LIMIT_IMAGES);
        }
        if(videos != null && videos.size() > limitNumberOfVideos)
        {
            throw new UserException(UserErrorCode.REACH_LIMIT_VIDEOS);
        }
    }
}
