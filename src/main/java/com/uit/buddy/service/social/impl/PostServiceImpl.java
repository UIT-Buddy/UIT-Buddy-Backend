package com.uit.buddy.service.social.impl;

import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.social.SocialErrorCode;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.social.PostMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.file.FileService;
import com.uit.buddy.service.social.PostService;
import com.uit.buddy.util.CursorUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final StudentRepository studentRepository;
    private final PostMapper postMapper;
    private final FileService fileService;

    @Value("${post.limit-upload-images}")
    private int limitNumberOfImages;

    @Value("${post.limit-upload-videos}")
    private int limitNumberOfVideos;

    @Override
    public void createPost(String mssv, String title, String content, CreatePostRequest request) {
        log.info("[Post Service] Create post for mssv: {}", mssv);
        validateLimitImagesAndVideos(request.images(), request.videos());
        if (!studentRepository.existsById(mssv)) {
            throw new UserException(UserErrorCode.STUDENT_NOT_FOUND);
        }
        List<PostMedia> medias = fileService.uploadMultiMedia(request.images(), request.videos());
        saveToDb(mssv, title, content, medias);
    }

    @Transactional
    protected void saveToDb(String mssv, String title, String content, List<PostMedia> medias) {
        Student author = studentRepository.getReferenceById(mssv);

        Post post = Post.builder().title(title).content(content).author(author).medias(medias).build();

        postRepository.save(post);
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
        return postRepository.findFeed(mssv, cursorTime, cursorId, limit + 1).stream()
                .map(postMapper::toPostFeedResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(UUID postId, String mssv) {
        return postRepository.findDetailWithStatus(postId, mssv).map(postMapper::toPostDetailResponseFromProjection)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));
    }

    @Override
    @Transactional
    public void updatePost(UUID postId, String mssv, UpdatePostRequest request) {
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
    }

    @Override
    @Transactional
    public void deletePost(UUID postId, String mssv) {
        Post post = getPostAndValidateOwner(postId, mssv);
        List<PostMedia> medias = post.getMedias();
        if (medias != null && !medias.isEmpty()) {
            fileService.deletePostMedia(medias);
        }
        postRepository.delete(post);
        log.info("[Post Service] Successfully deleted post {} and its stored media", postId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostFeedResponse> searchPost(String keyword, String mssv, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return postRepository.findAllPosts(mssv, pageable).map(postMapper::toPostFeedResponse);
        }
        return postRepository.searchPostFull(keyword, mssv, pageable).map(postMapper::toPostFeedResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostFeedResponse> getUserPosts(String targetMssv, String currentMssv, String cursor, int limit) {
        log.info("[Post Service] Getting posts for user: {}", targetMssv);

        if (!studentRepository.existsById(targetMssv)) {
            throw new UserException(UserErrorCode.STUDENT_NOT_FOUND);
        }

        LocalDateTime cursorTime = null;
        UUID cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorId = contents.id();
        }

        return postRepository.findUserPosts(targetMssv, currentMssv, cursorTime, cursorId, limit + 1).stream()
                .map(postMapper::toPostFeedResponse).toList();
    }

    private void validateLimitImagesAndVideos(List<MultipartFile> images, List<MultipartFile> videos) {
        if (images != null && images.size() > limitNumberOfImages) {
            throw new UserException(UserErrorCode.REACH_LIMIT_IMAGES);
        }
        if (videos != null && videos.size() > limitNumberOfVideos) {
            throw new UserException(UserErrorCode.REACH_LIMIT_VIDEOS);
        }
    }

    private Post getPostAndValidateOwner(UUID postId, String mssv) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));

        if (!post.getAuthor().getMssv().equals(mssv)) {
            log.warn("[Post Service] Unauthorized access attempt: Student {} tried to modify post {} owned by {}", mssv,
                    postId, post.getAuthor().getMssv());

            throw new SocialException(SocialErrorCode.UNAUTHORIZED);
        }

        return post;
    }
}
