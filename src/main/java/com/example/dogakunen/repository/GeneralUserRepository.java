package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.GeneralUser;
import com.example.dogakunen.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GeneralUserRepository extends JpaRepository<GeneralUser, Integer> {

    @Query(
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
    public List<GeneralUser> findAllGeneralUserByOrderById(@Param("month") Integer month);
}
