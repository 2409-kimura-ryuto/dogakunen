package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.MonthAttendanceRepository;
import com.example.dogakunen.repository.UserRepository;
import com.example.dogakunen.repository.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    /*
     * 申請対象者情報取得
     */
    public List<UserForm> findAllGeneralUser(Integer month) {
        List<User> results = userRepository.findAllGeneralUserByOrderById(month);
        List<UserForm> generalUsers = setGeneralUserForm(results);
        return generalUsers;
    }
    /*
     * DBから取得したgeneralUserをFormに変換
     */
    private List<UserForm> setGeneralUserForm(List<User> results) {
        List<UserForm> generalUsers = new ArrayList<>();

        for (User result : results) {
            UserForm generalUser = new UserForm();
            BeanUtils.copyProperties(result, generalUser);
            generalUsers.add(generalUser);
        }
        return generalUsers;
    }
}
