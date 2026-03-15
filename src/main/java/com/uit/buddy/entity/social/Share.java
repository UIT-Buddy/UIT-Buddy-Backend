package com.uit.buddy.entity.social;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.ShareType;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "shares", indexes = { @Index(name = "idx_shares_post", columnList = "post_id"),
        @Index(name = "idx_shares_student", columnList = "mssv"), })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Share extends AbstractBaseEntity {
    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_share_student"))
    private Student student;

    @Column(name = "post_id", length = 50, insertable = false, updatable = false)
    private UUID postId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_share_post"))
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(name = "share_type", nullable = false)
    private ShareType type;
}
