package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.LogForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.LogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class LogController {
    @Autowired
    HttpSession session;

    @Autowired
    LogService logService;

    @GetMapping("/attendanceLog")
    public ModelAndView getLog(){
        ModelAndView mav = new ModelAndView();
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        List<LogForm> logForms = logService.findAllLog(loginUser.getId());
        mav.addObject("logForms", logForms);
        mav.setViewName("/attendance_log");
        return mav;
    }
}
