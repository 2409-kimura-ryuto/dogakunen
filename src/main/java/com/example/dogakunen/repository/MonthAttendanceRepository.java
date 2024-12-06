package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.MonthAttendance;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthAttendanceRepository extends JpaRepository<MonthAttendance, Integer> {

    //勤怠マスタ(月)作成
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO month_attendances(user_id, month) " +
            "VALUES(:newUserId, :month)",
            nativeQuery = true)
    public void saveNewMonth(@Param("newUserId") Integer newUserId, @Param("month") Integer month);
}