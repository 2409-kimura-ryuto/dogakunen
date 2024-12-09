package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.entity.User;
import com.example.dogakunen.service.DateAttendanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.MonthAttendanceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

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

        //勤怠月取得
        MonthAttendanceForm monthAttendanceForm = monthAttendanceService.findByUserIdAndMonth(loginId, month);
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
        mav.addObject("monthAttendance", monthAttendanceForm);
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
    public ModelAndView postNewAttendance(@ModelAttribute(name = "formModel") @Validated DateAttendanceForm reqAttendance,
                                          BindingResult result) throws ParseException {
        //ログインユーザ情報から社員番号取得
        UserForm loginUser = (UserForm)session.getAttribute("loginUser");
        String employeeNumber = loginUser.getEmployeeNumber();

        //アクセスした日付を取得
        Calendar calender = Calendar.getInstance();
        Integer month = calender.get(Calendar.MONTH) + 1;

        //バリデーション
        List<String> errorMessages = new ArrayList<>();
        LocalTime startTime = reqAttendance.getWorkTimeStart();
        LocalTime finishTime = reqAttendance.getWorkTimeFinish();
        int attendanceNumber = reqAttendance.getAttendance();
        if (Objects.isNull(startTime) && attendanceNumber != 5){
            errorMessages.add("開始時刻を入力してください");
        }
        if (Objects.isNull(finishTime) && attendanceNumber != 5){
            errorMessages.add("終了時刻を入力してください");
        }
        if (attendanceNumber == 0){
            errorMessages.add("勤怠区分を登録してください");
        }
        if (attendanceNumber == 5 && (Objects.nonNull(startTime) || Objects.nonNull(finishTime))){
            errorMessages.add("無効な入力です");
        }
        if(result.hasErrors()) {
            //エラーがあったら、エラーメッセージを格納する
            //エラーメッセージの取得
            for (FieldError error : result.getFieldErrors()) {
                String message = error.getDefaultMessage();
                //取得したエラーメッセージをエラーメッセージのリストに格納
                errorMessages.add(message);
            }
        }
        if (!errorMessages.isEmpty()){
            ModelAndView mav = new ModelAndView();
            mav.addObject("formModel", reqAttendance);
            mav.addObject("errorMessages", errorMessages);
            mav.setViewName("/new_attendance");
            return mav;
        }

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

    /*
     * 勤怠編集処理
     */
    @PostMapping("/editAttendance")
    public ModelAndView postEditAttendance(@ModelAttribute(name = "formModel") @Validated DateAttendanceForm reqAttendance,
                                           BindingResult result, @RequestParam(name = "id") Integer id,
                                           @RequestParam(name = "month") Integer month) throws ParseException {
        //バリデーション
        List<String> errorMessages = new ArrayList<>();
        LocalTime startTime = reqAttendance.getWorkTimeStart();
        LocalTime finishTime = reqAttendance.getWorkTimeFinish();
        int attendanceNumber = reqAttendance.getAttendance();
        if (Objects.isNull(startTime) && attendanceNumber != 5){
            errorMessages.add("開始時刻を入力してください");
        }
        if (Objects.isNull(finishTime) && attendanceNumber != 5){
            errorMessages.add("終了時刻を入力してください");
        }
        if (attendanceNumber == 0){
            errorMessages.add("勤怠区分を登録してください");
        }
        if (attendanceNumber == 5 && (Objects.nonNull(startTime) || Objects.nonNull(finishTime))){
            errorMessages.add("無効な入力です");
        }
        if(result.hasErrors()) {
            //エラーがあったら、エラーメッセージを格納する
            //エラーメッセージの取得
            for (FieldError error : result.getFieldErrors()) {
                String message = error.getDefaultMessage();
                //取得したエラーメッセージをエラーメッセージのリストに格納
                errorMessages.add(message);
            }
        }
        if (!errorMessages.isEmpty()){
            ModelAndView mav = new ModelAndView();
            mav.addObject("formModel", reqAttendance);
            mav.addObject("errorMessages", errorMessages);
            mav.setViewName("/edit_attendance");
            return mav;
        }

        //ログインユーザ情報から社員番号取得
        UserForm loginUser = (UserForm)session.getAttribute("loginUser");
        String employeeNumber = loginUser.getEmployeeNumber();
        reqAttendance.setId(id);
        dateAttendanceService.updateAttendance(reqAttendance, employeeNumber,month);
        return new ModelAndView("redirect:/");
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
