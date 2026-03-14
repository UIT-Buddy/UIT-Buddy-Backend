package com.uit.buddy.entity.user;

import com.uit.buddy.entity.AbstractAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetting extends AbstractAuditEntity {

  @Id
  @Column(name = "mssv", length = 12)
  private String mssv;

  @Builder.Default
  @Column(name = "enable_notification", nullable = false)
  private boolean enableNotification = true;

  @Builder.Default
  @Column(name = "enable_schedule_reminder", nullable = false)
  private boolean enableScheduleReminder = true;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "mssv")
  private Student student;
}
