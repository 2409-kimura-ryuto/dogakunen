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
    public List<User> findByEmployeeNumber(String employeeNumber);

}
