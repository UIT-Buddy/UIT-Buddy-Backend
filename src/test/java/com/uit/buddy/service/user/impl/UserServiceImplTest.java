package com.uit.buddy.service.user.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.uit.buddy.dto.request.user.UpdateUserSettingRequest;
import com.uit.buddy.dto.response.user.UserSettingResponse;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.entity.user.UserSetting;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.user.UserMapper;
import com.uit.buddy.mapper.user.UserSettingMapper;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.repository.user.UserSettingRepository;
import com.uit.buddy.service.cometchat.CometChatService;
import com.uit.buddy.service.file.FileService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserSettingRepository userSettingRepository;

    @Mock
    private FileService fileService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserSettingMapper userSettingMapper;

    @Mock
    private CometChatService cometChatService;

    @InjectMocks
    private UserServiceImpl userService;

    private String mssv;
    private Student student;
    private UserSetting userSetting;

    @BeforeEach
    void setUp() {
        mssv = "22100001";

        student = new Student();
        student.setMssv(mssv);
        student.setFullName("Test Student");

        userSetting = new UserSetting();
        userSetting.setMssv(mssv);
        userSetting.setEnableNotification(true);
        userSetting.setEnableScheduleReminder(true);
        userSetting.setStudent(student);
    }

    @Test
    void shouldGetUserSettingsSuccessfully() {
        UserSettingResponse expectedResponse = UserSettingResponse.builder().enableNotification(true)
                .enableScheduleReminder(true).build();

        when(userSettingRepository.findById(mssv)).thenReturn(Optional.of(userSetting));
        when(userSettingMapper.toResponse(userSetting)).thenReturn(expectedResponse);

        UserSettingResponse result = userService.getUserSettings(mssv);

        assertThat(result).isNotNull();
        assertThat(result.isEnableNotification()).isTrue();
        assertThat(result.isEnableScheduleReminder()).isTrue();
        verify(userSettingRepository).findById(mssv);
        verify(userSettingMapper).toResponse(userSetting);
    }

    @Test
    void shouldThrowExceptionWhenUserSettingNotFound() {
        when(userSettingRepository.findById(mssv)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserSettings(mssv)).isInstanceOf(UserException.class);

        verify(userSettingRepository).findById(mssv);
    }

    @Test
    void shouldUpdateUserSettingsSuccessfully() {
        UpdateUserSettingRequest request = new UpdateUserSettingRequest(false, true);

        when(userSettingRepository.findById(mssv)).thenReturn(Optional.of(userSetting));

        userService.updateUserSettings(mssv, request);

        assertThat(userSetting.isEnableNotification()).isFalse();
        assertThat(userSetting.isEnableScheduleReminder()).isTrue();
        verify(userSettingRepository).findById(mssv);
        verify(userSettingRepository).save(userSetting);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUserSetting() {
        UpdateUserSettingRequest request = new UpdateUserSettingRequest(false, true);

        when(userSettingRepository.findById(mssv)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserSettings(mssv, request)).isInstanceOf(UserException.class);

        verify(userSettingRepository).findById(mssv);
        verify(userSettingRepository, never()).save(any());
    }

    @Test
    void shouldUpdateBothSettingsToFalse() {
        UpdateUserSettingRequest request = new UpdateUserSettingRequest(false, false);

        when(userSettingRepository.findById(mssv)).thenReturn(Optional.of(userSetting));

        userService.updateUserSettings(mssv, request);

        assertThat(userSetting.isEnableNotification()).isFalse();
        assertThat(userSetting.isEnableScheduleReminder()).isFalse();
        verify(userSettingRepository).save(userSetting);
    }

    @Test
    void shouldUpdateBothSettingsToTrue() {
        userSetting.setEnableNotification(false);
        userSetting.setEnableScheduleReminder(false);

        UpdateUserSettingRequest request = new UpdateUserSettingRequest(true, true);

        when(userSettingRepository.findById(mssv)).thenReturn(Optional.of(userSetting));

        userService.updateUserSettings(mssv, request);

        assertThat(userSetting.isEnableNotification()).isTrue();
        assertThat(userSetting.isEnableScheduleReminder()).isTrue();
        verify(userSettingRepository).save(userSetting);
    }
}
