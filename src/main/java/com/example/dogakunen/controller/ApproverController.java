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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.dogakunen.controller.AttendanceController.accessDate;

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
    public ModelAndView showUsers(@RequestParam(name = "date", required = false) String reqDate) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/show_users");
        /*
         *承認対象者情報取得
         */
        //アクセス年月取得→リクエストがあればリクエストされた年月に変更
        YearMonth accessDate = YearMonth.now();
        YearMonth getDate = null;
        if (reqDate != null) {
            getDate = YearMonth.parse(reqDate);
        } else {
            getDate = accessDate;
        }
        /*
        YearMonth getDate = null;
        if (reqDate != null){
            int num = Integer.parseInt(reqDate);
            getDate = accessDate.plusMonths(num);
        } else {
            getDate = accessDate;
        }
         */
        //アクセスもしくはリクエスト年月をもとに承認者一覧を取得
        List<GeneralUserForm> generalUsers = userService.findAllGeneralUser(getDate.getYear(),getDate.getMonthValue());
        mav.addObject("users", generalUsers);

        //プルダウン表示
        YearMonth finalGetDate = getDate;
        List<String> availableDates = IntStream.rangeClosed(-6, 6)
                .mapToObj(i -> finalGetDate.plusMonths(i).toString())
                .collect(Collectors.toList());
        mav.addObject("availableDates", availableDates);

        //前月・次月表示
        YearMonth preMonth = null;
        YearMonth nextMonth = null;
        if (reqDate != null) {
            preMonth = YearMonth.parse(reqDate).plusMonths(1);
            nextMonth = YearMonth.parse(reqDate).plusMonths(-1);
        } else {
            preMonth = accessDate.plusMonths(1);
            nextMonth = accessDate.plusMonths(-1);
        }
        mav.addObject("preMonth", preMonth);
        mav.addObject("nextMonth", nextMonth);

        return mav;
    }

    /*
     * 完了申請処理
     */
    @PutMapping("/request")
    public ModelAndView request(RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        int loginUserId = loginUser.getId();

        //完了申請のバリデーション
        List<String> requestErrorMessages = new ArrayList<String>();
        Calendar calender = Calendar.getInstance();
        calender.setTime(accessDate);
        int month = calender.get(Calendar.MONTH) + 1;
        int year = calender.get(Calendar.YEAR);
        int dayOfMonth = calender.getActualMaximum(Calendar.DAY_OF_MONTH);
        //勤怠情報を取得し、取得したリストのサイズと該当月の日数が一致していなければエラーメッセージを追加する
        List<GeneralDateAttendanceForm> generalDateAttendanceForms = dateAttendanceService.findGeneralDateAttendance(loginUserId, year, month);
        if (generalDateAttendanceForms.size() != dayOfMonth) {
            requestErrorMessages.add("・全ての勤怠を登録してから申請してください");
        }

        //エラーメッセージが存在する場合はエラーメッセージをセットし、ホーム画面にリダイレクト
        if (requestErrorMessages.size() > 0) {
            redirectAttributes.addFlashAttribute("requestErrorMessages", requestErrorMessages);
            mav.setViewName("redirect:/");
            return mav;
        }

        //更新したいカラムのIdを取得してmonthAttendanceFormにセット
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(loginUserId, year, month).getId());
        //勤怠状況ステータスを1:申請中にセット
        monthAttendanceForm.setAttendanceStatus(1);
        //userIdとmonthとyearもセットしないと0で更新されてしまう
        monthAttendanceForm.setUserId(loginUserId);
        monthAttendanceForm.setMonth(month);
        monthAttendanceForm.setYear(year);
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
                monthAttendanceService.findByUserIdAndMonth(Integer.parseInt(id), 2024, 12).getId();
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
        List<GeneralDateAttendanceForm> generalDateAttendanceForms = dateAttendanceService.findGeneralDateAttendance(Integer.parseInt(id), 2024, 12);
        //ユーザ毎に月の勤怠状況ステータスを取得
        MonthAttendanceForm monthAttendanceForm = monthAttendanceService.findByUserIdAndMonth(Integer.parseInt(id), 2024, 12);
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
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(id, 2024, 12).getId());
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
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(id, 2024,  12).getId());
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