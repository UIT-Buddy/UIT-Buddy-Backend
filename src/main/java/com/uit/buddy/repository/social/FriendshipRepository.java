package com.uit.buddy.repository.social;

import com.uit.buddy.entity.social.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    Optional<Friendship> findByUser1MssvAndUser2Mssv(String mssv1, String mssv2); // ensure that mssv1 < mssv2

    @Query("SELECT f FROM Friendship f WHERE f.user1Mssv = :mssv OR f.user2Mssv = :mssv")
    List<Friendship> findAllByUserMssv(String mssv);

    boolean existsByUser1MssvAndUser2Mssv(String mssv1, String mssv2);

    @Query("SELECT f FROM Friendship f " +
            "WHERE (f.user1Mssv = :mssv OR f.user2Mssv = :mssv) " +
            "AND (:cursorTime IS NULL OR f.createdAt < :cursorTime OR (f.createdAt = :cursorTime AND f.id < :cursorId)) "
            +
            "ORDER BY f.createdAt DESC, f.id DESC")
    List<Friendship> findFriendsWithCursor(
            @Param("mssv") String mssv,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);
}
