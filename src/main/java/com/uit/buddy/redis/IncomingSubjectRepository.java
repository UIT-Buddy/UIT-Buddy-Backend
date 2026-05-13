package com.uit.buddy.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomingSubjectRepository extends CrudRepository<IncomingSubject, String> {
}
