package com.uit.buddy.service.social.impl;

import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.AuthorInfo;
import com.uit.buddy.dto.response.social.MediaResponse;
import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.PostMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import com.uit.buddy.util.CursorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostServiceImpl Tests")
class PostServiceImplTest {

        @Mock
        private PostRepository postRepository;

        @Mock
        private StudentRepository studentRepository;

        @Mock
        private PostMapper postMapper;

        @Mock
        private CloudinaryService cloudinaryService;

        @InjectMocks
        private PostServiceImpl postService;

        private String mssv;
        private UUID postId;
        private Student student;
        private Post post;
        private PostRepository.PostFeedProjection mockProjection;

        @BeforeEach
        void setUp() {
                mssv = "21520001";
                postId = UUID.randomUUID();

                student = Student.builder()
                                .mssv(mssv)
                                .fullName("Test Student")
                                .avatarUrl("https://example.com/avatar.jpg")
                                .homeClassCode("21KTPM1")
                                .build();

                post = Post.builder()
                                .title("Test Title")
                                .content("Test Content")
                                .author(student)
                                .likeCount(5L)
                                .commentCount(3L)
                                .shareCount(2L)
                                .build();
                post.setId(postId);

                // Mock projection
                mockProjection = mock(PostRepository.PostFeedProjection.class);
                when(mockProjection.getId()).thenReturn(postId);
                when(mockProjection.getTitle()).thenReturn("Test Title");
                when(mockProjection.getContent()).thenReturn("Test Content");
                when(mockProjection.getMedias()).thenReturn("[]");
                when(mockProjection.getLikeCount()).thenReturn(5L);
                when(mockProjection.getCommentCount()).thenReturn(3L);
                when(mockProjection.getShareCount()).thenReturn(2L);
                when(mockProjection.getCreatedAt()).thenReturn(LocalDateTime.now());
                when(mockProjection.getUpdatedAt()).thenReturn(LocalDateTime.now());
                when(mockProjection.getAuthorMssv()).thenReturn(mssv);
                when(mockProjection.getAuthorFullName()).thenReturn("Test Student");
                when(mockProjection.getAuthorAvatarUrl()).thenReturn("https://example.com/avatar.jpg");
                when(mockProjection.getAuthorHomeClassCode()).thenReturn("21KTPM1");
                when(mockProjection.getIsLiked()).thenReturn(false);
                when(mockProjection.getIsShared()).thenReturn(false);
        }

        @Nested
        @DisplayName("Get Post Feed Tests")
        class GetPostFeedTests {

                @Test
                @DisplayName("Should get post feed without cursor successfully")
                void shouldGetPostFeedWithoutCursor() {
                        // Given
                        String cursor = null;
                        int limit = 10;
                        List<PostRepository.PostFeedProjection> projections = Arrays.asList(mockProjection,
                                        mockProjection);

                        PostFeedResponse feedResponse = new PostFeedResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content Snippet",
                                        Collections.emptyList(),
                                        new AuthorInfo(mssv, "Test Student", "https://example.com/avatar.jpg",
                                                        "21KTPM1"),
                                        5L,
                                        3L,
                                        2L,
                                        false,
                                        false,
                                        LocalDateTime.now());

                        when(postRepository.findFeed(eq(mssv), eq(null), eq(null), eq(limit)))
                                        .thenReturn(projections);
                        when(postMapper.toPostFeedResponse(any(PostRepository.PostFeedProjection.class)))
                                        .thenReturn(feedResponse);

                        // When
                        List<PostFeedResponse> result = postService.getPostFeed(mssv, cursor, limit);

                        // Then
                        assertThat(result).hasSize(2);
                        assertThat(result.get(0).id()).isEqualTo(postId);
                        assertThat(result.get(0).title()).isEqualTo("Test Title");
                        assertThat(result.get(0).likeCount()).isEqualTo(5L);

                        verify(postRepository).findFeed(mssv, null, null, limit);
                        verify(postMapper, times(2)).toPostFeedResponse(any(PostRepository.PostFeedProjection.class));
                }

                @Test
                @DisplayName("Should get post feed with cursor successfully")
                void shouldGetPostFeedWithCursor() {
                        // Given
                        LocalDateTime cursorTime = LocalDateTime.now().minusHours(1);
                        UUID cursorId = UUID.randomUUID();
                        String cursor = CursorUtils.encode(cursorTime, cursorId);
                        int limit = 5;

                        List<PostRepository.PostFeedProjection> projections = Arrays.asList(mockProjection);
                        PostFeedResponse feedResponse = new PostFeedResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content Snippet",
                                        Collections.emptyList(),
                                        new AuthorInfo(mssv, "Test Student", "https://example.com/avatar.jpg",
                                                        "21KTPM1"),
                                        5L,
                                        3L,
                                        2L,
                                        false,
                                        false,
                                        LocalDateTime.now());

                        when(postRepository.findFeed(eq(mssv), eq(cursorTime), eq(cursorId), eq(limit)))
                                        .thenReturn(projections);
                        when(postMapper.toPostFeedResponse(any(PostRepository.PostFeedProjection.class)))
                                        .thenReturn(feedResponse);

                        // When
                        List<PostFeedResponse> result = postService.getPostFeed(mssv, cursor, limit);

                        // Then
                        assertThat(result).hasSize(1);
                        assertThat(result.get(0).id()).isEqualTo(postId);

                        verify(postRepository).findFeed(mssv, cursorTime, cursorId, limit);
                        verify(postMapper).toPostFeedResponse(any(PostRepository.PostFeedProjection.class));
                }

                @Test
                @DisplayName("Should return empty list when no posts found")
                void shouldReturnEmptyListWhenNoPostsFound() {
                        // Given
                        String cursor = null;
                        int limit = 10;
                        List<PostRepository.PostFeedProjection> emptyProjections = Collections.emptyList();

                        when(postRepository.findFeed(eq(mssv), eq(null), eq(null), eq(limit)))
                                        .thenReturn(emptyProjections);

                        // When
                        List<PostFeedResponse> result = postService.getPostFeed(mssv, cursor, limit);

                        // Then
                        assertThat(result).isEmpty();

                        verify(postRepository).findFeed(mssv, null, null, limit);
                        verify(postMapper, never()).toPostFeedResponse(any());
                }

                @Test
                @DisplayName("Should handle invalid cursor gracefully")
                void shouldHandleInvalidCursorGracefully() {
                        // Given
                        String invalidCursor = "invalid-cursor";
                        int limit = 10;
                        List<PostRepository.PostFeedProjection> projections = Arrays.asList(mockProjection);

                        PostFeedResponse feedResponse = new PostFeedResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content Snippet",
                                        Collections.emptyList(),
                                        new AuthorInfo(mssv, "Test Student", "https://example.com/avatar.jpg",
                                                        "21KTPM1"),
                                        5L,
                                        3L,
                                        2L,
                                        false,
                                        false,
                                        LocalDateTime.now());

                        when(postRepository.findFeed(eq(mssv), eq(null), eq(null), eq(limit)))
                                        .thenReturn(projections);
                        when(postMapper.toPostFeedResponse(any(PostRepository.PostFeedProjection.class)))
                                        .thenReturn(feedResponse);

                        // When
                        List<PostFeedResponse> result = postService.getPostFeed(mssv, invalidCursor, limit);

                        // Then
                        assertThat(result).hasSize(1);

                        // Should fallback to null cursor when invalid
                        verify(postRepository).findFeed(mssv, null, null, limit);
                }
        }

        @Nested
        @DisplayName("Get Post Detail Tests")
        class GetPostDetailTests {

                @Test
                @DisplayName("Should get post detail successfully")
                void shouldGetPostDetailSuccessfully() {
                        // Given
                        PostDetailResponse detailResponse = new PostDetailResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content",
                                        Collections.emptyList(),
                                        new AuthorInfo(mssv, "Test Student", "https://example.com/avatar.jpg",
                                                        "21KTPM1"),
                                        5L,
                                        2L,
                                        3L,
                                        false,
                                        false,
                                        LocalDateTime.now(),
                                        LocalDateTime.now());

                        when(postRepository.findDetailWithStatus(postId, mssv)).thenReturn(Optional.of(mockProjection));
                        when(postMapper.toPostDetailResponseFromProjection(mockProjection)).thenReturn(detailResponse);

                        // When
                        PostDetailResponse result = postService.getPostDetail(postId, mssv);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.id()).isEqualTo(postId);
                        assertThat(result.title()).isEqualTo("Test Title");
                        assertThat(result.content()).isEqualTo("Test Content");
                        assertThat(result.likeCount()).isEqualTo(5L);
                        assertThat(result.commentCount()).isEqualTo(3L);
                        assertThat(result.shareCount()).isEqualTo(2L);

                        verify(postRepository).findDetailWithStatus(postId, mssv);
                        verify(postMapper).toPostDetailResponseFromProjection(mockProjection);
                }

                @Test
                @DisplayName("Should throw exception when post not found")
                void shouldThrowExceptionWhenPostNotFound() {
                        // Given
                        when(postRepository.findDetailWithStatus(postId, mssv)).thenReturn(Optional.empty());

                        // When & Then
                        assertThatThrownBy(() -> postService.getPostDetail(postId, mssv))
                                        .isInstanceOf(SocialException.class)
                                        .hasMessageContaining("Post not found");

                        verify(postRepository).findDetailWithStatus(postId, mssv);
                        verify(postMapper, never()).toPostDetailResponseFromProjection(any());
                }

                @Test
                @DisplayName("Should get post detail with media successfully")
                void shouldGetPostDetailWithMediaSuccessfully() {
                        // Given
                        String mediasJson = "[{\"type\":\"image\",\"url\":\"https://example.com/image.jpg\"}]";
                        when(mockProjection.getMedias()).thenReturn(mediasJson);

                        List<MediaResponse> medias = Arrays.asList(
                                        new MediaResponse("image", "https://example.com/image.jpg"));

                        PostDetailResponse detailResponse = new PostDetailResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content",
                                        medias,
                                        new AuthorInfo(mssv, "Test Student", "https://example.com/avatar.jpg",
                                                        "21KTPM1"),
                                        5L,
                                        2L,
                                        3L,
                                        false,
                                        false,
                                        LocalDateTime.now(),
                                        LocalDateTime.now());

                        when(postRepository.findDetailWithStatus(postId, mssv)).thenReturn(Optional.of(mockProjection));
                        when(postMapper.toPostDetailResponseFromProjection(mockProjection)).thenReturn(detailResponse);

                        // When
                        PostDetailResponse result = postService.getPostDetail(postId, mssv);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.medias()).hasSize(1);
                        assertThat(result.medias().get(0).type()).isEqualTo("image");
                        assertThat(result.medias().get(0).url()).isEqualTo("https://example.com/image.jpg");

                        verify(postRepository).findDetailWithStatus(postId, mssv);
                        verify(postMapper).toPostDetailResponseFromProjection(mockProjection);
                }
        }

        @Nested
        @DisplayName("Search Post Tests")
        class SearchPostTests {

                private Pageable pageable;

                @BeforeEach
                void setUp() {
                        pageable = PageRequest.of(0, 15);
                }

                @Test
                @DisplayName("Should search posts with keyword successfully")
                void shouldSearchPostsWithKeyword() {
                        // Given
                        String keyword = "test";
                        List<PostRepository.PostFeedProjection> projections = Arrays.asList(mockProjection,
                                        mockProjection);
                        Page<PostRepository.PostFeedProjection> projectionsPage = new PageImpl<>(projections, pageable,
                                        2);

                        PostFeedResponse feedResponse = new PostFeedResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content Snippet",
                                        Collections.emptyList(),
                                        new AuthorInfo(mssv, "Test Student", "https://example.com/avatar.jpg",
                                                        "21KTPM1"),
                                        5L,
                                        3L,
                                        2L,
                                        false,
                                        false,
                                        LocalDateTime.now());

                        when(postRepository.searchPostFull(keyword, mssv, pageable)).thenReturn(projectionsPage);
                        when(postMapper.toPostFeedResponse(any(PostRepository.PostFeedProjection.class)))
                                        .thenReturn(feedResponse);

                        // When
                        Page<PostFeedResponse> result = postService.searchPost(keyword, mssv, pageable);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).hasSize(2);
                        assertThat(result.getTotalElements()).isEqualTo(2);
                        assertThat(result.getContent().get(0).id()).isEqualTo(postId);
                        assertThat(result.getContent().get(0).title()).isEqualTo("Test Title");

                        verify(postRepository).searchPostFull(keyword, mssv, pageable);
                        verify(postMapper, times(2)).toPostFeedResponse(any(PostRepository.PostFeedProjection.class));
                }

                @Test
                @DisplayName("Should return empty page when keyword is null")
                void shouldReturnEmptyPageWhenKeywordIsNull() {
                        // Given
                        String keyword = null;

                        // When
                        Page<PostFeedResponse> result = postService.searchPost(keyword, mssv, pageable);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).isEmpty();
                        assertThat(result.getTotalElements()).isEqualTo(0);

                        verify(postRepository, never()).searchPostFull(anyString(), anyString(), any(Pageable.class));
                        verify(postMapper, never()).toPostFeedResponse(any());
                }

                @Test
                @DisplayName("Should return empty page when keyword is blank")
                void shouldReturnEmptyPageWhenKeywordIsBlank() {
                        // Given
                        String keyword = "   ";

                        // When
                        Page<PostFeedResponse> result = postService.searchPost(keyword, mssv, pageable);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).isEmpty();
                        assertThat(result.getTotalElements()).isEqualTo(0);

                        verify(postRepository, never()).searchPostFull(anyString(), anyString(), any(Pageable.class));
                        verify(postMapper, never()).toPostFeedResponse(any());
                }

                @Test
                @DisplayName("Should return empty page when no posts found")
                void shouldReturnEmptyPageWhenNoPostsFound() {
                        // Given
                        String keyword = "nonexistent";
                        Page<PostRepository.PostFeedProjection> emptyPage = new PageImpl<>(Collections.emptyList(),
                                        pageable, 0);

                        when(postRepository.searchPostFull(keyword, mssv, pageable)).thenReturn(emptyPage);

                        // When
                        Page<PostFeedResponse> result = postService.searchPost(keyword, mssv, pageable);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).isEmpty();
                        assertThat(result.getTotalElements()).isEqualTo(0);

                        verify(postRepository).searchPostFull(keyword, mssv, pageable);
                        verify(postMapper, never()).toPostFeedResponse(any());
                }

                @Test
                @DisplayName("Should search posts with special characters in keyword")
                void shouldSearchPostsWithSpecialCharacters() {
                        // Given
                        String keyword = "test@#$%";
                        List<PostRepository.PostFeedProjection> projections = Arrays.asList(mockProjection);
                        Page<PostRepository.PostFeedProjection> projectionsPage = new PageImpl<>(projections, pageable,
                                        1);

                        PostFeedResponse feedResponse = new PostFeedResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content Snippet",
                                        Collections.emptyList(),
                                        new AuthorInfo(mssv, "Test Student", "https://example.com/avatar.jpg",
                                                        "21KTPM1"),
                                        5L,
                                        3L,
                                        2L,
                                        false,
                                        false,
                                        LocalDateTime.now());

                        when(postRepository.searchPostFull(keyword, mssv, pageable)).thenReturn(projectionsPage);
                        when(postMapper.toPostFeedResponse(any(PostRepository.PostFeedProjection.class)))
                                        .thenReturn(feedResponse);

                        // When
                        Page<PostFeedResponse> result = postService.searchPost(keyword, mssv, pageable);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).hasSize(1);

                        verify(postRepository).searchPostFull(keyword, mssv, pageable);
                        verify(postMapper).toPostFeedResponse(any(PostRepository.PostFeedProjection.class));
                }
        }

        @Nested
        @DisplayName("Update Post Tests")
        class UpdatePostTests {

                private UpdatePostRequest updateRequest;

                @BeforeEach
                void setUp() {
                        updateRequest = new UpdatePostRequest("Updated Title", "Updated Content");
                }

                @Test
                @DisplayName("Should update post successfully")
                void shouldUpdatePostSuccessfully() {
                        // Given
                        PostDetailResponse detailResponse = new PostDetailResponse(
                                        postId,
                                        "Updated Title",
                                        "Updated Content",
                                        Collections.emptyList(),
                                        new AuthorInfo(mssv, "Test Student", "https://example.com/avatar.jpg",
                                                        "21KTPM1"),
                                        5L,
                                        2L,
                                        3L,
                                        false,
                                        false,
                                        LocalDateTime.now(),
                                        LocalDateTime.now());

                        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                        when(postRepository.save(post)).thenReturn(post);
                        when(postRepository.findDetailWithStatus(postId, mssv)).thenReturn(Optional.of(mockProjection));
                        when(postMapper.toPostDetailResponseFromProjection(mockProjection)).thenReturn(detailResponse);

                        // When
                        PostDetailResponse result = postService.updatePost(postId, mssv, updateRequest);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.title()).isEqualTo("Updated Title");
                        assertThat(result.content()).isEqualTo("Updated Content");

                        verify(postRepository).findById(postId);
                        verify(postRepository).save(post);
                        verify(postRepository).findDetailWithStatus(postId, mssv);
                        verify(postMapper).toPostDetailResponseFromProjection(mockProjection);
                }

                @Test
                @DisplayName("Should throw exception when post not found")
                void shouldThrowExceptionWhenPostNotFound() {
                        // Given
                        when(postRepository.findById(postId)).thenReturn(Optional.empty());

                        // When & Then
                        assertThatThrownBy(() -> postService.updatePost(postId, mssv, updateRequest))
                                        .isInstanceOf(SocialException.class)
                                        .hasMessageContaining("Post not found");

                        verify(postRepository, never()).save(any());
                }

                @Test
                @DisplayName("Should throw exception when user is not the owner")
                void shouldThrowExceptionWhenNotOwner() {
                        // Given
                        String otherMssv = "21520002";
                        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

                        // When & Then
                        assertThatThrownBy(() -> postService.updatePost(postId, otherMssv, updateRequest))
                                        .isInstanceOf(AuthException.class)
                                        .hasMessageContaining("permission");

                        verify(postRepository, never()).save(any());
                }
        }

        @Nested
        @DisplayName("Delete Post Tests")
        class DeletePostTests {

                @Test
                @DisplayName("Should delete post successfully")
                void shouldDeletePostSuccessfully() {
                        // Given
                        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                        doNothing().when(cloudinaryService).deletePostMedia(postId.toString());
                        doNothing().when(postRepository).delete(post);

                        // When
                        postService.deletePost(postId, mssv);

                        // Then
                        verify(postRepository).findById(postId);
                        verify(cloudinaryService).deletePostMedia(postId.toString());
                        verify(postRepository).delete(post);
                }

                @Test
                @DisplayName("Should throw exception when post not found")
                void shouldThrowExceptionWhenPostNotFound() {
                        // Given
                        when(postRepository.findById(postId)).thenReturn(Optional.empty());

                        // When & Then
                        assertThatThrownBy(() -> postService.deletePost(postId, mssv))
                                        .isInstanceOf(SocialException.class)
                                        .hasMessageContaining("Post not found");

                        verify(postRepository, never()).delete(any());
                }

                @Test
                @DisplayName("Should throw exception when user is not the owner")
                void shouldThrowExceptionWhenNotOwner() {
                        // Given
                        String otherMssv = "21520002";
                        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

                        // When & Then
                        assertThatThrownBy(() -> postService.deletePost(postId, otherMssv))
                                        .isInstanceOf(AuthException.class)
                                        .hasMessageContaining("permission");

                        verify(postRepository, never()).delete(any());
                        verify(cloudinaryService, never()).deletePostMedia(any());
                }
        }
}
