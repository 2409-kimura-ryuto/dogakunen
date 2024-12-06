package com.example.dogakunen.service;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.GeneralDateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.DateAttendanceRepository;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DateAttendanceService {

    @Autowired
    DateAttendanceRepository dateAttendanceRepository;

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

    //勤怠マスタ(日)作成
    public void saveNewDate(int newUserId) {

        dateAttendanceRepository.saveNewUser(newUserId);
    }


}
