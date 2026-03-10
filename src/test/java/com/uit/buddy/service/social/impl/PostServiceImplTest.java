package com.uit.buddy.service.social.impl;

import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
<<<<<<< HEAD
import com.uit.buddy.dto.response.social.PostDetailResponse;
=======
>>>>>>> 26e636de76530b8da91ada8cd20a4aa0fb8dcd35
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.social.PostMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.cloudinary.CloudinaryService;
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
import org.springframework.mock.web.MockMultipartFile;

<<<<<<< HEAD
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
=======
>>>>>>> 26e636de76530b8da91ada8cd20a4aa0fb8dcd35
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

        @BeforeEach
        void setUp() {
                mssv = "21520001";
                postId = UUID.randomUUID();

                student = Student.builder()
                                .mssv(mssv)
                                .fullName("Test Student")
                                .build();

                post = Post.builder()
                                .title("Test Title")
                                .content("Test Content")
                                .author(student)
                                .likeCount(0L)
                                .commentCount(0L)
                                .shareCount(0L)
                                .build();
                // Set id manually since it's auto-generated
                post.setId(postId);
        }

        @Nested
        @DisplayName("Create Post Tests")
        class CreatePostTests {

                private CreatePostRequest createRequest;
                private PostDetailResponse postDetailResponse;

                @BeforeEach
                void setUp() {
                        createRequest = new CreatePostRequest(
                                        "Test Title",
                                        "Test Content",
                                        null,
                                        null);

                        postDetailResponse = new PostDetailResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content",
                                        null,
                                        null,
                                        new PostDetailResponse.AuthorInfo(mssv, "Test Student", null),
                                        0L,
                                        0L,
                                        0L,
                                        LocalDateTime.now(),
                                        LocalDateTime.now());
                }

                @Test
                @DisplayName("Should create post without media successfully")
                void shouldCreatePostWithoutMedia() {
                        // Given
                        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
                        when(postRepository.save(any(Post.class))).thenReturn(post);
                        when(postMapper.toPostDetailResponse(post)).thenReturn(postDetailResponse);

                        // When
                        PostDetailResponse result = postService.createPost(mssv, createRequest, null, null);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.title()).isEqualTo("Test Title");
                        assertThat(result.content()).isEqualTo("Test Content");

                        verify(studentRepository).findById(mssv);
                        verify(postRepository, times(2)).save(any(Post.class));
                        verify(postMapper).toPostDetailResponse(post);
                        verify(cloudinaryService, never()).uploadPostImage(any(), any());
                        verify(cloudinaryService, never()).uploadPostVideo(any(), any());
                }

                @Test
                @DisplayName("Should create post with image successfully")
                void shouldCreatePostWithImage() {
                        // Given
                        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg",
                                        "test".getBytes());
                        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
                        when(postRepository.save(any(Post.class))).thenReturn(post);
                        when(cloudinaryService.uploadPostImage(any(), any()))
                                        .thenReturn("https://cloudinary.com/image.jpg");
                        when(postMapper.toPostDetailResponse(post)).thenReturn(postDetailResponse);

                        // When
                        PostDetailResponse result = postService.createPost(mssv, createRequest, image, null);

                        // Then
                        assertThat(result).isNotNull();
                        verify(cloudinaryService).uploadPostImage(image, postId.toString());
                        verify(cloudinaryService, never()).uploadPostVideo(any(), any());
                }

                @Test
                @DisplayName("Should throw exception when student not found")
                void shouldThrowExceptionWhenStudentNotFound() {
                        // Given
                        when(studentRepository.findById(mssv)).thenReturn(Optional.empty());

                        // When & Then
                        assertThatThrownBy(() -> postService.createPost(mssv, createRequest, null, null))
                                        .isInstanceOf(UserException.class)
                                        .hasMessageContaining("Student not found");

                        verify(postRepository, never()).save(any());
                }
        }

        @Nested
        @DisplayName("Update Post Tests")
        class UpdatePostTests {

                private UpdatePostRequest updateRequest;
                private PostDetailResponse postDetailResponse;

                @BeforeEach
                void setUp() {
                        updateRequest = new UpdatePostRequest("Updated Title", "Updated Content");
                        postDetailResponse = new PostDetailResponse(
                                        postId,
                                        "Updated Title",
                                        "Updated Content",
                                        null,
                                        null,
                                        new PostDetailResponse.AuthorInfo(mssv, "Test Student", null),
                                        0L,
                                        0L,
                                        0L,
                                        LocalDateTime.now(),
                                        LocalDateTime.now());
                }

                @Test
                @DisplayName("Should update post successfully")
                void shouldUpdatePostSuccessfully() {
                        // Given
                        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                        when(postMapper.toPostDetailResponse(post)).thenReturn(postDetailResponse);

                        // When
                        PostDetailResponse result = postService.updatePost(postId, mssv, updateRequest);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.title()).isEqualTo("Updated Title");
                        assertThat(result.content()).isEqualTo("Updated Content");

                        verify(postRepository).findById(postId);
                        verify(postMapper).toPostDetailResponse(post);
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

                        verify(postMapper, never()).toPostDetailResponse(any());
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
        }

        @Nested
        @DisplayName("Get Post Feed Tests")
        class GetPostFeedTests {

                @Test
                @DisplayName("Should get first page of post feed successfully")
                void shouldGetFirstPageSuccessfully() {
                        // Given
                        int limit = 10;
                        List<Post> posts = Arrays.asList(post, post, post);
                        PostFeedResponse feedResponse = createFeedResponse(postId);

                        when(postRepository.findFirstPage(limit + 1)).thenReturn(posts);
                        when(postMapper.toPostFeedResponse(any(Post.class))).thenReturn(feedResponse);

                        // When
                        List<PostFeedResponse> result = postService.getPostFeed(null, limit);

                        // Then
                        assertThat(result).hasSize(3);
                        verify(postRepository).findFirstPage(limit + 1);
                        verify(postRepository, never()).findNextPage(any(), any(), anyInt());
                }

                private PostFeedResponse createFeedResponse(UUID id) {
                        return new PostFeedResponse(
                                        id,
                                        "Test Title",
                                        "Test Content Snippet",
                                        null,
                                        null,
                                        new PostFeedResponse.AuthorInfo(mssv, "Test Student", null),
                                        0L,
                                        0L,
                                        0L,
                                        LocalDateTime.now());
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
                                        null,
                                        null,
                                        new PostDetailResponse.AuthorInfo(mssv, "Test Student", null),
                                        0L,
                                        0L,
                                        0L,
                                        LocalDateTime.now(),
                                        LocalDateTime.now());

                        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                        when(postMapper.toPostDetailResponse(post)).thenReturn(detailResponse);

                        // When
                        PostDetailResponse result = postService.getPostDetail(postId);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.id()).isEqualTo(postId);
                        assertThat(result.title()).isEqualTo("Test Title");
                        assertThat(result.content()).isEqualTo("Test Content");

                        verify(postRepository).findById(postId);
                        verify(postMapper).toPostDetailResponse(post);
                }

                @Test
                @DisplayName("Should throw exception when post not found")
                void shouldThrowExceptionWhenPostNotFound() {
                        // Given
                        when(postRepository.findById(postId)).thenReturn(Optional.empty());

                        // When & Then
                        assertThatThrownBy(() -> postService.getPostDetail(postId))
                                        .isInstanceOf(SocialException.class)
                                        .hasMessageContaining("Post not found");

                        verify(postMapper, never()).toPostDetailResponse(any());
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
                        List<UUID> foundIds = Arrays.asList(postId, UUID.randomUUID());
                        Page<Post> postPage = new PageImpl<>(Arrays.asList(post, post));
                        PostFeedResponse feedResponse = new PostFeedResponse(
                                        postId,
                                        "Test Title",
                                        "Test Content",
                                        null,
                                        null,
                                        new PostFeedResponse.AuthorInfo(mssv, "Test Student", null),
                                        0L,
                                        0L,
                                        0L,
                                        LocalDateTime.now());

                        when(postRepository.searchPostByKeyword(keyword)).thenReturn(foundIds);
                        when(postRepository.findAll(foundIds, pageable)).thenReturn(postPage);
                        when(postMapper.toPostFeedResponse(any(Post.class))).thenReturn(feedResponse);

                        // When
                        Page<PostFeedResponse> result = postService.searchPost(keyword, pageable);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).hasSize(2);

                        verify(postRepository).searchPostByKeyword(keyword);
                        verify(postRepository).findAll(foundIds, pageable);
                }

                @Test
                @DisplayName("Should return empty page when no posts found")
                void shouldReturnEmptyPageWhenNoPostsFound() {
                        // Given
                        String keyword = "nonexistent";
                        List<UUID> emptyIds = Collections.emptyList();

                        when(postRepository.searchPostByKeyword(keyword)).thenReturn(emptyIds);

                        // When
                        Page<PostFeedResponse> result = postService.searchPost(keyword, pageable);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getContent()).isEmpty();
                        assertThat(result.getTotalElements()).isEqualTo(0);

                        verify(postRepository).searchPostByKeyword(keyword);
                        verify(postRepository, never()).findAll(any(List.class), any(Pageable.class));
                }
        }
}
