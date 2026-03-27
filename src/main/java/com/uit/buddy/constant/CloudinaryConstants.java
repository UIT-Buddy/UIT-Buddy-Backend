package com.uit.buddy.constant;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.uit.buddy.enums.FileType;

public final class CloudinaryConstants {

    private CloudinaryConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String FOLDER_AVATARS = "avatars";
    public static final String FOLDER_POST_IMAGES = "posts/images";
    public static final String FOLDER_POST_VIDEOS = "posts/videos";
    public static final String FOLDER_DOCUMENT_FILES = "documents/files";

    // Document upload constants
    public static final String DOCUMENT_PUBLIC_ID_PREFIX = "doc";
    public static final String PATH_SEPARATOR = "/";
    public static final String DEFAULT_FILE_NAME = "document-file";

    // File type extension mappings
    private static final Map<String, FileType> FILE_TYPE_MAP = new HashMap<>();

    static {
        // Image extensions
        FILE_TYPE_MAP.put("png", FileType.IMAGE);
        FILE_TYPE_MAP.put("jpg", FileType.IMAGE);
        FILE_TYPE_MAP.put("jpeg", FileType.IMAGE);
        FILE_TYPE_MAP.put("webp", FileType.IMAGE);

        // Video extensions
        FILE_TYPE_MAP.put("mp4", FileType.VIDEO);
        FILE_TYPE_MAP.put("webm", FileType.VIDEO);

        // Word extensions
        FILE_TYPE_MAP.put("doc", FileType.WORD);
        FILE_TYPE_MAP.put("docx", FileType.WORD);
        FILE_TYPE_MAP.put("odt", FileType.WORD);
        FILE_TYPE_MAP.put("txt", FileType.WORD);
        FILE_TYPE_MAP.put("pdf", FileType.WORD);

        // Excel extensions
        FILE_TYPE_MAP.put("xls", FileType.EXCEL);
        FILE_TYPE_MAP.put("xlsx", FileType.EXCEL);
        FILE_TYPE_MAP.put("csv", FileType.EXCEL);

        // PowerPoint extensions
        FILE_TYPE_MAP.put("ppt", FileType.PPT);
        FILE_TYPE_MAP.put("pptx", FileType.PPT);
    }

    // Cloudinary upload parameter keys
    public static final String PARAM_PUBLIC_ID = "public_id";
    public static final String PARAM_FOLDER = "folder";
    public static final String PARAM_OVERWRITE = "overwrite";
    public static final String PARAM_RESOURCE_TYPE = "resource_type";
    public static final String PARAM_TRANSFORMATION = "transformation";

    // Resource types
    public static final String RESOURCE_TYPE_IMAGE = "image";
    public static final String RESOURCE_TYPE_VIDEO = "video";
    public static final String RESOURCE_TYPE_RAW = "raw";

    // Response keys
    public static final String RESPONSE_SECURE_URL = "secure_url";
    public static final String RESPONSE_RESULT = "result";

    // Transformation presets
    public static final String CROP_FILL = "fill";
    public static final String CROP_FIT = "fit";
    public static final String CROP_LIMIT = "limit";
    public static final String GRAVITY_FACE = "face";
    public static final String GRAVITY_CENTER = "center";

    // Folder default
    public static final String ROOT_FOLDER_NAME = "Storage";
    
    public static FileType extractFileType(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return FileType.OTHER;
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return FileType.OTHER;
        }

        String extension = fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
        return FILE_TYPE_MAP.getOrDefault(extension, FileType.OTHER);
    }

    public static String normalizeFileName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return DEFAULT_FILE_NAME;
        }
        return originalName.trim();
    }

    public static float toMegabytes(long sizeInBytes) {
        return sizeInBytes / (1024f * 1024f);
    }
}
