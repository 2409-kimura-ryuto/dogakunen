package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.entity.User;
import com.example.dogakunen.service.DateAttendanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.MonthAttendanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.Calendar;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.service.DateAttendanceService;
import com.example.dogakunen.service.UserService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AttendanceController {
    @Autowired
    HttpSession session;
    @Autowired
    DateAttendanceService dateAttendanceService;
    @Autowired
    MonthAttendanceService monthAttendanceService;

    /*
     *　勤怠情報取得処理
     */
    @GetMapping
    public ModelAndView top() {
        ModelAndView mav = new ModelAndView();

        //アクセスした日付の取得（実際の月の値を出すときは1を加算する必要がある）
        Calendar calender = Calendar.getInstance();
        int month = calender.get(Calendar.MONTH) + 1;

        //ログインユーザ情報取得
        UserForm loginUser =(UserForm) session.getAttribute("loginUser");
        Integer loginId = loginUser.getId();

        //勤怠記録の取得
        List<DateAttendanceForm> dateAttendances = dateAttendanceService.findALLAttendances(month, loginId);

        //勤怠状況ステータスによって申請ボタンの表示を切り替えるために勤怠状況ステータスを取得
        int attendanceStatus = monthAttendanceService.findByUserIdAndMonth(loginUser.getId(), 12).getAttendanceStatus();

        //承認者orシステム管理者フィルターのエラーメッセージをmavに詰めてセッション削除
        List<String> filterErrorMessages = (List<String>) session.getAttribute("filterErrorMessages");
        mav.addObject("filterErrorMessages", filterErrorMessages);
        session.removeAttribute("filterErrorMessages");

        //情報をセット
        mav.addObject("attendances",dateAttendances);
        mav.addObject("loginUser", loginUser);
        mav.addObject("attendanceStatus", attendanceStatus);
        mav.setViewName("/home");
        return mav;
    }

    /*
     * 新規勤怠登録画面表示
     */
    @GetMapping("/newAttendance")
    public ModelAndView getNewAttendance(){
        ModelAndView mav = new ModelAndView();

        //空のformModelを入れる
        DateAttendanceForm dateAttendance = new DateAttendanceForm();
        mav.addObject("formModel", dateAttendance);
        mav.setViewName("/new_attendance");
        return mav;
    }

    /*
     * 新規勤怠登録処理
     */
    @PostMapping("/newAttendance")
    public ModelAndView postNewAttendance(@ModelAttribute(name = "formModel") DateAttendanceForm reqAttendance) throws ParseException {
        //ログインユーザ情報から社員番号取得
        UserForm loginUser = (UserForm)session.getAttribute("loginUser");
        String employeeNumber = loginUser.getEmployeeNumber();

        //アクセスした日付を取得
        Calendar calender = Calendar.getInstance();
        Integer month = calender.get(Calendar.MONTH) + 1;

        //勤怠登録処理
        dateAttendanceService.postNew(reqAttendance, employeeNumber, month);
        return new ModelAndView("redirect:/");
    }

    //編集画面表示
    @GetMapping("/editAttendance/{id}")
    public ModelAndView getEditAttendance(@PathVariable Integer id){
        ModelAndView mav = new ModelAndView();
        //IDから勤怠情報取得
        DateAttendanceForm dateAttendanceForm = dateAttendanceService.findDateAttendanceById(id);
        mav.addObject("formModel", dateAttendanceForm);
        mav.setViewName("/edit_attendance");
        return mav;
    }

    /* 勤怠編集処理
    @PostMapping("/editAttendance")
    public ModelAndView postEditAttendance(@ModelAttribute(name = "formModel") DateAttendanceForm reqAttendance){
        dateAttendanceService.editAttendance(reqAttendance);
        return new ModelAndView("redirect:/");
    }
     */

    /*
     * 勤怠削除処理
     */
    @PutMapping("/deleteAttendance{id}")
    public ModelAndView deleteAttendance(@PathVariable Integer id) {
        //リクエストから取得したIDを引数にサービスを呼び出す
        dateAttendanceService.deleteAttendance(id);
        // ホーム画面にリダイレクト
        return new ModelAndView("redirect:/");
    }
}
