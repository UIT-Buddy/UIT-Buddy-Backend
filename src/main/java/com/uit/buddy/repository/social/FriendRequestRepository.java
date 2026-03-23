package com.uit.buddy.repository.social;

import com.uit.buddy.entity.social.FriendRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {

    Optional<FriendRequest> findBySenderMssvAndReceiverMssv(String senderMssv, String receiverMssv);

    @Query("SELECT fr FROM FriendRequest fr WHERE fr.status = 'PENDING' AND "
            + "((fr.senderMssv = :mssv1 AND fr.receiverMssv = :mssv2) OR (fr.senderMssv = :mssv2 AND fr.receiverMssv = :mssv1))")
    Optional<FriendRequest> findPendingRequestBetween(@Param("mssv1") String m1, @Param("mssv2") String m2);

    String CURSOR_PAGINATION_CONDITION = """
            AND fr.status = CAST(:status AS VARCHAR)
            AND (CAST(:cursorTime AS timestamp) IS NULL
                OR fr.created_at < CAST(:cursorTime AS timestamp)
                OR (fr.created_at = CAST(:cursorTime AS timestamp) AND fr.id < :cursorId))
            ORDER BY fr.created_at DESC, fr.id DESC
            LIMIT :limit
            """;

    @Query(value = "SELECT * FROM friend_requests fr WHERE fr.receiver_mssv = :mssv "
            + CURSOR_PAGINATION_CONDITION, nativeQuery = true)
    List<FriendRequest> findPendingWithCursor(@Param("mssv") String mssv, @Param("status") String status,
            @Param("cursorTime") LocalDateTime cursorTime, @Param("cursorId") UUID cursorId, @Param("limit") int limit);

    @Query(value = "SELECT * FROM friend_requests fr WHERE fr.sender_mssv = :mssv "
            + CURSOR_PAGINATION_CONDITION, nativeQuery = true)
    List<FriendRequest> findSentWithCursor(@Param("mssv") String mssv, @Param("status") String status,
            @Param("cursorTime") LocalDateTime cursorTime, @Param("cursorId") UUID cursorId, @Param("limit") int limit);
}
