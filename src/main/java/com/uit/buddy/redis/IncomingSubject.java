package com.uit.buddy.redis;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("incoming_subject")
public class IncomingSubject implements Serializable {
    @Id
    private String id; // mssv:classCode:date

    @TimeToLive
    private Long expiration; // in seconds
}
