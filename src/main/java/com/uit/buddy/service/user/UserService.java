package com.uit.buddy.service.user;

import com.uit.buddy.dto.request.user.UpdateUserRequest;
import com.uit.buddy.dto.response.user.FoundUserResponse;
import com.uit.buddy.dto.response.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse getMyProfile(String mssv);

    UserResponse updateProfile(String mssv, UpdateUserRequest request);

    String uploadAvatar(String mssv, MultipartFile file);

    Page<FoundUserResponse> searchStudentByKeyword(String keyword, Pageable pageable);

    void deleteStudentAccount(String mssv);
}
