package com.uit.buddy.repository.social;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.uit.buddy.entity.social.Post;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

        public interface PostFeedProjection {
                UUID getId();

                String getTitle();

                String getContent();

                String getImageUrl();

                String getVideoUrl();

                Long getLikeCount();

                Long getCommentCount();

                Long getShareCount();

                LocalDateTime getCreatedAt();

                String getAuthorFullName();

                String getAuthorAvatarUrl();

                String getAuthorHomeClassCode();

                // Trạng thái cá nhân
                boolean getIsLiked();

                boolean getIsDisliked();
        }

        @Query(value = """
                            SELECT p.id,
                                   ts_rank(
                                       setweight(to_tsvector('simple', coalesce(title,'')), 'A') ||
                                       setweight(to_tsvector('simple', coalesce(content,'')), 'B'),
                                       websearch_to_tsquery('simple', :keyword)
                                   ) AS rank
                            FROM posts p
                            WHERE
                                  (
                                    setweight(to_tsvector('simple', coalesce(title,'')), 'A') ||
                                    setweight(to_tsvector('simple', coalesce(content,'')), 'B')
                                  )
                                  @@ websearch_to_tsquery('simple', :keyword)
                            ORDER BY rank DESC
                        """, nativeQuery = true)
        List<UUID> searchPostByKeyword(String keyword);

        @EntityGraph(attributePaths = { "author" })
        @Query(value = """
                        SELECT p
                        FROM Post p
                        WHERE p.id IN :uuids
                        """)
        Page<Post> findAll(@Param("uuids") List<UUID> uuids, Pageable pageable);

        @Query(value = """
                        SELECT p.id as id, p.title as title, p.content as content,
                               p.image_url as imageUrl, p.video_url as videoUrl,
                               p.like_count as likeCount, p.comment_count as commentCount, p.share_count as shareCount,
                               p.created_at as createdAt,
                               s.full_name as authorFullName, s.avatar_url as authorAvatarUrl,
                               s.home_class_code as authorHomeClassCode, s.mssv as authorMssv,
                               EXISTS (SELECT 1 FROM reactions r WHERE r.post_id = p.id AND r.student_mssv = :mssv AND r.type = 'LIKE') as isLiked,
                               EXISTS (SELECT 1 FROM reactions r WHERE r.post_id = p.id AND r.student_mssv = :mssv AND r.type = 'DISLIKE') as isDisliked
                        FROM posts p
                        JOIN students s ON p.mssv = s.mssv
                        WHERE p.is_deleted = false
                          AND (:cursorTime IS NULL OR (p.created_at < :cursorTime) OR (p.created_at = :cursorTime AND p.id < :cursorId))
                        ORDER BY p.created_at DESC, p.id DESC
                        LIMIT :limit
                        """, nativeQuery = true)
        List<PostFeedProjection> findFeed(@Param("mssv") String mssv,
                        @Param("cursorTime") LocalDateTime cursorTime,
                        @Param("cursorId") UUID cursorId,
                        @Param("limit") int limit);

}
