package com.uit.buddy.service.cloudinary.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.uit.buddy.constant.CloudinaryConstants;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public String uploadAvatarFromUrl(String imageUrl, String publicId) {
        log.info("Uploading avatar directly from URL for MSSV: {}", publicId);
        return executeUpload(imageUrl, publicId);
    }

    @Override
    public String createDefaultAvatar(String mssv) {
        log.info("Creating default avatar copy for MSSV: {}", mssv);

        try {
            return executeUpload(CloudinaryConstants.DEFAULT_AVATAR_URL, mssv);

        } catch (Exception e) {
            log.error("Failed to create default avatar copy for MSSV: {}. Falling back to constant URL.", mssv);
            return CloudinaryConstants.DEFAULT_AVATAR_URL;
        }
    }

    private String executeUpload(Object source, String publicId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("public_id", publicId);
            params.put("folder", CloudinaryConstants.FOLDER_AVATARS);
            params.put("overwrite", true);
            params.put("resource_type", "image");
            params.put("transformation", AVATAR_TRANSFORM);

            Map<?, ?> result = cloudinary.uploader().upload(source, params);

            return result.get("secure_url").toString();

        } catch (Exception e) {
            log.error("Cloudinary upload failed for {}: {}", publicId, e.getMessage());
            throw new SystemException(
                    SystemErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Cloud storage service unavailable");
        }
    }
}