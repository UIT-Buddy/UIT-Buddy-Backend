package com.uit.buddy.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempTokenResponse {

    private String tempToken;
    private String mssv;
    private String email;
    private long expiresIn; // seconds
}
