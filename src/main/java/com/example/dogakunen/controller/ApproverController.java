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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Calendar;
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
    public ModelAndView request(RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        List<String> requestErrorMessages = new ArrayList<String>();
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        int loginUserId = loginUser.getId();

        //アクセスした日付の取得（実際の月の値を出すときは1を加算する必要がある）
        Calendar calender = Calendar.getInstance();
        int month = calender.get(Calendar.MONTH) + 1;

        //未登録があった場合のバリデーション
        //勤怠情報を取得し、勤務区分を１つ１つ確認。0:未登録があったらエラーメッセージを追加して繰り返し処理を終える
        List<GeneralDateAttendanceForm> generalDateAttendanceForms = dateAttendanceService.findGeneralDateAttendance(loginUserId, 12);
        for (GeneralDateAttendanceForm generalDateAttendanceForm : generalDateAttendanceForms) {
            if(generalDateAttendanceForm.getAttendance() == 0) {
                requestErrorMessages.add("・全ての勤怠を登録してから申請してください");
                break;
            }
        }

        //月の労働時間が200時間を超えた時のバリデーション
        //勤怠記録の取得
        List<DateAttendanceForm> dateAttendances = dateAttendanceService.findALLAttendances(month, loginUserId);
        //月の総労働時間計算
        String totalWorkTime = dateAttendanceService.sumTotalWorkTime(dateAttendances);
        long totalSeconds = 0;

        String[] timeParts = totalWorkTime.split(":");
        long hours = Long.parseLong(timeParts[0]);
        long minutes = Long.parseLong(timeParts[1]);

        //時間を秒単位に変換
        totalSeconds += hours * 3600 + minutes * 60;

        //720000秒=200時間
        if (totalSeconds > 720000) {
            requestErrorMessages.add("・月の総労働時間は200時間を超えないようにしてください");
        }

        //エラーメッセージが存在する場合はエラーメッセージをセットし、ホーム画面にリダイレクト
        if (requestErrorMessages.size() > 0) {
            redirectAttributes.addFlashAttribute("requestErrorMessages", requestErrorMessages);
            mav.setViewName("redirect:/");
            return mav;
        }

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
    public ModelAndView checkAttendance(@PathVariable String id, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        //idの正規表現チェック
        List<String> errorMessages = new ArrayList<String>();
        if ((id == null) || (!id.matches("^[0-9]+$"))) {
            errorMessages.add("・不正なパラメータが入力されました");
        }

        //勤怠状況が存在しないユーザのidが入力された際のバリデーション
        if (id.matches("^[0-9]+$")) {
            try {
                monthAttendanceService.findByUserIdAndMonth(Integer.parseInt(id), 12).getId();
            } catch (RuntimeException e) {
                errorMessages.add("・不正なパラメータが入力されました");
            }
        }

        //エラーメッセージが存在する場合はエラーメッセージをセットし、承認対象者一覧画面にリダイレクト
        if (errorMessages.size() > 0) {
            redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
            mav.setViewName("redirect:/show_users");
            return mav;
        }

        //勤怠情報取得
        List<GeneralDateAttendanceForm> generalDateAttendanceForms = dateAttendanceService.findGeneralDateAttendance(Integer.parseInt(id), 12);
        //ユーザ毎に月の勤怠状況ステータスを取得
        MonthAttendanceForm monthAttendanceForm = monthAttendanceService.findByUserIdAndMonth(Integer.parseInt(id), 12);
        mav.addObject("generalDateAttendances", generalDateAttendanceForms);
        mav.addObject("monthAttendanceForm", monthAttendanceForm);
        mav.setViewName("/check_attendance");
        return mav;
    }

    /*
     *　IDが空で渡ってきた場合
     */
    @GetMapping("/check_attendance/")
    public ModelAndView returnShowUsers(RedirectAttributes redirectAttributes){
        ModelAndView mav = new ModelAndView();
        //バリデーション
        List<String> errorMessages = new ArrayList<String>();
        errorMessages.add("・不正なパラメータが入力されました");
        redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
        mav.setViewName("redirect:/show_users");
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
        //勤怠マスタから対象者の12月のレコードのidを取得し、monthAttendanceFormにセット
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