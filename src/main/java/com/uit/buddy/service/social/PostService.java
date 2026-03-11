package com.uit.buddy.service.social;

import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostDetailResponse createPost(String mssv, String title, String content, CreatePostRequest request);

    PostDetailResponse updatePost(UUID postId, String mssv, UpdatePostRequest request);

    void deletePost(UUID postId, String mssv);

    List<PostFeedResponse> getPostFeed(String mssv, String cursor, int limit);

    PostDetailResponse getPostDetail(UUID postId, String mssv);

    Page<PostFeedResponse> searchPost(String keyword, String mssv, Pageable pageable);
}
