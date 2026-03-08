package com.uit.buddy.repository.social;

import org.springframework.stereotype.Repository;
import com.uit.buddy.entity.social.Post;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

}
