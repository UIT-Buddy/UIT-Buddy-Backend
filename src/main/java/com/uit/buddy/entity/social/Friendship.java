package com.uit.buddy.entity.social;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(name = "uk_friendship", columnNames = { "user1_mssv", "user2_mssv" }) }, indexes = {
                @Index(name = "idx_friendship_user1", columnList = "user1_mssv"),
                @Index(name = "idx_friendship_user2", columnList = "user2_mssv") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship extends AbstractBaseEntity {

    @Column(name = "user1_mssv", length = 12, insertable = false, updatable = false)
    private String user1Mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user1_mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_friendship_user1"))
    private Student user1;

    @Column(name = "user2_mssv", length = 12, insertable = false, updatable = false)
    private String user2Mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user2_mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_friendship_user2"))
    private Student user2;
}
