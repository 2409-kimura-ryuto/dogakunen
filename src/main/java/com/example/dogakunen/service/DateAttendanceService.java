package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DateAttendanceService {

    @Autowired
    DateAttendanceRepository dateAttendanceRepository;

    /*
     * 勤怠情報取得
     */
    public DateAttendanceForm findDateAttendance(Integer id, Integer month) {
        DateAttendance result = dateAttendanceRepository.findDateAttendanceByOrderByDate(id, month);
        DateAttendanceForm DateAttendance = setDateAttendanceForm(result);
        return DateAttendance;
    }
    /*
     * DBから取得したDateAttendanceをFormに変換
     */
    private DateAttendanceForm setDateAttendanceForm(DateAttendance result) {
        DateAttendanceForm dateAttendanceForm = new DateAttendanceForm();
        BeanUtils.copyProperties(result, dateAttendanceForm);
        return  dateAttendanceForm;
    }

    //勤怠マスタ(日)作成
    public void saveNewDate(int newUserId) {

        dateAttendanceRepository.saveNewUser(newUserId);
    }


}
