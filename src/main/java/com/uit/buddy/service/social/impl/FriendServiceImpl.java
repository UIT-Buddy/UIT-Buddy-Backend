package com.uit.buddy.service.social.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uit.buddy.dto.request.social.RespondFriendRequestRequest;
import com.uit.buddy.dto.request.social.SendFriendRequestRequest;
import com.uit.buddy.dto.response.social.FriendshipResponse;
import com.uit.buddy.dto.response.social.PendingFriendRequestResponse;
import com.uit.buddy.dto.response.social.SentFriendRequestResponse;
import com.uit.buddy.entity.social.FriendRequest;
import com.uit.buddy.entity.social.Friendship;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.FriendRequestStatus;
import com.uit.buddy.event.social.FriendRequestAcceptedEvent;
import com.uit.buddy.event.social.FriendRequestReceivedEvent;
import com.uit.buddy.exception.social.SocialErrorCode;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.social.FriendMapper;
import com.uit.buddy.repository.social.FriendRequestRepository;
import com.uit.buddy.repository.social.FriendshipRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.cometchat.CometChatService;
import com.uit.buddy.service.social.FriendService;
import com.uit.buddy.util.CursorUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FriendServiceImpl implements FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;
    private final StudentRepository studentRepository;
    private final CometChatService cometChatService;
    private final ApplicationEventPublisher eventPublisher;
    private final FriendMapper friendMapper;

    @Override
    public void sendFriendRequest(String senderMssv, SendFriendRequestRequest request) {
        String receiverMssv = request.receiverMssv();

        if (senderMssv.equals(receiverMssv)) {
            throw new SocialException(SocialErrorCode.CANNOT_FRIEND_YOURSELF);
        }
        if (areFriends(senderMssv, receiverMssv)) {
            throw new SocialException(SocialErrorCode.ALREADY_FRIENDS);
        }
        Optional<FriendRequest> existingRequest = friendRequestRepository
                .findPendingRequestBetween(senderMssv, receiverMssv);

        if (existingRequest.isPresent()) {
            throw new SocialException(SocialErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
        }

        Student receiver = studentRepository.findById(receiverMssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        Student sender = studentRepository.getReferenceById(senderMssv);

        FriendRequest friendRequest = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequest = friendRequestRepository.save(friendRequest);
        eventPublisher.publishEvent(new FriendRequestReceivedEvent(
                friendRequest.getId(),
                senderMssv,
                sender.getFullName(),
                receiverMssv));
    }

    @Override
    public void respondToFriendRequest(String receiverMssv, UUID requestId, RespondFriendRequestRequest request) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.FRIEND_REQUEST_NOT_FOUND));
        if (!friendRequest.getReceiverMssv().equals(receiverMssv)) {
            throw new SocialException(SocialErrorCode.UNAUTHORIZED);
        }
        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new SocialException(SocialErrorCode.FRIEND_REQUEST_ALREADY_RESPONDED);
        }

        String action = request.action().toString();

        if ("ACCEPT".equals(action)) {
            friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
            createFriendship(friendRequest.getSenderMssv(), receiverMssv);
            cometChatService.createFriendship(friendRequest.getSenderMssv(), receiverMssv);

            eventPublisher.publishEvent(new FriendRequestAcceptedEvent(
                    friendRequest.getId(),
                    receiverMssv,
                    friendRequest.getReceiver().getFullName(),
                    friendRequest.getSenderMssv()));

            log.info("[Friend Service] Friend request accepted: {} and {} are now friends",
                    friendRequest.getSenderMssv(), receiverMssv);
        } else if ("REJECT".equals(action)) {
            friendRequest.setStatus(FriendRequestStatus.REJECTED);
            log.info("[Friend Service] Friend request rejected by {}", receiverMssv);
        }
        friendRequestRepository.save(friendRequest);
    }

    @Override
    public void unfriend(String mssv, String friendMssv) {
        String[] sortedMssvs = sortMssvs(mssv, friendMssv);

        Friendship friendship = friendshipRepository.findByUser1MssvAndUser2Mssv(sortedMssvs[0], sortedMssvs[1])
                .orElseThrow(() -> new SocialException(SocialErrorCode.NOT_FRIENDS));

        friendshipRepository.delete(friendship);
        cometChatService.removeFriendship(mssv, friendMssv);
        log.info("[Friend Service] Unfriended: {} and {}", mssv, friendMssv);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PendingFriendRequestResponse> getPendingRequests(String mssv, String cursor, int limit) {
        LocalDateTime cursorTime = null;
        UUID cursorId = null;

        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorId = contents.id();
        }

        return friendRequestRepository.findPendingWithCursor(
                mssv,
                FriendRequestStatus.PENDING,
                cursorTime,
                cursorId,
                limit + 1).stream()
                .map(friendMapper::toPendingRequestResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SentFriendRequestResponse> getSentRequests(String mssv, String cursor, int limit) {
        LocalDateTime cursorTime = null;
        UUID cursorId = null;

        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorId = contents.id();
        }

        return friendRequestRepository.findSentWithCursor(
                mssv,
                FriendRequestStatus.PENDING,
                cursorTime,
                cursorId,
                limit + 1).stream()
                .map(friendMapper::toSentRequestResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponse> getFriends(String mssv, String cursor, int limit) {
        LocalDateTime cursorTime = null;
        UUID cursorId = null;

        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorId = contents.id();
        }

        return friendshipRepository.findFriendsWithCursor(
                mssv,
                cursorTime,
                cursorId,
                limit + 1).stream()
                .map(friendship -> {
                    Student friend = friendship.getUser1Mssv().equals(mssv) ? friendship.getUser2()
                            : friendship.getUser1();
                    return friendMapper.toFriendshipResponse(friendship, friend);
                })
                .toList();
    }

    @Override
    public boolean areFriends(String mssv1, String mssv2) {
        String[] sortedMssvs = sortMssvs(mssv1, mssv2);
        return friendshipRepository.existsByUser1MssvAndUser2Mssv(sortedMssvs[0], sortedMssvs[1]);
    }

    private void createFriendship(String mssv1, String mssv2) {
        String[] sortedMssvs = sortMssvs(mssv1, mssv2);

        Friendship friendship = Friendship.builder()
                .user1(studentRepository.getReferenceById(sortedMssvs[0]))
                .user2(studentRepository.getReferenceById(sortedMssvs[1]))
                .build();

        friendshipRepository.save(friendship);
    }

    private String[] sortMssvs(String mssv1, String mssv2) {
        if (mssv1.compareTo(mssv2) < 0) {
            return new String[] { mssv1, mssv2 };
        }
        return new String[] { mssv2, mssv1 };
    }
}
