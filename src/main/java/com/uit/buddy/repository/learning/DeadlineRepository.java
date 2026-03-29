package com.uit.buddy.repository.learning;

import com.uit.buddy.entity.redis.Deadline;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadlineRepository extends CrudRepository<Deadline, String> {

    boolean existsById(String mssv_deadline);

}
