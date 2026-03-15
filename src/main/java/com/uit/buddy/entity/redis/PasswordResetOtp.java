package com.uit.buddy.entity.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("password_reset_otp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetOtp {

    @Id
    private String mssv;

    @Indexed
    private String otpCode;

    private Integer attempts;

    @TimeToLive
    private Long ttl;
}
