package com.uit.buddy.service.file.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.buddy.config.S3Properties;
import com.uit.buddy.constant.StorageConstants;
import com.uit.buddy.dto.response.document.DocumentUploadResult;
import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import java.io.IOException;
import java.util.List;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3FileServiceImpl Tests")
class S3FileServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Properties properties;

    @Mock
    private Executor executor;

    private S3FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getBucketName()).thenReturn("uitbuddy-storage");
        lenient().when(properties.getRegion()).thenReturn("ap-southeast-2");
        lenient().when(properties.getPublicBaseUrl()).thenReturn("https://cdn.example.com");
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(executor).execute(any(Runnable.class));
        lenient().when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());
        lenient().when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        fileService = new S3FileServiceImpl(s3Client, properties, executor);
    }

    @Nested
    @DisplayName("Avatar Tests")
    class AvatarTests {

        @Test
        @DisplayName("Should return configured default avatar successfully")
        void shouldReturnConfiguredDefaultAvatarSuccessfully() {
            when(properties.getDefaultAvatarUrl()).thenReturn("https://cdn.example.com/avatars/default.png");

            String result = fileService.createDefaultAvatar("21520001");

            assertThat(result).isEqualTo("https://cdn.example.com/avatars/default.png");
            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should upload avatar successfully")
        void shouldUploadAvatarSuccessfully() {
            MultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "avatar".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            String result = fileService.uploadAvatar(file, "21520001");

            assertThat(result).isEqualTo("https://cdn.example.com/avatars/21520001");

            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
            assertThat(requestCaptor.getValue().bucket()).isEqualTo("uitbuddy-storage");
            assertThat(requestCaptor.getValue().key()).isEqualTo("avatars/21520001");
            assertThat(requestCaptor.getValue().contentType()).isEqualTo("image/jpeg");
        }

        @Test
        @DisplayName("Should throw system exception when avatar bytes cannot be read")
        void shouldThrowSystemExceptionWhenAvatarBytesCannotBeRead() throws Exception {
            MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getContentType()).thenReturn("image/jpeg");
            when(file.getBytes()).thenThrow(new IOException("Read error"));
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> fileService.uploadAvatar(file, "21520001")).isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should throw system exception when avatar upload fails")
        void shouldThrowSystemExceptionWhenAvatarUploadFails() {
            MultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", "avatar".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new RuntimeException("Upload failed"));

            assertThatThrownBy(() -> fileService.uploadAvatar(file, "21520001")).isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("Post Media Tests")
    class PostMediaTests {

        @Test
        @DisplayName("Should upload post image successfully")
        void shouldUploadPostImageSuccessfully() {
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            PostMedia result = fileService.uploadPostImage(file, "post-123");

            assertThat(result.getType()).isEqualTo(FileType.IMAGE);
            assertThat(result.getUrl()).isEqualTo("https://cdn.example.com/posts/images/post-123.jpg");
        }

        @Test
        @DisplayName("Should throw system exception when post image upload fails")
        void shouldThrowSystemExceptionWhenPostImageUploadFails() {
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "image".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new RuntimeException("Upload failed"));

            assertThatThrownBy(() -> fileService.uploadPostImage(file, "post-123")).isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should upload post video successfully")
        void shouldUploadPostVideoSuccessfully() {
            MultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "video".getBytes());
            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4", "video/webm" });

            PostMedia result = fileService.uploadPostVideo(file, "post-123");

            assertThat(result.getType()).isEqualTo(FileType.VIDEO);
            assertThat(result.getUrl()).isEqualTo("https://cdn.example.com/posts/videos/post-123.mp4");
        }

        @Test
        @DisplayName("Should throw system exception when post video upload fails")
        void shouldThrowSystemExceptionWhenPostVideoUploadFails() {
            MultipartFile file = new MockMultipartFile("file", "video.mp4", "video/mp4", "video".getBytes());
            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4", "video/webm" });
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new RuntimeException("Upload failed"));

            assertThatThrownBy(() -> fileService.uploadPostVideo(file, "post-123")).isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }
    }

    @Nested
    @DisplayName("Validate File Tests")
    class ValidateFileTests {

        @Test
        @DisplayName("Should throw exception when file is null")
        void shouldThrowExceptionWhenFileIsNull() {
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> fileService.validateFile(null, FileType.IMAGE)).isInstanceOf(UserException.class)
                    .extracting("code").isEqualTo(UserErrorCode.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("Should throw exception when file is empty")
        void shouldThrowExceptionWhenFileIsEmpty() {
            MultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> fileService.validateFile(emptyFile, FileType.IMAGE))
                    .isInstanceOf(UserException.class).extracting("code").isEqualTo(UserErrorCode.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("Should throw exception when file type is invalid")
        void shouldThrowExceptionWhenFileTypeIsInvalid() {
            MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatThrownBy(() -> fileService.validateFile(file, FileType.IMAGE)).isInstanceOf(UserException.class)
                    .extracting("code").isEqualTo(UserErrorCode.INVALID_FILE_TYPE.getCode());
        }

        @Test
        @DisplayName("Should validate image file successfully")
        void shouldValidateImageFileSuccessfully() {
            MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            assertThatNoException().isThrownBy(() -> fileService.validateFile(file, FileType.IMAGE));
        }

        @Test
        @DisplayName("Should validate video file successfully")
        void shouldValidateVideoFileSuccessfully() {
            MultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "content".getBytes());
            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4", "video/webm" });

            assertThatNoException().isThrownBy(() -> fileService.validateFile(file, FileType.VIDEO));
        }
    }

    @Nested
    @DisplayName("Delete Media Tests")
    class DeleteMediaTests {

        @Test
        @DisplayName("Should delete avatar successfully")
        void shouldDeleteAvatarSuccessfully() {
            fileService.deleteAvatar("21520001");

            ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
            verify(s3Client).deleteObject(requestCaptor.capture());
            assertThat(requestCaptor.getValue().bucket()).isEqualTo("uitbuddy-storage");
            assertThat(requestCaptor.getValue().key()).isEqualTo("avatars/21520001");
        }

        @Test
        @DisplayName("Should swallow exception when avatar deletion fails")
        void shouldSwallowExceptionWhenAvatarDeletionFails() {
            when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenThrow(new RuntimeException("Delete failed"));

            assertThatNoException().isThrownBy(() -> fileService.deleteAvatar("21520001"));
        }

        @Test
        @DisplayName("Should ignore null media list")
        void shouldIgnoreNullMediaList() {
            fileService.deletePostMedia(null);

            verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("Should ignore empty media list")
        void shouldIgnoreEmptyMediaList() {
            fileService.deletePostMedia(List.of());

            verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("Should delete only S3 and public CDN media")
        void shouldDeleteOnlyS3AndPublicCdnMedia() {
            when(properties.getPublicBaseUrl()).thenReturn("cdn.example.com");
            PostMedia cloudFrontMedia = PostMedia.builder().url("https://cdn.example.com/posts/images/post-image.jpg")
                    .type(FileType.IMAGE).build();
            PostMedia s3Media = PostMedia.builder()
                    .url("https://uitbuddy-storage.s3.ap-southeast-2.amazonaws.com/posts/videos/post-video.mp4")
                    .type(FileType.VIDEO).build();
            PostMedia legacyMedia = PostMedia.builder()
                    .url("https://legacy-storage.example.com/posts/images/old-image.jpg").type(FileType.IMAGE).build();

            fileService.deletePostMedia(List.of(cloudFrontMedia, s3Media, legacyMedia));

            ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
            verify(s3Client, times(2)).deleteObject(requestCaptor.capture());
            assertThat(requestCaptor.getAllValues()).extracting(DeleteObjectRequest::key)
                    .containsExactlyInAnyOrder("posts/images/post-image.jpg", "posts/videos/post-video.mp4");
        }

        @Test
        @DisplayName("Should swallow exception when media deletion fails")
        void shouldSwallowExceptionWhenMediaDeletionFails() {
            PostMedia media = PostMedia.builder().url("https://cdn.example.com/posts/images/post-image.jpg")
                    .type(FileType.IMAGE).build();
            when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenThrow(new RuntimeException("Delete failed"));

            assertThatNoException().isThrownBy(() -> fileService.deletePostMedia(List.of(media)));
        }
    }

    @Nested
    @DisplayName("Upload Multi Media Tests")
    class UploadMultiMediaTests {

        @Test
        @DisplayName("Should return empty list when no media is provided")
        void shouldReturnEmptyListWhenNoMediaIsProvided() {
            List<PostMedia> result = fileService.uploadMultiMedia(null, null);

            assertThat(result).isEmpty();
            verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should upload multiple images successfully")
        void shouldUploadMultipleImagesSuccessfully() {
            MultipartFile image1 = new MockMultipartFile("file1", "image1.jpg", "image/jpeg", "content1".getBytes());
            MultipartFile image2 = new MockMultipartFile("file2", "image2.jpg", "image/jpeg", "content2".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg", "image/png" });

            List<PostMedia> result = fileService.uploadMultiMedia(List.of(image1, image2), null);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(media -> media.getType() == FileType.IMAGE);
            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should upload mixed media successfully")
        void shouldUploadMixedMediaSuccessfully() {
            MultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "image".getBytes());
            MultipartFile video = new MockMultipartFile("video", "video.mp4", "video/mp4", "video".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg" });
            when(properties.getAllowedVideoTypes()).thenReturn(new String[] { "video/mp4" });

            List<PostMedia> result = fileService.uploadMultiMedia(List.of(image), List.of(video));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(PostMedia::getType).containsExactlyInAnyOrder(FileType.IMAGE, FileType.VIDEO);
        }

        @Test
        @DisplayName("Should fail fast when media validation fails")
        void shouldFailFastWhenMediaValidationFails() {
            MultipartFile invalidImage = new MockMultipartFile("image", "image.txt", "text/plain",
                    "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg" });

            assertThatThrownBy(() -> fileService.uploadMultiMedia(List.of(invalidImage), null))
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

            assertThatThrownBy(() -> fileService.uploadMultiMedia(List.of(image), null))
                    .isInstanceOf(SystemException.class).extracting("code")
                    .isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should wrap upload failure from async tasks")
        void shouldWrapUploadFailureFromAsyncTasks() {
            MultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "content".getBytes());
            when(properties.getAllowedImageTypes()).thenReturn(new String[] { "image/jpeg" });
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new RuntimeException("Upload failed"));

            assertThatThrownBy(() -> fileService.uploadMultiMedia(List.of(image), null))
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
            assertThat(fileService.uploadMultipleDocuments(null)).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list when document list is empty")
        void shouldReturnEmptyListWhenDocumentListIsEmpty() {
            assertThat(fileService.uploadMultipleDocuments(List.of())).isEmpty();
        }

        @Test
        @DisplayName("Should upload multiple documents successfully")
        void shouldUploadMultipleDocumentsSuccessfully() {
            MultipartFile file1 = new MockMultipartFile("file1", "report.pdf", "application/pdf",
                    new byte[2 * 1024 * 1024]);
            MultipartFile file2 = new MockMultipartFile("file2", "sheet.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[1024 * 1024]);

            List<DocumentUploadResult> result = fileService.uploadMultipleDocuments(List.of(file1, file2));

            assertThat(result).hasSize(2);
            assertThat(result.get(0).fileUrl()).contains(StorageConstants.FOLDER_DOCUMENT_FILES);
            assertThat(result.get(0).fileSize()).isEqualTo(2.0f);
            assertThat(result.get(0).fileType()).isEqualTo(FileType.WORD);
            assertThat(result.get(1).fileSize()).isEqualTo(1.0f);
            assertThat(result.get(1).fileType()).isEqualTo(FileType.EXCEL);
            verify(s3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should classify unknown document extension as other")
        void shouldClassifyUnknownDocumentExtensionAsOther() {
            MultipartFile file = new MockMultipartFile("file", "archive.bin", "application/octet-stream",
                    new byte[512]);

            List<DocumentUploadResult> result = fileService.uploadMultipleDocuments(List.of(file));

            assertThat(result).singleElement().satisfies(upload -> {
                assertThat(upload.fileType()).isEqualTo(FileType.OTHER);
                assertThat(upload.fileSize()).isGreaterThan(0.0f);
            });
        }

        @Test
        @DisplayName("Should fail when document is empty")
        void shouldFailWhenDocumentIsEmpty() {
            MultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

            assertThatThrownBy(() -> fileService.uploadMultipleDocuments(List.of(emptyFile)))
                    .isInstanceOf(CompletionException.class).cause().isInstanceOf(UserException.class)
                    .extracting("code").isEqualTo(UserErrorCode.FILE_EMPTY.getCode());
        }

        @Test
        @DisplayName("Should fail when document bytes cannot be read")
        void shouldFailWhenDocumentBytesCannotBeRead() throws Exception {
            MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getBytes()).thenThrow(new IOException("Read error"));

            assertThatThrownBy(() -> fileService.uploadMultipleDocuments(List.of(file)))
                    .isInstanceOf(CompletionException.class).cause().isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }

        @Test
        @DisplayName("Should fail when document upload fails")
        void shouldFailWhenDocumentUploadFails() {
            MultipartFile file = new MockMultipartFile("file", "report.pdf", "application/pdf", "content".getBytes());
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new RuntimeException("Upload failed"));

            assertThatThrownBy(() -> fileService.uploadMultipleDocuments(List.of(file)))
                    .isInstanceOf(CompletionException.class).cause().isInstanceOf(SystemException.class)
                    .extracting("code").isEqualTo(SystemErrorCode.EXTERNAL_SERVICE_ERROR.getCode());
        }
    }
}
