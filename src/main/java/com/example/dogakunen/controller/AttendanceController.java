package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.MonthAttendanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AttendanceController {

    @Autowired
    MonthAttendanceService monthAttendanceService;

    @Autowired
    HttpSession session;

    @GetMapping
    public ModelAndView top() {
        ModelAndView mav = new ModelAndView();

        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        //勤怠状況ステータスによって申請ボタンの表示を切り替えるために勤怠状況ステータスを取得
        int attendanceStatus = monthAttendanceService.findByUserIdAndMonth(loginUser.getId(), 12).getAttendanceStatus();
        mav.addObject("attendanceStatus", attendanceStatus);
        mav.setViewName("/home");
        return mav;
    }
}
