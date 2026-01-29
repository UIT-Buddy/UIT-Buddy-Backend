package com.uit.buddy.entity.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("refresh_token")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    private String refreshToken;

    @Indexed
    private String familyToken;

    @Indexed
    private String mssv;

    private String email;

    private boolean isRevoked;

    @TimeToLive
    private Long ttl;
}
