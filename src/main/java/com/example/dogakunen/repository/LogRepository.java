package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
    @Transactional
    @Query(value = "SELECT l FROM Log l JOIN FETCH l.user " +
            "WHERE l.user.id = :loginUserId " +
            "ORDER BY l.updatedDate DESC LIMIT 50")
    public List<Log> findAllLogByUserId(@Param("loginUserId") int loginUserId);
}
