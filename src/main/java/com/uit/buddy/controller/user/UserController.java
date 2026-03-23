package com.uit.buddy.controller.user;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.PageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.user.UpdateUserRequest;
import com.uit.buddy.dto.response.user.FoundUserResponse;
import com.uit.buddy.dto.response.user.UserResponse;
import com.uit.buddy.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "Endpoints for user profile and management")
public class UserController extends AbstractBaseController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Fetch detailed information of the currently authenticated student")
    public ResponseEntity<SingleResponse<UserResponse>> getMyProfile(@AuthenticationPrincipal String mssv) {
        log.info("[User Controller] Fetching profile for the current authenticated user");
        UserResponse response = userService.getMyProfile(mssv);
        return successSingle(response, "User profile retrieved successfully");
    }

    @PatchMapping("/update")
    @Operation(summary = "Update user profile", description = "Update specific fields of the user profile such as nickname, avatar, or bio")
    public ResponseEntity<SingleResponse<UserResponse>> updateProfile(@AuthenticationPrincipal String mssv,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("[User Controller] Request to update profile received");
        UserResponse response = userService.updateProfile(mssv, request);
        return successSingle(response, "Profile updated successfully!");
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload user avatar", description = "Upload a new avatar image for the authenticated user")
    public ResponseEntity<SingleResponse<String>> uploadAvatar(@RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String mssv) {
        log.info("[User Controller] Request to upload avatar received");
        String avatarUrl = userService.uploadAvatar(mssv, file);
        return successSingle(avatarUrl, "Avatar uploaded successfully!");
    }

    @GetMapping("/{mssv}")
    @Operation(summary = "Get other student profile", description = "Fetch detailed information of other student")
    public ResponseEntity<SingleResponse<UserResponse>> getOtherStudentProfile(@PathVariable String mssv) {
        log.info("[User Controller] Fetching profile for student has mssv: {}", mssv);
        UserResponse response = userService.getMyProfile(mssv);
        return successSingle(response, "User profile retrieved successfully");
    }

    @GetMapping("search")
    @Operation(summary = "Search student", description = "Search information of other student with keyword and filter")
    public ResponseEntity<PageResponse<FoundUserResponse>> searchStudentByKeywordAndFilters(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "created_at") String sortBy, @RequestParam(required = false) String keyword) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<FoundUserResponse> responses = userService.searchStudentByKeyword(keyword, pageable);
        return paging(responses, "Search user with keyword and filter successfully");
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Delete Account", description = "Permanently delete user's account and all associated data")
    public ResponseEntity<SuccessResponse> deleteAccount(@AuthenticationPrincipal String mssv) {
        userService.deleteStudentAccount(mssv);
        return success("Account deleted successfully! We're sorry to see you go.");
    }
}
