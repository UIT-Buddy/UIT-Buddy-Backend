package com.uit.buddy.service.social;

import com.uit.buddy.dto.request.social.SharePostRequest;
import com.uit.buddy.dto.response.social.UserShareResponse;
import com.uit.buddy.enums.ShareType;
import java.util.List;
import java.util.UUID;

public interface ShareService {

  boolean sharePost(UUID postId, String mssv, ShareType type, SharePostRequest request);

  List<UserShareResponse> getPostShares(UUID postId, String mssv, String cursor, int limit);
}
