package com.uit.buddy.repository.social;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import com.uit.buddy.entity.social.Post;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {
    @Async
    @Query(value = """
                SELECT p.id,
                       ts_rank(
                           setweight(to_tsvector('simple', coalesce(title,'')), 'A') ||
                           setweight(to_tsvector('simple', coalesce(content,'')), 'B'),
                           websearch_to_tsquery('simple', :keyword)
                       ) AS rank
                FROM posts p
                WHERE
                      (
                        setweight(to_tsvector('simple', coalesce(title,'')), 'A') ||
                        setweight(to_tsvector('simple', coalesce(content,'')), 'B')
                      )
                      @@ websearch_to_tsquery('simple', :keyword)
                ORDER BY rank DESC
            """, nativeQuery = true)
    List<UUID> searchPostByKeyword(String keyword);
    @EntityGraph(attributePaths = {"author"})
    @Query(value = """
        SELECT p
        FROM Post p
        WHERE p.id IN :uuids
        """)
    Page<Post> findAll(@Param("uuids") List<UUID> uuids, Pageable pageable);
}
