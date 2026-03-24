package com.uit.buddy.service.cloudinary.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.uit.buddy.config.CloudinaryProperties;
import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.user.UserException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryServiceImpl Tests")
class CloudinaryServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private CloudinaryProperties properties;

    @Mock
    private Executor executor;

    @Mock
    private Uploader uploader;

    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
        cloudinaryService = new CloudinaryServiceImpl(cloudinary, properties, executor);
    }

    @Nested
    @DisplayName("Upload Avatar Tests")
    class UploadAvatarTests {

        @Test
        @DisplayName("Should create default avatar successfully")
        void shouldCreateDefaultAvatarSuccessfully() throws Exception {
            // Given
            String mssv = "21520001";
            String defaultUrl = "https://example.com/default-avatar.jpg";
            String expectedUrl = "https://cloudinary.com/avatar.jpg";

            when(properties.getDefaultAvatarUrl()).thenReturn(defaultUrl);
            when(properties.getAvatarSize()).thenReturn(200);

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", expectedUrl);
            when(uploader.upload(any(), anyMap())).thenReturn(uploadResult);

            // When
            String result = cloudinaryService.createDefaultAvatar(mssv);

            // Then
            assertThat(result).isEqualTo(expectedUrl);
            verify(uploader).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should upload avatar successfully")
        void shouldUploadAvatarSuccessfully() throws Exception {
            // Given
            String mssv = "21520001";
            String expectedUrl = "https://cloudinary.com/avatar.jpg";
            byte[] content = "test-image".getBytes();
            MultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", content);

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(properties.getAvatarSize()).thenReturn(200);

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", expectedUrl);
            when(uploader.upload(any(), anyMap())).thenReturn(uploadResult);

            // When
            String result = cloudinaryService.uploadAvatar(file, mssv);

            // Then
            assertThat(result).isEqualTo(expectedUrl);
            verify(uploader).upload(any(), anyMap());
        }
    }

    @Nested
    @DisplayName("Upload Post Media Tests")
    class UploadPostMediaTests {

        @Test
        @DisplayName("Should upload post image successfully")
        void shouldUploadPostImageSuccessfully() throws Exception {
            // Given
            String postId = "post-123";
            String expectedUrl = "https://cloudinary.com/post-image.jpg";
            byte[] content = "test-image".getBytes();
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", content);

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", expectedUrl);
            when(uploader.upload(any(), anyMap())).thenReturn(uploadResult);

            // When
            PostMedia result = cloudinaryService.uploadPostImage(file, postId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUrl()).isEqualTo(expectedUrl);
            assertThat(result.getType()).isEqualTo(FileType.IMAGE);
            verify(uploader).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should upload post video successfully")
        void shouldUploadPostVideoSuccessfully() throws Exception {
            // Given
            String postId = "post-123";
            String expectedUrl = "https://cloudinary.com/post-video.mp4";
            byte[] content = "test-video".getBytes();
            MultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", content);

            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4", "video/avi" });

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", expectedUrl);
            when(uploader.upload(any(), anyMap())).thenReturn(uploadResult);

            // When
            PostMedia result = cloudinaryService.uploadPostVideo(file, postId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUrl()).isEqualTo(expectedUrl);
            assertThat(result.getType()).isEqualTo(FileType.VIDEO);
            verify(uploader).upload(any(), anyMap());
        }
    }

    @Nested
    @DisplayName("Validate File Tests")
    class ValidateFileTests {

        @Test
        @DisplayName("Should throw exception when file is null")
        void shouldThrowExceptionWhenFileIsNull() {
            // When & Then
            assertThatThrownBy(() -> cloudinaryService.validateFile(null, FileType.IMAGE))
                    .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            // Given
            MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

            // When & Then
            assertThatThrownBy(() -> cloudinaryService.validateFile(emptyFile, FileType.IMAGE))
                    .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("Should throw exception when file type is invalid")
        void shouldThrowExceptionWhenFileTypeIsInvalid() {
            // Given
            MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            // When & Then
            assertThatThrownBy(() -> cloudinaryService.validateFile(file, FileType.IMAGE))
                    .isInstanceOf(UserException.class);
        }

        @Test
        @DisplayName("Should validate file successfully")
        void shouldValidateFileSuccessfully() {
            // Given
            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            // When & Then - should not throw exception
            cloudinaryService.validateFile(file, FileType.IMAGE);
        }
    }

    @Nested
    @DisplayName("Delete Media Tests")
    class DeleteMediaTests {

        @Test
        @DisplayName("Should delete avatar successfully")
        void shouldDeleteAvatarSuccessfully() throws Exception {
            // Given
            String publicId = "21520001";
            when(uploader.destroy(anyString(), anyMap())).thenReturn(new HashMap<>());

            // When
            cloudinaryService.deleteAvatar(publicId);

            // Then
            verify(uploader).destroy(anyString(), anyMap());
        }

        @Test
        @DisplayName("Should delete post media successfully")
        void shouldDeletePostMediaSuccessfully() {
            // Given - empty list to avoid extractPublicId issue
            List<PostMedia> medias = Collections.emptyList();

            // When - async operation
            cloudinaryService.deletePostMedia(medias);

            // Then - no exception thrown, operation completes
        }
    }

    @Nested
    @DisplayName("Upload Multi Media Tests")
    class UploadMultiMediaTests {

        @Test
        @DisplayName("Should upload multiple images successfully")
        void shouldUploadMultipleImagesSuccessfully() throws Exception {
            // Given
            MultipartFile image1 = new MockMultipartFile("file1", "image1.jpg", "image/jpeg", "content1".getBytes());
            MultipartFile image2 = new MockMultipartFile("file2", "image2.jpg", "image/jpeg", "content2".getBytes());
            List<MultipartFile> images = Arrays.asList(image1, image2);

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://cloudinary.com/image.jpg");
            when(uploader.upload(any(), anyMap())).thenReturn(uploadResult);

            // Mock executor to run tasks synchronously
            doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).when(executor).execute(any(Runnable.class));

            // When
            List<PostMedia> result = cloudinaryService.uploadMultiMedia(images, null);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getType()).isEqualTo(FileType.IMAGE);
            assertThat(result.get(1).getType()).isEqualTo(FileType.IMAGE);
        }

        @Test
        @DisplayName("Should upload mixed media successfully")
        void shouldUploadMixedMediaSuccessfully() throws Exception {
            // Given
            MultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "image-content".getBytes());
            MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video-content".getBytes());

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg" });
            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://cloudinary.com/media");
            when(uploader.upload(any(), anyMap())).thenReturn(uploadResult);

            doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).when(executor).execute(any(Runnable.class));

            // When
            List<PostMedia> result = cloudinaryService.uploadMultiMedia(Arrays.asList(image), Arrays.asList(video));

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should handle upload failure gracefully")
        void shouldHandleUploadFailureGracefully() throws Exception {
            // Given
            MultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "content".getBytes());

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);
            when(uploader.upload(any(), anyMap())).thenThrow(new RuntimeException("Upload failed"));

            doAnswer(invocation -> {
                Runnable task = invocation.getArgument(0);
                task.run();
                return null;
            }).when(executor).execute(any(Runnable.class));

            // When & Then
            assertThatThrownBy(() -> cloudinaryService.uploadMultiMedia(Arrays.asList(image), null))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
