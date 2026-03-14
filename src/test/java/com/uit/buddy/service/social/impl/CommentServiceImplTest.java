package com.uit.buddy.service.social.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.uit.buddy.dto.request.social.CreateCommentRequest;
import com.uit.buddy.dto.request.social.UpdateCommentRequest;
import com.uit.buddy.dto.response.social.CommentResponse;
import com.uit.buddy.dto.response.social.UserSummary;
import com.uit.buddy.entity.social.Comment;
import com.uit.buddy.entity.social.CommentReaction;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.CommentMapper;
import com.uit.buddy.repository.social.CommentReactionRepository;
import com.uit.buddy.repository.social.CommentRepository;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.social.projection.CommentProjection;
import com.uit.buddy.repository.user.StudentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentServiceImplTest {

  @Mock private PostRepository postRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private CommentReactionRepository commentReactionRepository;
  @Mock private CommentMapper commentMapper;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private CommentServiceImpl commentService;

  private String mssv;
  private UUID postId;
  private UUID commentId;
  private Post post;
  private Comment comment;
  private Student student;
  private CommentProjection projection;

  @BeforeEach
  void setUp() {
    mssv = "22100001";
    postId = UUID.randomUUID();
    commentId = UUID.randomUUID();

    student = new Student();
    student.setMssv(mssv);
    student.setFullName("Test Student");

    post = Post.builder().title("Test Post").content("Test Content").author(student).build();
    ReflectionTestUtils.setField(post, "id", postId);
    ReflectionTestUtils.setField(post, "mssv", mssv); // Set mssv field

    comment = Comment.builder().post(post).author(student).content("Test Comment").build();
    ReflectionTestUtils.setField(comment, "id", commentId);
    ReflectionTestUtils.setField(comment, "mssv", mssv); // Set mssv field

    projection = mock(CommentProjection.class);
    when(projection.getId()).thenReturn(commentId);
    when(projection.getContent()).thenReturn("Test Comment");
    when(projection.getMssv()).thenReturn(mssv);
    when(projection.getFullName()).thenReturn("Test Student");
    when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());
  }

  @Test
  void shouldCreateCommentSuccessfully() {
    CreateCommentRequest request = new CreateCommentRequest("Test comment content");

    when(postRepository.findById(postId)).thenReturn(Optional.of(post));
    when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
    when(commentRepository.save(any(Comment.class))).thenReturn(comment);

    commentService.createComment(postId, mssv, request);

    verify(postRepository).findById(postId);
    verify(studentRepository).findById(mssv);
    verify(commentRepository).save(any(Comment.class));
    verify(postRepository).incrementCommentCount(postId);
  }

  @Test
  void shouldThrowExceptionWhenPostNotFoundForComment() {
    CreateCommentRequest request = new CreateCommentRequest("Test comment");
    when(postRepository.findById(postId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.createComment(postId, mssv, request))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldThrowExceptionWhenStudentNotFoundForComment() {
    CreateCommentRequest request = new CreateCommentRequest("Test comment");
    when(postRepository.findById(postId)).thenReturn(Optional.of(post));
    when(studentRepository.findById(mssv)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.createComment(postId, mssv, request))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldReplyToCommentSuccessfully() {
    CreateCommentRequest request = new CreateCommentRequest("Reply content");

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(studentRepository.getReferenceById(mssv)).thenReturn(student);
    when(commentRepository.save(any(Comment.class))).thenReturn(comment);

    commentService.replyToComment(commentId, mssv, request);

    verify(commentRepository).findById(commentId);
    verify(commentRepository).save(any(Comment.class));
    verify(postRepository).incrementCommentCount(postId);
    verify(commentRepository).incrementReplyCount(commentId);
  }

  @Test
  void shouldThrowExceptionWhenParentCommentNotFound() {
    CreateCommentRequest request = new CreateCommentRequest("Reply content");
    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.replyToComment(commentId, mssv, request))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldUpdateCommentSuccessfully() {
    UpdateCommentRequest request = new UpdateCommentRequest("Updated content");

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    commentService.updateComment(commentId, mssv, request);

    assertThat(comment.getContent()).isEqualTo("Updated content");
    verify(commentRepository).findById(commentId);
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNotOwnedComment() {
    UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.updateComment(commentId, "22100002", request))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistentComment() {
    UpdateCommentRequest request = new UpdateCommentRequest("Updated content");
    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.updateComment(commentId, mssv, request))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldDeleteCommentSuccessfully() {
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    commentService.deleteComment(commentId, mssv);

    verify(commentRepository).delete(comment);
    verify(postRepository).decrementCommentCount(postId);
  }

  @Test
  void shouldThrowExceptionWhenDeletingNotOwnedComment() {
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    assertThatThrownBy(() -> commentService.deleteComment(commentId, "22100002"))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistentComment() {
    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.deleteComment(commentId, mssv))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldToggleCommentLikeSuccessfully() {
    when(commentRepository.existsById(commentId)).thenReturn(true);
    when(commentReactionRepository.findByCommentIdAndMssv(commentId, mssv))
        .thenReturn(Optional.empty());
    when(commentReactionRepository.save(any(CommentReaction.class)))
        .thenReturn(new CommentReaction());

    boolean result = commentService.toggleCommentLike(commentId, mssv);

    assertThat(result).isTrue();
    verify(commentReactionRepository).save(any(CommentReaction.class));
    verify(commentRepository).incrementLikeCount(commentId);
  }

  @Test
  void shouldUnlikeCommentWhenAlreadyLiked() {
    CommentReaction existingReaction = new CommentReaction();
    when(commentRepository.existsById(commentId)).thenReturn(true);
    when(commentReactionRepository.findByCommentIdAndMssv(commentId, mssv))
        .thenReturn(Optional.of(existingReaction));

    boolean result = commentService.toggleCommentLike(commentId, mssv);

    assertThat(result).isFalse();
    verify(commentReactionRepository).delete(existingReaction);
    verify(commentRepository).decrementLikeCount(commentId);
  }

  @Test
  void shouldThrowExceptionWhenLikingNonExistentComment() {
    when(commentRepository.existsById(commentId)).thenReturn(false);

    assertThatThrownBy(() -> commentService.toggleCommentLike(commentId, mssv))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldGetPostCommentsSuccessfully() {
    CommentResponse commentResponse =
        new CommentResponse(
            commentId,
            "Test Comment",
            new UserSummary(mssv, "Test Student", "avatar.jpg"),
            0L,
            0L,
            false,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null);

    when(postRepository.findById(postId)).thenReturn(Optional.of(post));
    when(commentRepository.findCommentsWithCursor(
            eq(postId), eq(null), eq(mssv), any(), any(), eq(11)))
        .thenReturn(List.of(projection));
    when(commentMapper.toCommentResponse(projection)).thenReturn(commentResponse);

    List<CommentResponse> result = commentService.getPostComments(postId, mssv, null, 10);

    assertThat(result).hasSize(1);
    verify(postRepository).findById(postId);
    verify(commentRepository)
        .findCommentsWithCursor(eq(postId), eq(null), eq(mssv), any(), any(), eq(11));
  }

  @Test
  void shouldThrowExceptionWhenGettingCommentsForNonExistentPost() {
    when(postRepository.findById(postId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.getPostComments(postId, mssv, null, 10))
        .isInstanceOf(SocialException.class);
  }

  @Test
  void shouldGetCommentRepliesSuccessfully() {
    CommentResponse replyResponse =
        new CommentResponse(
            UUID.randomUUID(),
            "Reply Comment",
            new UserSummary(mssv, "Test Student", "avatar.jpg"),
            0L,
            0L,
            false,
            LocalDateTime.now(),
            LocalDateTime.now(),
            commentId);

    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(commentRepository.findCommentsWithCursor(
            eq(postId), eq(commentId), eq(mssv), any(), any(), eq(11)))
        .thenReturn(List.of(projection));
    when(commentMapper.toCommentResponse(projection)).thenReturn(replyResponse);

    List<CommentResponse> result = commentService.getCommentReplies(commentId, mssv, null, 10);

    assertThat(result).hasSize(1);
    verify(commentRepository).findById(commentId);
    verify(commentRepository)
        .findCommentsWithCursor(eq(postId), eq(commentId), eq(mssv), any(), any(), eq(11));
  }

  @Test
  void shouldThrowExceptionWhenGettingRepliesForNonExistentComment() {
    when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> commentService.getCommentReplies(commentId, mssv, null, 10))
        .isInstanceOf(SocialException.class);
  }
}
