package com.uit.buddy.repository.social;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uit.buddy.entity.social.Post;
import com.uit.buddy.repository.social.projection.PostFeedProjection;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

    String SELECT_BASE = """
            p.id as id, p.title as title, p.content as content, p.medias as medias,
            p.like_count as likeCount, p.comment_count as commentCount, p.share_count as shareCount,
            p.created_at as createdAt, p.updated_at as updatedAt,
            s.mssv as authorMssv, s.full_name as authorFullName,
            s.avatar_url as authorAvatarUrl, s.home_class_code as authorHomeClassCode,
            EXISTS (SELECT 1 FROM reactions r WHERE r.post_id = p.id AND r.mssv = :mssv) as isLiked
            """;

    @Query(value = "SELECT " + SELECT_BASE
            + """
                    , ts_rank(
                          setweight(to_tsvector('simple', coalesce(p.title,'')), 'A') ||
                          setweight(to_tsvector('simple', coalesce(p.content,'')), 'B'),
                          websearch_to_tsquery('simple', :keyword)
                      ) AS rank
                    FROM posts p
                    JOIN students s ON p.mssv = s.mssv
                    WHERE (
                          setweight(to_tsvector('simple', coalesce(p.title,'')), 'A') ||
                          setweight(to_tsvector('simple', coalesce(p.content,'')), 'B')
                      ) @@ websearch_to_tsquery('simple', :keyword)
                    ORDER BY rank DESC, p.created_at DESC
                    """, countQuery = """
                    SELECT count(*) FROM posts p
                    WHERE to_tsvector('simple', coalesce(p.title,'') || ' ' || coalesce(p.content,'')) @@ websearch_to_tsquery('simple', :keyword)
                    """, nativeQuery = true)
    Page<PostFeedProjection> searchPostFull(@Param("keyword") String keyword, @Param("mssv") String mssv,
            Pageable pageable);

    @Query(value = "SELECT " + SELECT_BASE + """
            FROM posts p
            JOIN students s ON p.mssv = s.mssv
            ORDER BY p.created_at DESC, p.id DESC
            """, countQuery = "SELECT count(*) FROM posts", nativeQuery = true)
    Page<PostFeedProjection> findAllPosts(@Param("mssv") String mssv, Pageable pageable);

    @Query(value = "SELECT " + SELECT_BASE + """
            FROM posts p
            JOIN students s ON p.mssv = s.mssv
            WHERE (CAST(:cursorTime AS timestamp) IS NULL
                OR p.created_at < CAST(:cursorTime AS timestamp)
                OR (p.created_at = CAST(:cursorTime AS timestamp) AND p.id < :cursorId))
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<PostFeedProjection> findFeed(@Param("mssv") String mssv, @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") UUID cursorId, @Param("limit") int limit);

    @Query(value = "SELECT " + SELECT_BASE + """
            FROM posts p JOIN students s ON p.mssv = s.mssv WHERE p.id = :postId""", nativeQuery = true)
    Optional<PostFeedProjection> findDetailWithStatus(@Param("postId") UUID postId, @Param("mssv") String mssv);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    void incrementLikeCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :postId AND p.likeCount > 0")
    void decrementLikeCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :postId")
    void incrementCommentCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.shareCount = p.shareCount + 1 WHERE p.id = :postId")
    void incrementShareCount(@Param("postId") UUID postId);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :postId AND p.commentCount > 0")
    void decrementCommentCount(@Param("postId") UUID postId);

    @Query(value = "SELECT " + SELECT_BASE + """
            FROM posts p
            JOIN students s ON p.mssv = s.mssv
            WHERE p.mssv = :targetMssv
                AND (CAST(:cursorTime AS timestamp) IS NULL
                    OR p.created_at < CAST(:cursorTime AS timestamp)
                    OR (p.created_at = CAST(:cursorTime AS timestamp) AND p.id < :cursorId))
            ORDER BY p.created_at DESC, p.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<PostFeedProjection> findUserPosts(@Param("targetMssv") String targetMssv, @Param("mssv") String currentMssv,
            @Param("cursorTime") LocalDateTime cursorTime, @Param("cursorId") UUID cursorId, @Param("limit") int limit);
}
