package com.uit.buddy.entity.auth;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("signup_otp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpToken {

    @Id
    private String otp;

    @Indexed
    private String mssv;

    private int attempts;

    private boolean isRevoked;

    @TimeToLive
    private Long ttl;
}