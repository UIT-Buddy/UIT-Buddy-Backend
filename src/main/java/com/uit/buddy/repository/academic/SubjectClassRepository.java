package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.SubjectClass;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectClassRepository extends CrudRepository<SubjectClass, String> {
}
