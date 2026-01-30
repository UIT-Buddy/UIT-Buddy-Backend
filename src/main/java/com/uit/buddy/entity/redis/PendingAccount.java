package com.uit.buddy.entity.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("pending_account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingAccount {
    @Id
    private String mssv;

    private String email;

    private boolean isRevoked;

    @TimeToLive
    private Long ttl;
}
