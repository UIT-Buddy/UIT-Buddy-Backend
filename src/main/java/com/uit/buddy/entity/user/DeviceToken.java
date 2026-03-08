package com.uit.buddy.entity.user;

import com.uit.buddy.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device_tokens", indexes = {
        @Index(name = "idx_device_mssv", columnList = "mssv"),
        @Index(name = "idx_fcm_token", columnList = "fcm_token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken extends AbstractBaseEntity {

    @Column(name = "mssv", nullable = false, length = 12)
    private String mssv;

    @Column(name = "fcm_token", nullable = false, columnDefinition = "TEXT")
    private String fcmToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mssv", insertable = false, updatable = false)
    private Student student;
}