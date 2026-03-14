package com.uit.buddy.service.cloudinary;

import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.enums.FileType;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    String createDefaultAvatar(String mssv);

    void deleteAvatar(String publicId);

    String uploadAvatar(MultipartFile file, String mssv);

    PostMedia uploadPostImage(MultipartFile file, String postId);

    PostMedia uploadPostVideo(MultipartFile file, String postId);

    void deletePostMedia(List<PostMedia> medias);

    void validateFile(MultipartFile file, FileType fileType);

    List<PostMedia> uploadMultiMedia(List<MultipartFile> images, List<MultipartFile> videos);
}
