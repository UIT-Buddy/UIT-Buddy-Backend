package com.uit.buddy.service.user.impl;

import com.uit.buddy.dto.request.user.UpdateUserRequest;
import com.uit.buddy.dto.response.user.UserResponse;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.mapper.user.UserMapper;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final StudentRepository studentRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(@AuthenticationPrincipal String mssv) {
        log.info("[User Service] Fetching profile for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.STUDENT_NOT_FOUND));

        return userMapper.toUserResponse(student);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(@AuthenticationPrincipal String mssv, UpdateUserRequest request) {
        log.info("[User Service] Updating profile for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.STUDENT_NOT_FOUND));

        if (request.avatarUrl() != null) {
            log.debug("[User Service] Updating avatarUrl for MSSV: {}", mssv);
            student.setAvatarUrl(request.avatarUrl());
        }

        if (request.bio() != null) {
            log.debug("[User Service] Updating bio for MSSV: {}", mssv);
            student.setBio(request.bio());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("[User Service] Successfully updated profile for MSSV: {}", mssv);

        return userMapper.toUserResponse(updatedStudent);
    }
}