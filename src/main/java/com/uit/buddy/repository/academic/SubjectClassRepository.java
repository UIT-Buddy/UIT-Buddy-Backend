package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.Semester;
import com.uit.buddy.entity.academic.SubjectClass;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectClassRepository extends JpaRepository<SubjectClass, UUID> {
    List<SubjectClass> findAllByClassCodeInAndSemester(Collection<String> classCodes, Semester semester);

}
