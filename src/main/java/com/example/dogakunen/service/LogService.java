package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.LogForm;
import com.example.dogakunen.repository.LogRepository;
import com.example.dogakunen.repository.UserRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.Log;
import com.example.dogakunen.repository.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LogService {
    @Autowired
    LogRepository logRepository;

    @Autowired
    UserRepository userRepository;

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

    public void editLog(DateAttendanceForm preAttendance ,DateAttendanceForm reqAttendance, String employeeNumber) throws IllegalAccessException, ParseException {
        List<User> userList = userRepository.findByEmployeeNumber(employeeNumber);
        User loginUser = userList.get(0);
        List<Log> logList = new ArrayList<>();
        for(Field field : reqAttendance.getClass().getDeclaredFields()){
            field.setAccessible(true);
            if (field.get(reqAttendance) != null && !field.get(reqAttendance).equals(field.get(preAttendance))){
                Log logEntity = new Log();
                logEntity.setOperation("更新");
                logEntity.setUser(loginUser);
                logEntity.setDate(reqAttendance.getDate());
                //更新日時の設定
                Date nowDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = sdf.format(nowDate);
                logEntity.setCreatedDate(sdf.parse(currentTime));
                logEntity.setUpdatedDate(sdf.parse(currentTime));
                switch(field.getName()){
                    case "attendance":
                        logEntity.setField("勤務区分");
                        logEntity.setContent(
                                switch (reqAttendance.getAttendance()){
                                    case 1 -> "社内業務（オンサイト）";
                                    case 2 -> "社内業務（オフサイト）";
                                    case 3 -> "顧客業務（オンサイト）";
                                    case 4 -> "顧客業務（オフサイト）";
                                    case 5 -> "休日";
                                    default -> "未登録";
                                }
                        );
                        break;
                    case "workTimeStart":
                        logEntity.setField("開始時刻");
                        logEntity.setContent(reqAttendance.getWorkTimeStart().toString());
                        break;
                    case "workTimeFinish":
                        logEntity.setField("終了時刻");
                        logEntity.setContent(reqAttendance.getWorkTimeFinish().toString());
                        break;
                    case "breakTime":
                        logEntity.setField("休憩時間");
                        logEntity.setContent(reqAttendance.getBreakTime());
                        break;
                    case "memo":
                        logEntity.setField("メモ");
                        logEntity.setContent(reqAttendance.getMemo());
                        break;
                    default:
                        continue;
                }
                logList.add(logEntity);
            }
        }
        for(Log log : logList) {
            logRepository.save(log);
        }
    }


    public void newLog(DateAttendanceForm reqAttendance, String employeeNumber) throws ParseException {
        List<Log> logList = setLogList(reqAttendance, employeeNumber);
        for(Log log : logList) {
            logRepository.save(log);
        }
    }

    public List<Log> setLogList(DateAttendanceForm reqAttendance, String employeeNumber) throws ParseException {
        List<User> userList = userRepository.findByEmployeeNumber(employeeNumber);
        User loginUser = userList.get(0);
        List<Log> logList = new ArrayList<>();

        for (Field field : reqAttendance.getClass().getDeclaredFields()){
            Log logEntity = new Log();
            logEntity.setDate(reqAttendance.getDate());
            logEntity.setUser(loginUser);
            logEntity.setOperation("登録");
            //更新日時の設定
            Date nowDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = sdf.format(nowDate);
            logEntity.setCreatedDate(sdf.parse(currentTime));
            logEntity.setUpdatedDate(sdf.parse(currentTime));
            switch(field.getName()){
                case "attendance":
                    logEntity.setField("勤務区分");
                    logEntity.setContent(
                        switch (reqAttendance.getAttendance()){
                            case 1 -> "社内業務（オンサイト）";
                            case 2 -> "社内業務（オフサイト）";
                            case 3 -> "顧客業務（オンサイト）";
                            case 4 -> "顧客業務（オフサイト）";
                            case 5 -> "休日";
                            default -> "未登録";
                        }
                    );
                    break;
                case "workTimeStart":
                    logEntity.setField("開始時刻");
                    logEntity.setContent(reqAttendance.getWorkTimeStart().toString());
                    break;
                case "workTimeFinish":
                    logEntity.setField("終了時刻");
                    logEntity.setContent(reqAttendance.getWorkTimeFinish().toString());
                    break;
                case "breakTime":
                    logEntity.setField("休憩時間");
                    logEntity.setContent(reqAttendance.getBreakTime());
                    break;
                case "memo":
                    logEntity.setField("メモ");
                    logEntity.setContent(reqAttendance.getMemo());
                    break;
                default:
                    continue;
            }
            logList.add(logEntity);
        }
        return logList;
    }
}
