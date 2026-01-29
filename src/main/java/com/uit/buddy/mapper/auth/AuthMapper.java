package com.uit.buddy.mapper.auth;

import com.uit.buddy.dto.response.auth.AuthResponse;
import com.uit.buddy.entity.auth.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "user", source = "user", qualifiedByName = "toUserInfo")
    AuthResponse toAuthResponse(User user, String accessToken, String refreshToken);

    @Named("toUserInfo")
    @Mapping(target = "id", expression = "java(user.getId().toString())")
    AuthResponse.UserInfo toUserInfo(User user);
}
