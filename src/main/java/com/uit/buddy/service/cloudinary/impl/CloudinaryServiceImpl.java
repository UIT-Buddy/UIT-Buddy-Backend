package com.uit.buddy.service.cloudinary.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.uit.buddy.constant.CloudinaryConstants;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.service.cloudinary.CloudinaryService;

import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private static final Transformation<?> AVATAR_TRANSFORM = new Transformation<>()
            .width(CloudinaryConstants.AVATAR_SIZE)
            .height(CloudinaryConstants.AVATAR_SIZE)
            .crop(CloudinaryConstants.CROP_FILL)
            .gravity(CloudinaryConstants.GRAVITY_FACE);

    @Override
    public String createDefaultAvatar(String mssv) {
        return executeUpload(CloudinaryConstants.DEFAULT_AVATAR_URL, mssv);
    }

    @Override
    public void deleteAvatar(String publicId) {
        try {
            log.info("[Cloudinary Service] Initiating cleanup for publicId: {}", publicId);
            String fullPath = CloudinaryConstants.FOLDER_AVATARS + "/" + publicId;
            Map<?, ?> result = cloudinary.uploader().destroy(fullPath, Map.of());
            log.info("[Cloudinary Service] Cleanup result for {}: {}", publicId,
                    result.get(CloudinaryConstants.RESPONSE_RESULT));
        } catch (Exception e) {
            log.error("[Cloudinary Service] Failed to delete image {}: {}", publicId, e.getMessage());
        }
    }

    @Override
    public String uploadAvatar(MultipartFile file, String mssv) {
        validateImage(file);

        try {
            byte[] fileBytes = file.getBytes();
            return executeUpload(fileBytes, mssv);
        } catch (IOException e) {
            log.error("[Cloudinary Service] Failed to read file bytes for user {}: {}", mssv, e.getMessage());
            throw new SystemException(
                    SystemErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Failed to process image file");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("[Cloudinary Service] File is null or empty");
            throw new UserException(UserErrorCode.FILE_EMPTY);
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(CloudinaryConstants.ALLOWED_IMAGE_TYPES).contains(contentType)) {
            log.warn("[Cloudinary Service] Invalid file type: {}", contentType);
            throw new UserException(UserErrorCode.INVALID_FILE_TYPE,
                    "Only JPEG, PNG, and WebP images are allowed");
        }

        if (file.getSize() > CloudinaryConstants.MAX_FILE_SIZE) {
            log.warn("[Cloudinary Service] File size {} exceeds maximum {}",
                    file.getSize(), CloudinaryConstants.MAX_FILE_SIZE);
            throw new UserException(UserErrorCode.FILE_TOO_LARGE,
                    "File size must not exceed 5MB");
        }
    }

    private String executeUpload(Object source, String publicId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put(CloudinaryConstants.PARAM_PUBLIC_ID, publicId);
            params.put(CloudinaryConstants.PARAM_FOLDER, CloudinaryConstants.FOLDER_AVATARS);
            params.put(CloudinaryConstants.PARAM_OVERWRITE, true);
            params.put(CloudinaryConstants.PARAM_RESOURCE_TYPE, CloudinaryConstants.RESOURCE_TYPE_IMAGE);
            params.put(CloudinaryConstants.PARAM_TRANSFORMATION, AVATAR_TRANSFORM);

            Map<?, ?> result = cloudinary.uploader().upload(source, params);

            return result.get(CloudinaryConstants.RESPONSE_SECURE_URL).toString();

        } catch (Exception e) {
            log.error("Cloudinary upload failed for {}: {}", publicId, e.getMessage());
            throw new SystemException(
                    SystemErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Cloud storage service unavailable");
        }
    }
}