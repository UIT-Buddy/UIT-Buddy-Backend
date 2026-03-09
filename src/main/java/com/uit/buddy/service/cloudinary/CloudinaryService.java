package com.uit.buddy.service.cloudinary;

import org.springframework.web.multipart.MultipartFile;

import com.uit.buddy.enums.FileType;

public interface CloudinaryService {

    String createDefaultAvatar(String mssv);

    void deleteAvatar(String publicId);

    String uploadAvatar(MultipartFile file, String mssv);

    String uploadPostImage(MultipartFile file, String postId);

    String uploadPostVideo(MultipartFile file, String postId);

    void deletePostMedia(String postId);

    void validateFile(MultipartFile file, FileType fileType);
}
