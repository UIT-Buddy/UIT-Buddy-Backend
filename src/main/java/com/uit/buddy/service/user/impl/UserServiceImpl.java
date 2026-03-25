package com.uit.buddy.service.user.impl;

import com.uit.buddy.dto.request.user.UpdateUserRequest;
import com.uit.buddy.dto.request.user.UpdateUserSettingRequest;
import com.uit.buddy.dto.response.user.FoundUserResponse;
import com.uit.buddy.dto.response.user.UserResponse;
import com.uit.buddy.dto.response.user.UserSettingResponse;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.entity.user.UserSetting;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.user.UserMapper;
import com.uit.buddy.mapper.user.UserSettingMapper;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.repository.user.UserSettingRepository;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import com.uit.buddy.service.cometchat.CometChatService;
import com.uit.buddy.service.social.FriendService;
import com.uit.buddy.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final StudentRepository studentRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final CometChatService cometChatService;
    private final UserSettingRepository userSettingRepository;
    private final UserSettingMapper userSettingMapper;
    private final FriendService friendService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String mssv) {
        log.info("[User Service] Fetching profile for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        return userMapper.toUserResponse(student, false);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getOtherUserProfile(String targetMssv, String currentUserMssv) {
        log.info("[User Service] Fetching profile for target MSSV: {} by current user: {}", targetMssv,
                currentUserMssv);

        Student student = studentRepository.findById(targetMssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        boolean isFriend = friendService.areFriends(currentUserMssv, targetMssv);

        return userMapper.toUserResponse(student, isFriend);
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

        return userMapper.toUserResponse(updatedStudent, false);
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

        String avatarUrl = cloudinaryService.uploadAvatar(file, mssv);

        student.setAvatarUrl(avatarUrl);
        studentRepository.save(student);

        cometChatService.syncUserAvatar(student.getCometUid(), student.getFullName(), avatarUrl);

        log.info("[User Service] Successfully uploaded avatar for MSSV: {}", mssv);
        return avatarUrl;
    }

    @Override
    public Page<FoundUserResponse> searchStudentByKeyword(String keyword, String currentUserMssv, Pageable pageable) {
        Page<Student> page = studentRepository.searchStudentByKeyword(keyword, pageable);
        log.info("[UserService]: fetching user with keyword and filter");

        return page.map(student -> {
            boolean isFriend = friendService.areFriends(currentUserMssv, student.getMssv());
            return userMapper.toFoundUserResponse(student, isFriend);
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
}
