package com.uit.buddy.service.social;

import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.PostResponse;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public interface PostService {

    PostResponse createPost(String mssv, CreatePostRequest request, MultipartFile image, MultipartFile video);

    PostResponse updatePost(UUID postId, String mssv, UpdatePostRequest request);

    void deletePost(UUID postId, String mssv);

}
