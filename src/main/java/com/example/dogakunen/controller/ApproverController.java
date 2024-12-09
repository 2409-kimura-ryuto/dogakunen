package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.*;
import com.example.dogakunen.service.DateAttendanceService;
import com.example.dogakunen.service.MonthAttendanceService;
import com.example.dogakunen.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ApproverController {

    @Autowired
    UserService userService;

    @Autowired
    DateAttendanceService dateAttendanceService;

    @Autowired
    MonthAttendanceService monthAttendanceService;

    @Autowired
    HttpSession session;

    /*
     * 承認対象者一覧画面
     */
    @GetMapping("/show_users")
    public ModelAndView showUsers() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/show_users");
        //承認対象者情報取得
        List<GeneralUserForm> generalUsers = userService.findAllGeneralUser(12);
        mav.addObject("users", generalUsers);
        return mav;
    }

    /*
     * 完了申請処理
     */
    @PutMapping("/request")
    public ModelAndView request() {
        ModelAndView mav = new ModelAndView();
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        int loginUserId = loginUser.getId();
        //更新したいカラムのIdを取得してmonthAttendanceFormにセット
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(loginUserId, 12).getId());
        //勤怠状況ステータスを1:申請中にセット
        monthAttendanceForm.setAttendanceStatus(1);
        //userIdとmonthもセットしないと0で更新されてしまう
        monthAttendanceForm.setUserId(loginUserId);
        monthAttendanceForm.setMonth(12);
        monthAttendanceService.changeStatus(monthAttendanceForm);
        //ホーム画面にリダイレクト
        mav.setViewName("redirect:/");
        return mav;
    }

    /*
     * 勤怠状況確認画面
     */
    @GetMapping("/check_attendance/{id}")
    public ModelAndView checkAttendance(@PathVariable Integer id) {
        ModelAndView mav = new ModelAndView();
        //勤怠情報取得
        List<GeneralDateAttendanceForm> generalDateAttendanceForms = dateAttendanceService.findGeneralDateAttendance(id, 12);
        //ユーザ毎に月の勤怠状況ステータスを取得
        MonthAttendanceForm monthAttendanceForm = monthAttendanceService.findByUserIdAndMonth(id, 12);
        mav.addObject("generalDateAttendances", generalDateAttendanceForms);
        mav.addObject("monthAttendanceForm", monthAttendanceForm);
        mav.setViewName("/check_attendance");
        return mav;
    }

    /*
     * 承認処理
     */
    @PutMapping("/approve/{id}")
    public ModelAndView approve(@PathVariable Integer id) {
        ModelAndView mav = new ModelAndView();
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        //勤怠マスタから対象者の12月のレコードのidを取得し、monthAttendanceFormnにセット
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(id, 12).getId());
        //2:承認済みをセット
        monthAttendanceForm.setAttendanceStatus(2);
        //userIdとmonthもセットしないと0で更新されてしまう
        monthAttendanceForm.setUserId(id);
        monthAttendanceForm.setMonth(12);
        //勤怠記録ステータスを2:承認済みに更新
        monthAttendanceService.changeStatus(monthAttendanceForm);
        //承認対象者一覧画面にリダイレクト
        mav.setViewName("redirect:/show_users");
        return mav;
    }

    /*
     * 差し戻し処理
     */
    @PutMapping("/sendBack/{id}")
    public ModelAndView sendBack(@PathVariable Integer id) {
        ModelAndView mav = new ModelAndView();
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        //勤怠マスタから対象者の12月のレコードのidを取得し、monthAttendanceFormnにセット
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(id, 12).getId());
        //0:申請前をセット
        monthAttendanceForm.setAttendanceStatus(0);
        //userIdとmonthもセットしないと0で更新されてしまう
        monthAttendanceForm.setUserId(id);
        monthAttendanceForm.setMonth(12);
        //勤怠記録ステータスを0:申請前に更新
        monthAttendanceService.changeStatus(monthAttendanceForm);
        //承認対象者一覧画面にリダイレクト
        mav.setViewName("redirect:/show_users");
        return mav;
    }

}
