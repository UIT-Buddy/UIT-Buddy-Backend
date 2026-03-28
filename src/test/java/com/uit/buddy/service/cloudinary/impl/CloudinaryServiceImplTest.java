package com.uit.buddy.service.cloudinary.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.uit.buddy.config.CloudinaryProperties;
import com.uit.buddy.constant.CloudinaryConstants;
import com.uit.buddy.dto.response.document.DocumentUploadResult;
import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.service.file.impl.CloudinaryServiceImpl;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    @Mock
    private Api api;

    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
        lenient().when(cloudinary.api()).thenReturn(api);
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(executor).execute(any(Runnable.class));
        cloudinaryService = new CloudinaryServiceImpl(cloudinary, properties, executor);
    }

    @Nested
    @DisplayName("Avatar Tests")
    class AvatarTests {

        @Test
        @DisplayName("Should create default avatar successfully")
        void shouldCreateDefaultAvatarSuccessfully() throws Exception {
            String mssv = "21520001";
            String defaultUrl = "https://example.com/default-avatar.jpg";
            String expectedUrl = "https://cloudinary.com/avatar.jpg";

            when(properties.getDefaultAvatarUrl()).thenReturn(defaultUrl);
            when(properties.getAvatarSize()).thenReturn(200);
            when(uploader.upload(any(), anyMap()))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, expectedUrl));

            String result = cloudinaryService.createDefaultAvatar(mssv);

            assertThat(result).isEqualTo(expectedUrl);

            ArgumentCaptor<Object> sourceCaptor = ArgumentCaptor.forClass(Object.class);
            verify(uploader).upload(sourceCaptor.capture(), anyMap());
            assertThat(sourceCaptor.getValue()).isEqualTo(defaultUrl);
        }

        @Test
        @DisplayName("Should throw system exception when default avatar upload fails")
        void shouldThrowSystemExceptionWhenDefaultAvatarUploadFails() throws Exception {
            when(properties.getDefaultAvatarUrl()).thenReturn("https://example.com/default-avatar.jpg");
            when(properties.getAvatarSize()).thenReturn(200);
            when(uploader.upload(any(), anyMap())).thenThrow(new IOException("Cloudinary unavailable"));

            assertThatThrownBy(() -> cloudinaryService.createDefaultAvatar("21520001"))
                    .isInstanceOf(SystemException.class).extracting("code")
                    .isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should upload avatar successfully")
        void shouldUploadAvatarSuccessfully() throws Exception {
            String mssv = "21520001";
            String expectedUrl = "https://cloudinary.com/avatar.jpg";
            MultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "test-image".getBytes());

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(properties.getAvatarSize()).thenReturn(200);
            when(uploader.upload(any(), anyMap()))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, expectedUrl));

            String result = cloudinaryService.uploadAvatar(file, mssv);

            assertThat(result).isEqualTo(expectedUrl);
            verify(uploader).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should throw system exception when avatar bytes cannot be read")
        void shouldThrowSystemExceptionWhenAvatarBytesCannotBeRead() throws Exception {
            MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getBytes()).thenThrow(new IOException("Read error"));
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> cloudinaryService.uploadAvatar(file, "21520001"))
                    .isInstanceOf(SystemException.class).extracting("code")
                    .isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should throw system exception when avatar upload fails")
        void shouldThrowSystemExceptionWhenAvatarUploadFails() throws Exception {
            MultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "test-image".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(properties.getAvatarSize()).thenReturn(200);
            when(uploader.upload(any(), anyMap())).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> cloudinaryService.uploadAvatar(file, "21520001"))
                    .isInstanceOf(SystemException.class).extracting("code")
                    .isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("Post Media Tests")
    class PostMediaTests {

        @Test
        @DisplayName("Should upload post image successfully")
        void shouldUploadPostImageSuccessfully() throws Exception {
            String postId = "post-123";
            String expectedUrl = "https://cloudinary.com/post-image.jpg";
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "test-image".getBytes());

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);
            when(uploader.upload(any(), anyMap()))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, expectedUrl));

            PostMedia result = cloudinaryService.uploadPostImage(file, postId);

            assertThat(result).isNotNull();
            assertThat(result.getUrl()).isEqualTo(expectedUrl);
            assertThat(result.getType()).isEqualTo(FileType.IMAGE);
            verify(uploader).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should throw system exception when post image upload fails")
        void shouldThrowSystemExceptionWhenPostImageUploadFails() throws Exception {
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "test-image".getBytes());

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);
            when(uploader.upload(any(), anyMap())).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> cloudinaryService.uploadPostImage(file, "post-123"))
                    .isInstanceOf(SystemException.class).extracting("code")
                    .isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should upload post video successfully")
        void shouldUploadPostVideoSuccessfully() throws Exception {
            String postId = "post-123";
            String expectedUrl = "https://cloudinary.com/post-video.mp4";
            MultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "test-video".getBytes());

            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4", "video/avi" });
            when(uploader.upload(any(), anyMap()))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, expectedUrl));

            PostMedia result = cloudinaryService.uploadPostVideo(file, postId);

            assertThat(result).isNotNull();
            assertThat(result.getUrl()).isEqualTo(expectedUrl);
            assertThat(result.getType()).isEqualTo(FileType.VIDEO);
            verify(uploader).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should throw system exception when post video upload fails")
        void shouldThrowSystemExceptionWhenPostVideoUploadFails() throws Exception {
            MultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "test-video".getBytes());

            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4", "video/avi" });
            when(uploader.upload(any(), anyMap())).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> cloudinaryService.uploadPostVideo(file, "post-123"))
                    .isInstanceOf(SystemException.class).extracting("code")
                    .isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("Validate File Tests")
    class ValidateFileTests {

        @Test
        @DisplayName("Should throw exception when file is null")
        void shouldThrowExceptionWhenFileIsNull() {
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> cloudinaryService.validateFile(null, FileType.IMAGE))
                    .isInstanceOf(UserException.class).extracting("code").isEqualTo(UserErrorCode.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> cloudinaryService.validateFile(emptyFile, FileType.IMAGE))
                    .isInstanceOf(UserException.class).extracting("code").isEqualTo(UserErrorCode.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("Should throw exception when file type is invalid")
        void shouldThrowExceptionWhenFileTypeIsInvalid() {
            MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> cloudinaryService.validateFile(file, FileType.IMAGE))
                    .isInstanceOf(UserException.class).extracting("code")
                    .isEqualTo(UserErrorCode.INVALID_FILE_TYPE.getCode());
        }

        @Test
        @DisplayName("Should validate image file successfully")
        void shouldValidateImageFileSuccessfully() {
            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatNoException().isThrownBy(() -> cloudinaryService.validateFile(file, FileType.IMAGE));
        }

        @Test
        @DisplayName("Should validate video file successfully")
        void shouldValidateVideoFileSuccessfully() {
            MultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "content".getBytes());
            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4", "video/webm" });

            assertThatNoException().isThrownBy(() -> cloudinaryService.validateFile(file, FileType.VIDEO));
        }
    }

    @Nested
    @DisplayName("Delete Media Tests")
    class DeleteMediaTests {

        @Test
        @DisplayName("Should delete avatar successfully")
        void shouldDeleteAvatarSuccessfully() throws Exception {
            when(uploader.destroy(anyString(), anyMap())).thenReturn(new HashMap<>());

            cloudinaryService.deleteAvatar("21520001");

            verify(uploader).destroy(eq(CloudinaryConstants.FOLDER_AVATARS + "/21520001"),
                    eq(Map.of(CloudinaryConstants.PARAM_RESOURCE_TYPE, CloudinaryConstants.RESOURCE_TYPE_IMAGE)));
        }

        @Test
        @DisplayName("Should swallow exception when avatar deletion fails")
        void shouldSwallowExceptionWhenAvatarDeletionFails() throws Exception {
            when(uploader.destroy(anyString(), anyMap())).thenThrow(new IOException("Delete failed"));

            assertThatNoException().isThrownBy(() -> cloudinaryService.deleteAvatar("21520001"));
        }

        @Test
        @DisplayName("Should ignore null media list")
        void shouldIgnoreNullMediaList() {
            cloudinaryService.deletePostMedia(null);

            verify(cloudinary, never()).api();
        }

        @Test
        @DisplayName("Should ignore empty media list")
        void shouldIgnoreEmptyMediaList() {
            cloudinaryService.deletePostMedia(List.of());

            verify(cloudinary, never()).api();
        }

        @Test
        @DisplayName("Should delete post media grouped by resource type")
        void shouldDeletePostMediaGroupedByResourceType() throws Exception {
            PostMedia image = PostMedia.builder()
                    .url("https://res.cloudinary.com/demo/image/upload/v1/posts/images/post-image.jpg")
                    .type(FileType.IMAGE).build();
            PostMedia video = PostMedia.builder()
                    .url("https://res.cloudinary.com/demo/video/upload/v1/posts/videos/post-video.mp4")
                    .type(FileType.VIDEO).build();

            when(api.deleteResources(any(), anyMap())).thenReturn(null);

            cloudinaryService.deletePostMedia(List.of(image, video));

            verify(api).deleteResources(eq(List.of("posts/images/post-image")),
                    eq(Map.of(CloudinaryConstants.PARAM_RESOURCE_TYPE, CloudinaryConstants.RESOURCE_TYPE_IMAGE)));
            verify(api).deleteResources(eq(List.of("posts/videos/post-video")),
                    eq(Map.of(CloudinaryConstants.PARAM_RESOURCE_TYPE, CloudinaryConstants.RESOURCE_TYPE_VIDEO)));
        }

        @Test
        @DisplayName("Should swallow exception when bulk media deletion fails")
        void shouldSwallowExceptionWhenBulkMediaDeletionFails() throws Exception {
            PostMedia image = PostMedia.builder()
                    .url("https://res.cloudinary.com/demo/image/upload/v1/posts/images/post-image.jpg")
                    .type(FileType.IMAGE).build();

            when(api.deleteResources(any(), anyMap())).thenThrow(new RuntimeException("Bulk delete failed"));

            assertThatNoException().isThrownBy(() -> cloudinaryService.deletePostMedia(List.of(image)));
        }
    }

    @Nested
    @DisplayName("Upload Multi Media Tests")
    class UploadMultiMediaTests {

        @Test
        @DisplayName("Should return empty list when no media is provided")
        void shouldReturnEmptyListWhenNoMediaIsProvided() throws Exception {
            List<PostMedia> result = cloudinaryService.uploadMultiMedia(null, null);

            assertThat(result).isEmpty();
            verify(uploader, never()).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should upload multiple images successfully")
        void shouldUploadMultipleImagesSuccessfully() throws Exception {
            MultipartFile image1 = new MockMultipartFile("file1", "image1.jpg", "image/jpeg", "content1".getBytes());
            MultipartFile image2 = new MockMultipartFile("file2", "image2.jpg", "image/jpeg", "content2".getBytes());

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);
            when(uploader.upload(any(), anyMap()))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, "https://cloudinary.com/image.jpg"));

            List<PostMedia> result = cloudinaryService.uploadMultiMedia(List.of(image1, image2), null);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(media -> media.getType() == FileType.IMAGE);
            verify(uploader, times(2)).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should upload mixed media successfully")
        void shouldUploadMixedMediaSuccessfully() throws Exception {
            MultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "image-content".getBytes());
            MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video-content".getBytes());

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg" });
            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);
            when(uploader.upload(any(), anyMap()))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, "https://cloudinary.com/media"));

            List<PostMedia> result = cloudinaryService.uploadMultiMedia(List.of(image), List.of(video));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(PostMedia::getType).containsExactlyInAnyOrder(FileType.IMAGE, FileType.VIDEO);
            verify(uploader, times(2)).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should fail fast when media validation fails")
        void shouldFailFastWhenMediaValidationFails() {
            MultipartFile invalidImage = new MockMultipartFile("image", "image.txt", "text/plain",
                    "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg" });

            assertThatThrownBy(() -> cloudinaryService.uploadMultiMedia(List.of(invalidImage), null))
                    .isInstanceOf(UserException.class).extracting("code")
                    .isEqualTo(UserErrorCode.INVALID_FILE_TYPE.getCode());
        }

        @Test
        @DisplayName("Should fail when media bytes cannot be read")
        void shouldFailWhenMediaBytesCannotBeRead() throws Exception {
            MultipartFile image = org.mockito.Mockito.mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(false);
            when(image.getContentType()).thenReturn("image/jpeg");
            when(image.getBytes()).thenThrow(new IOException("Read error"));
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> cloudinaryService.uploadMultiMedia(List.of(image), null))
                    .isInstanceOf(SystemException.class).extracting("code")
                    .isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should wrap upload failure from async tasks")
        void shouldWrapUploadFailureFromAsyncTasks() throws Exception {
            MultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "content".getBytes());

            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg" });
            when(properties.getPostImageWidth()).thenReturn(1080);
            when(properties.getPostImageHeight()).thenReturn(1080);
            when(uploader.upload(any(), anyMap())).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> cloudinaryService.uploadMultiMedia(List.of(image), null))
                    .isInstanceOf(CompletionException.class).cause().isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("Document Upload Tests")
    class DocumentUploadTests {

        @Test
        @DisplayName("Should return empty list when document list is null")
        void shouldReturnEmptyListWhenDocumentListIsNull() {
            assertThat(cloudinaryService.uploadMultipleDocuments(null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when document list is empty")
        void shouldReturnEmptyListWhenDocumentListIsEmpty() {
            assertThat(cloudinaryService.uploadMultipleDocuments(List.of())).isEmpty();
        }

        @Test
        @DisplayName("Should upload multiple documents successfully")
        void shouldUploadMultipleDocumentsSuccessfully() throws Exception {
            MultipartFile file1 = new MockMultipartFile("file1", "report.pdf", "application/pdf",
                    new byte[2 * 1024 * 1024]);
            MultipartFile file2 = new MockMultipartFile("file2", "sheet.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[1024 * 1024]);

            when(uploader.upload(any(), anyMap()))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, "https://cloudinary.com/report.pdf"))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, "https://cloudinary.com/sheet.xlsx"));

            List<DocumentUploadResult> result = cloudinaryService.uploadMultipleDocuments(List.of(file1, file2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).fileUrl()).isEqualTo("https://cloudinary.com/report.pdf");
            assertThat(result.get(0).fileSize()).isEqualTo(2.0f);
            assertThat(result.get(0).fileType()).isEqualTo(FileType.WORD);
            assertThat(result.get(1).fileUrl()).isEqualTo("https://cloudinary.com/sheet.xlsx");
            assertThat(result.get(1).fileSize()).isEqualTo(1.0f);
            assertThat(result.get(1).fileType()).isEqualTo(FileType.EXCEL);
            verify(uploader, times(2)).upload(any(), anyMap());
        }

        @Test
        @DisplayName("Should classify unknown document extension as other")
        void shouldClassifyUnknownDocumentExtensionAsOther() throws Exception {
            MultipartFile file = new MockMultipartFile("file", "archive.bin", "application/octet-stream",
                    new byte[512]);
            when(uploader.upload(any(), anyMap()))
                    .thenReturn(Map.of(CloudinaryConstants.RESPONSE_SECURE_URL, "https://cloudinary.com/archive.bin"));

            List<DocumentUploadResult> result = cloudinaryService.uploadMultipleDocuments(List.of(file));

            assertThat(result).singleElement().satisfies(upload -> {
                assertThat(upload.fileType()).isEqualTo(FileType.OTHER);
                assertThat(upload.fileSize()).isGreaterThan(0.0f);
            });
        }

        @Test
        @DisplayName("Should fail when document is empty")
        void shouldFailWhenDocumentIsEmpty() {
            MultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

            assertThatThrownBy(() -> cloudinaryService.uploadMultipleDocuments(List.of(emptyFile)))
                    .isInstanceOf(CompletionException.class).cause().isInstanceOf(UserException.class)
                    .extracting("code").isEqualTo(UserErrorCode.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("Should fail when document bytes cannot be read")
        void shouldFailWhenDocumentBytesCannotBeRead() throws Exception {
            MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenThrow(new IOException("Read error"));

            assertThatThrownBy(() -> cloudinaryService.uploadMultipleDocuments(List.of(file)))
                    .isInstanceOf(CompletionException.class).cause().isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should fail when document upload fails")
        void shouldFailWhenDocumentUploadFails() throws Exception {
            MultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());
            when(uploader.upload(any(), anyMap())).thenThrow(new IOException("Upload failed"));

            assertThatThrownBy(() -> cloudinaryService.uploadMultipleDocuments(List.of(file)))
                    .isInstanceOf(CompletionException.class).cause().isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }
    }
}
