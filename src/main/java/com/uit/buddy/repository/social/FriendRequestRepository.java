package com.uit.buddy.repository.social;

import com.uit.buddy.entity.social.FriendRequest;
import com.uit.buddy.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    Optional<FriendRequest> findBySenderMssvAndReceiverMssv(String senderMssv, String receiverMssv);

    List<FriendRequest> findBySenderMssvAndStatus(String senderMssv, FriendRequestStatus status);

    List<FriendRequest> findByReceiverMssvAndStatus(String receiverMssv, FriendRequestStatus status);

    @Query("SELECT fr FROM FriendRequest fr WHERE " +
            "(fr.senderMssv = :mssv1 AND fr.receiverMssv = :mssv2) OR " +
            "(fr.senderMssv = :mssv2 AND fr.receiverMssv = :mssv1)")
    Optional<FriendRequest> findBetweenUsers(String mssv1, String mssv2);

    @Query("SELECT fr FROM FriendRequest fr WHERE fr.status = 'PENDING' AND " +
            "((fr.senderMssv = :m1 AND fr.receiverMssv = :m2) OR (fr.senderMssv = :m2 AND fr.receiverMssv = :m1))")
    Optional<FriendRequest> findPendingRequestBetween(String m1, String m2);

    @Query("SELECT fr FROM FriendRequest fr " +
            "WHERE fr.receiverMssv = :mssv AND fr.status = :status " +
            "AND (:cursorTime IS NULL OR fr.createdAt < :cursorTime OR (fr.createdAt = :cursorTime AND fr.id < :cursorId)) "
            +
            "ORDER BY fr.createdAt DESC, fr.id DESC")
    List<FriendRequest> findPendingWithCursor(
            @Param("mssv") String mssv,
            @Param("status") FriendRequestStatus status,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);

    @Query("SELECT fr FROM FriendRequest fr " +
            "WHERE fr.senderMssv = :mssv AND fr.status = :status " +
            "AND (:cursorTime IS NULL OR fr.createdAt < :cursorTime OR (fr.createdAt = :cursorTime AND fr.id < :cursorId)) "
            +
            "ORDER BY fr.createdAt DESC, fr.id DESC")
    List<FriendRequest> findSentWithCursor(
            @Param("mssv") String mssv,
            @Param("status") FriendRequestStatus status,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);
}
