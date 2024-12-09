package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
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
    DateAttendanceService dateAttendanceService;

    /*
     * ホーム画面表示
     */
    @GetMapping
    public String top() {
        return "/home";
    }

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
