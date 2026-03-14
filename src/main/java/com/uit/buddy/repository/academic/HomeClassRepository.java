package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.HomeClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HomeClassRepository extends JpaRepository<HomeClass, String> {
}
