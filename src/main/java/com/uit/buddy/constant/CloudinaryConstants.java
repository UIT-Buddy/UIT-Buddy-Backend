package com.uit.buddy.constant;

public final class CloudinaryConstants {

  private CloudinaryConstants() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String FOLDER_AVATARS = "avatars";
  public static final String FOLDER_POST_IMAGES = "posts/images";
  public static final String FOLDER_POST_VIDEOS = "posts/videos";

  // Cloudinary upload parameter keys
  public static final String PARAM_PUBLIC_ID = "public_id";
  public static final String PARAM_FOLDER = "folder";
  public static final String PARAM_OVERWRITE = "overwrite";
  public static final String PARAM_RESOURCE_TYPE = "resource_type";
  public static final String PARAM_TRANSFORMATION = "transformation";

  // Resource types
  public static final String RESOURCE_TYPE_IMAGE = "image";
  public static final String RESOURCE_TYPE_VIDEO = "video";

  // Response keys
  public static final String RESPONSE_SECURE_URL = "secure_url";
  public static final String RESPONSE_RESULT = "result";

  // Transformation presets
  public static final String CROP_FILL = "fill";
  public static final String CROP_FIT = "fit";
  public static final String CROP_LIMIT = "limit";
  public static final String GRAVITY_FACE = "face";
  public static final String GRAVITY_CENTER = "center";
}
