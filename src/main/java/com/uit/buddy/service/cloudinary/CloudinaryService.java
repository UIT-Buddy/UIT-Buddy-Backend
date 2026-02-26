package com.uit.buddy.service.cloudinary;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    String createDefaultAvatar(String mssv);

    void deleteAvatar(String publicId);

    String uploadAvatar(MultipartFile file, String mssv);
}
