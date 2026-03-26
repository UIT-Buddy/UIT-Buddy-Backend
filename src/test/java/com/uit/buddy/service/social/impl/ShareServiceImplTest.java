package com.uit.buddy.service.social.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

import com.uit.buddy.dto.request.social.SharePostRequest;
import com.uit.buddy.dto.response.social.UserShareResponse;
import com.uit.buddy.dto.response.social.UserSummary;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.social.Share;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.PostType;
import com.uit.buddy.enums.ShareType;
import com.uit.buddy.event.social.PostSharedEvent;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.mapper.social.ShareMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.social.ShareRepository;
import com.uit.buddy.repository.social.projection.ShareProjection;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.util.CursorUtils;
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
class ShareServiceImplTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private ShareRepository shareRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ShareMapper shareMapper;
    @Mock
    private com.uit.buddy.client.CometChatClient cometChatClient;

    @InjectMocks
    private ShareServiceImpl shareService;

    private String mssv;
    private UUID postId;
    private Post post;
    private Student student;
    private ShareProjection projection;

    @BeforeEach
    void setUp() {
        mssv = "22100001";
        postId = UUID.randomUUID();

        student = new Student();
        student.setMssv(mssv);
        student.setFullName("Test Student");

        post = Post.builder().title("Test Post").content("Test Content").author(student).build();
        ReflectionTestUtils.setField(post, "id", postId);
        ReflectionTestUtils.setField(post, "mssv", "22100002"); // Different from actor to test event publishing

        projection = mock(ShareProjection.class);
        when(projection.getMssv()).thenReturn(mssv);
        when(projection.getFullName()).thenReturn("Test Student");
        when(projection.getSharedAt()).thenReturn(LocalDateTime.now());
    }

    @Test
    void shouldSharePostToProfileSuccessfully() {
        SharePostRequest request = new SharePostRequest("Sharing this post", null, null);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        // Mock postRepository.save to return a post with ID
        Post savedPost = Post.builder().title("Shared: Test Post").content("Test Content").author(student).build();
        ReflectionTestUtils.setField(savedPost, "id", UUID.randomUUID());
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        verify(postRepository).save(any(Post.class)); // For creating shared post
        verify(postRepository).incrementShareCount(postId);
        verify(eventPublisher).publishEvent(any(PostSharedEvent.class));
    }

    @Test
    void shouldSharePostToMessageSuccessfully() {
        SharePostRequest request = new SharePostRequest("Sharing via message", "receiver123", "user");

        Student receiver = new Student();
        receiver.setMssv("receiver123");
        receiver.setFullName("Receiver Student");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(studentRepository.findById("receiver123")).thenReturn(Optional.of(receiver));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());
        doNothing().when(cometChatClient).sendMessage(any(), eq(mssv));

        boolean result = shareService.sharePost(postId, mssv, ShareType.MESSAGE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        verify(postRepository, never()).save(any(Post.class)); // No shared post created for MESSAGE
        verify(postRepository).incrementShareCount(postId);
        verify(eventPublisher).publishEvent(any(PostSharedEvent.class));
        verify(cometChatClient).sendMessage(any(), eq(mssv));
    }

    @Test
    void shouldNotIncrementShareCountWhenAlreadyShared() {
        SharePostRequest request = new SharePostRequest("Sharing again", null, null);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(true);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        // Mock postRepository.save to return a post with ID
        Post savedPost = Post.builder().title("Shared: Test Post").content("Test Content").author(student).build();
        ReflectionTestUtils.setField(savedPost, "id", UUID.randomUUID());
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        verify(postRepository, never()).incrementShareCount(postId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldNotPublishEventWhenSharingOwnPost() {
        Post ownPost = Post.builder().title("Own Post").content("My content").author(student).build();
        ReflectionTestUtils.setField(ownPost, "id", postId);
        ReflectionTestUtils.setField(ownPost, "mssv", mssv); // Same as actor

        SharePostRequest request = new SharePostRequest("Sharing my own post", null, null);

        when(postRepository.findById(postId)).thenReturn(Optional.of(ownPost));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        // Mock postRepository.save to return a post with ID
        Post savedPost = Post.builder().title("Shared: Own Post").content("My content").author(student).build();
        ReflectionTestUtils.setField(savedPost, "id", UUID.randomUUID());
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, request);

        assertThat(result).isTrue();
        verify(postRepository).incrementShareCount(postId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowExceptionWhenPostNotFound() {
        SharePostRequest request = new SharePostRequest("Sharing", null, null);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.sharePost(postId, mssv, ShareType.PROFILE, request))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldThrowExceptionWhenStudentNotFound() {
        SharePostRequest request = new SharePostRequest("Sharing", null, null);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.sharePost(postId, mssv, ShareType.PROFILE, request))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldGetPostSharesSuccessfully() {
        UserShareResponse shareResponse = new UserShareResponse(new UserSummary(mssv, "Test Student", "avatar.jpg"),
                LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(shareRepository.findSharesWithCursor(eq(postId), any(), any(), eq(11))).thenReturn(List.of(projection));
        when(shareMapper.toShareResponse(projection)).thenReturn(shareResponse);

        List<UserShareResponse> result = shareService.getPostShares(postId, mssv, null, 10);

        assertThat(result).hasSize(1);
        verify(postRepository).findById(postId);
        verify(shareRepository).findSharesWithCursor(eq(postId), any(), any(), eq(11));
    }

    @Test
    void shouldThrowExceptionWhenGettingSharesForNonExistentPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shareService.getPostShares(postId, mssv, null, 10))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldHandleNullShareRequest() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        // Mock postRepository.save to return a post with ID
        Post savedPost = Post.builder().title("Shared: Test Post").content("Test Content").author(student).build();
        ReflectionTestUtils.setField(savedPost, "id", UUID.randomUUID());
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, null);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void shouldGetPostSharesWithCursor() {
        // Create valid cursor using CursorUtils
        String cursor = CursorUtils.encode(LocalDateTime.now(), UUID.randomUUID());
        UserShareResponse shareResponse = new UserShareResponse(new UserSummary(mssv, "Test Student", "avatar.jpg"),
                LocalDateTime.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(shareRepository.findSharesWithCursor(eq(postId), any(), any(), eq(11))).thenReturn(List.of(projection));
        when(shareMapper.toShareResponse(projection)).thenReturn(shareResponse);

        List<UserShareResponse> result = shareService.getPostShares(postId, mssv, cursor, 10);

        assertThat(result).hasSize(1);
        verify(shareRepository).findSharesWithCursor(eq(postId), any(), any(), eq(11));
    }

    @Test
    void shouldSharePostToMessageWithReceiverAndType() {
        SharePostRequest request = new SharePostRequest("Check this out!", "receiver123", "user");

        Student receiver = new Student();
        receiver.setMssv("receiver123");
        receiver.setFullName("Receiver Student");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(studentRepository.findById("receiver123")).thenReturn(Optional.of(receiver));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());
        doNothing().when(cometChatClient).sendMessage(any(), eq(mssv));

        boolean result = shareService.sharePost(postId, mssv, ShareType.MESSAGE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(argThat(share -> share.getType() == ShareType.MESSAGE));
        verify(postRepository, never()).save(any(Post.class)); // MESSAGE type doesn't create shared post
        verify(postRepository).incrementShareCount(postId);
        verify(eventPublisher).publishEvent(any(PostSharedEvent.class));
        verify(cometChatClient).sendMessage(any(), eq(mssv));
    }

    @Test
    void shouldSharePostToProfileWithCaption() {
        SharePostRequest request = new SharePostRequest("Great post!", null, null);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        // Mock postRepository.save to return a post with ID
        Post savedPost = Post.builder().title("").content("Great post!").author(student).build();
        ReflectionTestUtils.setField(savedPost, "id", UUID.randomUUID());
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        boolean result = shareService.sharePost(postId, mssv, ShareType.PROFILE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(argThat(share -> share.getType() == ShareType.PROFILE));
        verify(postRepository).save(any(Post.class)); // PROFILE type creates shared post
        verify(postRepository).incrementShareCount(postId);
        verify(eventPublisher).publishEvent(any(PostSharedEvent.class));
    }

    @Test
    void shouldSharePostToMessageWithGroupReceiver() {
        SharePostRequest request = new SharePostRequest("Sharing to group", "group456", "group");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(shareRepository.existsByPostIdAndMssv(postId, mssv)).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());
        doNothing().when(cometChatClient).sendMessage(any(), eq(mssv));

        boolean result = shareService.sharePost(postId, mssv, ShareType.MESSAGE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(argThat(share -> share.getType() == ShareType.MESSAGE));
        verify(postRepository).incrementShareCount(postId);
        verify(cometChatClient).sendMessage(any(), eq(mssv));
    }

    @Test
    void shouldThrowExceptionWhenSharingToMessageWithoutReceiver() {
        SharePostRequest request = new SharePostRequest("Missing receiver", null, null);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));

        // This should throw exception because MESSAGE type requires receiverId
        assertThatThrownBy(() -> shareService.sharePost(postId, mssv, ShareType.MESSAGE, request))
                .isInstanceOf(SocialException.class);
    }

    @Test
    void shouldTraceToRootOriginalPostWhenSharingSharedPost() {
        // Setup: A creates original post, B shares it, C shares B's shared post
        Student studentA = new Student();
        studentA.setMssv("22100001");
        studentA.setFullName("Student A");

        Student studentB = new Student();
        studentB.setMssv("22100002");
        studentB.setFullName("Student B");

        Student studentC = new Student();
        studentC.setMssv("22100003");
        studentC.setFullName("Student C");

        // Original post by A
        UUID originalPostId = UUID.randomUUID();
        Post originalPost = Post.builder().title("Original Post").content("Original Content").author(studentA).build();
        ReflectionTestUtils.setField(originalPost, "id", originalPostId);
        ReflectionTestUtils.setField(originalPost, "mssv", "22100001");

        // B's shared post (points to A's post)
        UUID sharedPostByBId = UUID.randomUUID();
        Post sharedPostByB = Post.builder().title("").content("B's caption").author(studentB).originalPost(originalPost)
                .type(PostType.SHARE).build();
        ReflectionTestUtils.setField(sharedPostByB, "id", sharedPostByBId);
        ReflectionTestUtils.setField(sharedPostByB, "mssv", "22100002");

        SharePostRequest request = new SharePostRequest("C's caption", null, null);

        // C tries to share B's shared post
        when(postRepository.findById(sharedPostByBId)).thenReturn(Optional.of(sharedPostByB));
        when(studentRepository.findById("22100003")).thenReturn(Optional.of(studentC));
        when(shareRepository.existsByPostIdAndMssv(originalPostId, "22100003")).thenReturn(false);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        Post savedPost = Post.builder().title("").content("C's caption").author(studentC).build();
        ReflectionTestUtils.setField(savedPost, "id", UUID.randomUUID());
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        boolean result = shareService.sharePost(sharedPostByBId, "22100003", ShareType.PROFILE, request);

        assertThat(result).isTrue();
        // Verify share record points to original post A, not B's shared post
        verify(shareRepository).save(argThat(share -> share.getPost().getId().equals(originalPostId)));
        // Verify increment is called on original post A
        verify(postRepository).incrementShareCount(originalPostId);
        // Verify created shared post points to original post A
        verify(postRepository)
                .save(argThat(p -> p.getOriginalPost() != null && p.getOriginalPost().getId().equals(originalPostId)));
        // Verify event is published with original post author
        verify(eventPublisher).publishEvent(any(PostSharedEvent.class));
    }

    @Test
    void shouldNotIncrementShareCountWhenUserAlreadySharedRootPost() {
        // Setup: A creates original post, B shares it, C shares A's post, then C tries
        // to share B's shared post
        Student studentA = new Student();
        studentA.setMssv("22100001");
        studentA.setFullName("Student A");

        Student studentB = new Student();
        studentB.setMssv("22100002");
        studentB.setFullName("Student B");

        Student studentC = new Student();
        studentC.setMssv("22100003");
        studentC.setFullName("Student C");

        UUID originalPostId = UUID.randomUUID();
        Post originalPost = Post.builder().title("Original Post").content("Original Content").author(studentA).build();
        ReflectionTestUtils.setField(originalPost, "id", originalPostId);
        ReflectionTestUtils.setField(originalPost, "mssv", "22100001");

        UUID sharedPostByBId = UUID.randomUUID();
        Post sharedPostByB = Post.builder().title("").content("B's caption").author(studentB).originalPost(originalPost)
                .type(PostType.SHARE).build();
        ReflectionTestUtils.setField(sharedPostByB, "id", sharedPostByBId);
        ReflectionTestUtils.setField(sharedPostByB, "mssv", "22100002");

        SharePostRequest request = new SharePostRequest("C's second share", null, null);

        when(postRepository.findById(sharedPostByBId)).thenReturn(Optional.of(sharedPostByB));
        when(studentRepository.findById("22100003")).thenReturn(Optional.of(studentC));
        // C already shared the root original post
        when(shareRepository.existsByPostIdAndMssv(originalPostId, "22100003")).thenReturn(true);
        when(shareRepository.save(any(Share.class))).thenReturn(new Share());

        Post savedPost = Post.builder().title("").content("C's second share").author(studentC).build();
        ReflectionTestUtils.setField(savedPost, "id", UUID.randomUUID());
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        boolean result = shareService.sharePost(sharedPostByBId, "22100003", ShareType.PROFILE, request);

        assertThat(result).isTrue();
        verify(shareRepository).save(any(Share.class));
        // Should NOT increment share count since C already shared the root post
        verify(postRepository, never()).incrementShareCount(any());
        // Should NOT publish event
        verify(eventPublisher, never()).publishEvent(any());
    }
}
