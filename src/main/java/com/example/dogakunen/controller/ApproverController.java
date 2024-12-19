package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.*;
import com.example.dogakunen.holidayCsv.HolidayCsvParser;
import com.example.dogakunen.service.DateAttendanceService;
import com.example.dogakunen.service.MonthAttendanceService;
import com.example.dogakunen.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Autowired
    HolidayCsvParser holidayCsvParser;

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
            getDate = YearMonth.parse(reqDate, DateTimeFormatter.ofPattern("yyyy年MM月"));
        } else {
            getDate = accessDate;
        }

        //アクセスもしくはリクエスト年月をもとに承認者一覧を取得
        int year = getDate.getYear();
        int month = getDate.getMonthValue();
        List<GeneralUserForm> generalUsers = userService.findAllGeneralUser(year, month);
        mav.addObject("users", generalUsers);
        mav.addObject("year", Integer.toString(year));
        mav.addObject("month", Integer.toString(month));

        //プルダウン表示
        YearMonth finalGetDate = getDate;
        List<String> availableDates = IntStream.rangeClosed(-6, 6)
                .mapToObj(i -> finalGetDate.plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy年MM月")))
                .collect(Collectors.toList());
        mav.addObject("availableDates", availableDates);

        //前月・次月表示
        String preMonth = null;
        String nextMonth = null;
        if (reqDate != null) {
            preMonth = getDate.plusMonths(-1).format(DateTimeFormatter.ofPattern("yyyy年MM月"));
            nextMonth = getDate.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy年MM月"));
        } else {
            preMonth = accessDate.plusMonths(-1).format(DateTimeFormatter.ofPattern("yyyy年MM月"));
            nextMonth = accessDate.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy年MM月"));
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

        //月の労働時間が200時間を超えた時のバリデーション
        //勤怠記録の取得
        List<DateAttendanceForm> dateAttendances = dateAttendanceService.findALLAttendances(2024, month, loginUserId);
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
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(loginUserId, year, month).getId());
        //YearMonth now = YearMonth.now();
        //int year = now.getYear();
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
    public ModelAndView checkAttendance(@PathVariable String id, @RequestParam(name = "year") String year,
                                        @RequestParam(name = "month") String month, RedirectAttributes redirectAttributes) throws ParseException {
        ModelAndView mav = new ModelAndView();

        //idの正規表現チェック
        List<String> errorMessages = new ArrayList<String>();
        if ((id == null) || (!id.matches("^[0-9]+$")) || (year == null) || (!year.matches("20[0-9][0-9]"))
                                || (month == null) || (!month.matches("[1-9]|[1][0-2]"))) {
            errorMessages.add("・不正なパラメータが入力されました");
            redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
            mav.setViewName("redirect:/show_users");
            return mav;
        }

        //勤怠状況が存在しないユーザのidが入力された際のバリデーション
        if (id.matches("^[0-9]+$")) {
            try {
                monthAttendanceService.findByUserIdAndMonth(Integer.parseInt(id), Integer.parseInt(year), Integer.parseInt(month)).getId();
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

        //該当月の1カ月分のリスト作成
        Calendar calender = Calendar.getInstance();
        calender.set(Integer.parseInt(year), Integer.parseInt(month)-1, 1, 0, 0, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
        String start = sdf.format(calender.getTime());
        Date startDate = sdf.parse(start);
        int endDay = calender.getActualMaximum(Calendar.DAY_OF_MONTH);
        calender.set(Calendar.DAY_OF_MONTH, endDay);
        String end = sdf.format(calender.getTime());
        Date endDate = sdf.parse(end);

        List<Date> dates = new ArrayList<Date>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startDate);

        while (calendar.getTime().before(endDate))
        {
            Date result = calendar.getTime();
            dates.add(result);
            calendar.add(Calendar.DATE, 1);
        }
        dates.add(endDate);
        //日付を画面にバインド
        mav.addObject("monthDates", dates);

        //祝日の取得
        List<String> holidays = holidayCsvParser.parse().stream()
                .map(holiday -> holiday.getDate().toString()) // LocalDateを文字列に変換
                .collect(Collectors.toList());
        mav.addObject("holidays", holidays);


        //勤怠情報取得
        List<GeneralDateAttendanceForm> generalDateAttendanceForms = dateAttendanceService.findGeneralDateAttendance(Integer.parseInt(id), Integer.parseInt(year), Integer.parseInt(month));
        //ユーザ毎に月の勤怠状況ステータスを取得
        MonthAttendanceForm monthAttendanceForm = monthAttendanceService.findByUserIdAndMonth(Integer.parseInt(id), Integer.parseInt(year), Integer.parseInt(month));
        mav.addObject("generalDateAttendances", generalDateAttendanceForms);
        mav.addObject("monthAttendanceForm", monthAttendanceForm);
        //追加(戻るボタンを押下時に日付が保持されるようYearMonth型の年月を作成したのち、String型に変換して画面にバインド)
        YearMonth ym = YearMonth.of(Integer.parseInt(year), Integer.parseInt(month));
        String yearMonth = ym.format(DateTimeFormatter.ofPattern("yyyy年MM月"));
        mav.addObject("yearMonth", yearMonth);

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
    @PutMapping("/approve/{id}/{year}/{month}")
    public ModelAndView approve(@PathVariable Integer id, @PathVariable String year, @PathVariable String month) {
        ModelAndView mav = new ModelAndView();
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        //勤怠マスタから対象者の12月のレコードのidを取得し、monthAttendanceFormにセット
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(id, Integer.parseInt(year), Integer.parseInt(month)).getId());
        //2:承認済みをセット
        monthAttendanceForm.setAttendanceStatus(2);
        //userIdとmonthもセットしないと0で更新されてしまう
        monthAttendanceForm.setUserId(id);
        monthAttendanceForm.setYear(Integer.parseInt(year));
        monthAttendanceForm.setMonth(Integer.parseInt(month));
        //勤怠記録ステータスを2:承認済みに更新
        monthAttendanceService.changeStatus(monthAttendanceForm);
        //承認対象者一覧画面にリダイレクト
        mav.setViewName("redirect:/show_users");
        return mav;
    }

    /*
     * 差し戻し処理
     */
    @PutMapping("/sendBack/{id}/{year}/{month}")
    public ModelAndView sendBack(@PathVariable Integer id, @PathVariable String year, @PathVariable String month) {
        ModelAndView mav = new ModelAndView();
        MonthAttendanceForm monthAttendanceForm = new MonthAttendanceForm();
        //勤怠マスタから対象者の12月のレコードのidを取得し、monthAttendanceFormにセット
        monthAttendanceForm.setId(monthAttendanceService.findByUserIdAndMonth(id, Integer.parseInt(year), Integer.parseInt(month)).getId());
        //0:申請前をセット
        monthAttendanceForm.setAttendanceStatus(0);
        //userIdとmonthもセットしないと0で更新されてしまう
        monthAttendanceForm.setUserId(id);
        monthAttendanceForm.setYear(Integer.parseInt(year));
        monthAttendanceForm.setMonth(Integer.parseInt(month));
        //勤怠記録ステータスを0:申請前に更新
        monthAttendanceService.changeStatus(monthAttendanceForm);
        //承認対象者一覧画面にリダイレクト
        mav.setViewName("redirect:/show_users");
        return mav;
    }
}