package com.uit.buddy.repository.social;

import com.uit.buddy.entity.social.Share;
import com.uit.buddy.repository.social.projection.ShareProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareRepository extends CrudRepository<Share, UUID> {

    @Query(value = """
            SELECT
                s.mssv as mssv,
                s.full_name as fullName,
                s.avatar_url as avatarUrl,
                sh.created_at as sharedAt
            FROM shares sh
            JOIN students s ON sh.mssv = s.mssv
            WHERE sh.post_id = :postId
              AND (CAST(:cursorTime AS timestamp) IS NULL
                   OR sh.created_at < CAST(:cursorTime AS timestamp)
                   OR (sh.created_at = CAST(:cursorTime AS timestamp) AND s.mssv < :cursorMssv))
            ORDER BY sh.created_at DESC, s.mssv DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ShareProjection> findSharesWithCursor(@Param("postId") UUID postId,
            @Param("cursorTime") LocalDateTime cursorTime, @Param("cursorMssv") String cursorMssv,
            @Param("limit") int limit);

    Optional<Share> findByPostIdAndMssv(UUID postId, String mssv);

    boolean existsByPostIdAndMssv(UUID postId, String mssv);
}
