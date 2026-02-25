package com.uit.buddy.service.cloudinary;

public interface CloudinaryService {
    String uploadAvatarFromUrl(String imageUrl, String publicId);

    String createDefaultAvatar(String mssv);

    void deleteAvatar(String publicId);
}
