package com.uit.buddy.repository.user;

import com.uit.buddy.entity.user.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    Optional<Student> findByMssv(String mssv);

    boolean existsByMssv(String mssv);
}
