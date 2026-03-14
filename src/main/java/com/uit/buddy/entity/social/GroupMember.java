package com.uit.buddy.entity.social;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "group_members",
    indexes = {
      @Index(name = "idx_group_member_student", columnList = "mssv"),
      @Index(name = "idx_group_member_unique", columnList = "group_id, mssv", unique = true)
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember extends AbstractBaseEntity {

  @Column(name = "group_id", length = 50, insertable = false, updatable = false)
  private String groupId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "group_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(name = "fk_member_group"))
  private Group group;

  @Column(name = "mssv", length = 12, insertable = false, updatable = false)
  private String mssv;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "mssv",
      referencedColumnName = "mssv",
      foreignKey = @ForeignKey(name = "fk_member_student"))
  private Student student;
}
