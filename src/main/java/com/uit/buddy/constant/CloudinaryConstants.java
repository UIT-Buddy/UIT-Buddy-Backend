package com.uit.buddy.constant;

public final class CloudinaryConstants {

    private CloudinaryConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String FOLDER_AVATARS = "avatars";

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
}
