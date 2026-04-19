package com.uit.buddy.service.user.impl;

import com.uit.buddy.client.UitClient;
import com.uit.buddy.dto.request.user.UpdateUserRequest;
import com.uit.buddy.dto.request.user.UpdateUserSettingRequest;
import com.uit.buddy.dto.request.user.UpdateWsTokenRequest;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import com.uit.buddy.dto.response.user.FoundUserResponse;
import com.uit.buddy.dto.response.user.UserResponse;
import com.uit.buddy.dto.response.user.UserSettingResponse;
import com.uit.buddy.entity.academic.AcademicSummary;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.entity.user.UserSetting;
import com.uit.buddy.enums.FriendStatus;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.user.UserMapper;
import com.uit.buddy.mapper.user.UserSettingMapper;
import com.uit.buddy.repository.academic.AcademicSummaryRepository;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.repository.user.UserSettingRepository;
import com.uit.buddy.service.cometchat.CometChatService;
import com.uit.buddy.service.encryption.WsTokenEncryptionService;
import com.uit.buddy.service.file.FileService;
import com.uit.buddy.service.social.FriendService;
import com.uit.buddy.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final StudentRepository studentRepository;
    private final UserMapper userMapper;
    private final FileService fileService;
    private final CometChatService cometChatService;
    private final UserSettingRepository userSettingRepository;
    private final UserSettingMapper userSettingMapper;
    private final FriendService friendService;
    private final AcademicSummaryRepository academicSummaryRepository;
    private final PostRepository postRepository;
    private final UitClient uitClient;
    private final WsTokenEncryptionService wsTokenEncryptionService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String mssv) {
        log.info("[User Service] Fetching profile for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        UserResponse baseProfile = userMapper.toUserResponse(student, FriendStatus.NONE);
        return enrichMyProfile(baseProfile, mssv);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getOtherUserProfile(String targetMssv, String currentUserMssv) {
        log.info("[User Service] Fetching profile for target MSSV: {} by current user: {}", targetMssv,
                currentUserMssv);

        Student student = studentRepository.findById(targetMssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        FriendStatus friendStatus = friendService.getFriendStatus(currentUserMssv, targetMssv);

        return userMapper.toUserResponse(student, friendStatus);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String mssv, UpdateUserRequest request) {
        log.info("[User Service] Updating profile for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        if (request.bio() != null) {
            log.debug("[User Service] Updating bio for MSSV: {}", mssv);
            student.setBio(request.bio());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("[User Service] Successfully updated profile for MSSV: {}", mssv);

        UserResponse baseProfile = userMapper.toUserResponse(updatedStudent, FriendStatus.NONE);
        return enrichMyProfile(baseProfile, mssv);
    }

    @Override
    @Transactional
    public String uploadAvatar(String mssv, MultipartFile file) {
        log.info("[User Service] Uploading avatar for MSSV: {}", mssv);

        if (file == null || file.isEmpty()) {
            throw new UserException(UserErrorCode.FILE_EMPTY);
        }

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        String avatarUrl = fileService.uploadAvatar(file, mssv);

        student.setAvatarUrl(avatarUrl);
        studentRepository.save(student);

        cometChatService.syncUserAvatar(student.getCometUid(), student.getFullName(), avatarUrl);

        log.info("[User Service] Successfully uploaded avatar for MSSV: {}", mssv);
        return avatarUrl;
    }

    @Override
    @Transactional
    public String uploadCover(String mssv, MultipartFile file) {
        log.info("[User Service] Uploading cover for MSSV: {}", mssv);

        if (file == null || file.isEmpty()) {
            throw new UserException(UserErrorCode.FILE_EMPTY);
        }

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        String coverUrl = fileService.uploadCover(file, mssv);

        student.setCoverUrl(coverUrl);
        studentRepository.save(student);

        log.info("[User Service] Successfully uploaded cover for MSSV: {}", mssv);
        return coverUrl;
    }

    @Override
    public Page<FoundUserResponse> searchStudentByKeyword(String keyword, String currentUserMssv, Pageable pageable) {
        Page<Student> page = studentRepository.searchStudentByKeyword(keyword, pageable);
        log.info("[UserService]: fetching user with keyword and filter");

        return page.map(student -> {
            FriendStatus friendStatus = friendService.getFriendStatus(currentUserMssv, student.getMssv());
            return userMapper.toFoundUserResponse(student, friendStatus);
        });
    }

    @Override
    @Transactional
    public void deleteStudentAccount(String mssv) {
        log.info("[User Service] Deleting account for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        studentRepository.delete(student);
        log.info("[User Service] Successfully deleted account for MSSV: {}", mssv);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSettingResponse getUserSettings(String mssv) {
        UserSetting userSetting = userSettingRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));
        return userSettingMapper.toResponse(userSetting);
    }

    @Override
    @Transactional
    public void updateUserSettings(String mssv, UpdateUserSettingRequest request) {
        UserSetting userSetting = userSettingRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        userSetting.setEnableNotification(request.enableNotification());
        userSetting.setEnableScheduleReminder(request.enableScheduleReminder());

        userSettingRepository.save(userSetting);
    }

    @Override
    @Transactional
    public void updateWsToken(String mssv, UpdateWsTokenRequest request) {
        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        SiteInfoResponse siteInfo;
        try {
            siteInfo = uitClient.fetchSiteInfo(request.wstoken());
        } catch (RestClientException e) {
            log.error("[User Service] Failed to connect Moodle API while updating wstoken for MSSV: {}", mssv, e);
            throw new UserException(UserErrorCode.INVALID_WSTOKEN, "Failed to validate WsToken");
        } catch (Exception e) {
            log.warn("[User Service] Invalid wstoken while updating for MSSV: {}", mssv, e);
            throw new UserException(UserErrorCode.INVALID_WSTOKEN);
        }

        if (siteInfo.username() == null || siteInfo.username().isBlank()
                || !mssv.equalsIgnoreCase(siteInfo.username().trim())) {
            throw new UserException(UserErrorCode.INVALID_WSTOKEN, "WsToken does not belong to current user");
        }

        student.setEncryptedWstoken(wsTokenEncryptionService.encryptWstoken(request.wstoken()));
        studentRepository.save(student);
    }

    private UserResponse enrichMyProfile(UserResponse baseProfile, String mssv) {
        AcademicSummary academicSummary = academicSummaryRepository.findByMssv(mssv).orElse(null);

        Float accumulatedGpa = academicSummary != null ? academicSummary.getAccumulatedGpa() : null;
        Integer accumulatedCredits = academicSummary != null ? academicSummary.getAccumulatedCredits() : 0;
        Long postCount = postRepository.countByMssv(mssv);

        return new UserResponse(baseProfile.mssv(), baseProfile.fullName(), baseProfile.email(),
                baseProfile.avatarUrl(), baseProfile.coverUrl(), baseProfile.bio(), baseProfile.homeClassCode(),
                baseProfile.friendStatus(), accumulatedGpa, accumulatedCredits, postCount);
    }
}
