package com.uit.buddy.entity.social;

import java.util.ArrayList;
import java.util.List;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_post", columnList = "post_id"),
        @Index(name = "idx_comment_author", columnList = "mssv")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends AbstractBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_post"))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_author"))
    private Student author;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", foreignKey = @ForeignKey(name = "fk_comment_parent"))
    private Comment parentComment;

    @Builder.Default
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    public void addReply(Comment reply) {
        this.replies.add(reply);
        reply.setParentComment(this);
    }

    public void removeReply(Comment reply) {
        this.replies.remove(reply);
        reply.setParentComment(null);
    }
}