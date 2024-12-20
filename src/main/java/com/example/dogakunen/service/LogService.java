package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.DateAttendanceListForm;
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

@Service
public class LogService {
    @Autowired
    LogRepository logRepository;

    @Autowired
    UserRepository userRepository;

    //勤怠操作履歴を全て持ってくる
    public List<LogForm> findAllLog(int loginUserId){
        List<Log> results = logRepository.findAllLogByUserId(loginUserId);
        List<LogForm> logs = setLog(results);
        return logs;
    }

    //entityをformに詰める
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

    //勤怠編集時にログを登録
    public void editLog(DateAttendanceForm preAttendance ,DateAttendanceForm reqAttendance, String employeeNumber) throws IllegalAccessException, ParseException, NoSuchFieldException {
        //ログインユーザをとってくる
        List<User> userList = userRepository.findByEmployeeNumber(employeeNumber);
        User loginUser = userList.get(0);
        List<Log> logList = new ArrayList<>();
        //フィールドで回して変更した箇所のみ登録
        loop : for(Field field : reqAttendance.getClass().getDeclaredFields()){
            field.setAccessible(true);
            Field prefield = preAttendance.getClass().getDeclaredField(field.getName());
            prefield.setAccessible(true);
            if (field.get(reqAttendance) != null && field.getName().equals("attendance") && field.get(reqAttendance).equals(5)
                    && prefield.getName().equals("attendance") && prefield.get(preAttendance).equals(5)){
                break;
            }
            if (field.get(reqAttendance) != null && !field.get(reqAttendance).equals(field.get(preAttendance))){
                Log logEntity = new Log();
                logEntity.setOperation("更新");
                logEntity.setUser(loginUser);
                logEntity.setDate(reqAttendance.getDate());
                //更新日時の設定
                Date nowDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String currentTime = sdf.format(nowDate);
                logEntity.setCreatedDate(sdf.parse(currentTime));
                logEntity.setUpdatedDate(sdf.parse(currentTime));
                switch(field.getName()){
                    case "attendance":
                        logEntity.setField("勤務区分");
                        switch (reqAttendance.getAttendance()){
                            case 1 : logEntity.setContent("「" + "社内業務（オンサイト）" + "」");
                            break;
                            case 2 : logEntity.setContent("「" + "社内業務（オフサイト）" + "」");
                            break;
                            case 3 : logEntity.setContent("「" + "顧客業務（オンサイト）" + "」");
                            break;
                            case 4 : logEntity.setContent("「" + "顧客業務（オフサイト）" + "」");
                            break;
                            case 5 : logEntity.setContent("「" + "休日" + "」");
                                     logList.add(logEntity);
                                    if(!reqAttendance.getMemo().isBlank()) {
                                        Log logEntity2 = new Log();
                                        logEntity2.setDate(reqAttendance.getDate());
                                        logEntity2.setUser(loginUser);
                                        logEntity2.setOperation("更新");
                                        logEntity2.setCreatedDate(sdf.parse(currentTime));
                                        logEntity2.setUpdatedDate(sdf.parse(currentTime));
                                        logEntity2.setField("メモ");
                                        logEntity2.setContent("「" + reqAttendance.getMemo() + "」");
                                        logList.add(logEntity2);
                                    }
                                     break loop;
                            default : break;
                        }
                        break;
                    case "workTimeStart":
                        logEntity.setField("開始時刻");
                        logEntity.setContent("「" + reqAttendance.getWorkTimeStart().toString()+ "」");
                        break;
                    case "workTimeFinish":
                        logEntity.setField("終了時刻");
                        logEntity.setContent("「" + reqAttendance.getWorkTimeFinish().toString() + "」");
                        break;
                    case "breakTime":
                        logEntity.setField("休憩時間");
                        logEntity.setContent("「" + reqAttendance.getBreakTime() + "」");
                        break;
                    case "memo":
                        if(!reqAttendance.getMemo().isBlank()) {
                            logEntity.setField("メモ");
                            logEntity.setContent("「" + reqAttendance.getMemo() + "」");
                        }
                        break;
                    default:
                        continue;
                }
                if(logEntity.getField() != null && logEntity.getContent() != null){
                    logList.add(logEntity);
                }
            }
        }
        //1件ずつ登録
        for(Log log : logList) {
            logRepository.save(log);
        }
    }

    //新規登録勤怠のログを登録
    public void newLog(DateAttendanceForm reqAttendance, String employeeNumber) throws ParseException {
        //勤怠情報をログentityListにつめる
        List<Log> logList = setLogList(reqAttendance, employeeNumber);
        //1件ずつ登録
        for(Log log : logList) {
            logRepository.save(log);
        }
    }

    //ログをentityにつめる
    public List<Log> setLogList(DateAttendanceForm reqAttendance, String employeeNumber) throws ParseException {
        List<User> userList = userRepository.findByEmployeeNumber(employeeNumber);
        User loginUser = userList.get(0);
        List<Log> logList = new ArrayList<>();

        loop : for (Field field : reqAttendance.getClass().getDeclaredFields()){
            Log logEntity = new Log();
            logEntity.setDate(reqAttendance.getDate());
            logEntity.setUser(loginUser);
            logEntity.setOperation("登録");
            //更新日時の設定
            Date nowDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String currentTime = sdf.format(nowDate);
            logEntity.setCreatedDate(sdf.parse(currentTime));
            logEntity.setUpdatedDate(sdf.parse(currentTime));
            switch(field.getName()){
                case "attendance":
                    logEntity.setField("勤務区分");
                    switch (reqAttendance.getAttendance()){
                        case 1 : logEntity.setContent("「" + "社内業務（オンサイト）" + "」");
                        break;
                        case 2 : logEntity.setContent("「" + "社内業務（オフサイト）" + "」");
                        break;
                        case 3 : logEntity.setContent("「" + "顧客業務（オンサイト）" + "」");
                        break;
                        case 4 : logEntity.setContent("「" + "顧客業務（オフサイト）" + "」");
                        break;
                        case 5 : logEntity.setContent("「" + "休日" + "」");
                                 logList.add(logEntity);
                                if(!reqAttendance.getMemo().isBlank()) {
                                    Log logEntity2 = new Log();
                                    logEntity2.setDate(reqAttendance.getDate());
                                    logEntity2.setUser(loginUser);
                                    logEntity2.setOperation("登録");
                                    logEntity2.setCreatedDate(sdf.parse(currentTime));
                                    logEntity2.setUpdatedDate(sdf.parse(currentTime));
                                    logEntity2.setField("メモ");
                                    logEntity2.setContent("「" + reqAttendance.getMemo() + "」");
                                    logList.add(logEntity2);
                                }
                                 break loop;
                        default : break;
                    }
                    break;
                case "workTimeStart":
                    logEntity.setField("開始時刻");
                    logEntity.setContent("「" + reqAttendance.getWorkTimeStart().toString() + "」");
                    break;
                case "workTimeFinish":
                    logEntity.setField("終了時刻");
                    logEntity.setContent("「" + reqAttendance.getWorkTimeFinish().toString() + "」");
                    break;
                case "breakTime":
                    logEntity.setField("休憩時間");
                    logEntity.setContent("「" + reqAttendance.getBreakTime() + "」");
                    break;
                case "memo":
                    if(!reqAttendance.getMemo().isBlank()) {
                        logEntity.setField("メモ");
                        logEntity.setContent("「" + reqAttendance.getMemo() + "」");
                    }
                    break;
                default:
                    continue;
            }
            if(logEntity.getField() != null && logEntity.getContent() != null){
                logList.add(logEntity);
            }
        }
        return logList;
    }


    //新規登録勤怠のログを登録
    public void newAllLog(DateAttendanceListForm.Attendance reqAttendance, String employeeNumber) throws ParseException {
        //勤怠情報をログentityListにつめる
        List<Log> logList = setAllLogList(reqAttendance, employeeNumber);
        //1件ずつ登録
        for(Log log : logList) {
            logRepository.save(log);
        }
    }

    public List<Log> setAllLogList(DateAttendanceListForm.Attendance reqAttendance, String employeeNumber) throws ParseException {
        List<User> userList = userRepository.findByEmployeeNumber(employeeNumber);
        User loginUser = userList.get(0);
        List<Log> logList = new ArrayList<>();

        loop : for (Field field : reqAttendance.getClass().getDeclaredFields()){
            Log logEntity = new Log();
            logEntity.setDate(reqAttendance.getDate());
            logEntity.setUser(loginUser);
            logEntity.setOperation("登録");
            //更新日時の設定
            Date nowDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String currentTime = sdf.format(nowDate);
            logEntity.setCreatedDate(sdf.parse(currentTime));
            logEntity.setUpdatedDate(sdf.parse(currentTime));
            switch(field.getName()){
                case "attendance":
                    logEntity.setField("勤務区分");
                    switch (reqAttendance.getAttendance()){
                        case 1 : logEntity.setContent("「" + "社内業務（オンサイト）" + "」");
                            break;
                        case 2 : logEntity.setContent("「" + "社内業務（オフサイト）" + "」");
                            break;
                        case 3 : logEntity.setContent("「" + "顧客業務（オンサイト）" + "」");
                            break;
                        case 4 : logEntity.setContent("「" + "顧客業務（オフサイト）" + "」");
                            break;
                        case 5 : logEntity.setContent("「" + "休日" + "」");
                            logList.add(logEntity);
                            if(!reqAttendance.getMemo().isBlank()) {
                                Log logEntity2 = new Log();
                                logEntity2.setDate(reqAttendance.getDate());
                                logEntity2.setUser(loginUser);
                                logEntity2.setOperation("登録");
                                logEntity2.setCreatedDate(sdf.parse(currentTime));
                                logEntity2.setUpdatedDate(sdf.parse(currentTime));
                                logEntity2.setField("メモ");
                                logEntity2.setContent("「" + reqAttendance.getMemo() + "」");
                                logList.add(logEntity2);
                            }
                            break loop;
                        default : break;
                    }
                    break;
                case "workTimeStart":
                    logEntity.setField("開始時刻");
                    logEntity.setContent("「" + reqAttendance.getWorkTimeStart().toString() + "」");
                    break;
                case "workTimeFinish":
                    logEntity.setField("終了時刻");
                    logEntity.setContent("「" + reqAttendance.getWorkTimeFinish().toString() + "」");
                    break;
                case "breakTime":
                    logEntity.setField("休憩時間");
                    logEntity.setContent("「" + reqAttendance.getBreakTime() + "」");
                    break;
                case "memo":
                    if(!reqAttendance.getMemo().isBlank()) {
                        logEntity.setField("メモ");
                        logEntity.setContent("「" + reqAttendance.getMemo() + "」");
                    }
                    break;
                default:
                    continue;
            }
            if(logEntity.getField() != null && logEntity.getContent() != null){
                logList.add(logEntity);
            }
        }
        return logList;
    }

    public void editAllLog(DateAttendanceForm preAttendance , DateAttendanceListForm.Attendance reqAttendance, String employeeNumber) throws IllegalAccessException, ParseException, NoSuchFieldException {
        //ログインユーザをとってくる
        List<User> userList = userRepository.findByEmployeeNumber(employeeNumber);
        User loginUser = userList.get(0);
        List<Log> logList = new ArrayList<>();

        //フィールドで回して変更した箇所のみ登録
        loop : for(Field field : reqAttendance.getClass().getDeclaredFields()){
            field.setAccessible(true);
            Field prefield = preAttendance.getClass().getDeclaredField(field.getName());
            prefield.setAccessible(true);
            if (field.get(reqAttendance) != null && field.getName().equals("attendance") && field.get(reqAttendance).equals(5)
                    && prefield.getName().equals("attendance") && prefield.get(preAttendance).equals(5)){
                break;
            }
            if (field.get(reqAttendance) != null && !field.get(reqAttendance).equals(prefield.get(preAttendance))){
                Log logEntity = new Log();
                logEntity.setOperation("更新");
                logEntity.setUser(loginUser);
                logEntity.setDate(preAttendance.getDate());
                //更新日時の設定
                Date nowDate = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String currentTime = sdf.format(nowDate);
                logEntity.setCreatedDate(sdf.parse(currentTime));
                logEntity.setUpdatedDate(sdf.parse(currentTime));
                switch(field.getName()){
                    case "attendance":
                        logEntity.setField("勤務区分");
                        switch (reqAttendance.getAttendance()){
                            case 1 : logEntity.setContent("「" + "社内業務（オンサイト）" + "」");
                                break;
                            case 2 : logEntity.setContent("「" + "社内業務（オフサイト）" + "」");
                                break;
                            case 3 : logEntity.setContent("「" + "顧客業務（オンサイト）" + "」");
                                break;
                            case 4 : logEntity.setContent("「" + "顧客業務（オフサイト）" + "」");
                                break;
                            case 5 : logEntity.setContent("「" + "休日" + "」");
                                logList.add(logEntity);
                                if(!reqAttendance.getMemo().isBlank()) {
                                    Log logEntity2 = new Log();
                                    logEntity2.setDate(reqAttendance.getDate());
                                    logEntity2.setUser(loginUser);
                                    logEntity2.setOperation("更新");
                                    logEntity2.setCreatedDate(sdf.parse(currentTime));
                                    logEntity2.setUpdatedDate(sdf.parse(currentTime));
                                    logEntity2.setField("メモ");
                                    logEntity2.setContent("「" + reqAttendance.getMemo() + "」");
                                    logList.add(logEntity2);
                                }
                                break loop;
                            default : break;
                        }
                        break;
                    case "workTimeStart":
                        logEntity.setField("開始時刻");
                        logEntity.setContent("「" + reqAttendance.getWorkTimeStart().toString()+ "」");
                        break;
                    case "workTimeFinish":
                        logEntity.setField("終了時刻");
                        logEntity.setContent("「" + reqAttendance.getWorkTimeFinish().toString() + "」");
                        break;
                    case "breakTime":
                        logEntity.setField("休憩時間");
                        logEntity.setContent("「" + reqAttendance.getBreakTime() + "」");
                        break;
                    case "memo":
                        if(!reqAttendance.getMemo().isBlank()) {
                            logEntity.setField("メモ");
                            logEntity.setContent("「" + reqAttendance.getMemo() + "」");
                        }
                        break;
                    default:
                        continue;
                }
                if(logEntity.getField() != null && logEntity.getContent() != null){
                    logList.add(logEntity);
                }
            }
        }
        //1件ずつ登録
        for(Log log : logList) {
            logRepository.save(log);
        }
    }
}
