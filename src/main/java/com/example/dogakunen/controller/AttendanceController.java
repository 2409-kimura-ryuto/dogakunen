package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.DateAttendanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import java.util.Calendar;
import java.util.List;

@Controller
public class AttendanceController {
    @Autowired
    HttpSession session;
    @Autowired
    DateAttendanceService dateAttendanceService;

    @GetMapping
    public ModelAndView top() {
        ModelAndView mav = new ModelAndView();
        Calendar calender = Calendar.getInstance();
        int month = calender.get(Calendar.MONTH) + 1;
        UserForm loginUser =(UserForm) session.getAttribute("loginUser");
        Integer loginId = loginUser.getId();
        List<DateAttendanceForm> dateAttendances = dateAttendanceService.findALLAttendances(month, loginId);
        mav.addObject("attendances",dateAttendances);
        mav.setViewName("/home");
        return mav;
    }
}
