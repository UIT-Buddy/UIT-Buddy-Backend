package com.uit.buddy.controller.user;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.user.UpdateUserRequest;
import com.uit.buddy.dto.request.user.FcmTokenRequest;
import com.uit.buddy.dto.response.user.UserResponse;
import com.uit.buddy.service.user.UserService;
import com.uit.buddy.service.fcm.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "Endpoints for user profile and management")
public class UserController extends AbstractBaseController {

    private final UserService userService;
    private final FcmService fcmService;

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
    public ResponseEntity<SingleResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String mssv) {
        log.info("[User Controller] Request to upload avatar received");
        String avatarUrl = userService.uploadAvatar(mssv, file);
        return successSingle(avatarUrl, "Avatar uploaded successfully!");
    }

    @PatchMapping("/fcm-token")
    @Operation(summary = "Sync FCM Token", description = "Register or update FCM token for multi-device support")
    public ResponseEntity<SingleResponse<Void>> syncFcmToken(
            @AuthenticationPrincipal String mssv,
            @Valid @RequestBody FcmTokenRequest request) {

        log.info("[User Controller] Syncing FCM token for MSSV: {}", mssv);
        fcmService.syncDeviceToken(mssv, request.fcmToken());

        return successSingle(null, "FCM token synced successfully!");
    }
    @GetMapping("/{mssv}")
    @Operation(summary = "Get other student profile", description = "Fetch detailed information of other student")
    public ResponseEntity<SingleResponse<UserResponse>> getOtherStudentProfile(@PathVariable String mssv)
    {
        log.info("[User Controller] Fetching profile for student has mssv: {}", mssv);
        UserResponse response = userService.getMyProfile(mssv);
        return successSingle(response, "User profile retrieved successfully");
    }
}