package com.uit.buddy.service.file.impl;

import com.uit.buddy.config.S3Properties;
import com.uit.buddy.constant.StorageConstants;
import com.uit.buddy.dto.response.document.DocumentUploadResult;
import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.service.file.FileService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class S3FileServiceImpl implements FileService {

    private final S3Client s3Client;
    private final S3Properties properties;
    private final Executor executor;

    public S3FileServiceImpl(S3Client s3Client, S3Properties properties,
            @Qualifier("uploadExecutor") Executor executor) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.executor = executor;
    }

    @Override
    public String createDefaultAvatar(String mssv) {
        return properties.getDefaultAvatarUrl();
    }

    @Override
    public void deleteAvatar(String publicId) {
        deleteObject(buildAvatarKey(publicId));
    }

    @Override
    public String uploadAvatar(MultipartFile file, String mssv) {
        validateFile(file, FileType.IMAGE);
        String key = buildAvatarKey(mssv);
        uploadObject(extractBytes(file), key, file.getContentType());
        return buildPublicUrl(key);
    }

    @Override
    public String uploadCover(MultipartFile file, String mssv) {
        validateFile(file, FileType.IMAGE);
        String key = buildCoverKey(mssv);
        uploadObject(extractBytes(file), key, file.getContentType());
        return buildPublicUrl(key);
    }

    @Override
    public PostMedia uploadPostImage(MultipartFile file, String postId) {
        validateFile(file, FileType.IMAGE);
        return uploadPostMedia(extractBytes(file), file.getContentType(), file.getOriginalFilename(),
                StorageConstants.FOLDER_POST_IMAGES, postId, FileType.IMAGE);
    }

    @Override
    public PostMedia uploadPostVideo(MultipartFile file, String postId) {
        validateFile(file, FileType.VIDEO);
        return uploadPostMedia(extractBytes(file), file.getContentType(), file.getOriginalFilename(),
                StorageConstants.FOLDER_POST_VIDEOS, postId, FileType.VIDEO);
    }

    @Override
    public void deletePostMedia(List<PostMedia> medias) {
        if (medias == null || medias.isEmpty()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            for (PostMedia media : medias) {
                String key = extractKeyFromUrl(media.getUrl());
                if (key == null) {
                    continue;
                }
                deleteObject(key);
            }
        }, executor);
    }

    @Override
    public void validateFile(MultipartFile file, FileType fileType) {
        String[] allowed = fileType == FileType.IMAGE ? properties.getAllowedImageTypes()
                : properties.getAllowedVideoTypes();

        if (file == null || file.isEmpty()) {
            throw new UserException(UserErrorCode.FILE_EMPTY);
        }

        if (file.getContentType() == null || allowed == null
                || Arrays.stream(allowed).noneMatch(file.getContentType()::equalsIgnoreCase)) {
            throw new UserException(UserErrorCode.INVALID_FILE_TYPE,
                    "Allowed: " + String.join(", ", allowed == null ? new String[0] : allowed));
        }
    }

    @Override
    public List<PostMedia> uploadMultiMedia(List<MultipartFile> images, List<MultipartFile> videos) {
        validateFiles(images, videos);

        List<MultipartFile> safeImages = images != null ? images : Collections.emptyList();
        List<MultipartFile> safeVideos = videos != null ? videos : Collections.emptyList();
        List<CompletableFuture<PostMedia>> futures = new ArrayList<>();

        for (MultipartFile file : safeImages) {
            UploadPayload payload = toPayload(file);
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            futures.add(CompletableFuture.supplyAsync(() -> uploadPostMedia(payload.data(), payload.contentType(),
                    payload.originalFilename(), StorageConstants.FOLDER_POST_IMAGES, uniqueId, FileType.IMAGE),
                    executor));
        }

        for (MultipartFile file : safeVideos) {
            UploadPayload payload = toPayload(file);
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            futures.add(CompletableFuture.supplyAsync(() -> uploadPostMedia(payload.data(), payload.contentType(),
                    payload.originalFilename(), StorageConstants.FOLDER_POST_VIDEOS, uniqueId, FileType.VIDEO),
                    executor));
        }

        return futures.stream().map(CompletableFuture::join).toList();
    }

    @Override
    public void deleteDocument(String fileUrl) {
        String key = extractKeyFromUrl(fileUrl);
        if (key != null) {
            deleteObject(key);
        }
    }

    @Override
    public List<DocumentUploadResult> uploadMultipleDocuments(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompletableFuture<DocumentUploadResult>> futures = new ArrayList<>();
        for (MultipartFile file : files) {
            futures.add(CompletableFuture.supplyAsync(() -> uploadDocument(file), executor));
        }

        return futures.stream().map(CompletableFuture::join).toList();
    }

    private DocumentUploadResult uploadDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new UserException(UserErrorCode.FILE_EMPTY);
        }

        String extension = StorageConstants.resolveExtension(file.getOriginalFilename(), file.getContentType());
        String key = buildKey(StorageConstants.FOLDER_DOCUMENT_FILES, UUID.randomUUID().toString(), extension);
        uploadObject(extractBytes(file), key, file.getContentType());
        return new DocumentUploadResult(buildPublicUrl(key), StorageConstants.toMegabytes(file.getSize()),
                StorageConstants.extractFileType(file.getOriginalFilename()));
    }

    private PostMedia uploadPostMedia(byte[] data, String contentType, String originalFilename, String folder,
            String objectId, FileType fileType) {
        String extension = StorageConstants.resolveExtension(originalFilename, contentType);
        String key = buildKey(folder, objectId, extension);
        uploadObject(data, key, contentType);
        return PostMedia.builder().url(buildPublicUrl(key)).type(fileType).build();
    }

    private void uploadObject(byte[] data, String key, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder().bucket(properties.getBucketName()).key(key)
                    .contentType(contentType).build();

            s3Client.putObject(request, RequestBody.fromBytes(data));
            log.info("[S3] Uploaded object: {}", key);
        } catch (Exception e) {
            log.error("[S3] Upload failed for {}: {}", key, e.getMessage());
            throw new SystemException(SystemErrorCode.EXTERNAL_SERVICE_ERROR, "Cloud storage error");
        }
    }

    private void deleteObject(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(properties.getBucketName()).key(key).build());
            log.info("[S3] Deleted object: {}", key);
        } catch (Exception e) {
            log.error("[S3] Delete failed for {}: {}", key, e.getMessage());
        }
    }

    private String buildAvatarKey(String mssv) {
        return StorageConstants.FOLDER_AVATARS + StorageConstants.PATH_SEPARATOR + mssv;
    }

    private String buildCoverKey(String mssv) {
        return StorageConstants.FOLDER_COVERS + StorageConstants.PATH_SEPARATOR + mssv;
    }

    private String buildKey(String folder, String objectId, String extension) {
        String key = folder + StorageConstants.PATH_SEPARATOR + objectId;
        if (extension == null || extension.isBlank()) {
            return key;
        }
        return key + "." + extension;
    }

    private String buildPublicUrl(String key) {
        String publicBaseUrl = normalizeBaseUrl(properties.getPublicBaseUrl());
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            return publicBaseUrl + StorageConstants.PATH_SEPARATOR + key;
        }

        return "https://" + properties.getBucketName() + ".s3." + properties.getRegion() + ".amazonaws.com/" + key;
    }

    private String extractKeyFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }

        String cleanUrl = stripQueryString(url);
        String publicBaseUrl = normalizeBaseUrl(properties.getPublicBaseUrl());
        if (publicBaseUrl != null && !publicBaseUrl.isBlank()) {
            String prefix = publicBaseUrl + StorageConstants.PATH_SEPARATOR;
            if (cleanUrl.startsWith(prefix)) {
                return cleanUrl.substring(prefix.length());
            }
        }

        String s3BaseUrl = "https://" + properties.getBucketName() + ".s3." + properties.getRegion()
                + ".amazonaws.com/";
        if (cleanUrl.startsWith(s3BaseUrl)) {
            return cleanUrl.substring(s3BaseUrl.length());
        }

        return null;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }

        String normalized = baseUrl.trim();
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://" + normalized;
        }

        if (normalized.endsWith("/")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String stripQueryString(String url) {
        int queryIndex = url.indexOf('?');
        return queryIndex >= 0 ? url.substring(0, queryIndex) : url;
    }

    private UploadPayload toPayload(MultipartFile file) {
        return new UploadPayload(extractBytes(file), file.getContentType(), file.getOriginalFilename());
    }

    private byte[] extractBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new SystemException(SystemErrorCode.EXTERNAL_SERVICE_ERROR, "Read error");
        }
    }

    private void validateFiles(List<MultipartFile> images, List<MultipartFile> videos) {
        if (images != null) {
            for (MultipartFile image : images) {
                validateFile(image, FileType.IMAGE);
            }
        }
        if (videos != null) {
            for (MultipartFile video : videos) {
                validateFile(video, FileType.VIDEO);
            }
        }
    }

    private record UploadPayload(byte[] data, String contentType, String originalFilename) {
    }
}
