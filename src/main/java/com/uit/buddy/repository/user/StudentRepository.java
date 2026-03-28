package com.uit.buddy.repository.user;

import com.uit.buddy.entity.user.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    @Query(value = """
                SELECT *,
                       ts_rank(
                           setweight(to_tsvector('simple', coalesce(full_name,'')), 'A') ||
                           setweight(to_tsvector('simple', coalesce(mssv,'')), 'B'),
                           websearch_to_tsquery('simple', :keyword)
                       ) AS rank
                FROM students
                WHERE
                      (
                        setweight(to_tsvector('simple', coalesce(full_name,'')), 'A') ||
                        setweight(to_tsvector('simple', coalesce(mssv,'')), 'B')
                      )
                      @@ websearch_to_tsquery('simple', :keyword)
                ORDER BY rank DESC
            """, nativeQuery = true)
    Page<Student> searchStudentByKeyword(String keyword, Pageable pageable);

    @Query(value = "SELECT mssv FROM students WHERE mssv like '%52%'", nativeQuery = true)
    List<String> findMssvAll();

}
