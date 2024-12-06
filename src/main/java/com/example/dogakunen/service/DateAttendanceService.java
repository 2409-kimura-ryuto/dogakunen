package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DateAttendanceService {

    @Autowired
    DateAttendanceRepository dateAttendanceRepository;

    //勤怠マスタ(日)作成
    public void saveNewDate(int newUserId) {

        dateAttendanceRepository.saveNewUser(newUserId);
    }


}
