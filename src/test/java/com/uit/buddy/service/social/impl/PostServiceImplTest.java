package com.uit.buddy.service.social.impl;

import com.uit.buddy.dto.request.social.CreatePostRequest;
import com.uit.buddy.dto.request.social.UpdatePostRequest;
import com.uit.buddy.dto.response.social.AuthorInfo;
import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.social.PostMedia;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.social.SocialException;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.social.PostMapper;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.repository.social.projection.PostFeedProjection;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostServiceImplTest {

        @Mock
        private PostRepository postRepository;
        @Mock
        private StudentRepository studentRepository;
        @Mock
        private PostMapper postMapper;
        @Mock
        private CloudinaryService cloudinaryService;

        @InjectMocks
        private PostServiceImpl postService;

        private String mssv;
        private UUID postId;
        private Student student;
        private Post post;
        private PostFeedProjection projection;

        @BeforeEach
        void setUp() {
                mssv = "22100001";
                postId = UUID.randomUUID();

                student = new Student();
                student.setMssv(mssv);
                student.setFullName("Test Student");

                post = Post.builder()
                                .title("Test Title")
                                .content("Test Content")
                                .author(student)
                                .medias(new ArrayList<>())
                                .build();
                ReflectionTestUtils.setField(post, "id", postId);
                ReflectionTestUtils.setField(postService, "limitNumberOfImages", 10);
                ReflectionTestUtils.setField(postService, "limitNumberOfVideos", 3);

                projection = mock(PostFeedProjection.class);
                when(projection.getId()).thenReturn(postId);
                when(projection.getTitle()).thenReturn("Test Title");
                when(projection.getContent()).thenReturn("Test Content");
                when(projection.getAuthorMssv()).thenReturn(mssv);
                when(projection.getAuthorFullName()).thenReturn("Test Student");
                when(projection.getCreatedAt()).thenReturn(LocalDateTime.now());
        }

        @Test
        void shouldCreatePostSuccessfully() {
                CreatePostRequest request = new CreatePostRequest(null, null);

                when(studentRepository.existsById(mssv)).thenReturn(true);
                when(studentRepository.getReferenceById(mssv)).thenReturn(student);
                when(cloudinaryService.uploadMultiMedia(null, null)).thenReturn(Collections.emptyList());
                when(postRepository.save(any(Post.class))).thenReturn(post);

                postService.createPost(mssv, "Test Title", "Test Content", request);

                verify(studentRepository).existsById(mssv);
                verify(cloudinaryService).uploadMultiMedia(null, null);
                verify(postRepository).save(any(Post.class));
        }

        @Test
        void shouldThrowExceptionWhenStudentNotFound() {
                CreatePostRequest request = new CreatePostRequest(null, null);
                when(studentRepository.existsById(mssv)).thenReturn(false);

                assertThatThrownBy(() -> postService.createPost(mssv, "Title", "Content", request))
                                .isInstanceOf(UserException.class);
        }

        @Test
        void shouldThrowExceptionWhenExceedingImageLimit() {
                List<MultipartFile> images = new ArrayList<>();
                for (int i = 0; i < 11; i++) {
                        images.add(mock(MultipartFile.class));
                }
                CreatePostRequest request = new CreatePostRequest(images, null);

                assertThatThrownBy(() -> postService.createPost(mssv, "Title", "Content", request))
                                .isInstanceOf(UserException.class);
        }

        @Test
        void shouldThrowExceptionWhenExceedingVideoLimit() {
                List<MultipartFile> videos = new ArrayList<>();
                for (int i = 0; i < 4; i++) {
                        videos.add(mock(MultipartFile.class));
                }
                CreatePostRequest request = new CreatePostRequest(null, videos);

                assertThatThrownBy(() -> postService.createPost(mssv, "Title", "Content", request))
                                .isInstanceOf(UserException.class);
        }

        @Test
        void shouldGetPostFeedSuccessfully() {
                PostFeedResponse feedResponse = new PostFeedResponse(
                                postId, "Title", "Content", Collections.emptyList(),
                                new AuthorInfo(mssv, "Test", "avatar.jpg", "21KTPM1"),
                                5L, 3L, 2L, false, LocalDateTime.now());

                when(postRepository.findFeed(eq(mssv), any(), any(), eq(11)))
                                .thenReturn(List.of(projection));
                when(postMapper.toPostFeedResponse(projection)).thenReturn(feedResponse);

                List<PostFeedResponse> result = postService.getPostFeed(mssv, null, 10);

                assertThat(result).hasSize(1);
                verify(postRepository).findFeed(eq(mssv), any(), any(), eq(11));
        }

        @Test
        void shouldGetPostDetailSuccessfully() {
                PostDetailResponse detailResponse = new PostDetailResponse(
                                postId, "Title", "Content", Collections.emptyList(),
                                new AuthorInfo(mssv, "Test", "avatar.jpg", "21KTPM1"),
                                5L, 3L, 2L, false, LocalDateTime.now(), LocalDateTime.now());

                when(postRepository.findDetailWithStatus(postId, mssv)).thenReturn(Optional.of(projection));
                when(postMapper.toPostDetailResponseFromProjection(projection)).thenReturn(detailResponse);

                PostDetailResponse result = postService.getPostDetail(postId, mssv);

                assertThat(result).isNotNull();
                assertThat(result.id()).isEqualTo(postId);
                verify(postRepository).findDetailWithStatus(postId, mssv);
        }

        @Test
        void shouldThrowExceptionWhenPostNotFoundForDetail() {
                when(postRepository.findDetailWithStatus(postId, mssv)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> postService.getPostDetail(postId, mssv))
                                .isInstanceOf(SocialException.class);
        }

        @Test
        void shouldUpdatePostSuccessfully() {
                UpdatePostRequest request = new UpdatePostRequest("New Title", "New Content");

                when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                when(postRepository.save(any(Post.class))).thenReturn(post);

                postService.updatePost(postId, mssv, request);

                assertThat(post.getTitle()).isEqualTo("New Title");
                assertThat(post.getContent()).isEqualTo("New Content");
                verify(postRepository).save(post);
        }

        @Test
        void shouldUpdatePostWithPartialData() {
                UpdatePostRequest request = new UpdatePostRequest("New Title", null);

                when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                when(postRepository.save(any(Post.class))).thenReturn(post);

                postService.updatePost(postId, mssv, request);

                assertThat(post.getTitle()).isEqualTo("New Title");
                assertThat(post.getContent()).isEqualTo("Test Content"); // unchanged
                verify(postRepository).save(post);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNotOwnedPost() {
                UpdatePostRequest request = new UpdatePostRequest("New Title", "New Content");
                when(postRepository.findById(postId)).thenReturn(Optional.of(post));

                assertThatThrownBy(() -> postService.updatePost(postId, "22100002", request))
                                .isInstanceOf(SocialException.class);
        }

        @Test
        void shouldThrowExceptionWhenUpdatingNonExistentPost() {
                UpdatePostRequest request = new UpdatePostRequest("New Title", "New Content");
                when(postRepository.findById(postId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> postService.updatePost(postId, mssv, request))
                                .isInstanceOf(SocialException.class);
        }

        @Test
        void shouldDeletePostSuccessfully() {
                PostMedia media = new PostMedia(FileType.IMAGE, "url");
                post.setMedias(List.of(media));

                when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                doNothing().when(cloudinaryService).deletePostMedia(anyList());
                doNothing().when(postRepository).delete(post);

                postService.deletePost(postId, mssv);

                verify(cloudinaryService).deletePostMedia(anyList());
                verify(postRepository).delete(post);
        }

        @Test
        void shouldDeletePostWithoutMedia() {
                post.setMedias(Collections.emptyList());

                when(postRepository.findById(postId)).thenReturn(Optional.of(post));
                doNothing().when(postRepository).delete(post);

                postService.deletePost(postId, mssv);

                verify(cloudinaryService, never()).deletePostMedia(anyList());
                verify(postRepository).delete(post);
        }

        @Test
        void shouldThrowExceptionWhenDeletingNotOwnedPost() {
                when(postRepository.findById(postId)).thenReturn(Optional.of(post));

                assertThatThrownBy(() -> postService.deletePost(postId, "22100002"))
                                .isInstanceOf(SocialException.class);
        }

        @Test
        void shouldThrowExceptionWhenDeletingNonExistentPost() {
                when(postRepository.findById(postId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> postService.deletePost(postId, mssv))
                                .isInstanceOf(SocialException.class);
        }

        @Test
        void shouldSearchPostsWithKeyword() {
                Page<PostFeedProjection> projectionPage = new PageImpl<>(List.of(projection));
                PostFeedResponse feedResponse = new PostFeedResponse(
                                postId, "Title", "Content", Collections.emptyList(),
                                new AuthorInfo(mssv, "Test", "avatar.jpg", "21KTPM1"),
                                5L, 3L, 2L, false, LocalDateTime.now());

                when(postRepository.searchPostFull(eq("test"), eq(mssv), any())).thenReturn(projectionPage);
                when(postMapper.toPostFeedResponse(projection)).thenReturn(feedResponse);

                Page<PostFeedResponse> result = postService.searchPost("test", mssv, PageRequest.of(0, 10));

                assertThat(result.getContent()).hasSize(1);
                verify(postRepository).searchPostFull(eq("test"), eq(mssv), any());
        }

        @Test
        void shouldGetAllPostsWhenKeywordIsNull() {
                Page<PostFeedProjection> projectionPage = new PageImpl<>(List.of(projection));
                PostFeedResponse feedResponse = new PostFeedResponse(
                                postId, "Title", "Content", Collections.emptyList(),
                                new AuthorInfo(mssv, "Test", "avatar.jpg", "21KTPM1"),
                                5L, 3L, 2L, false, LocalDateTime.now());

                when(postRepository.findAllPosts(eq(mssv), any())).thenReturn(projectionPage);
                when(postMapper.toPostFeedResponse(projection)).thenReturn(feedResponse);

                Page<PostFeedResponse> result = postService.searchPost(null, mssv, PageRequest.of(0, 10));

                assertThat(result.getContent()).hasSize(1);
                verify(postRepository).findAllPosts(eq(mssv), any());
        }

        @Test
        void shouldGetAllPostsWhenKeywordIsBlank() {
                Page<PostFeedProjection> projectionPage = new PageImpl<>(List.of(projection));
                PostFeedResponse feedResponse = new PostFeedResponse(
                                postId, "Title", "Content", Collections.emptyList(),
                                new AuthorInfo(mssv, "Test", "avatar.jpg", "21KTPM1"),
                                5L, 3L, 2L, false, LocalDateTime.now());

                when(postRepository.findAllPosts(eq(mssv), any())).thenReturn(projectionPage);
                when(postMapper.toPostFeedResponse(projection)).thenReturn(feedResponse);

                Page<PostFeedResponse> result = postService.searchPost("", mssv, PageRequest.of(0, 10));

                assertThat(result.getContent()).hasSize(1);
                verify(postRepository).findAllPosts(eq(mssv), any());
        }
}
