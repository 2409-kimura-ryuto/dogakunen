package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    //勤怠マスタ(日)作成
    @jakarta.transaction.Transactional
    @Modifying
    @Query(value = "INSERT INTO date_attendances(date, user_id) " +
            "SELECT *, :newUserId " +
            "FROM generate_series( cast(:startDate as timestamp), date_trunc('month', cast(:startDate as timestamp) + '3 months') + '-1 days', '1 days')",
            nativeQuery = true)
    public void saveNewUser(@Param("newUserId") Integer newUserId, @Param("startDate") String startDate);

    //勤怠マスタ(日)のmonthに値入れる
    @jakarta.transaction.Transactional
    @Modifying
    @Query(value = "UPDATE date_attendances SET " +
            "month = extract(month from date)" ,
            nativeQuery = true)
    public void saveMonth();

    //勤怠削除時に使用
    //各カラムを0もしくはnullでupdate
    @Transactional
    @Modifying
    @Query(
            value = "UPDATE date_attendances SET " +
                    "attendance = '0', " +
                    "work_time_start = '00:00:00', " +
                    "work_time_finish = '00:00:00', " +
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
