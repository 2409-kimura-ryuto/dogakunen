package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.DateAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AttendanceController {
    @Autowired
    DateAttendanceService dateAttendanceService;

    @GetMapping
    public String top(UserForm loginUser) {
        Integer loginId = loginUser.getId();
        dateAttendanceService.findALLAttendances(loginId);
        return "/home";
    }
}
