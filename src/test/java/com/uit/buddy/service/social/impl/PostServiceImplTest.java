package com.uit.buddy.service.social.impl;

import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.PostResponse;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService Unit Tests")
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

    // Test data
    private String mssv;
    private Student student;
    private Post post;
    private UUID postId;
    private CreatePostRequest createRequest;
    private UpdatePostRequest updateRequest;
    private PostResponse postResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
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
                .build();
        post.setId(postId);

        createRequest = new CreatePostRequest(
                "New Post Title",
                "New Post Content",
                null,
                null);

        updateRequest = new UpdatePostRequest(
                "Updated Title",
                "Updated Content");

        postResponse = PostResponse.builder()
                .id(postId)
                .mssv(mssv)
                .authorName("Test Student")
                .authorAvatar(null)
                .title("Test Title")
                .content("Test Content")
                .imageUrl(null)
                .videoUrl(null)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    // ============ CREATE POST TESTS ============

    @Test
    @DisplayName("Should create post successfully without media")
    void createPost_WithoutMedia_Success() {
        // Given
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        // When
        PostResponse result = postService.createPost(mssv, createRequest, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Title");

        verify(studentRepository).findById(mssv);
        verify(postRepository, times(2)).save(any(Post.class)); // Save twice
        verify(postMapper).toPostResponse(post);
        verify(cloudinaryService, never()).uploadPostImage(any(), any());
        verify(cloudinaryService, never()).uploadPostVideo(any(), any());
    }

    @Test
    @DisplayName("Should create post with image successfully")
    void createPost_WithImage_Success() {
        // Given
        MultipartFile image = mock(MultipartFile.class);
        when(image.isEmpty()).thenReturn(false);
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(cloudinaryService.uploadPostImage(any(), any()))
                .thenReturn("https://cloudinary.com/image.jpg");
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        // When
        PostResponse result = postService.createPost(mssv, createRequest, image, null);

        // Then
        assertThat(result).isNotNull();
        verify(cloudinaryService).uploadPostImage(eq(image), anyString());
        verify(cloudinaryService, never()).uploadPostVideo(any(), any());
    }

    @Test
    @DisplayName("Should create post with video successfully")
    void createPost_WithVideo_Success() {
        // Given
        MultipartFile video = mock(MultipartFile.class);
        when(video.isEmpty()).thenReturn(false);
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(cloudinaryService.uploadPostVideo(any(), any()))
                .thenReturn("https://cloudinary.com/video.mp4");
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        // When
        PostResponse result = postService.createPost(mssv, createRequest, null, video);

        // Then
        assertThat(result).isNotNull();
        verify(cloudinaryService).uploadPostVideo(eq(video), anyString());
        verify(cloudinaryService, never()).uploadPostImage(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when student not found")
    void createPost_StudentNotFound_ThrowsException() {
        // Given
        when(studentRepository.findById(mssv)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.createPost(mssv, createRequest, null, null))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("Student not found");

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should prioritize video over image when both provided")
    void createPost_WithBothImageAndVideo_PrioritizesVideo() {
        // Given
        MultipartFile image = mock(MultipartFile.class);
        MultipartFile video = mock(MultipartFile.class);
        lenient().when(image.isEmpty()).thenReturn(false);
        when(video.isEmpty()).thenReturn(false);
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(cloudinaryService.uploadPostVideo(any(), any()))
                .thenReturn("https://cloudinary.com/video.mp4");
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        // When
        postService.createPost(mssv, createRequest, image, video);

        // Then
        verify(cloudinaryService).uploadPostVideo(eq(video), anyString());
        verify(cloudinaryService, never()).uploadPostImage(any(), any());
    }

    // ============ UPDATE POST TESTS ============

    @Test
    @DisplayName("Should update post successfully")
    void updatePost_ValidData_Success() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        // When
        PostResponse result = postService.updatePost(postId, mssv, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(post.getTitle()).isEqualTo("Updated Title");
        assertThat(post.getContent()).isEqualTo("Updated Content");

        verify(postRepository).findById(postId);
        verify(postMapper).toPostResponse(post);
    }

    @Test
    @DisplayName("Should update only title when content is null")
    void updatePost_OnlyTitle_Success() {
        // Given
        UpdatePostRequest titleOnlyRequest = new UpdatePostRequest("New Title", null);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        // When
        postService.updatePost(postId, mssv, titleOnlyRequest);

        // Then
        assertThat(post.getTitle()).isEqualTo("New Title");
        assertThat(post.getContent()).isEqualTo("Test Content"); // Unchanged
    }

    @Test
    @DisplayName("Should update only content when title is null")
    void updatePost_OnlyContent_Success() {
        // Given
        UpdatePostRequest contentOnlyRequest = new UpdatePostRequest(null, "New Content");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        // When
        postService.updatePost(postId, mssv, contentOnlyRequest);

        // Then
        assertThat(post.getTitle()).isEqualTo("Test Title"); // Unchanged
        assertThat(post.getContent()).isEqualTo("New Content");
    }

    @Test
    @DisplayName("Should not update title when it's blank")
    void updatePost_BlankTitle_NotUpdated() {
        // Given
        UpdatePostRequest blankTitleRequest = new UpdatePostRequest("   ", "New Content");
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMapper.toPostResponse(post)).thenReturn(postResponse);

        // When
        postService.updatePost(postId, mssv, blankTitleRequest);

        // Then
        assertThat(post.getTitle()).isEqualTo("Test Title"); // Unchanged
        assertThat(post.getContent()).isEqualTo("New Content");
    }

    @Test
    @DisplayName("Should throw exception when post not found")
    void updatePost_PostNotFound_ThrowsException() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.updatePost(postId, mssv, updateRequest))
                .isInstanceOf(SocialException.class)
                .hasMessageContaining("Post not found");
    }

    @Test
    @DisplayName("Should throw exception when user is not the owner")
    void updatePost_NotOwner_ThrowsException() {
        // Given
        String differentMssv = "21520999";
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> postService.updatePost(postId, differentMssv, updateRequest))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("permission");

        verify(postMapper, never()).toPostResponse(any());
    }

    // ============ DELETE POST TESTS ============

    @Test
    @DisplayName("Should delete post successfully")
    void deletePost_ValidData_Success() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doNothing().when(cloudinaryService).deletePostMedia(anyString());
        doNothing().when(postRepository).delete(post);

        // When
        postService.deletePost(postId, mssv);

        // Then
        verify(postRepository).findById(postId);
        verify(cloudinaryService).deletePostMedia(postId.toString());
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("Should throw exception when post not found for deletion")
    void deletePost_PostNotFound_ThrowsException() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.deletePost(postId, mssv))
                .isInstanceOf(SocialException.class)
                .hasMessageContaining("Post not found");

        verify(cloudinaryService, never()).deletePostMedia(any());
        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when user is not the owner for deletion")
    void deletePost_NotOwner_ThrowsException() {
        // Given
        String differentMssv = "21520999";
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> postService.deletePost(postId, differentMssv))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("permission");

        verify(cloudinaryService, never()).deletePostMedia(any());
        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should delete post even if cloudinary deletion fails")
    void deletePost_CloudinaryFails_StillDeletesPost() {
        // Given
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        doThrow(new RuntimeException("Cloudinary error"))
                .when(cloudinaryService).deletePostMedia(anyString());

        // When & Then
        assertThatThrownBy(() -> postService.deletePost(postId, mssv))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cloudinary error");

        verify(cloudinaryService).deletePostMedia(postId.toString());
        verify(postRepository, never()).delete(any()); // Transaction rollback
    }
}
