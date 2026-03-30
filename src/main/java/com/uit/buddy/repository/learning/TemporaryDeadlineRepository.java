package com.uit.buddy.repository.learning;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uit.buddy.entity.learning.TemporaryDeadline;

@Repository
public interface TemporaryDeadlineRepository extends JpaRepository<TemporaryDeadline, UUID> {
    List<TemporaryDeadline> findByMssv(String mssv);
}
