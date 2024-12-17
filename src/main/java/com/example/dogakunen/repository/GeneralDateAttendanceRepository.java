package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.GeneralDateAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GeneralDateAttendanceRepository extends JpaRepository<GeneralDateAttendance, Integer> {
    @Query(
            value = "SELECT * " +
                    "FROM date_attendances " +
                    "WHERE date_attendances.user_id = :userId " +
                    "AND date_attendances.year = :year " +
                    "AND date_attendances.month = :month " +
                    "ORDER BY date_attendances.date" ,
            nativeQuery = true
    )
    public List<GeneralDateAttendance> findDateAttendanceByOrderByDate(@Param("userId") Integer userId, @Param("year") Integer year,
                                                                       @Param("month") Integer month);
}
