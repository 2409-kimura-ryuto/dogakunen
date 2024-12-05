package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    //ユーザーの取得（システム管理画面）
    @Query(value = "SELECT u.id AS id, u.employee_number AS employee_number, u.password AS password, u.name AS name, u.position_id AS position_id, u.is_stopped AS is_stopped, p.name AS positionName, u.created_date AS created_date, u.updated_date AS updated_date " +
            "FROM users u " +
            "INNER JOIN positions p ON u.position_id = p.id " +
            "ORDER BY u.employee_number ASC",
            nativeQuery = true)
    public List<User> selectUser();

}
