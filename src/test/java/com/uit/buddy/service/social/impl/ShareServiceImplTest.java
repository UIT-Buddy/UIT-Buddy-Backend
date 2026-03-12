package com.uit.buddy.service.social.impl;

import com.uit.buddy.dto.request.social.SharePostRequest;
import com.uit.buddy.dto.response.social.UserShareResponse;
import com.uit.buddy.dto.response.social.UserSummary;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.social.Share;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.ShareType;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.ShareMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.social.ShareRepository;
import com.uit.buddy.repository.social.projection.ShareProjection;
import com.uit.buddy.repository.user.StudentRepository;
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
class ShareServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private ShareRepository shareRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ShareMapper shareMapper;

    @InjectMocks
    private ShareServiceImpl shareService;

    private String mssv;
    private UUID postId;
    private Post post;
    private Student student;
    private ShareProjection projection;

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

        projection = mock(ShareProjection.class);
        when(projection.getMssv()).thenReturn(mssv);
        when(projection.getFullName()).thenReturn("Test Student");
        when(projection.getSharedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    void shouldSharePostToProfileSuccessfully() {
        SharePostRequest request = new SharePostRequest("Sharing this post");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());
        when(postRepository.save(any(Post.class))).thenReturn(new Post());

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        verify(postRepository).save(any(Post.class)); // For creating shared post
        verify(postRepository).incrementShareCount(postId);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void shouldSharePostToMessageSuccessfully() {
        SharePostRequest request = new SharePostRequest("Sharing via message");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        boolean result = shareService.sharePost(postId, mssv, ShareType.MESSAGE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        verify(postRepository, never()).save(any(Post.class)); // No shared post created for MESSAGE
        verify(postRepository).incrementShareCount(postId);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void shouldNotIncrementShareCountWhenAlreadyShared() {
        SharePostRequest request = new SharePostRequest("Sharing again");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(true);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        verify(postRepository, never()).incrementShareCount(postId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldNotPublishEventWhenSharingOwnPost() {
        Post ownPost = Post.builder()
                .title("Own Post")
                .content("My content")
                .author(student)
                .build();
        ReflectionTestUtils.setField(ownPost, "id", postId);

        SharePostRequest request = new SharePostRequest("Sharing my own post");

        when(postRepository.findById(postId)).thenReturn(Optional.of(ownPost));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, request);

        assertThat(result).isTrue();
        verify(postRepository).incrementShareCount(postId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenPostNotFound() {
        SharePostRequest request = new SharePostRequest("Sharing");
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.sharePost(postId, mssv, ShareType.PROFILE, request))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldThrowExceptionWhenStudentNotFound() {
        SharePostRequest request = new SharePostRequest("Sharing");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.sharePost(postId, mssv, ShareType.PROFILE, request))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldGetPostSharesSuccessfully() {
        UserShareResponse shareResponse = new UserShareResponse(
                new UserSummary(mssv, "Test Student", "avatar.jpg"),
                LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(shareRepository.findSharesWithCursor(eq(postId), any(), any(), eq(11)))
                .thenReturn(List.of(projection));
        when(shareMapper.toShareResponse(projection)).thenReturn(shareResponse);

        List<UserShareResponse> result = shareService.getPostShares(postId, mssv, null, 10);

        assertThat(result).hasSize(1);
        verify(postRepository).findById(postId);
        verify(shareRepository).findSharesWithCursor(eq(postId), any(), any(), eq(11));
    }

    @Test
    void shouldThrowExceptionWhenGettingSharesForNonExistentPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.getPostShares(postId, mssv, null, 10))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldHandleNullShareRequest() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());
        when(postRepository.save(any(Post.class))).thenReturn(new Post());

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, null);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void shouldGetPostSharesWithCursor() {
        String cursor = "encoded_cursor";
        UserShareResponse shareResponse = new UserShareResponse(
                new UserSummary(mssv, "Test Student", "avatar.jpg"),
                LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(shareRepository.findSharesWithCursor(eq(postId), any(), any(), eq(11)))
                .thenReturn(List.of(projection));
        when(shareMapper.toShareResponse(projection)).thenReturn(shareResponse);

        List<UserShareResponse> result = shareService.getPostShares(postId, mssv, cursor, 10);

        assertThat(result).hasSize(1);
        verify(shareRepository).findSharesWithCursor(eq(postId), any(), any(), eq(11));
    }
}