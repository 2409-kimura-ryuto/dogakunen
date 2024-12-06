package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.MonthAttendanceRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.MonthAttendance;
import com.example.dogakunen.repository.entity.Position;
import com.example.dogakunen.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MonthAttendanceService {

    @Autowired
    MonthAttendanceRepository monthAttendanceRepository;

    //勤怠マスタ(月)作成
    public void saveNewMonth(int newUserId) {

        int month;
        for(month = 1; month < 13; month++){
            //monthAttendance.setMonth(i);
            monthAttendanceRepository.saveNewMonth(newUserId, month);
        }
    }

}
