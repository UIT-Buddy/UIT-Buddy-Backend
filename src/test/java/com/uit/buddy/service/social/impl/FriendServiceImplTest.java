package com.uit.buddy.service.social.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.uit.buddy.dto.request.social.RespondFriendRequestRequest;
import com.uit.buddy.dto.request.social.SendFriendRequestRequest;
import com.uit.buddy.dto.response.social.FriendshipResponse;
import com.uit.buddy.dto.response.social.PendingFriendRequestResponse;
import com.uit.buddy.dto.response.social.SentFriendRequestResponse;
import com.uit.buddy.dto.response.social.UserSummary;
import com.uit.buddy.entity.social.FriendRequest;
import com.uit.buddy.entity.social.Friendship;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.FriendRequestStatus;
import com.uit.buddy.enums.FriendResponseAction;
import com.uit.buddy.event.social.FriendRequestAcceptedEvent;
import com.uit.buddy.event.social.FriendRequestReceivedEvent;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.social.FriendMapper;
import com.uit.buddy.repository.social.FriendRequestRepository;
import com.uit.buddy.repository.social.FriendshipRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.cometchat.CometChatService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FriendServiceImplTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;
    @Mock
    private FriendshipRepository friendshipRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private CometChatService cometChatService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private FriendMapper friendMapper;

    @InjectMocks
    private FriendServiceImpl friendService;

    private String mssv1;
    private String mssv2;
    private Student student1;
    private Student student2;
    private UUID requestId;
    private FriendRequest friendRequest;
    private Friendship friendship;

    @BeforeEach
    void setUp() {
        mssv1 = "22100001";
        mssv2 = "22100002";
        requestId = UUID.randomUUID();

        student1 = new Student();
        student1.setMssv(mssv1);
        student1.setFullName("Student One");
        student1.setAvatarUrl("avatar1.jpg");

        student2 = new Student();
        student2.setMssv(mssv2);
        student2.setFullName("Student Two");
        student2.setAvatarUrl("avatar2.jpg");

        friendRequest = FriendRequest.builder().sender(student1).receiver(student2).status(FriendRequestStatus.PENDING)
                .build();
        ReflectionTestUtils.setField(friendRequest, "id", requestId);
        ReflectionTestUtils.setField(friendRequest, "senderMssv", mssv1);
        ReflectionTestUtils.setField(friendRequest, "receiverMssv", mssv2);
        ReflectionTestUtils.setField(friendRequest, "createdAt", LocalDateTime.now());

        friendship = Friendship.builder().user1(student1).user2(student2).build();
        ReflectionTestUtils.setField(friendship, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(friendship, "user1Mssv", mssv1);
        ReflectionTestUtils.setField(friendship, "user2Mssv", mssv2);
        ReflectionTestUtils.setField(friendship, "createdAt", LocalDateTime.now());
    }

    @Test
    void shouldSendFriendRequestSuccessfully() {
        SendFriendRequestRequest request = new SendFriendRequestRequest(mssv2);

        when(friendshipRepository.existsByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(false);
        when(friendRequestRepository.findPendingRequestBetween(mssv1, mssv2)).thenReturn(Optional.empty());
        when(studentRepository.getReferenceById(mssv1)).thenReturn(student1);
        when(studentRepository.findById(mssv2)).thenReturn(Optional.of(student2));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);

        boolean result = friendService.toggleFriendRequest(mssv1, request);

        assertThat(result).isTrue();
        verify(friendRequestRepository).save(any(FriendRequest.class));
        verify(eventPublisher).publishEvent(any(FriendRequestReceivedEvent.class));
    }

    @Test
    void shouldCancelPendingFriendRequest() {
        SendFriendRequestRequest request = new SendFriendRequestRequest(mssv2);

        when(friendshipRepository.existsByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(false);
        when(friendRequestRepository.findPendingRequestBetween(mssv1, mssv2)).thenReturn(Optional.of(friendRequest));
        doNothing().when(friendRequestRepository).delete(friendRequest);

        boolean result = friendService.toggleFriendRequest(mssv1, request);

        assertThat(result).isFalse();
        verify(friendRequestRepository).delete(friendRequest);
        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSendingRequestToSelf() {
        SendFriendRequestRequest request = new SendFriendRequestRequest(mssv1);

        assertThatThrownBy(() -> friendService.toggleFriendRequest(mssv1, request)).isInstanceOf(SocialException.class);
    }

    @Test
    void shouldThrowExceptionWhenAlreadyFriends() {
        SendFriendRequestRequest request = new SendFriendRequestRequest(mssv2);

        when(friendshipRepository.existsByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(true);

        assertThatThrownBy(() -> friendService.toggleFriendRequest(mssv1, request)).isInstanceOf(SocialException.class);
    }

    @Test
    void shouldThrowExceptionWhenSenderNotFound() {
        SendFriendRequestRequest request = new SendFriendRequestRequest(mssv2);

        when(friendshipRepository.existsByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(false);
        when(friendRequestRepository.findPendingRequestBetween(mssv1, mssv2)).thenReturn(Optional.empty());
        when(studentRepository.getReferenceById(mssv1)).thenReturn(student1);
        when(studentRepository.findById(mssv2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.toggleFriendRequest(mssv1, request)).isInstanceOf(UserException.class);
    }

    @Test
    void shouldThrowExceptionWhenReceiverNotFound() {
        SendFriendRequestRequest request = new SendFriendRequestRequest(mssv2);

        when(friendshipRepository.existsByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(false);
        when(friendRequestRepository.findPendingRequestBetween(mssv1, mssv2)).thenReturn(Optional.empty());
        when(studentRepository.getReferenceById(mssv1)).thenReturn(student1);
        when(studentRepository.findById(mssv2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.toggleFriendRequest(mssv1, request)).isInstanceOf(UserException.class);
    }

    @Test
    void shouldAcceptFriendRequestSuccessfully() {
        RespondFriendRequestRequest request = new RespondFriendRequestRequest(FriendResponseAction.ACCEPT);

        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));
        when(studentRepository.getReferenceById(mssv1)).thenReturn(student1);
        when(studentRepository.getReferenceById(mssv2)).thenReturn(student2);
        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);
        doNothing().when(cometChatService).createFriendship(mssv1, mssv2);

        friendService.respondToFriendRequest(mssv2, requestId, request);

        assertThat(friendRequest.getStatus()).isEqualTo(FriendRequestStatus.ACCEPTED);
        verify(friendshipRepository).save(any(Friendship.class));
        verify(cometChatService).createFriendship(mssv1, mssv2);
        verify(eventPublisher).publishEvent(any(FriendRequestAcceptedEvent.class));
        verify(friendRequestRepository, never()).save(friendRequest);
    }

    @Test
    void shouldRejectFriendRequestSuccessfully() {
        RespondFriendRequestRequest request = new RespondFriendRequestRequest(FriendResponseAction.REJECT);

        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        friendService.respondToFriendRequest(mssv2, requestId, request);

        assertThat(friendRequest.getStatus()).isEqualTo(FriendRequestStatus.REJECTED);
        verify(friendshipRepository, never()).save(any());
        verify(cometChatService, never()).createFriendship(any(), any());
        verify(friendRequestRepository, never()).save(friendRequest);
    }

    @Test
    void shouldThrowExceptionWhenRespondingToNonExistentRequest() {
        RespondFriendRequestRequest request = new RespondFriendRequestRequest(FriendResponseAction.ACCEPT);

        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.respondToFriendRequest(mssv2, requestId, request))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldThrowExceptionWhenUnauthorizedToRespond() {
        RespondFriendRequestRequest request = new RespondFriendRequestRequest(FriendResponseAction.ACCEPT);

        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        assertThatThrownBy(() -> friendService.respondToFriendRequest("22100999", requestId, request))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldThrowExceptionWhenRequestAlreadyResponded() {
        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
        RespondFriendRequestRequest request = new RespondFriendRequestRequest(FriendResponseAction.ACCEPT);

        when(friendRequestRepository.findById(requestId)).thenReturn(Optional.of(friendRequest));

        assertThatThrownBy(() -> friendService.respondToFriendRequest(mssv2, requestId, request))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldUnfriendSuccessfully() {
        when(friendshipRepository.findByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(Optional.of(friendship));
        doNothing().when(friendshipRepository).delete(friendship);
        doNothing().when(cometChatService).removeFriendship(mssv1, mssv2);

        friendService.unfriend(mssv1, mssv2);

        verify(friendshipRepository).delete(friendship);
        verify(cometChatService).removeFriendship(mssv1, mssv2);
    }

    @Test
    void shouldThrowExceptionWhenUnfriendingNonFriend() {
        when(friendshipRepository.findByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.unfriend(mssv1, mssv2)).isInstanceOf(SocialException.class);
    }

    @Test
    void shouldGetPendingRequestsSuccessfully() {
        PendingFriendRequestResponse response = new PendingFriendRequestResponse(requestId,
                new UserSummary(mssv1, "Student One", "avatar1.jpg"), LocalDateTime.now());

        when(friendRequestRepository.findPendingWithCursor(eq(mssv2), eq("PENDING"), any(), any(), eq(11)))
                .thenReturn(List.of(friendRequest));
        when(friendMapper.toPendingRequestResponse(friendRequest)).thenReturn(response);

        List<PendingFriendRequestResponse> result = friendService.getPendingRequests(mssv2, null, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sender().mssv()).isEqualTo(mssv1);
        verify(friendRequestRepository).findPendingWithCursor(eq(mssv2), eq("PENDING"), any(), any(), eq(11));
    }

    @Test
    void shouldGetSentRequestsSuccessfully() {
        SentFriendRequestResponse response = new SentFriendRequestResponse(requestId,
                new UserSummary(mssv2, "Student Two", "avatar2.jpg"), LocalDateTime.now());

        when(friendRequestRepository.findSentWithCursor(eq(mssv1), eq("PENDING"), any(), any(), eq(11)))
                .thenReturn(List.of(friendRequest));
        when(friendMapper.toSentRequestResponse(friendRequest)).thenReturn(response);

        List<SentFriendRequestResponse> result = friendService.getSentRequests(mssv1, null, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).receiver().mssv()).isEqualTo(mssv2);
        verify(friendRequestRepository).findSentWithCursor(eq(mssv1), eq("PENDING"), any(), any(), eq(11));
    }

    @Test
    void shouldGetFriendsSuccessfully() {
        FriendshipResponse response = new FriendshipResponse(UUID.randomUUID(),
                new UserSummary(mssv2, "Student Two", "avatar2.jpg"), LocalDateTime.now());

        when(friendshipRepository.findFriendsWithCursor(eq(mssv1), any(), any(), eq(11)))
                .thenReturn(List.of(friendship));
        when(friendMapper.toFriendshipResponse(eq(friendship), any(Student.class))).thenReturn(response);

        List<FriendshipResponse> result = friendService.getFriends(mssv1, null, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).friend().mssv()).isEqualTo(mssv2);
        verify(friendshipRepository).findFriendsWithCursor(eq(mssv1), any(), any(), eq(11));
    }

    @Test
    void shouldReturnTrueWhenAreFriends() {
        when(friendshipRepository.existsByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(true);

        boolean result = friendService.areFriends(mssv1, mssv2);

        assertThat(result).isTrue();
        verify(friendshipRepository).existsByUser1MssvAndUser2Mssv(mssv1, mssv2);
    }

    @Test
    void shouldReturnFalseWhenNotFriends() {
        when(friendshipRepository.existsByUser1MssvAndUser2Mssv(mssv1, mssv2)).thenReturn(false);

        boolean result = friendService.areFriends(mssv1, mssv2);

        assertThat(result).isFalse();
        verify(friendshipRepository).existsByUser1MssvAndUser2Mssv(mssv1, mssv2);
    }

    @Test
    void shouldHandleReverseMssvsInAreFriends() {
        String higherMssv = "22100003";
        when(friendshipRepository.existsByUser1MssvAndUser2Mssv(mssv1, higherMssv)).thenReturn(true);

        boolean result = friendService.areFriends(higherMssv, mssv1);

        assertThat(result).isTrue();
        verify(friendshipRepository).existsByUser1MssvAndUser2Mssv(mssv1, higherMssv);
    }
}
