package com.uit.buddy.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private UserInfo user;

    @JsonIgnore
    private String refreshToken;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String email;
        private String mssv;
        private String fullName;
    }
}
