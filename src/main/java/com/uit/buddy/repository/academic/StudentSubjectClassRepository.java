package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.StudentSubjectClass;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentSubjectClassRepository extends CrudRepository<StudentSubjectClass, UUID> {
    boolean existsByStudentMssvAndSubjectClassClassCode(String mssv, String classCode);
}
