package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.*;
import com.example.dogakunen.service.DateAttendanceService;
import com.example.dogakunen.service.MonthAttendanceService;
import com.example.dogakunen.service.UserService;
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

    /*
     * 承認対象者一覧画面
     */
    @GetMapping("/show_users")
    public ModelAndView showUsers() {
        ModelAndView mav = new ModelAndView();
//        // form用の空のentityを準備
//        UserForm userForm = new UserForm();
        mav.setViewName("/show_users");
//        // 準備した空のFormを保管
//        mav.addObject("userForm", userForm);
//        //ログインフィルターのエラーメッセージをmavに詰めてセッション削除
//        List<String> errorMessages = (List<String>) session.getAttribute("errorMessagesLoginFilter");
//        mav.addObject("errorMessagesLoginFilter", errorMessages);
//        session.removeAttribute("errorMessagesLoginFilter");
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
        //ログインユーザIDを取得
//        UserForm userForm = (UserForm) session.getAttribute("user");
        //取得したログインユーザIDをMonthAttendansFormのuserIdとにセットし
        //対象月もセットしてserviceのsaveメソッドで更新処理
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        monthAttendanceForm.setUserId(1);
        monthAttendanceForm.setMonth(12);
        //更新したいカラムのIdを取得してmonthAttendanceFormにセット
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(monthAttendanceForm).getId());
        monthAttendanceForm.setAttendanceStatus(1);
        monthAttendanceService.changeStatus(monthAttendanceForm);
        //ホーム画面にリダイレクトするように変更
        mav.setViewName("redirect:/show_users");
        return mav;
    }

    /*
     * 勤怠状況確認画面
     */
    @GetMapping("/check_attendance/{id}")
    public ModelAndView checkAttendance(@PathVariable Integer id) {
        ModelAndView mav = new ModelAndView();
        List<GeneralDateAttendanceForm> generalDateAttendanceForms = dateAttendanceService.findGeneralDateAttendance(id, 12);
        mav.addObject("generalDateAttendances", generalDateAttendanceForms);
        mav.setViewName("/check_attendance");
        return mav;
    }

}
