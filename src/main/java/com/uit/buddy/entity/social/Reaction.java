package com.uit.buddy.entity.social;

import java.util.UUID;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reactions", indexes = {
        @Index(name = "idx_reaction_post", columnList = "post_id"),
        @Index(name = "idx_reaction_student", columnList = "mssv"),
        @Index(name = "idx_reaction_unique", columnList = "mssv, post_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_reaction_student"))
    private Student student;

    @Column(name = "post_id", length = 50, insertable = false, updatable = false)
    private UUID postId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_reaction_post"))
    private Post post;
}