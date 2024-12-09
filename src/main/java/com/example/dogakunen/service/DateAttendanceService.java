package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
import com.example.dogakunen.repository.UserRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ZERO;

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

    /*
     * 勤怠情報取得(勤怠削除時)
     */
    public void deleteAttendance(Integer id) {
        //break_timeとwork_time用の0を用意
        String zero = "0 hours 0 minutes 0 seconds";
        //勤怠記録のIDと用意した0を引数にリポジトリを呼び出す
        dateAttendanceRepository.updateAttendance(id, zero);
    }
}
