package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import org.antlr.v4.runtime.misc.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<DateAttendanceForm> findALLAttendances(int month, Integer loginId){
        List<DateAttendance> results = dateAttendanceRepository.findAllAttendances(month, loginId);
        List<DateAttendanceForm> dateAttendances = setDateAttendanceForm(results);
        return dateAttendances;
    }

    public List<DateAttendanceForm> setDateAttendanceForm(List<DateAttendance> results){
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

    public void postNew(DateAttendanceForm reqAttendance, UserForm loginUser) throws ParseException {
        DateAttendance dateAttendance = setEntity(reqAttendance,loginUser);
        dateAttendanceRepository.save(dateAttendance);
    }

    public DateAttendance setEntity(DateAttendanceForm reqAttendance, UserForm loginUser) throws ParseException {
        DateAttendance dateAttendance = new DateAttendance();

        dateAttendance.setUser(loginUser);
        dateAttendance.setDate(reqAttendance.getDate());
        dateAttendance.setMonth(reqAttendance.getMonth());
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
}
