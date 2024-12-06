package com.example.dogakunen.repository;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.entity.MonthAttendance;
import com.example.dogakunen.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    //ログイン時のユーザ情報取得
    public List<User> findByEmployeeNumber(Integer employeeNumber);

    @Query(
//            value = "SELECT " +
//                    "u.employee_number AS employeeNumber, u.name AS name, p.name AS positionName, m.month AS month, m.attendance_status AS attendanceStatus " +
//                    "FROM users u " +
//                    "INNER JOIN positions p ON u.position_id = p.id " +
//                    "INNER JOIN month_attendances m ON u.id = m.user_id " +
//                    "WHERE u.position_id= 1 " +
//                    "AND m.month= :month " +
//                    "ORDER BY u.id ASC " ,
//            nativeQuery = true

            /*passwordやis_stoppedはviewで表示させないが、Userにフィールドとして定義されているため
              取得しないとエラーになってしまう*/
            value = "SELECT " +
                    "users.id AS id, " +
                    "users.password AS password, " +
                    "users.employee_number AS employee_number, " +
                    "users.name AS name, " +
                    "users.position_id AS position_id, " +
                    "positions.name AS position_name, " +
                    "month_attendances.month AS month, " +
                    "month_attendances.attendance_status AS attendance_status, " +
                    "users.is_stopped AS is_stopped, " +
                    "users.created_date AS created_date, " +
                    "users.updated_date AS updated_date " +
                    "FROM users " +
                    "INNER JOIN positions " +
                    "ON users.position_id = positions.id " +
                    "INNER JOIN month_attendances " +
                    "ON users.id = month_attendances.user_id " +
                    "WHERE users.position_id= 1 " +
                    "AND month_attendances.month= :month " +
                    "ORDER BY users.id" ,
            nativeQuery = true
    )
    public List<User> findAllGeneralUserByOrderById(@Param("month") Integer month);

}
