package com.uit.buddy.entity.social;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "chats",
    indexes = {
      @Index(name = "idx_chat_group", columnList = "group_id"),
      @Index(name = "idx_chat_sender", columnList = "mssv_sender"),
      @Index(name = "idx_chat_receiver", columnList = "mssv_receiver")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat extends AbstractBaseEntity {

  @Column(name = "group_id", length = 50, insertable = false, updatable = false)
  private String groupId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "group_id",
      referencedColumnName = "id",
      foreignKey = @ForeignKey(name = "fk_chat_group"))
  private Group group;

  @Column(name = "mssv_sender", length = 12, insertable = false, updatable = false)
  private String mssvSender;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "mssv_sender",
      referencedColumnName = "mssv",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_chat_sender"))
  private Student sender;

  @Column(name = "mssv_receiver", length = 12, insertable = false, updatable = false)
  private String mssvReceiver;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "mssv_receiver",
      referencedColumnName = "mssv",
      foreignKey = @ForeignKey(name = "fk_chat_receiver"))
  private Student receiver;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;
}
