package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.time.Duration;


@Repository
public interface DateAttendanceRepository extends JpaRepository<DateAttendance, Integer> {

   //勤怠情報取得
    @Transactional
    @Query(value = "SELECT d FROM DateAttendance d JOIN FETCH d.user " +
            "WHERE d.month = :month " + "AND d.user.id = :loginId " +
            "ORDER BY d.date ASC")
    public List<DateAttendance> findAllAttendances(@Param("month") int month, @Param("loginId") Integer loginID);

    public List<DateAttendance> findByUserAndDate(User loginUser, Date date);

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

    //勤怠マスタ(日)作成
    @jakarta.transaction.Transactional
    @Modifying
    @Query(value = "INSERT INTO date_attendances(date, user_id, month) " +
            "SELECT *, :newUserId, 12 " +
            "FROM generate_series( cast('2024-12-01' as timestamp), date_trunc('month', cast('2024-12-01' as timestamp) + '1 months') + '-1 days', '1 days')",
            nativeQuery = true)
    public void saveNewUser(@Param("newUserId") Integer newUserId);

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

    //勤怠登録時に使用
    @Transactional
    @Modifying
    @Query(
         value = "UPDATE date_attendances SET " +
                 "attendance = :attendance, " +
                 "work_time_start = :workTimeStart, " +
                 "work_time_finish = :workTimeFinish, " +
                 "break_time = CAST(:breakTime AS interval), " +
                 "work_time = CAST(:workTime AS interval), " +
                 "memo = :memo " +
                 "WHERE id = :id" ,
         nativeQuery = true
    )
    public void addAttendance(@Param("id") Integer id, @Param("attendance") Integer attendance, @Param("workTimeStart") LocalTime workTimeStart, @Param("workTimeFinish") LocalTime workTimeFinish, @Param("breakTime") String breakTime, @Param("workTime") String workTime, @Param("memo") String memo);


}
