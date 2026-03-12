package com.uit.buddy.service.social.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.buddy.dto.response.social.UserReactionResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.social.Reaction;
import com.uit.buddy.event.social.PostLikedEvent;
import com.uit.buddy.exception.social.SocialErrorCode;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.ReactionMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.social.ReactionRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.social.ReactionService;
import com.uit.buddy.util.CursorUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionServiceImpl implements ReactionService {

    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;
    private final StudentRepository studentRepository;
    private final ReactionMapper reactionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public boolean togglePostLike(UUID postId, String mssv) {
        log.info("[Reaction Service] Toggling like for post: {} by student: {}", postId, mssv);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));

        Optional<Reaction> existingReaction = reactionRepository.findByPostIdAndMssv(postId, mssv);

        if (existingReaction.isPresent()) {
            reactionRepository.delete(existingReaction.get());
            postRepository.decrementLikeCount(postId);
            return false;
        }

        Reaction newReaction = Reaction.builder()
                .student(studentRepository.getReferenceById(mssv))
                .post(post)
                .build();

        reactionRepository.save(newReaction);
        postRepository.incrementLikeCount(postId);

        if (!post.getMssv().equals(mssv)) {
            String actorName = studentRepository.findById(mssv)
                    .map(student -> student.getFullName())
                    .orElse(mssv);

            eventPublisher.publishEvent(new PostLikedEvent(
                    mssv,
                    actorName,
                    post.getMssv(),
                    postId,
                    post.getContent()));
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserReactionResponse> getPostReactions(UUID postId, String mssv, String cursor, int limit) {

        postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));

        LocalDateTime cursorTime = null;
        String cursorMssv = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorMssv = contents.id().toString();
        }

        return reactionRepository.findReactionsWithCursor(postId, cursorTime, cursorMssv, limit + 1)
                .stream()
                .map(reactionMapper::toReactionResponse)
                .toList();
    }
}
