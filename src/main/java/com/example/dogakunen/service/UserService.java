package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.UserRepository;
import com.example.dogakunen.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
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
     * ログイン時のユーザ情報取得
     */

    public UserForm selectLoginUser(Integer employeeNumber){
        //社員番号をもとにユーザ情報取得
        List<User> results = userRepository.findByEmployeeNumber(employeeNumber);
        //存在しないアカウントの場合nullを返す
        if (results.size() == 0) {
            return null;
        }
        //アカウントが存在した場合、ユーザ情報をentityからformに詰める
        List<UserForm> loginUser = setUserForm(results);
        return loginUser.get(0);
    }

    /*
     * entityからformに詰める作業
     */

    private List<UserForm> setUserForm(List<User> results) {
        List<UserForm> users = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            UserForm user = new UserForm();
            User result = results.get(i);
            user.setId(result.getId());
            user.setPassword(result.getPassword());
            user.setName(result.getName());
            user.setEmployeeNumber(result.getEmployeeNumber());
            user.setPositionId(result.getPositionId());
            user.setIsStopped(result.getIsStopped());
            user.setCreatedDate(result.getCreatedDate());
            user.setUpdatedDate(result.getUpdatedDate());
            users.add(user);
        }
        return users;
    }

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
//            generalUser.setPositionName(result.getPosition().getName());
//            generalUser.setAttendanceStatus(result.getMonthAttendance().getAttendanceStatus());
            generalUsers.add(generalUser);
        }
        return generalUsers;
    }
}
