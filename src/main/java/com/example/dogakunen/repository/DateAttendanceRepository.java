package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DateAttendanceRepository extends JpaRepository<DateAttendance, Integer> {

    //勤怠マスタ(日)作成
    @Transactional
    @Modifying
    @Query(value = "INSERT INTO date_attendances(date, user_id, month) " +
                "SELECT *, :newUserId, 12 " +
                "FROM generate_series( cast('2024-12-01' as timestamp), date_trunc('month', cast('2024-12-01' as timestamp) + '1 months') + '-1 days', '1 days')",
                nativeQuery = true)
    public void saveNewUser(@Param("newUserId") Integer newUserId);
}
