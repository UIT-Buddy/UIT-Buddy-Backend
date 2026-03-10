package com.uit.buddy.entity.social;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_author", columnList = "mssv"),
        @Index(name = "idx_post_created_id", columnList = "created_at DESC, id DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_post_author"))
    private Student author;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "medias", columnDefinition = "jsonb")
    @Builder.Default
    private List<PostMedia> medias = new ArrayList<>();

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private long commentCount = 0L;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private long likeCount = 0L;

    @Builder.Default
    @Column(name = "share_count", nullable = false)
    private long shareCount = 0L;

    @Version
    private Long version;

}