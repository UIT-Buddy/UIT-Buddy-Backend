package com.uit.buddy.constant;

public final class CloudinaryConstants {

    private CloudinaryConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String FOLDER_AVATARS = "avatars";

    // Cloudinary upload parameter keys
    public static final String PARAM_PUBLIC_ID = "public_id";
    public static final String PARAM_FOLDER = "folder";
    public static final String PARAM_OVERWRITE = "overwrite";
    public static final String PARAM_RESOURCE_TYPE = "resource_type";
    public static final String PARAM_TRANSFORMATION = "transformation";

    // Resource types
    public static final String RESOURCE_TYPE_IMAGE = "image";

    // Response keys
    public static final String RESPONSE_SECURE_URL = "secure_url";
    public static final String RESPONSE_RESULT = "result";

    // Default avatar URL
    public static final String DEFAULT_AVATAR_URL = "https://res.cloudinary.com/dgogrzt5d/image/upload/v1771910699/avatar-trang-4_jdjzjm.jpg";

    // Image sizes
    public static final int AVATAR_SIZE = 400;
    public static final int THUMBNAIL_SIZE = 150;
    public static final int POST_IMAGE_WIDTH = 1200;
    public static final int POST_IMAGE_HEIGHT = 800;

    // Transformation presets
    public static final String CROP_FILL = "fill";
    public static final String CROP_FIT = "fit";
    public static final String CROP_LIMIT = "limit";
    public static final String GRAVITY_FACE = "face";
    public static final String GRAVITY_CENTER = "center";

    // Image validation
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_TYPES = { "image/jpeg", "image/jpg", "image/png", "image/webp" };
}
