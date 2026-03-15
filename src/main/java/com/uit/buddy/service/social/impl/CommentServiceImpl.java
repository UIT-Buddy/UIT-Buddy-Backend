package com.uit.buddy.service.social.impl;

import com.uit.buddy.dto.request.social.CreateCommentRequest;
import com.uit.buddy.dto.request.social.UpdateCommentRequest;
import com.uit.buddy.dto.response.social.CommentResponse;
import com.uit.buddy.entity.social.Comment;
import com.uit.buddy.entity.social.CommentReaction;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.event.social.PostCommentedEvent;
import com.uit.buddy.exception.social.SocialErrorCode;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.CommentMapper;
import com.uit.buddy.repository.social.CommentReactionRepository;
import com.uit.buddy.repository.social.CommentRepository;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.social.CommentService;
import com.uit.buddy.util.CursorUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final StudentRepository studentRepository;
    private final CommentReactionRepository commentReactionRepository;
    private final CommentMapper commentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void createComment(UUID postId, String mssv, CreateCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND));

        var author = studentRepository.findById(mssv)
                .orElseThrow(() -> new SocialException(SocialErrorCode.UNAUTHORIZED));

        Comment comment = Comment.builder().post(post).author(author).content(request.content()).build();

        comment = commentRepository.save(comment);
        postRepository.incrementCommentCount(postId);

        if (!post.getMssv().equals(mssv)) {
            String actorName = studentRepository.findById(mssv).map(student -> student.getFullName()).orElse(mssv);

            eventPublisher.publishEvent(new PostCommentedEvent(mssv, actorName, post.getMssv(), postId, comment.getId(),
                    request.content()));
        }
    }

    @Override
    @Transactional
    public void replyToComment(UUID commentId, String mssv, CreateCommentRequest request) {
        var parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.COMMENT_NOT_FOUND));

        var post = parentComment.getPost();

        Comment reply = Comment.builder().post(post).parentComment(parentComment)
                .author(studentRepository.getReferenceById(mssv)).content(request.content()).build();

        reply = commentRepository.save(reply);
        postRepository.incrementCommentCount(post.getId());
        commentRepository.incrementReplyCount(commentId);

        if (!parentComment.getMssv().equals(mssv)) {
            String actorName = studentRepository.findById(mssv).map(student -> student.getFullName()).orElse(mssv);

            eventPublisher.publishEvent(new PostCommentedEvent(mssv, actorName, parentComment.getMssv(), post.getId(),
                    reply.getId(), request.content()));
        }
    }

    @Override
    @Transactional
    public void updateComment(UUID commentId, String mssv, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMssv().equals(mssv)) {
            throw new SocialException(SocialErrorCode.UNAUTHORIZED);
        }

        comment.setContent(request.content());
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId, String mssv) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMssv().equals(mssv)) {
            throw new SocialException(SocialErrorCode.UNAUTHORIZED);
        }

        commentRepository.delete(comment);
        postRepository.decrementCommentCount(comment.getPost().getId());
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(UUID commentId, String mssv) {
        if (!commentRepository.existsById(commentId)) {
            throw new SocialException(SocialErrorCode.COMMENT_NOT_FOUND);
        }

        var existingReaction = commentReactionRepository.findByCommentIdAndMssv(commentId, mssv);

        if (existingReaction.isPresent()) {
            commentReactionRepository.delete(existingReaction.get());
            commentRepository.decrementLikeCount(commentId);
            return false;
        } else {
            CommentReaction reaction = CommentReaction.builder().commentId(commentId).mssv(mssv).build();
            commentReactionRepository.save(reaction);
            commentRepository.incrementLikeCount(commentId);
            return true;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getPostComments(UUID postId, String mssv, String cursor, int limit) {
        log.info("[Post Service] Getting root comments for post: {}", postId);

        postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));

        LocalDateTime cursorTime = null;
        UUID cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorId = contents.id();
        }

        return commentRepository.findCommentsWithCursor(postId, null, mssv, cursorTime, cursorId, limit + 1).stream()
                .map(commentMapper::toCommentResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentReplies(UUID commentId, String mssv, String cursor, int limit) {
        log.info("[Post Service] Getting replies for comment: {}", commentId);

        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.COMMENT_NOT_FOUND, "Comment not found"));

        LocalDateTime cursorTime = null;
        UUID cursorId = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorId = contents.id();
        }

        return commentRepository.findCommentsWithCursor(parentComment.getPost().getId(), commentId, mssv, cursorTime,
                cursorId, limit + 1).stream().map(commentMapper::toCommentResponse).toList();
    }
}
