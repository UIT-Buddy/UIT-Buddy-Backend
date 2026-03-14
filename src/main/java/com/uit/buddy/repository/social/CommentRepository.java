package com.uit.buddy.repository.social;

import com.uit.buddy.entity.social.Comment;
import com.uit.buddy.repository.social.projection.CommentProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID> {

    @Query(value = """
            SELECT
                c.id as id,
                c.content as content,
                c.like_count as likeCount,
                c.created_at as createdAt,
                c.updated_at as updatedAt,
                c.parent_comment_id as parentId,
                s.mssv as mssv,
                s.full_name as fullName,
                s.avatar_url as avatarUrl,
                (SELECT count(*) FROM comments reply WHERE reply.parent_comment_id = c.id) as replyCount,
                EXISTS (SELECT 1 FROM comment_reactions cr WHERE cr.comment_id = c.id AND cr.mssv = :mssv) as isLiked
            FROM comments c
            JOIN students s ON c.mssv = s.mssv
            WHERE c.post_id = :postId
              AND (
                   (:parentId IS NULL AND c.parent_comment_id IS NULL)
                   OR (c.parent_comment_id = :parentId)
              )
              AND (
                   CAST(:cursorTime AS timestamp) IS NULL
                   OR c.created_at < CAST(:cursorTime AS timestamp)
                   OR (c.created_at = CAST(:cursorTime AS timestamp) AND c.id < :cursorId)
              )
            ORDER BY c.created_at DESC, c.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<CommentProjection> findCommentsWithCursor(@Param("postId") UUID postId, @Param("parentId") UUID parentId,
            @Param("mssv") String mssv, @Param("cursorTime") LocalDateTime cursorTime, @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);

    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + 1 WHERE c.id = :commentId")
    void incrementReplyCount(@Param("commentId") UUID commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    void incrementLikeCount(@Param("commentId") UUID commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId")
    void decrementLikeCount(@Param("commentId") UUID commentId);
}
