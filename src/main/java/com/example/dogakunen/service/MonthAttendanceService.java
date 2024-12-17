package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.repository.MonthAttendanceRepository;
import com.example.dogakunen.repository.entity.MonthAttendance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.Position;
import com.example.dogakunen.repository.entity.User;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MonthAttendanceService {

    @Autowired
    MonthAttendanceRepository monthAttendanceRepository;

    /*
     * 勤怠状況ステータス変更(完了申請、承認、差し戻し時に使用）
     */
    public void changeStatus(MonthAttendanceForm MonthAttendanceform) {
        MonthAttendance saveMonthAttendance = setMonthAttendanceEntity(MonthAttendanceform);
        monthAttendanceRepository.save(saveMonthAttendance);
    }

    /*
     * リクエストから取得した情報をentityに設定
     */
    private MonthAttendance setMonthAttendanceEntity(MonthAttendanceForm MonthAttendanceform) {
        MonthAttendance monthAttendance = new MonthAttendance();
        BeanUtils.copyProperties(MonthAttendanceform, monthAttendance);
        return monthAttendance;
    }

    /*
     * 勤怠(月)から対象のカラムを取得
     */
    public MonthAttendanceForm findByUserIdAndMonth(Integer id, Integer year, Integer month) {
        MonthAttendance result = monthAttendanceRepository.findByUserIdAndMonth(id, year, month);

        //【追加①】resultがnullの時、nullを返す
        if (result == null) {
            return null;
        }

        MonthAttendanceForm monthAttendanceResult = setMonthAttendanceForm(result);
        return monthAttendanceResult;
    }

    /*
     * DBから取得したデータをFormに設定
     */
    private MonthAttendanceForm setMonthAttendanceForm(MonthAttendance result) {
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        BeanUtils.copyProperties(result, monthAttendanceForm);
        return  monthAttendanceForm;
    }

    //勤怠マスタ(月)作成
    public void saveNewMonth(int newUserId) {

        int month;
        for(month = 1; month < 13; month++){
            //monthAttendance.setMonth(i);
            monthAttendanceRepository.saveNewMonth(newUserId, month);
        }
    }

}