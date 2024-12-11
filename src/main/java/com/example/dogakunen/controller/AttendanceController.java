package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.entity.User;
import com.example.dogakunen.service.DateAttendanceService;
import io.micrometer.common.util.StringUtils;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.time.LocalTime;
import java.util.*;

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
        dateAttendance.setBreakTime("00:00");
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
        Date date = reqAttendance.getDate();
        LocalTime startTime = reqAttendance.getWorkTimeStart();
        LocalTime finishTime = reqAttendance.getWorkTimeFinish();
        String breakTime = reqAttendance.getBreakTime();
        int attendanceNumber = reqAttendance.getAttendance();
        if(date == null){
            errorMessages.add("・日付を入力してください");
        }
        if (Objects.isNull(startTime) && attendanceNumber != 5){
            errorMessages.add("・開始時刻を入力してください");
        }
        if (Objects.isNull(finishTime) && attendanceNumber != 5){
            errorMessages.add("・終了時刻を入力してください");
        }
        if (attendanceNumber == 0){
            errorMessages.add("・勤怠区分を登録してください");
        }
        if (attendanceNumber == 5 && (Objects.nonNull(startTime) || Objects.nonNull(finishTime) || !breakTime.equals("00:00"))){
            errorMessages.add("・無効な入力です");
        }
        if (Objects.nonNull(startTime) && Objects.nonNull(finishTime) && !startTime.isBefore(finishTime)){
            errorMessages.add("・無効な入力です");
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
        //勤務区分が休日の場合
        if (reqAttendance.getAttendance() == 5){
            reqAttendance.setWorkTimeStart(LocalTime.parse("00:00"));
            reqAttendance.setWorkTimeFinish(LocalTime.parse("00:00"));
        }
        //勤怠登録処理
        dateAttendanceService.postNew(reqAttendance, employeeNumber, month);

        return new ModelAndView("redirect:/");
    }

    //編集画面表示
    @GetMapping("/editAttendance/{id}")
    public ModelAndView getEditAttendance(@PathVariable String id, RedirectAttributes redirectAttributes){
        ModelAndView mav = new ModelAndView();

        //idの正規表現チェック
        List<String> errorMessages = new ArrayList<String>();
        if ((id == null) || (!id.matches("^[0-9]+$"))) {
            errorMessages.add("・不正なパラメータが入力されました");
        }

        //勤怠状況が存在しない勤怠(日)のidが入力された際のバリデーション
        if (id.matches("^[0-9]+$")) {
            try {
                dateAttendanceService.findDateAttendanceById(Integer.parseInt(id));
            } catch (RuntimeException e) {
                errorMessages.add("・不正なパラメータが入力されました");
            }
        }

        //エラーメッセージが存在する場合はエラーメッセージをセットし、ホーム画面にリダイレクト
        if (errorMessages.size() > 0) {
            redirectAttributes.addFlashAttribute("parameterErrorMessages", errorMessages);
            mav.setViewName("redirect:/");
            return mav;
        }

        //IDから勤怠情報取得
        DateAttendanceForm dateAttendanceForm = dateAttendanceService.findDateAttendanceById(Integer.parseInt(id));
        mav.addObject("formModel", dateAttendanceForm);
        mav.setViewName("/edit_attendance");
        return mav;
    }

    /*
     *　IDが空で渡ってきた場合
     */
    @GetMapping("/editAttendance/")
    public ModelAndView returnEditAttendance(RedirectAttributes redirectAttributes){
        ModelAndView mav = new ModelAndView();
        //バリデーション
        List<String> errorMessages = new ArrayList<String>();
        errorMessages.add("・不正なパラメータが入力されました");
        redirectAttributes.addFlashAttribute("parameterErrorMessages", errorMessages);
        mav.setViewName("redirect:/");
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
        String breakTime = reqAttendance.getBreakTime();
        int attendanceNumber = reqAttendance.getAttendance();
        if (Objects.isNull(startTime) && attendanceNumber != 5){
            errorMessages.add("・開始時刻を入力してください");
        }
        if (Objects.isNull(finishTime) && attendanceNumber != 5){
            errorMessages.add("・終了時刻を入力してください");
        }
        if (attendanceNumber == 0){
            errorMessages.add("・勤怠区分を登録してください");
        }
        if (attendanceNumber == 5 && (!startTime.equals(LocalTime.parse("00:00")) || !finishTime.equals(LocalTime.parse("00:00")) 
                || (!breakTime.equals("00:00:00") && !breakTime.equals("00:00")))){
            errorMessages.add("・無効な入力です");
        }
        if (attendanceNumber != 5 && Objects.nonNull(startTime) && Objects.nonNull(finishTime) && !startTime.isBefore(finishTime)){
            errorMessages.add("・無効な入力です");
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

        //勤務区分が休日の場合
        if (reqAttendance.getAttendance() == 5){
            reqAttendance.setWorkTimeStart(LocalTime.parse("00:00"));
            reqAttendance.setWorkTimeFinish(LocalTime.parse("00:00"));
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