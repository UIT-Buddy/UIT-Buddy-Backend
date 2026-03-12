package com.uit.buddy.service.social.impl;

import com.uit.buddy.dto.response.social.UserReactionResponse;
import com.uit.buddy.dto.response.social.UserSummary;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.social.Reaction;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.ReactionMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.social.ReactionRepository;
import com.uit.buddy.repository.social.projection.ReactionProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private ReactionRepository reactionRepository;
    @Mock
    private ReactionMapper reactionMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReactionServiceImpl reactionService;

    private String mssv;
    private UUID postId;
    private Post post;
    private Student student;
    private ReactionProjection projection;

    @BeforeEach
    void setUp() {
        mssv = "22100001";
        postId = UUID.randomUUID();

        student = new Student();
        student.setMssv(mssv);
        student.setFullName("Test Student");

        post = Post.builder()
                .title("Test Post")
                .content("Test Content")
                .author(student)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);

        projection = mock(ReactionProjection.class);
        when(projection.getMssv()).thenReturn(mssv);
        when(projection.getFullName()).thenReturn("Test Student");
        when(projection.getReactedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    void shouldLikePostSuccessfully() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reactionRepository.findByPostIdAndMssv(postId, mssv)).thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class))).thenReturn(new Reaction());

        boolean result = reactionService.togglePostLike(postId, mssv);

        assertThat(result).isTrue();
        verify(reactionRepository).save(any(Reaction.class));
        verify(postRepository).incrementLikeCount(postId);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void shouldUnlikePostSuccessfully() {
        Reaction existingReaction = new Reaction();
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reactionRepository.findByPostIdAndMssv(postId, mssv)).thenReturn(Optional.of(existingReaction));

        boolean result = reactionService.togglePostLike(postId, mssv);

        assertThat(result).isFalse();
        verify(reactionRepository).delete(existingReaction);
        verify(postRepository).decrementLikeCount(postId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldNotPublishEventWhenLikingOwnPost() {
        Post ownPost = Post.builder()
                .title("Own Post")
                .content("My content")
                .author(student)
                .build();
        ReflectionTestUtils.setField(ownPost, "id", postId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(ownPost));
        when(reactionRepository.findByPostIdAndMssv(postId, mssv)).thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class))).thenReturn(new Reaction());

        boolean result = reactionService.togglePostLike(postId, mssv);

        assertThat(result).isTrue();
        verify(postRepository).incrementLikeCount(postId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenPostNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reactionService.togglePostLike(postId, mssv))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldGetPostReactionsSuccessfully() {
        UserReactionResponse reactionResponse = new UserReactionResponse(
                new UserSummary(mssv, "Test Student", "avatar.jpg"),
                LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reactionRepository.findReactionsWithCursor(eq(postId), any(), any(), eq(11)))
                .thenReturn(List.of(projection));
        when(reactionMapper.toReactionResponse(projection)).thenReturn(reactionResponse);

        List<UserReactionResponse> result = reactionService.getPostReactions(postId, mssv, null, 10);

        assertThat(result).hasSize(1);
        verify(postRepository).findById(postId);
        verify(reactionRepository).findReactionsWithCursor(eq(postId), any(), any(), eq(11));
    }

    @Test
    void shouldThrowExceptionWhenGettingReactionsForNonExistentPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reactionService.getPostReactions(postId, mssv, null, 10))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldGetPostReactionsWithCursor() {
        String cursor = "encoded_cursor";
        UserReactionResponse reactionResponse = new UserReactionResponse(
                new UserSummary(mssv, "Test Student", "avatar.jpg"),
                LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reactionRepository.findReactionsWithCursor(eq(postId), any(), any(), eq(11)))
                .thenReturn(List.of(projection));
        when(reactionMapper.toReactionResponse(projection)).thenReturn(reactionResponse);

        List<UserReactionResponse> result = reactionService.getPostReactions(postId, mssv, cursor, 10);

        assertThat(result).hasSize(1);
        verify(reactionRepository).findReactionsWithCursor(eq(postId), any(), any(), eq(11));
    }

    @Test
    void shouldHandleEmptyReactionsList() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reactionRepository.findReactionsWithCursor(eq(postId), any(), any(), eq(11)))
                .thenReturn(List.of());

        List<UserReactionResponse> result = reactionService.getPostReactions(postId, mssv, null, 10);

        assertThat(result).isEmpty();
        verify(postRepository).findById(postId);
        verify(reactionRepository).findReactionsWithCursor(eq(postId), any(), any(), eq(11));
    }

    @Test
    void shouldCreateReactionWithCorrectProperties() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(reactionRepository.findByPostIdAndMssv(postId, mssv)).thenReturn(Optional.empty());
        when(reactionRepository.save(any(Reaction.class))).thenAnswer(invocation -> {
            Reaction reaction = invocation.getArgument(0);
            assertThat(reaction.getMssv()).isEqualTo(mssv);
            assertThat(reaction.getPostId()).isEqualTo(postId);
            return reaction;
        });

        boolean result = reactionService.togglePostLike(postId, mssv);

        assertThat(result).isTrue();
        verify(reactionRepository).save(any(Reaction.class));
    }
}