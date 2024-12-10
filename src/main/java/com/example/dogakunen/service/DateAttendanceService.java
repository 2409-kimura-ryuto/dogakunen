package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.GeneralDateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
import com.example.dogakunen.repository.UserRepository;
import com.example.dogakunen.repository.GeneralDateAttendanceRepository;
import com.example.dogakunen.repository.UserRepository;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.GeneralDateAttendance;
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


import java.time.LocalDate;
import java.util.*;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.Duration.ZERO;

@Service
public class DateAttendanceService {
    @Autowired
    DateAttendanceRepository dateAttendanceRepository;
    @Autowired
    UserRepository userRepository;
    /*
     *　勤怠情報取得処理
     */
    public List<DateAttendanceForm> findALLAttendances(int month, Integer loginId) {
        //データ取得処理
        List<DateAttendance> results = dateAttendanceRepository.findAllAttendances(month, loginId);
        //フォームに詰め替え
        List<DateAttendanceForm> dateAttendances = setDateAttendanceForm(results);
        return dateAttendances;
    }

    /*
     * entityからformに詰め替え
     */
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
            dateAttendance.setWorkTime(result.getWorkTime());
            dateAttendance.setBreakTime(result.getBreakTime());
            dateAttendance.setMemo(result.getMemo());
            dateAttendance.setUserName(result.getUser().getName());
            dateAttendance.setEmployeeNumber(result.getUser().getEmployeeNumber());

            dateAttendances.add(dateAttendance);
            /*
            //休憩時間のフォーマット変換
            //休憩時間を取得
            String breakTime = result.getBreakTime();
            //休憩時間をduration型に変換
            String[] parts = breakTime.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            Duration breakDuration = Duration.ofHours(hours).plusMinutes(minutes);
            //休憩時間を「00:00」のフォーマットに変換
            String formattedBreakTime = String.format("%d:%02d", breakDuration.toHoursPart(), breakDuration.toMinutesPart());
            //休憩時間をセット
            dateAttendance.setBreakTime(formattedBreakTime);

            //労働時間を計算
            if(result.getWorkTimeStart() != null && result.getWorkTimeFinish() != null) {
                //労働開始時間と労働終了時間から労働時間を算出
                Duration duration = Duration.between(result.getWorkTimeStart(), result.getWorkTimeFinish());
                //労働時間から休憩時間を引いて純労働時間を算出
                Duration workDuration = duration.minus(breakDuration);
                //フォーマットを「00:00」に変換
                String formattedTime = String.format("%d:%02d", workDuration.toHoursPart(), workDuration.toMinutesPart());
                dateAttendance.setWorkTime(formattedTime);
             */
        }
        return dateAttendances;
    }

    /*
     *　新規勤怠登録処理
     */
    public void postNew(DateAttendanceForm reqAttendance, String employeeNumber, Integer month) throws ParseException {
        //社員番号からユーザ情報を持ってくる
        List<User> results = userRepository.findByEmployeeNumber(employeeNumber);
        List<DateAttendance> findResults = dateAttendanceRepository.findByUserAndDate(results.get(0), reqAttendance.getDate());
        reqAttendance.setId(findResults.get(0).getId());
        reqAttendance.setMonth(month);

        //労働時間を計算
        //休憩時間を取得
        String breakTime1 = reqAttendance.getBreakTime();
        //休憩時間をduration型に変換
        String[] parts = breakTime1.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = 0;
        Duration breakDuration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        //労働開始時間と労働終了時間から労働時間を算出
        Duration duration = Duration.between(reqAttendance.getWorkTimeStart(), reqAttendance.getWorkTimeFinish());
        //労働時間から休憩時間を引いて純労働時間を算出
        Duration workDuration = duration.minus(breakDuration);
        //フォーマットを「00:00」に変換
        String formattedWorkTime = String.format("%02d:%02d:%02d", workDuration.toHoursPart(), workDuration.toMinutesPart(), duration.toSecondsPart());
        //フォーマットを

        //算出した労働時間をセット
        DateAttendance dateAttendance = setEntity(reqAttendance, results.get(0));

        //entityから取り出した要素を引数にリポジトリを呼び出す
        Integer id = dateAttendance.getId();
        Integer attendance = dateAttendance.getAttendance();
        LocalTime workTimeStart = dateAttendance.getWorkTimeStart();
        LocalTime workTimeFinish = dateAttendance.getWorkTimeFinish();
        String breakTime = dateAttendance.getBreakTime() + ":00";
        String memo = dateAttendance.getMemo();

        dateAttendanceRepository.addAttendance(id, attendance, workTimeStart, workTimeFinish, breakTime, formattedWorkTime, memo);
    }
    /*
     * 編集する勤怠情報を持ってくる
     */
    public DateAttendanceForm findDateAttendanceById(Integer id){
        List<DateAttendance> results = new ArrayList<>();
        results.add(dateAttendanceRepository.findById(id).orElse(null));
        List<DateAttendanceForm> dateAttendances = setDateAttendanceForm(results);
        return dateAttendances.get(0);
    }
    /*
     * 勤怠編集処理
     */
    public void updateAttendance(DateAttendanceForm reqAttendance, String employeeNumber, Integer month) throws ParseException {
        //社員番号からユーザ情報を持ってくる
        List<User> results = userRepository.findByEmployeeNumber(employeeNumber);
        reqAttendance.setMonth(month);

        //労働時間を計算
        //休憩時間を取得
        String breakTime1 = reqAttendance.getBreakTime();
        //休憩時間をduration型に変換
        String[] parts = breakTime1.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = 0;
        Duration breakDuration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        //労働開始時間と労働終了時間から労働時間を算出
        Duration duration = Duration.between(reqAttendance.getWorkTimeStart(), reqAttendance.getWorkTimeFinish());
        //労働時間から休憩時間を引いて純労働時間を算出
        Duration workDuration = duration.minus(breakDuration);
        //フォーマットを「00:00」に変換
        String formattedWorkTime = String.format("%02d:%02d:%02d", workDuration.toHoursPart(), workDuration.toMinutesPart(), duration.toSecondsPart());
        reqAttendance.setWorkTime(formattedWorkTime);
        //算出した労働時間をセット
        DateAttendance dateAttendance = setEntity(reqAttendance, results.get(0));

        //entityから取り出した要素を引数にリポジトリを呼び出す
        Integer id = dateAttendance.getId();
        Integer attendance = dateAttendance.getAttendance();
        LocalTime workTimeStart = dateAttendance.getWorkTimeStart();
        LocalTime workTimeFinish = dateAttendance.getWorkTimeFinish();
        String breakTime = dateAttendance.getBreakTime();
        String memo = dateAttendance.getMemo();

        dateAttendanceRepository.addAttendance(id, attendance, workTimeStart, workTimeFinish, breakTime, formattedWorkTime, memo);
    }


    /*
     * formからentityに詰め替え
     */
    public DateAttendance setEntity(DateAttendanceForm reqAttendance, User loginUser) throws ParseException {
        DateAttendance dateAttendance = new DateAttendance();

        dateAttendance.setUser(loginUser);
        dateAttendance.setMonth(reqAttendance.getMonth());
        dateAttendance.setId(reqAttendance.getId());
        dateAttendance.setDate(reqAttendance.getDate());
        dateAttendance.setBreakTime(reqAttendance.getBreakTime());
        dateAttendance.setWorkTimeStart(reqAttendance.getWorkTimeStart());
        dateAttendance.setWorkTimeFinish(reqAttendance.getWorkTimeFinish());
        dateAttendance.setAttendance(reqAttendance.getAttendance());
        dateAttendance.setMemo(reqAttendance.getMemo());

        //現在時刻の登録
        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = sdf.format(nowDate);
        dateAttendance.setCreatedDate(sdf.parse(currentTime));
        dateAttendance.setUpdatedDate(sdf.parse(currentTime));


        return dateAttendance;
    }

    @Autowired
    GeneralDateAttendanceRepository generalDateAttendanceRepository;

    /*
     * 社員の勤怠情報取得
     */
    public List<GeneralDateAttendanceForm> findGeneralDateAttendance(Integer id, Integer month) {
        List<GeneralDateAttendance> results = generalDateAttendanceRepository.findDateAttendanceByOrderByDate(id, month);
        List<GeneralDateAttendanceForm> generalDateAttendanceForm = setGeneralDateAttendanceForm(results);
        return generalDateAttendanceForm;
    }

    /*
     * DBから取得したDateAttendanceをFormに変換
     */
    private List<GeneralDateAttendanceForm> setGeneralDateAttendanceForm(List<GeneralDateAttendance> results) {
        List<GeneralDateAttendanceForm> generalDateAttendances = new ArrayList<>();

        for (GeneralDateAttendance result : results) {
            GeneralDateAttendanceForm generalDateAttendanceForm = new GeneralDateAttendanceForm();
            BeanUtils.copyProperties(result, generalDateAttendanceForm);
            generalDateAttendances.add(generalDateAttendanceForm);
        }
        return generalDateAttendances;
    }

    /*
     * 勤怠マスタ(日)作成
     */
    public void saveNewDate(int newUserId) {

        Calendar calender = Calendar.getInstance();
        int year = calender.get(Calendar.YEAR);
        int month = calender.get(Calendar.MONTH) + 1;
        String startDate = year + "-" + month + "-01 00:00:00";

        dateAttendanceRepository.saveNewUser(newUserId, startDate);
        dateAttendanceRepository.saveMonth();
    }


    /*
     * 勤怠削除
     */
    public void deleteAttendance(Integer id) {
        //break_timeとwork_time用の0を用意
        String zero = "0 hours 0 minutes 0 seconds";
        //勤怠記録のIDと用意した0を引数にリポジトリを呼び出す
        dateAttendanceRepository.updateAttendance(id, zero);
    }
}
