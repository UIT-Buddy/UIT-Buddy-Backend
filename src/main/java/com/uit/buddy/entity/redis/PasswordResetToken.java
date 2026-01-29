package com.uit.buddy.entity.redis;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("password_reset_otp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    private String otp;

    @Indexed
    private String mssv;

    private int attempts;

    private boolean isRevoked;

    @TimeToLive
    private Long ttl;
}
