package com.uit.buddy.service.social;

import com.uit.buddy.dto.request.social.CreateCommentRequest;
import com.uit.buddy.dto.request.social.UpdateCommentRequest;
import com.uit.buddy.dto.response.social.CommentResponse;
import java.util.List;
import java.util.UUID;

public interface CommentService {

  void createComment(UUID postId, String mssv, CreateCommentRequest request);

  void replyToComment(UUID commentId, String mssv, CreateCommentRequest request);

  void updateComment(UUID commentId, String mssv, UpdateCommentRequest request);

  void deleteComment(UUID commentId, String mssv);

  boolean toggleCommentLike(UUID commentId, String mssv);

  List<CommentResponse> getPostComments(UUID postId, String mssv, String cursor, int limit);

  List<CommentResponse> getCommentReplies(UUID commentId, String mssv, String cursor, int limit);
}
