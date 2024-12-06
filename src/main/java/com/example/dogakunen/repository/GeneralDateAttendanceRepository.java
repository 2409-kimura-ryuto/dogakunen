package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.GeneralDateAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GeneralDateAttendanceRepository extends JpaRepository<GeneralDateAttendance, Integer> {
    @Query(
            value = "SELECT " +
                    "date_attendances.id AS id, " +
                    "date_attendances.user_id AS user_id, " +
                    "date_attendances.date AS date, " +
                    "date_attendances.month AS month, " +
                    "date_attendances.attendance AS attendance, " +
                    "date_attendances.work_time_start AS work_time_start, " +
                    "date_attendances.work_time_finish AS work_time_finish, " +
                    "date_attendances.break_time AS break_time, " +
                    "date_attendances.work_time AS work_time, " +
                    "date_attendances.memo AS memo, " +
                    "date_attendances.created_date AS created_date, " +
                    "date_attendances.updated_date AS updated_date, " +
                    "MAX(month_attendances.attendance_status) AS attendance_status " +
                    "FROM date_attendances " +
                    "INNER JOIN month_attendances " +
                    "ON date_attendances.month = month_attendances.month " +
                    "WHERE date_attendances.user_id = :userId " +
                    "AND date_attendances.month = :month " +
                    "GROUP BY date_attendances.id " +
                    "ORDER BY date_attendances.date" ,
            nativeQuery = true
    )
    public List<GeneralDateAttendance> findDateAttendanceByOrderByDate(@Param("userId") Integer userId, @Param("month") Integer month);
}
