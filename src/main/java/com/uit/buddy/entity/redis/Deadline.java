package com.uit.buddy.entity.redis;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("deadline")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deadline {
    @Id
    private String mssv_deadline;

    @Indexed
    private String mssv;

    private String deadlineName;

    private LocalDate dueDate;

    private LocalTime dueTime;
}
