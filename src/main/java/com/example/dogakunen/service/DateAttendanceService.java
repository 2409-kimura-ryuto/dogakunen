package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import org.antlr.v4.runtime.misc.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
}
