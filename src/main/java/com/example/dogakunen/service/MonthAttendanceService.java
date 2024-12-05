package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.repository.MonthAttendanceRepository;
import com.example.dogakunen.repository.entity.MonthAttendance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonthAttendanceService {

    @Autowired
    MonthAttendanceRepository monthAttendanceRepository;

    /*
     * 勤怠状況ステータス変更
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
    public MonthAttendanceForm findByUserIdAndMonth(MonthAttendanceForm monthAttendanceForm) {
        MonthAttendance result = monthAttendanceRepository.findByUserIdAndMonth(monthAttendanceForm.getUserId(), monthAttendanceForm.getMonth());
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
}
