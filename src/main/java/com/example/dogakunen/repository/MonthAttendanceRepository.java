package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.MonthAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthAttendanceRepository extends JpaRepository<MonthAttendance, Integer> {
    @Query(
            value = "SELECT * FROM month_attendances " +
                    "WHERE user_id = :userId " +
                    "AND month = :month" ,
            nativeQuery = true
    )
    public MonthAttendance findByUserIdAndMonth(@Param("userId") Integer userId, @Param("month") Integer month);

}