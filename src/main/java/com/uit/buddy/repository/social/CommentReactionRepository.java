package com.uit.buddy.repository.social;

import com.uit.buddy.entity.social.CommentReaction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReaction, UUID> {
  Optional<CommentReaction> findByCommentIdAndMssv(UUID commentId, String mssv);

  @Transactional
  void deleteByCommentIdAndMssv(UUID commentId, String mssv);
}
