package com.uit.buddy.entity.redis;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("pending_account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingAccount {

  @Id private String mssv;

  @Indexed private String signupToken;

  private String encryptedWstoken;

  private String fullName;

  private String avatarUrl;

  private String homeClassCode;

  @TimeToLive private Long ttl;
}
