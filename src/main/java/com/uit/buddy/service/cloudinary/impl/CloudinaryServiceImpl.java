package com.uit.buddy.service.cloudinary.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.uit.buddy.config.CloudinaryProperties;
import com.uit.buddy.constant.CloudinaryConstants;
import com.uit.buddy.dto.response.social.MediaResponse;
import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public String createDefaultAvatar(String mssv) {
        return executeAvatarUpload(properties.getDefaultAvatarUrl(), mssv,
                CloudinaryConstants.FOLDER_AVATARS,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE,
                getAvatarTransform());
    }

    @Override
    public String uploadAvatar(MultipartFile file, String mssv) {
        validateFile(file, FileType.IMAGE);
        return executeAvatarUpload(extractBytes(file), mssv,
                CloudinaryConstants.FOLDER_AVATARS,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE,
                getAvatarTransform()
        );
    }

    @Override
    public PostMedia uploadPostImage(MultipartFile file, String postId) {
        validateFile(file, FileType.IMAGE);
        Transformation<?> postTransform = new Transformation<>()
                .width(properties.getPostImageWidth())
                .height(properties.getPostImageHeight())
                .crop(CloudinaryConstants.CROP_LIMIT);

        return executeUpload(extractBytes(file), postId,
                CloudinaryConstants.FOLDER_POST_IMAGES,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE,
                postTransform,
                FileType.IMAGE
        );
    }

    @Override
    public PostMedia uploadPostVideo(MultipartFile file, String postId) {
        validateFile(file, FileType.VIDEO);
        return executeUpload(extractBytes(file), postId,
                CloudinaryConstants.FOLDER_POST_VIDEOS,
                CloudinaryConstants.RESOURCE_TYPE_VIDEO,
                null,
                FileType.VIDEO
        );
    }

    @Override
    public void deleteAvatar(String publicId) {
        executeDelete(CloudinaryConstants.FOLDER_AVATARS + "/" + publicId, CloudinaryConstants.RESOURCE_TYPE_IMAGE);
    }

    @Override
    public void deletePostMedia(String postId) {
        executeDelete(CloudinaryConstants.FOLDER_POST_IMAGES + "/" + postId, CloudinaryConstants.RESOURCE_TYPE_IMAGE);
        executeDelete(CloudinaryConstants.FOLDER_POST_VIDEOS + "/" + postId, CloudinaryConstants.RESOURCE_TYPE_VIDEO);
    }

    private Transformation<?> getAvatarTransform() {
        return new Transformation<>()
                .width(properties.getAvatarSize())
                .height(properties.getAvatarSize())
                .crop(CloudinaryConstants.CROP_FILL)
                .gravity(CloudinaryConstants.GRAVITY_FACE);
    }

    @Override
    public void validateFile(MultipartFile file, FileType fileType) {
        String[] allowed = (fileType == FileType.IMAGE)
                ? properties.getAllowedImageTypes()
                : properties.getAllowedVideoTypes();

        long maxSize = (fileType == FileType.IMAGE)
                ? properties.getMaxImageSize()
                : properties.getMaxVideoSize();

        if (file == null || file.isEmpty())
            throw new UserException(UserErrorCode.FILE_EMPTY);

        if (file.getContentType() == null
                || Arrays.stream(allowed).noneMatch(file.getContentType()::equalsIgnoreCase)) {
            throw new UserException(UserErrorCode.INVALID_FILE_TYPE, "Allowed: " + String.join(", ", allowed));
        }

        if (file.getSize() > maxSize) {
            throw new UserException(UserErrorCode.FILE_TOO_LARGE,
                    fileType.name() + " limit: " + fileType.getFormattedMaxSize(maxSize));
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
            return PostMedia.builder()
                    .url(result.get(CloudinaryConstants.RESPONSE_SECURE_URL).toString())
                    .type(fileType)
                    .build();
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
    public List<PostMedia> uploadMultiMedia(List<MultipartFile> images, List<MultipartFile> videos, String postId) {
        // Validate all files primarily
        validateFiles(images, videos);
        // Create threads to increase the response speed
        int poolSize = calculatePoolSize(images, videos);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        try {
            List<CompletableFuture<PostMedia>> futures = new ArrayList<>();

            submitUploads(futures, images, file -> uploadPostImage(file, postId), executor);
            submitUploads(futures, videos, file -> uploadPostVideo(file, postId), executor);

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        } finally {
            executor.shutdown();
        }
    }

    private void submitUploads(
            List<CompletableFuture<PostMedia>> futures,
            List<MultipartFile> files,
            Function<MultipartFile, PostMedia> uploadFunction,
            ExecutorService executor
    ) {
        if(files != null)
            for (MultipartFile file : files) {
                futures.add(
                        CompletableFuture.supplyAsync(() -> uploadFunction.apply(file), executor)
                );
            }
    }
    private void validateFiles(List<MultipartFile> images, List<MultipartFile> videos) {
        if(images != null)
            for (MultipartFile image : images) {
                validateFile(image, FileType.IMAGE);
            }
        if(videos != null)
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
            throw new SystemException(SystemErrorCode.EXTERNAL_SERVICE_ERROR, "Cloud storage error");
        }
    }
    private int calculatePoolSize(List<MultipartFile> images, List<MultipartFile> videos)
    {
        int imageCount = images != null ? images.size() : 0;
        int videoCount = videos != null ? videos.size() : 0;
        int maxPossibleThread = 10;
        return Math.max(imageCount + videoCount, maxPossibleThread);
    }
}
