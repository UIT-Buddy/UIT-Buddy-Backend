package com.uit.buddy.entity.social;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group extends AbstractBaseEntity {

  @Column(name = "creator_id", length = 12, insertable = false, updatable = false)
  private String creatorId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "creator_id",
      referencedColumnName = "mssv",
      foreignKey = @ForeignKey(name = "fk_group_creator"))
  private Student creator;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "avatar_url", length = 512)
  private String avatarUrl;
}
