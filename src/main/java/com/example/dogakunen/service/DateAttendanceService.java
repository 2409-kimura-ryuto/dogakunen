package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
import com.example.dogakunen.repository.UserRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import org.antlr.v4.runtime.misc.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DateAttendanceService {
    @Autowired
    DateAttendanceRepository dateAttendanceRepository;
    @Autowired
    UserRepository userRepository;

    public List<DateAttendanceForm> findALLAttendances(int month, Integer loginId) {
        List<DateAttendance> results = dateAttendanceRepository.findAllAttendances(month, loginId);
        List<DateAttendanceForm> dateAttendances = setDateAttendanceForm(results);
        return dateAttendances;
    }

    public List<DateAttendanceForm> setDateAttendanceForm(List<DateAttendance> results) {
        List<DateAttendanceForm> dateAttendances = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            DateAttendanceForm dateAttendance = new DateAttendanceForm();
            DateAttendance result = results.get(i);
            dateAttendance.setId(result.getId());
            dateAttendance.setDate(result.getDate());
            dateAttendance.setMonth(result.getMonth());
            dateAttendance.setAttendance(result.getAttendance());
            dateAttendance.setWorkTimeStart(result.getWorkTimeStart());
            dateAttendance.setWorkTimeFinish(result.getWorkTimeFinish());
            dateAttendance.setBreakTime(result.getBreakTime());
            dateAttendance.setWorkTime(result.getWorkTime());
            dateAttendance.setMemo(result.getMemo());
            dateAttendance.setUserName(result.getUser().getName());
            dateAttendance.setEmployeeNumber(result.getUser().getEmployeeNumber());

            dateAttendances.add(dateAttendance);
        }
        return dateAttendances;
    }

    public void postNew(DateAttendanceForm reqAttendance, Integer employeeNumber, Integer month) throws ParseException {
        List<User> results = userRepository.findByEmployeeNumber(employeeNumber);
        DateAttendance dateAttendance = setEntity(reqAttendance, results.get(0), month);
        dateAttendanceRepository.save(dateAttendance);
    }

    public DateAttendance setEntity(DateAttendanceForm reqAttendance, User loginUser, Integer month) throws ParseException {
        DateAttendance dateAttendance = new DateAttendance();

        dateAttendance.setUser(loginUser);
        dateAttendance.setMonth(month);
        dateAttendance.setDate(reqAttendance.getDate());
        dateAttendance.setBreakTime(reqAttendance.getBreakTime());
        dateAttendance.setWorkTimeStart(reqAttendance.getWorkTimeStart());
        dateAttendance.setWorkTimeFinish(reqAttendance.getWorkTimeFinish());
        dateAttendance.setAttendance(reqAttendance.getAttendance());
        dateAttendance.setMemo(reqAttendance.getMemo());

        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(nowDate);
        dateAttendance.setCreatedDate(sdf.parse(currentTime));
        dateAttendance.setUpdatedDate(sdf.parse(currentTime));


        return dateAttendance;
    }
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
