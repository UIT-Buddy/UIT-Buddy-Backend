package com.uit.buddy.entity.social;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_author", columnList = "mssv")
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

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "video_url", length = 512)
    private String videoUrl;

    @Formula("(select count(*) from comments c where c.post_id = id)")
    private long commentCount;

    @Formula("(select count(*) from reactions r where r.post_id = id)")
    private long likeCount;

    @Formula("(select count(*) from shares s where s.post_id = id)")
    private long shareCount;
}