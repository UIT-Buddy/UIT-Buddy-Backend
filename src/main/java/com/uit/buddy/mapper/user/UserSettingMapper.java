package com.uit.buddy.mapper.user;

import com.uit.buddy.dto.response.user.UserSettingResponse;
import com.uit.buddy.entity.user.UserSetting;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSettingMapper {
    UserSettingResponse toResponse(UserSetting userSetting);
}
