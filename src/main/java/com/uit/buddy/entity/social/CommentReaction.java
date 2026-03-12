package com.uit.buddy.entity.social;

import java.util.UUID;

import com.uit.buddy.entity.AbstractBaseEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_reactions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_reaction_user", columnNames = { "comment_id", "mssv" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReaction extends AbstractBaseEntity {
    @Column(name = "comment_id", nullable = false)
    private UUID commentId;

    @Column(name = "mssv", nullable = false, length = 12)
    private String mssv;
}
