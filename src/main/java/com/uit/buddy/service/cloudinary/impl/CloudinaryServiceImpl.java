package com.uit.buddy.service.cloudinary.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.uit.buddy.config.CloudinaryProperties;
import com.uit.buddy.constant.CloudinaryConstants;
import com.uit.buddy.dto.response.document.DocumentUploadResult;
import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;
    private final Executor executor;

    public CloudinaryServiceImpl(Cloudinary cloudinary, CloudinaryProperties properties,
            @Qualifier("uploadExecutor") Executor executor) {
        this.cloudinary = cloudinary;
        this.properties = properties;
        this.executor = executor;
    }

    @Override
    public String createDefaultAvatar(String mssv) {
        return executeAvatarUpload(properties.getDefaultAvatarUrl(), mssv, CloudinaryConstants.FOLDER_AVATARS,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE, getAvatarTransform());
    }

    @Override
    public String uploadAvatar(MultipartFile file, String mssv) {
        validateFile(file, FileType.IMAGE);
        return executeAvatarUpload(extractBytes(file), mssv, CloudinaryConstants.FOLDER_AVATARS,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE, getAvatarTransform());
    }

    @Override
    public PostMedia uploadPostImage(MultipartFile file, String postId) {
        validateFile(file, FileType.IMAGE);
        Transformation<?> postTransform = new Transformation<>().width(properties.getPostImageWidth())
                .height(properties.getPostImageHeight()).crop(CloudinaryConstants.CROP_LIMIT);

        return executeUpload(extractBytes(file), postId, CloudinaryConstants.FOLDER_POST_IMAGES,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE, postTransform, FileType.IMAGE);
    }

    @Override
    public PostMedia uploadPostVideo(MultipartFile file, String postId) {
        validateFile(file, FileType.VIDEO);
        return executeUpload(extractBytes(file), postId, CloudinaryConstants.FOLDER_POST_VIDEOS,
                CloudinaryConstants.RESOURCE_TYPE_VIDEO, null, FileType.VIDEO);
    }

    @Override
    public void deleteAvatar(String publicId) {
        executeDelete(CloudinaryConstants.FOLDER_AVATARS + "/" + publicId, CloudinaryConstants.RESOURCE_TYPE_IMAGE);
    }

    @Override
    public void deletePostMedia(List<PostMedia> medias) {
        if (medias == null || medias.isEmpty())
            return;

        List<String> imageIds = new ArrayList<>();
        List<String> videoIds = new ArrayList<>();

        for (PostMedia media : medias) {
            String publicId = extractPublicId(media.getUrl(), media.getType());
            if (publicId == null)
                continue;

            if (media.getType() == FileType.VIDEO) {
                videoIds.add(publicId);
            } else {
                imageIds.add(publicId);
            }
        }
        CompletableFuture.runAsync(() -> {
            try {
                if (!imageIds.isEmpty()) {
                    cloudinary.api().deleteResources(imageIds, Map.of("resource_type", "image"));
                    log.info("[Cloudinary] Bulk deleted images: {}", imageIds);
                }
                if (!videoIds.isEmpty()) {
                    cloudinary.api().deleteResources(videoIds, Map.of("resource_type", "video"));
                    log.info("[Cloudinary] Bulk deleted videos: {}", videoIds);
                }
            } catch (Exception e) {
                log.error("[Cloudinary] Bulk delete failed: {}", e.getMessage());
            }
        }, executor);
    }

    private Transformation<?> getAvatarTransform() {
        return new Transformation<>().width(properties.getAvatarSize()).height(properties.getAvatarSize())
                .crop(CloudinaryConstants.CROP_FILL).gravity(CloudinaryConstants.GRAVITY_FACE);
    }

    @Override
    public void validateFile(MultipartFile file, FileType fileType) {
        String[] allowed = (fileType == FileType.IMAGE) ? properties.getAllowedImageTypes()
                : properties.getAllowedVideoTypes();

        if (file == null || file.isEmpty())
            throw new UserException(UserErrorCode.FILE_EMPTY);

        if (file.getContentType() == null
                || Arrays.stream(allowed).noneMatch(file.getContentType()::equalsIgnoreCase)) {
            throw new UserException(UserErrorCode.INVALID_FILE_TYPE, "Allowed: " + String.join(", ", allowed));
        }
    }

    private PostMedia executeUpload(Object source, String publicId, String folder, String resType,
            Transformation<?> trans, FileType fileType) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(CloudinaryConstants.PARAM_PUBLIC_ID, publicId);
            params.put(CloudinaryConstants.PARAM_FOLDER, folder);
            params.put(CloudinaryConstants.PARAM_OVERWRITE, true);
            params.put(CloudinaryConstants.PARAM_RESOURCE_TYPE, resType);
            if (trans != null)
                params.put(CloudinaryConstants.PARAM_TRANSFORMATION, trans);

            Map<?, ?> result = cloudinary.uploader().upload(source, params);
            log.info("[CLOUDINARY RESPONSE SUCCESSFULLY] {}", publicId);
            return PostMedia.builder().url(result.get(CloudinaryConstants.RESPONSE_SECURE_URL).toString())
                    .type(fileType).build();
        } catch (Exception e) {
            log.error("[Cloudinary] Upload failed for {}: {}", publicId, e.getMessage());
            throw new SystemException(SystemErrorCode.EXTERNAL_SERVICE_ERROR, "Cloud storage error");
        }
    }

    private void executeDelete(String path, String resType) {
        try {
            cloudinary.uploader().destroy(path, Map.of(CloudinaryConstants.PARAM_RESOURCE_TYPE, resType));
        } catch (Exception e) {
            log.error("[Cloudinary] Delete failed for {}: {}", path, e.getMessage());
        }
    }

    private byte[] extractBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new SystemException(SystemErrorCode.EXTERNAL_SERVICE_ERROR, "Read error");
        }
    }

    @Override
    public List<PostMedia> uploadMultiMedia(List<MultipartFile> images, List<MultipartFile> videos) {
        validateFiles(images, videos);

        List<MultipartFile> safeImages = (images != null) ? images : Collections.emptyList();
        List<MultipartFile> safeVideos = (videos != null) ? videos : Collections.emptyList();

        List<CompletableFuture<PostMedia>> futures = new ArrayList<>();

        for (MultipartFile file : safeImages) {
            byte[] data = extractBytes(file);
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            futures.add(CompletableFuture.supplyAsync(() -> uploadPostImage(data, uniqueId), executor));
        }

        for (MultipartFile file : safeVideos) {
            byte[] data = extractBytes(file);
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            futures.add(CompletableFuture.supplyAsync(() -> uploadPostVideo(data, uniqueId), executor));
        }

        return futures.stream().map(CompletableFuture::join).toList();
    }

    private PostMedia uploadPostImage(byte[] data, String publicId) {
        Transformation<?> postTransform = new Transformation<>().width(properties.getPostImageWidth())
                .height(properties.getPostImageHeight()).crop(CloudinaryConstants.CROP_LIMIT);

        return executeUpload(data, publicId, CloudinaryConstants.FOLDER_POST_IMAGES,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE, postTransform, FileType.IMAGE);
    }

    private PostMedia uploadPostVideo(byte[] data, String publicId) {
        return executeUpload(data, publicId, CloudinaryConstants.FOLDER_POST_VIDEOS,
                CloudinaryConstants.RESOURCE_TYPE_VIDEO, null, FileType.VIDEO);
    }

    private void validateFiles(List<MultipartFile> images, List<MultipartFile> videos) {
        if (images != null)
            for (MultipartFile image : images) {
                validateFile(image, FileType.IMAGE);
            }
        if (videos != null)
            for (MultipartFile video : videos) {
                validateFile(video, FileType.VIDEO);
            }
    }

    private String executeAvatarUpload(Object source, String publicId, String folder, String resType,
            Transformation<?> trans) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(CloudinaryConstants.PARAM_PUBLIC_ID, publicId);
            params.put(CloudinaryConstants.PARAM_FOLDER, folder);
            params.put(CloudinaryConstants.PARAM_OVERWRITE, true);
            params.put(CloudinaryConstants.PARAM_RESOURCE_TYPE, resType);
            if (trans != null)
                params.put(CloudinaryConstants.PARAM_TRANSFORMATION, trans);

            Map<?, ?> result = cloudinary.uploader().upload(source, params);
            return result.get(CloudinaryConstants.RESPONSE_SECURE_URL).toString();
        } catch (Exception e) {
            log.error("[Cloudinary] Upload avatar failed for {}: {}", publicId, e.getMessage());
            throw new SystemException(SystemErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }

    private String extractPublicId(String url, FileType type) {
        String folder = (type == FileType.VIDEO) ? CloudinaryConstants.FOLDER_POST_VIDEOS
                : CloudinaryConstants.FOLDER_POST_IMAGES;

        int folderIndex = url.indexOf(folder);
        int lastDotIndex = url.lastIndexOf(".");

        return url.substring(folderIndex, lastDotIndex);
    }

    @Override
    public List<DocumentUploadResult> uploadMultipleDocuments(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<CompletableFuture<DocumentUploadResult>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileId = UUID.randomUUID().toString();
            String publicId = CloudinaryConstants.DOCUMENT_PUBLIC_ID_PREFIX + CloudinaryConstants.PATH_SEPARATOR
                    + fileId;
            futures.add(CompletableFuture.supplyAsync(() -> uploadDocument(file, publicId), executor));
        }

        return futures.stream().map(CompletableFuture::join).toList();
    }

    private DocumentUploadResult uploadDocument(MultipartFile file, String publicId) {
        if (file == null || file.isEmpty()) {
            throw new UserException(UserErrorCode.FILE_EMPTY);
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put(CloudinaryConstants.PARAM_PUBLIC_ID, publicId);
            params.put(CloudinaryConstants.PARAM_FOLDER, CloudinaryConstants.FOLDER_DOCUMENT_FILES);
            params.put(CloudinaryConstants.PARAM_OVERWRITE, true);
            params.put(CloudinaryConstants.PARAM_RESOURCE_TYPE, CloudinaryConstants.RESOURCE_TYPE_RAW);

            Map<?, ?> result = cloudinary.uploader().upload(extractBytes(file), params);
            String fileUrl = result.get(CloudinaryConstants.RESPONSE_SECURE_URL).toString();
            float fileSize = (float) file.getSize() / (1024 * 1024); // Convert bytes to MB
            FileType fileType = CloudinaryConstants.extractFileType(file.getOriginalFilename());
            return new DocumentUploadResult(fileUrl, fileSize, fileType);
        } catch (Exception e) {
            log.error("[Cloudinary] Upload document failed for {}: {}", publicId, e.getMessage());
            throw new SystemException(SystemErrorCode.EXTERNAL_SERVICE_ERROR);
        }
    }
}
