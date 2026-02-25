package com.uit.buddy.service.user;

import com.uit.buddy.dto.request.user.UpdateUserRequest;
import com.uit.buddy.dto.response.user.UserResponse;

public interface UserService {
    UserResponse getMyProfile(String mssv);

    UserResponse updateProfile(String mssv, UpdateUserRequest request);
}