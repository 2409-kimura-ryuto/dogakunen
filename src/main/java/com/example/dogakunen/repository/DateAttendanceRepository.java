package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface DateAttendanceRepository extends JpaRepository<DateAttendance, Integer> {
    @Transactional
    @Query(value = "SELECT d FROM DateAttendance d JOIN FETCH d.user " +
            "WHERE d.month = :month " + "AND d.user.id = :loginId " +
            "ORDER BY d.date ASC")
    public List<DateAttendance> findAllAttendances(@Param("month") int month, @Param("loginId") Integer loginID);
}
