package com.uit.buddy.repository.social;

import com.uit.buddy.entity.social.FriendRequest;
import com.uit.buddy.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
