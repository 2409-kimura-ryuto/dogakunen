package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.LogForm;
import com.example.dogakunen.repository.LogRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LogService {
    @Autowired
    LogRepository logRepository;

    public List<LogForm> findAllLog(int loginUserId){
        List<Log> results = logRepository.findAllLogByUserId(loginUserId);
        List<LogForm> logs = setLog(results);
        return logs;
    }

    public List<LogForm> setLog(List<Log> results){
        List<LogForm> logForms = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            LogForm log = new LogForm();
            Log result = results.get(i);
            log.setId(result.getId());
            log.setUserId(result.getUser().getId());
            log.setOperation(result.getOperation());
            log.setDate(result.getDate());
            log.setField(result.getField());
            log.setContent(result.getContent());
            log.setUserName(result.getUser().getName());
            log.setEmployeeNumber(result.getUser().getEmployeeNumber());
            log.setUpdatedDate(result.getUpdatedDate());
            logForms.add(log);
        }
        return logForms;
    }
}
