package com.uit.buddy.service.user.impl;

import com.uit.buddy.dto.request.user.UpdateUserRequest;
import com.uit.buddy.dto.response.user.FoundUserResponse;
import com.uit.buddy.dto.response.user.UserResponse;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.user.UserMapper;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import com.uit.buddy.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(@AuthenticationPrincipal String mssv) {
        log.info("[User Service] Fetching profile for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        return userMapper.toUserResponse(student);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(@AuthenticationPrincipal String mssv, UpdateUserRequest request) {
        log.info("[User Service] Updating profile for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        if (request.bio() != null) {
            log.debug("[User Service] Updating bio for MSSV: {}", mssv);
            student.setBio(request.bio());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("[User Service] Successfully updated profile for MSSV: {}", mssv);

        return userMapper.toUserResponse(updatedStudent);
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

        log.info("[User Service] Successfully uploaded avatar for MSSV: {}", mssv);
        return avatarUrl;
    }

    @Override
    public Page<FoundUserResponse> searchStudentByKeyword(String keyword, Pageable pageable) {
        Page<Student> page = studentRepository.searchStudentByKeyword(keyword, pageable);
        log.info("[UserService]: fetching user with keyword and filter");
        return page.map(userMapper::toFoundUserResponse);
    }



}