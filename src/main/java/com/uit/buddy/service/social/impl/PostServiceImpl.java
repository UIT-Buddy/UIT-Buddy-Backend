package com.uit.buddy.service.social.impl;

import java.util.List;
import java.util.UUID;

import com.uit.buddy.util.CursorUtils;
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

    @Override
    @Transactional
    public PostDetailResponse createPost(String mssv, CreatePostRequest request, MultipartFile image,
            MultipartFile video) {
        log.info("[Post Service] Create post for mssv: {}", mssv);

        Student author = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(
                        UserErrorCode.STUDENT_NOT_FOUND,
                        "Student not found"));

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .author(author)
                .build();

        Post savedPost = postRepository.save(post);

        handleMediaUpload(image, video, savedPost);

        savedPost = postRepository.save(post);
        log.info("[Post Service] Post saved successfully with ID: {}", post.getId());
        return postMapper.toPostDetailResponse(savedPost);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostFeedResponse> getPostFeed(String cursor, int limit) {
        log.info("[Post Service] Fetching post feed. Cursor: {}, Limit: {}", cursor, limit);
        int fetchSize = limit + 1;

        List<Post> posts;

        if (cursor == null || cursor.isBlank()) {
            posts = postRepository.findFirstPage(fetchSize);
        } else {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);

            posts = postRepository.findNextPage(contents.timestamp(), contents.id(), fetchSize);
        }
        return posts.stream().map(postMapper::toPostFeedResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(UUID postId) {
        log.info("[Post Service] Getting detail for post: {}", postId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));

        return postMapper.toPostDetailResponse(post);
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

        log.info("[Post Service] Successfully updated post: {}", postId);
        return postMapper.toPostDetailResponse(post);
    }

    @Override
    @Transactional
    public void deletePost(UUID postId, String mssv) {
        Post post = getPostAndValidateOwner(postId, mssv);
        cloudinaryService.deletePostMedia(postId.toString());
        postRepository.delete(post);
    }

    @Override
    public Page<PostFeedResponse> searchPost(String keyword, Pageable pageable) {
        List<UUID> finalFoundPosts = postRepository.searchPostByKeyword(keyword);
        log.info("FINAL POSTS FOUND: {}", finalFoundPosts);
        Page<Post> page = postRepository.findAll(finalFoundPosts, pageable);
        return page.map(postMapper::toPostFeedResponse);
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

    private void handleMediaUpload(MultipartFile image, MultipartFile video, Post post) {
        String publicId = post.getId().toString();

        if (video != null && !video.isEmpty()) {
            post.setVideoUrl(cloudinaryService.uploadPostVideo(video, publicId));
        } else if (image != null && !image.isEmpty()) {
            post.setImageUrl(cloudinaryService.uploadPostImage(image, publicId));
        }
    }
}
