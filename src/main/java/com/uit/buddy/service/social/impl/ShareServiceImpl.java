package com.uit.buddy.service.social.impl;

import com.uit.buddy.client.CometChatClient;
import com.uit.buddy.dto.request.client.CometChatSendMessageRequest;
import com.uit.buddy.dto.request.social.SharePostRequest;
import com.uit.buddy.dto.response.client.CometChatConversationResponse;
import com.uit.buddy.dto.response.social.ShareTargetResponse;
import com.uit.buddy.dto.response.social.UserShareResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.social.Share;
import com.uit.buddy.enums.PostType;
import com.uit.buddy.enums.ShareType;
import com.uit.buddy.event.social.PostSharedEvent;
import com.uit.buddy.exception.social.SocialErrorCode;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.ShareMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.social.ShareRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.social.ShareService;
import com.uit.buddy.util.CursorUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShareServiceImpl implements ShareService {

    private final PostRepository postRepository;
    private final ShareRepository shareRepository;
    private final StudentRepository studentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ShareMapper shareMapper;
    private final CometChatClient cometChatClient;

    @Override
    @Transactional
    public boolean sharePost(UUID postId, String mssv, ShareType type, SharePostRequest request) {
        Post postToShare = postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND));

        var student = studentRepository.findById(mssv)
                .orElseThrow(() -> new SocialException(SocialErrorCode.UNAUTHORIZED));

        Post rootOriginalPost = postToShare;
        while (rootOriginalPost.getOriginalPost() != null) {
            rootOriginalPost = rootOriginalPost.getOriginalPost();
        }

        boolean isFirstTime = !shareRepository.existsByPostIdAndMssv(rootOriginalPost.getId(), mssv);

        Share share = Share.builder().post(rootOriginalPost).student(student).type(type).build();
        shareRepository.save(share);

        UUID sharedPostId = null;

        switch (type) {
        case PROFILE -> {
            Post sharedPost = Post.builder().author(student).title("").content(request != null ? request.content() : "")
                    .originalPost(rootOriginalPost).type(PostType.SHARE).build();
            sharedPostId = postRepository.save(sharedPost).getId();
        }
        case MESSAGE -> {
            if (request == null || request.receiverId() == null || request.receiverId().isBlank()) {
                throw new SocialException(SocialErrorCode.INVALID_REQUEST);
            }

            String receiverType = request.receiverType() != null ? request.receiverType() : "user";

            if (!receiverType.equals("user") && !receiverType.equals("group")) {
                throw new SocialException(SocialErrorCode.INVALID_RECEIVE_TYPE);
            }

            if ("user".equals(receiverType)) {
                studentRepository.findById(request.receiverId())
                        .orElseThrow(() -> new SocialException(SocialErrorCode.STUDENT_NOT_FOUND));
            }

            String messageText = request.content() != null && !request.content().isBlank() ? request.content() : "";

            CometChatSendMessageRequest messageRequest = CometChatSendMessageRequest.builder()
                    .receiver(request.receiverId()).receiverType(receiverType).category("message").type("text")
                    .data(java.util.Map.of("text", messageText, "metadata",
                            Map.of("postId", rootOriginalPost.getId().toString())))
                    .build();

            cometChatClient.sendMessage(messageRequest, mssv);
        }
        }

        if (isFirstTime) {
            postRepository.incrementShareCount(rootOriginalPost.getId());

            if (!rootOriginalPost.getMssv().equals(mssv)) {
                String actorName = studentRepository.findById(mssv).map(s -> s.getFullName()).orElse(mssv);
                eventPublisher.publishEvent(new PostSharedEvent(mssv, actorName, rootOriginalPost.getMssv(),
                        rootOriginalPost.getId(), sharedPostId));
            }
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserShareResponse> getPostShares(UUID postId, String mssv, String cursor, int limit) {
        log.info("[Post Service] Getting shares for post: {}", postId);

        postRepository.findById(postId)
                .orElseThrow(() -> new SocialException(SocialErrorCode.POST_NOT_FOUND, "Post not found"));

        LocalDateTime cursorTime = null;
        String cursorMssv = null;
        if (cursor != null && !cursor.isBlank()) {
            CursorUtils.CursorContents contents = CursorUtils.decode(cursor);
            cursorTime = contents.timestamp();
            cursorMssv = contents.id().toString();
        }

        return shareRepository.findSharesWithCursor(postId, cursorTime, cursorMssv, limit + 1).stream()
                .map(shareMapper::toShareResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShareTargetResponse> getShareTargets(String mssv) {
        log.info("[Share Service] Getting share targets for user: {}", mssv);

        studentRepository.findById(mssv).orElseThrow(() -> new SocialException(SocialErrorCode.UNAUTHORIZED));

        List<ShareTargetResponse> targets = new java.util.ArrayList<>();

        try {
            CometChatConversationResponse conversationResponse = cometChatClient.getConversations(mssv);
            if (conversationResponse != null && conversationResponse.data() != null) {
                for (var conversation : conversationResponse.data()) {
                    var conversationWith = conversation.conversationWith();
                    var lastMessage = conversation.lastMessage();

                    LocalDateTime lastInteractionAt = lastMessage != null && lastMessage.sentAt() != null
                            ? LocalDateTime.ofInstant(Instant.ofEpochSecond(lastMessage.sentAt()),
                                    ZoneId.systemDefault())
                            : LocalDateTime.now();

                    if ("user".equals(conversation.conversationType())) {
                        targets.add(new ShareTargetResponse(conversationWith.uid(), conversationWith.name(),
                                conversationWith.avatar(), com.uit.buddy.enums.ShareTargetType.USER,
                                lastInteractionAt));
                    } else if ("group".equals(conversation.conversationType())) {
                        targets.add(new ShareTargetResponse(conversationWith.guid(), conversationWith.name(),
                                conversationWith.avatar(), com.uit.buddy.enums.ShareTargetType.GROUP,
                                lastInteractionAt));
                    }
                }
            }
        } catch (Exception e) {
            log.error("[Share Service] Failed to fetch conversations from CometChat", e);
        }
        return targets;
    }
}
