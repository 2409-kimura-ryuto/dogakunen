package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.MonthAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Repository
public interface DateAttendanceRepository extends JpaRepository<DateAttendance, Integer> {
    @Query(
            value = "SELECT" +
                    "date_attendances.id AS id " +
                    "date_attendances.user_id AS user_id " +
                    "date_attendances.date AS date " +
                    "date_attendances.month AS month " +
                    "date_attendances.attendance AS attendance " +
                    "date_attendances.work_time_start AS work_time_start " +
                    "date_attendances.work_time_finish AS work_time_finish " +
                    "date_attendances.break_time AS break_time " +
                    "date_attendances.work_time AS work_time " +
                    "date_attendances.memo AS memo " +
                    "date_attendances.created_date AS created_date " +
                    "date_attendances.updated_date AS updated_date " +
                    "date_attendances.attendance_status AS attendance_status " +
                    " FROM month_attendances " +
                    "WHERE user_id = :userId " +
                    "AND month = :month" ,
            nativeQuery = true
    )
    public DateAttendance findDateAttendanceByOrderByDate(@Param("userId") Integer userId, @Param("month") Integer month);


    //勤怠削除時に使用
    //各カラムを0もしくはnullでupdate
    @Transactional
    @Modifying
    @Query(
            value = "UPDATE date_attendances SET " +
                    "attendance = '0', " +
                    "work_time_start = null, " +
                    "work_time_finish = null, " +
                    "break_time = CAST(:zero AS interval), " +
                    "work_time = CAST(:zero AS interval), " +
                    "memo = null " +
                    "WHERE id = :id" ,
            nativeQuery = true
    )
    public void updateAttendance(@Param("id") Integer id, @Param("zero") String zero);


}
