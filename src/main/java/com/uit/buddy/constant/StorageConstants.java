package com.uit.buddy.constant;

import com.uit.buddy.enums.FileType;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class StorageConstants {

    private StorageConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String FOLDER_AVATARS = "avatars";
    public static final String FOLDER_POST_IMAGES = "posts/images";
    public static final String FOLDER_POST_VIDEOS = "posts/videos";
    public static final String FOLDER_DOCUMENT_FILES = "documents/files";

    public static final String DEFAULT_FILE_NAME = "document-file";
    public static final String ROOT_FOLDER_NAME = "Storage";
    public static final String PATH_SEPARATOR = "/";

    private static final Map<String, FileType> FILE_TYPE_MAP = new HashMap<>();
    private static final Map<String, String> CONTENT_TYPE_EXTENSION_MAP =
            new HashMap<>();

    static {
        FILE_TYPE_MAP.put("png", FileType.IMAGE);
        FILE_TYPE_MAP.put("jpg", FileType.IMAGE);
        FILE_TYPE_MAP.put("jpeg", FileType.IMAGE);
        FILE_TYPE_MAP.put("webp", FileType.IMAGE);

        FILE_TYPE_MAP.put("mp4", FileType.VIDEO);
        FILE_TYPE_MAP.put("webm", FileType.VIDEO);

        FILE_TYPE_MAP.put("doc", FileType.WORD);
        FILE_TYPE_MAP.put("docx", FileType.WORD);
        FILE_TYPE_MAP.put("odt", FileType.WORD);
        FILE_TYPE_MAP.put("txt", FileType.WORD);
        FILE_TYPE_MAP.put("pdf", FileType.WORD);

        FILE_TYPE_MAP.put("xls", FileType.EXCEL);
        FILE_TYPE_MAP.put("xlsx", FileType.EXCEL);
        FILE_TYPE_MAP.put("csv", FileType.EXCEL);

        FILE_TYPE_MAP.put("ppt", FileType.PPT);
        FILE_TYPE_MAP.put("pptx", FileType.PPT);

        CONTENT_TYPE_EXTENSION_MAP.put("image/jpeg", "jpg");
        CONTENT_TYPE_EXTENSION_MAP.put("image/jpg", "jpg");
        CONTENT_TYPE_EXTENSION_MAP.put("image/png", "png");
        CONTENT_TYPE_EXTENSION_MAP.put("image/webp", "webp");
        CONTENT_TYPE_EXTENSION_MAP.put("video/mp4", "mp4");
        CONTENT_TYPE_EXTENSION_MAP.put("video/webm", "webm");
        CONTENT_TYPE_EXTENSION_MAP.put("application/pdf", "pdf");
        CONTENT_TYPE_EXTENSION_MAP.put("application/msword", "doc");
        CONTENT_TYPE_EXTENSION_MAP.put(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx");
        CONTENT_TYPE_EXTENSION_MAP.put("text/plain", "txt");
        CONTENT_TYPE_EXTENSION_MAP.put("application/vnd.ms-excel", "xls");
        CONTENT_TYPE_EXTENSION_MAP.put(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "xlsx");
        CONTENT_TYPE_EXTENSION_MAP.put("text/csv", "csv");
        CONTENT_TYPE_EXTENSION_MAP.put("application/vnd.ms-powerpoint", "ppt");
        CONTENT_TYPE_EXTENSION_MAP.put(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "pptx");
    }

    public static FileType extractFileType(String fileName) {
        String extension = extractExtension(fileName);
        if (extension == null) {
            return FileType.OTHER;
        }

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

    public static String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return null;
        }

        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    public static String resolveExtension(String fileName, String contentType) {
        String extension = extractExtension(fileName);
        if (extension != null) {
            return extension;
        }

        if (contentType == null || contentType.isBlank()) {
            return null;
        }

        return CONTENT_TYPE_EXTENSION_MAP.get(contentType.toLowerCase(Locale.ROOT));
    }
}
