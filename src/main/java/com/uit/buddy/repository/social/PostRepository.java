package com.uit.buddy.repository.social;

import org.springframework.stereotype.Repository;
import com.uit.buddy.entity.social.Post;

import io.lettuce.core.dynamic.annotation.Param;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {
    @Query(value = "SELECT * FROM posts ORDER BY created_at DESC, id DESC LIMIT :limit", nativeQuery = true)
    List<Post> findFirstPage(@Param("limit") int limit);

    @Query(value = """
            SELECT * FROM posts
            WHERE (created_at < :cursorTime) OR (created_at = :cursorTime AND id < :cursorId) ORDER BY created_at DESC, id DESC LIMIT :limit
            """, nativeQuery = true)
    List<Post> findNextPage(@Param("cursorTime") LocalDateTime cursorTime, @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);
}
