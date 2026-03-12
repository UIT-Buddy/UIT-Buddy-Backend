package com.uit.buddy.service.social.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.buddy.dto.request.social.SharePostRequest;
import com.uit.buddy.dto.response.social.UserShareResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.social.Share;
import com.uit.buddy.enums.PostType;
import com.uit.buddy.enums.ShareType;
import com.uit.buddy.event.social.PostSharedEvent;
import com.uit.buddy.exception.social.SocialErrorCode;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.ShareMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.social.ShareRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.social.ShareService;
import com.uit.buddy.util.CursorUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShareServiceImpl implements ShareService {

    private final PostRepository postRepository;
    private final ShareRepository shareRepository;
    private final StudentRepository studentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ShareMapper shareMapper;

    @Override
    @Transactional
    public boolean sharePost(UUID postId, String mssv, ShareType type, SharePostRequest request) {
        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND));

        var student = studentRepository.findById(mssv)
                .orElseThrow(() -> new SocialException(SocialErrorCode.UNAUTHORIZED));

        boolean isFirstTime = !shareRepository.existsByPostIdAndMssv(postId, mssv);

        Share share = Share.builder()
                .post(originalPost)
                .student(student)
                .type(type)
                .build();
        shareRepository.save(share);

        UUID sharedPostId = null;

        switch (type) {
            case PROFILE -> {
                Post sharedPost = Post.builder()
                        .author(student)
                        .title("") // Title is required
                        .content(request != null ? request.content() : "")
                        .originalPost(originalPost)
                        .type(PostType.SHARE)
                        .build();
                sharedPostId = postRepository.save(sharedPost).getId();
            }
            case MESSAGE -> {
                // sau này implement sau
            }
        }

        if (isFirstTime) {
            postRepository.incrementShareCount(postId);

            if (!originalPost.getMssv().equals(mssv)) {
                String actorName = studentRepository.findById(mssv)
                        .map(s -> s.getFullName())
                        .orElse(mssv);

                // Bắn event với sharedPostId (có thể null nếu share qua message)
                eventPublisher.publishEvent(new PostSharedEvent(
                        mssv,
                        actorName,
                        originalPost.getMssv(),
                        originalPost.getId(),
                        sharedPostId));
            }
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserShareResponse> getPostShares(UUID postId, String mssv, String cursor, int limit) {
        log.info("[Post Service] Getting shares for post: {}", postId);

        postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));

        LocalDateTime cursorTime = null;
        String cursorMssv = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorMssv = contents.id().toString();
        }

        return shareRepository.findSharesWithCursor(postId, cursorTime, cursorMssv, limit + 1)
                .stream()
                .map(shareMapper::toShareResponse)
                .toList();
    }
}
