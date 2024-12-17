package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.*;
import com.example.dogakunen.holidayCsv.HolidayCsvParser;
import com.example.dogakunen.repository.entity.User;
import com.example.dogakunen.service.DateAttendanceService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.MonthAttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.YearMonth;
import java.util.List;
import com.example.dogakunen.holidayCsv.Holiday;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
public class AttendanceController {
    @Autowired
    HttpSession session;

    @Autowired
    DateAttendanceService dateAttendanceService;

    @Autowired
    MonthAttendanceService monthAttendanceService;

    @Autowired
    HolidayCsvParser holidayCsvParser;

    /*
     *　勤怠情報取得処理
     */
    @GetMapping
    public ModelAndView top() throws ParseException {
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

        //月の総労働時間計算
        String totalWorkTime = dateAttendanceService.sumTotalWorkTime(dateAttendances);

        //月の総休憩時間計算
        String totalBreakTime = dateAttendanceService.sumTotalBreakTime(dateAttendances);

        //月の所定時間計算(月の日数から土日祝を省く)
        //ここの引数の数字も動的にする
        int workingHours = calculateWorkingHours(2023, 7, 8);

        //承認者orシステム管理者フィルターのエラーメッセージをmavに詰めてセッション削除
        List<String> filterErrorMessages = (List<String>) session.getAttribute("filterErrorMessages");
        mav.addObject("filterErrorMessages", filterErrorMessages);
        session.removeAttribute("filterErrorMessages");

        //情報をセット
        mav.addObject("attendances",dateAttendances);
        mav.addObject("monthAttendance", monthAttendanceForm);
        mav.addObject("loginUser", loginUser);
        mav.addObject("attendanceStatus", attendanceStatus);
        mav.addObject("totalWorkTime", totalWorkTime);
        mav.addObject("totalBreakTime", totalBreakTime);
        mav.addObject("workingHours", workingHours);
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

        //休憩時間のバリデーション
        //労働開始/終了時間がnullだとエラーになるためnullじゃない時のみ処理を行う
        if (reqAttendance.getWorkTimeStart() != null && reqAttendance.getWorkTimeFinish() != null) {
            try {
                //労働時間を計算し変数に代入
                String totalWorkTime = dateAttendanceService.calculateWorkTime(reqAttendance);
                //労働時間と休憩時間をLocalTime型に変換
                LocalTime workTimeParsed = LocalTime.parse(totalWorkTime);
                LocalTime breakTimeParsed = LocalTime.parse(reqAttendance.getBreakTime());

                //労働時間と休憩時間を秒単位に変換
                long workSeconds = workTimeParsed.toSecondOfDay();
                long breakSeconds = breakTimeParsed.toSecondOfDay();

                //労働時間が6時間超8時間未満の場合
                if (workSeconds > 6 * 3600 && workSeconds < 8 * 3600 && breakSeconds < 45 * 60) {
                    errorMessages.add("・労働時間が6時間超8時間未満の場合は、休憩時間を最低45分取得してください");
                }

                //労働時間が8時間超の場合
                if (workSeconds > 8 * 3600 && breakSeconds < 60 * 60) {
                    errorMessages.add("・労働時間が8時間超の場合は、休憩時間を最低1時間取得してください");
                }
            } catch (DateTimeParseException e) {
                errorMessages.add("・休憩時間は労働時間を上回らないようにしてください");
            }
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
        List<String> errorMessages = new ArrayList<String>();

        //勤怠状況が存在しない勤怠(日)のidが入力された際のバリデーション
        if (id.matches("^[0-9]+$")) {
            try {
                dateAttendanceService.findDateAttendanceById(Integer.parseInt(id));
            } catch (RuntimeException e) {
                errorMessages.add("・不正なパラメータが入力されました");
                redirectAttributes.addFlashAttribute("parameterErrorMessages", errorMessages);
                mav.setViewName("redirect:/");
                return mav;
            }
        }

        //idの正規表現チェック
        if ((id == null) || (!id.matches("^[0-9]+$"))) {
            errorMessages.add("・不正なパラメータが入力されました");
            redirectAttributes.addFlashAttribute("parameterErrorMessages", errorMessages);
            mav.setViewName("redirect:/");
            return mav;
        }

        //URLのIDの勤怠(日)のユーザIDが自分以外のユーザIDの場合のバリデーションと
        //未登録or申請中/承認済みの場合に編集画面に遷移できないようにするバリデーション
        UserForm loginUser = (UserForm)session.getAttribute("loginUser");
        int loginUserId = loginUser.getId();
        int useId = dateAttendanceService.findDateAttendanceById(Integer.parseInt(id)).getUserId();
        int attendance = dateAttendanceService.findDateAttendanceById(Integer.parseInt(id)).getAttendance();
        int attendanceStatus = monthAttendanceService.findByUserIdAndMonth(loginUserId, 12).getAttendanceStatus();

        if (loginUserId != useId || attendance == 0 || attendanceStatus != 0) {
            errorMessages.add("・不正なパラメータが入力されました");
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
        ModelAndView mav = new ModelAndView();
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

        //休憩時間のバリデーション
        //労働開始/終了時間がnullだとエラーになるためnullじゃない時のみ処理を行う
        if (reqAttendance.getWorkTimeStart() != null && reqAttendance.getWorkTimeFinish() != null) {
            try {
                //労働時間を計算し変数に代入
                String totalWorkTime = dateAttendanceService.calculateWorkTime(reqAttendance);
                //労働時間と休憩時間をLocalTime型に変換
                LocalTime workTimeParsed = LocalTime.parse(totalWorkTime);
                LocalTime breakTimeParsed = LocalTime.parse(reqAttendance.getBreakTime());

                //労働時間と休憩時間を秒単位に変換
                long workSeconds = workTimeParsed.toSecondOfDay();
                long breakSeconds = breakTimeParsed.toSecondOfDay();

                //労働時間が6時間超8時間未満の場合
                if (workSeconds > 6 * 3600 && workSeconds < 8 * 3600 && breakSeconds < 45 * 60) {
                    errorMessages.add("・労働時間が6時間超8時間未満の場合は、休憩時間を最低45分取得してください");
                }

                //労働時間が8時間超の場合
                if (workSeconds > 8 * 3600 && breakSeconds < 60 * 60) {
                    errorMessages.add("・労働時間が8時間超の場合は、休憩時間を最低1時間取得してください");
                }
            //労働時間がマイナスになった際は例外が発生するため、その例外をキャッチした際にバリデーション処理を記述
            } catch (DateTimeParseException e) {
                errorMessages.add("・労働時間は休憩時間より下回らないようにしてください");
            }
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

    /*
     * 勤怠一括登録/編集画面表示
     */
    @GetMapping("/all_update_attendance")
    public ModelAndView allUpdateAttendance() throws ParseException {
        ModelAndView mav = new ModelAndView();
//        mav.setViewName("/show_users");
//        //空のformModelを入れる
//        DateAttendanceForm dateAttendance = new DateAttendanceForm();
//        dateAttendance.setBreakTime("00:00");
//        mav.addObject("formModel", dateAttendance);
        //【追加⑤】
        Calendar calender = Calendar.getInstance();
        //accessDateをセッションに入れる
        //session.setAttribute("accessDate", accessDate);
        //Date accessDateSession = (Date) session.getAttribute("accessDate");
        //calender.setTime(accessDate);
        int month = calender.get(Calendar.MONTH) + 1;
        int year = calender.get(Calendar.YEAR);

        calender.set(Calendar.DAY_OF_MONTH, 1);
        calender.set(Calendar.HOUR_OF_DAY, 0);
        calender.set(Calendar.MINUTE, 0);
        calender.set(Calendar.SECOND, 0);
        //Date型のフォーマット揃える（dateAttendancesのdateと）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
        String start = sdf.format(calender.getTime());
        Date startDate = sdf.parse(start);
        //Date startDate = calender.getTime();
        int endDay = calender.getActualMaximum(Calendar.DAY_OF_MONTH);
        calender.set(Calendar.DAY_OF_MONTH, endDay);
        //Date型のフォーマット揃える（dateAttendancesのdateと）
        String end = sdf.format(calender.getTime());
        Date endDate = sdf.parse(end);
        //Date endDate = calender.getTime();
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


//        // リストに勤怠情報を追加
//        List<DateAttendanceListForm.Attendance> attendances = List.of(attendance1, attendance2);
        //勤怠記録の取得
        //個々の引数は動的に変わるようにする
        List<DateAttendanceListForm.Attendance> attendances = dateAttendanceService.findAllAttendancesList(12, 9);

        // AttendanceFormにリストを設定
        DateAttendanceListForm formModel = new DateAttendanceListForm();
        formModel.setAttendances(attendances);

        mav.addObject("monthDates", dates);
        mav.addObject("formModel", formModel);
        mav.setViewName("/all_update_attendance");
        return mav;
    }

    /*
     * 勤怠一括登録/編集処理
     */
    @PostMapping("/updateAll")
    public ModelAndView updateAll(@ModelAttribute DateAttendanceListForm formModel) throws ParseException {
        ModelAndView mav = new ModelAndView();
        //フォームから送信された複数の勤怠情報を処理
        List<DateAttendanceListForm.Attendance> attendances = formModel.getAttendances();
        dateAttendanceService.updateAllAttendances(attendances, "2024009", 12);
        mav.setViewName("redirect:/");
        return mav;
    }

    /*
     * 所定日数を計算するメソッド(内閣府のCSV読み込みバージョン)
     */
    public int calculateWorkingHours(int year, int month, int dailyWorkHours) {
        //指定された月の全日付を生成
        YearMonth yearMonth = YearMonth.of(year, month);
        List<LocalDate> holidays = holidayCsvParser.parse().stream()
                .map(Holiday::getDate)
                .toList();

        int totalWorkingHours = 0;
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate currentDate = yearMonth.atDay(day);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            //土日祝を除外
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY || holidays.contains(currentDate)) {
                continue;
            }

            //営業日としてカウント
            totalWorkingHours += dailyWorkHours;
        }
        return totalWorkingHours;
    }
}