package com.uit.buddy.entity.social;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.FriendRequestStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "friend_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_friend_request", columnNames = { "sender_mssv", "receiver_mssv" })
}, indexes = {
        @Index(name = "idx_friend_request_sender", columnList = "sender_mssv"),
        @Index(name = "idx_friend_request_receiver", columnList = "receiver_mssv"),
        @Index(name = "idx_friend_request_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequest extends AbstractBaseEntity {

    @Column(name = "sender_mssv", length = 12, insertable = false, updatable = false)
    private String senderMssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_friend_request_sender"))
    private Student sender;

    @Column(name = "receiver_mssv", length = 12, insertable = false, updatable = false)
    private String receiverMssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_friend_request_receiver"))
    private Student receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private FriendRequestStatus status;
}
