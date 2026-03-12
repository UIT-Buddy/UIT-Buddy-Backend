package com.uit.buddy.repository.social;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uit.buddy.entity.social.Reaction;

import org.springframework.data.repository.CrudRepository;
import com.uit.buddy.repository.social.projection.ReactionProjection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionRepository extends CrudRepository<Reaction, UUID> {

    @Query(value = """
            SELECT
                s.mssv as mssv,
                s.full_name as fullName,
                s.avatar_url as avatarUrl,
                r.created_at as reactedAt
            FROM reactions r
            JOIN students s ON r.mssv = s.mssv
            WHERE r.post_id = :postId
              AND (
                   CAST(:cursorTime AS timestamp) IS NULL
                   OR r.created_at < CAST(:cursorTime AS timestamp)
                   OR (r.created_at = CAST(:cursorTime AS timestamp) AND s.mssv < :cursorMssv)
              )
            ORDER BY r.created_at DESC, s.mssv DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ReactionProjection> findReactionsWithCursor(
            @Param("postId") UUID postId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorMssv") String cursorMssv,
            @Param("limit") int limit);

    Optional<Reaction> findByPostIdAndMssv(UUID postId, String mssv);

    void deleteByPostIdAndMssv(UUID postId, String mssv);

    boolean existsByPostIdAndMssv(UUID postId, String mssv);
}
