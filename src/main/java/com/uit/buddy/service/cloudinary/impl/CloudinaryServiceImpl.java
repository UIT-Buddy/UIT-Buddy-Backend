package com.uit.buddy.service.cloudinary.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.uit.buddy.config.CloudinaryProperties;
import com.uit.buddy.constant.CloudinaryConstants;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public String createDefaultAvatar(String mssv) {
        return executeUpload(properties.getDefaultAvatarUrl(), mssv,
                CloudinaryConstants.FOLDER_AVATARS,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE,
                getAvatarTransform());
    }

    @Override
    public String uploadAvatar(MultipartFile file, String mssv) {
        validateFile(file, FileType.IMAGE);
        return executeUpload(extractBytes(file), mssv,
                CloudinaryConstants.FOLDER_AVATARS,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE,
                getAvatarTransform());
    }

    @Override
    public String uploadPostImage(MultipartFile file, String postId) {
        validateFile(file, FileType.IMAGE);
        Transformation<?> postTransform = new Transformation<>()
                .width(properties.getPostImageWidth())
                .height(properties.getPostImageHeight())
                .crop(CloudinaryConstants.CROP_LIMIT);

        return executeUpload(extractBytes(file), postId,
                CloudinaryConstants.FOLDER_POST_IMAGES,
                CloudinaryConstants.RESOURCE_TYPE_IMAGE,
                postTransform);
    }

    @Override
    public String uploadPostVideo(MultipartFile file, String postId) {
        validateFile(file, FileType.VIDEO);
        return executeUpload(extractBytes(file), postId,
                CloudinaryConstants.FOLDER_POST_VIDEOS,
                CloudinaryConstants.RESOURCE_TYPE_VIDEO,
                null);
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

    private String executeUpload(Object source, String publicId, String folder, String resType,
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
}