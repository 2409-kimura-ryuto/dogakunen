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
            dateAttendance.setBreakTime(result.getBreakTime());

            //労働時間を計算
            if(result.getWorkTimeStart() != null && result.getWorkTimeFinish() != null) {
                Duration duration = Duration.between(result.getWorkTimeStart(), result.getWorkTimeFinish());
                dateAttendance.setWorkTime(duration.toHoursPart() + "時間" + duration.toMinutesPart() + "分");
            }
            dateAttendance.setMemo(result.getMemo());
            dateAttendance.setUserName(result.getUser().getName());
            dateAttendance.setEmployeeNumber(result.getUser().getEmployeeNumber());

            dateAttendances.add(dateAttendance);
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
        DateAttendance dateAttendance = setEntity(reqAttendance, results.get(0));
        dateAttendanceRepository.save(dateAttendance);
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
    public void editAttendance(DateAttendanceForm reqAttendance, String employeeNumber) throws ParseException {
        //社員番号からユーザ情報を持ってくる
        List<User> results = userRepository.findByEmployeeNumber(employeeNumber);
        DateAttendance dateAttendance = setEntity(reqAttendance, results.get(0));
        dateAttendanceRepository.save(dateAttendance);
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

        dateAttendanceRepository.saveNewUser(newUserId);
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
