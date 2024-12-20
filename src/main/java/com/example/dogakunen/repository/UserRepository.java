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

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    //ログイン時のユーザ情報取得
    public List<User> findByEmployeeNumber(String employeeNumber);

    //ユーザーの取得（システム管理画面）
    @Query(value = "SELECT u.id AS id, u.employee_number AS employee_number, u.password AS password, u.name AS name, u.position_id AS position_id, u.is_stopped AS is_stopped, p.name AS positionName, u.created_date AS created_date, u.updated_date AS updated_date " +
            "FROM users u " +
            "INNER JOIN positions p ON u.position_id = p.id " +
            "ORDER BY u.employee_number ASC",
            nativeQuery = true)
    public List<User> selectUser();

    //アカウントの停止・復活のみ更新
    @Transactional
    @Modifying
    @Query(value = "UPDATE users SET is_stopped = :isStoppedId, updated_date = CURRENT_TIMESTAMP WHERE id = :userId", nativeQuery = true)
    public void editIsStopped(@Param("isStoppedId") Integer isStoppedId, @Param("userId")Integer userId);

}
