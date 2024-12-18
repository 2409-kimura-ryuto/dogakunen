package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.entity.AdministratorCSV;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import com.example.dogakunen.service.DateAttendanceService;
import com.example.dogakunen.service.LogService;
import com.opencsv.exceptions.CsvException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.service.MonthAttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.YearMonth;
import java.util.List;
import com.example.dogakunen.holidayCsv.Holiday;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import com.example.dogakunen.controller.form.*;
import com.example.dogakunen.holidayCsv.HolidayCsvParser;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    LogService logService;

    public static Date accessDate = new Date();

    /*
     *　勤怠情報取得処理
     */
    @GetMapping
    public ModelAndView top() throws ParseException {
        ModelAndView mav = new ModelAndView();

        //アクセスした日付の取得（実際の月の値を出すときは1を加算する必要がある）
        Calendar calender = Calendar.getInstance();
        //accessDateをセッションに入れる
        //session.setAttribute("accessDate", accessDate);
        //Date accessDateSession = (Date) session.getAttribute("accessDate");
        calender.setTime(accessDate);
        int month = calender.get(Calendar.MONTH) + 1;
        int year = calender.get(Calendar.YEAR);

        //【追加⑤】
        calender.set(Calendar.DAY_OF_MONTH, 1);
        calender.set(Calendar.HOUR_OF_DAY, 0);
        calender.set(Calendar.MINUTE, 0);
        calender.set(Calendar.SECOND, 0);
        //Date型のフォーマット揃える（dateAttendancesのdateのフォーマット）
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

        //祝日の取得
        List<String> holidays = holidayCsvParser.parse().stream()
                .map(holiday -> holiday.getDate().toString()) // LocalDateを文字列に変換
                .collect(Collectors.toList());

        //プルダウン用の表示リスト作成
        List<String> pullDown = new ArrayList<>();
        Calendar pullDownStart = Calendar.getInstance();
        pullDownStart.setTime(startDate);
        Calendar pullDownEnd = Calendar.getInstance();
        pullDownEnd.setTime(endDate);

        for(int i = -6; i <= 6; i++){
            pullDownStart.add(Calendar.MONTH, i);
            pullDownEnd.add(Calendar.MONTH, i);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
            String startPullDown = sdf2.format(pullDownStart.getTime());
            String endPullDown = sdf2.format(pullDownEnd.getTime());
            //「2024年12月1日～2024年12月31日」の文字列を作成
            String allPullDown = startPullDown + "～" + endPullDown;
            //プルダウン用の表示リストに格納
            pullDown.add(allPullDown);
            //startDateとendDateセットし直す（次の繰り返し処理で、-6カ月した月からさらに-5カ月になってしまうため）
            pullDownStart.setTime(startDate);
            pullDownEnd.setTime(endDate);
        }

        /*
        //Mapの宣言
        //Map<Integer, String> map = new HashMap<>();

        int i = -6;
        for(String str : pullDown) {
            // MapにListの値を追加
            map.put(i, str);
            i++;
        }
        // キーでソートする
        Object[] mapkey = map.keySet().toArray();
        Arrays.sort(mapkey);
        */

        //ログインユーザ情報取得
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        Integer loginId = loginUser.getId();

        //勤怠月取得
        MonthAttendanceForm monthAttendanceForm = monthAttendanceService.findByUserIdAndMonth(loginId, year, month);
        //勤怠記録の取得
        List<DateAttendanceForm> dateAttendances = dateAttendanceService.findALLAttendances (year, month, loginId);

        //勤怠状況ステータスによって申請ボタンの表示を切り替えるために勤怠状況ステータスを取得
        //【追加③】勤怠記録ステータスはデフォルトで0(申請前)を設定。monthAttendanceFormがnullじゃない時、int attendanceStatusを取得
        int attendanceStatus = 0;
        if(monthAttendanceForm != null) {
            attendanceStatus = monthAttendanceForm.getAttendanceStatus();
        }
        //【追加④】勤怠記録と、勤怠(月)の情報が無いとき（勤怠登録全くしていない）はそれぞれ空のFormを設定する
        if(dateAttendances == null) {
            dateAttendances = new ArrayList<>();
        }
        if(monthAttendanceForm == null) {
            monthAttendanceForm = new MonthAttendanceForm();
        }

        //月の総労働時間計算
        String totalWorkTime = dateAttendanceService.sumTotalWorkTime(dateAttendances);

        //月の総休憩時間計算
        String totalBreakTime = dateAttendanceService.sumTotalBreakTime(dateAttendances);

        //月の所定時間計算(月の日数から土日祝を省く)
        //一日の所定時間を8時間で定義
        int dayWorkingHour = 8;
        int workingHours = calculateWorkingHours(year, month, dayWorkingHour);

        //承認者orシステム管理者フィルターのエラーメッセージをmavに詰めてセッション削除
        List<String> filterErrorMessages = (List<String>) session.getAttribute("filterErrorMessages");
        mav.addObject("filterErrorMessages", filterErrorMessages);
        session.removeAttribute("filterErrorMessages");

        //情報をセット
        mav.addObject("attendances", dateAttendances);
        mav.addObject("monthAttendance", monthAttendanceForm);
        mav.addObject("loginUser", loginUser);
        mav.addObject("attendanceStatus", attendanceStatus);
        mav.addObject("totalWorkTime", totalWorkTime);
        mav.addObject("totalBreakTime", totalBreakTime);
        mav.addObject("workingHours", workingHours);
        //【追加】月の日付を画面にバインド
        mav.addObject("monthDates", dates);
        //祝日を画面にバインド
        mav.addObject("holidays", holidays);
        //mav.addObject("map", map);
        mav.addObject("pullDown", pullDown);
        mav.setViewName("/home");
        return mav;
    }

    /*
     * プルダウンで期間選択・「前月」「翌月」リンク押下時
     */
    @GetMapping("/selectMonth")
    public ModelAndView selectMonth(@RequestParam(name = "selectMonth") Integer selectMonth) {
        ModelAndView mav = new ModelAndView();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(accessDate);
        //リクエストパラメータ「selectMonth」で受け取った数字分加算・減算する処理
        calendar.add(Calendar.MONTH, selectMonth);
        accessDate = calendar.getTime();

        return new ModelAndView("redirect:/");
    }

    /*
     * 勤怠登録・編集画面表示
     */
    @GetMapping("/newOrEdit")
    public ModelAndView getEditAttendance(@RequestParam(name = "id", required = false) String id , @RequestParam(name = "date") @DateTimeFormat(pattern = "yyyy/MM/dd") Date date, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        List<String> errorMessages = new ArrayList<String>();
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        Integer UserID = loginUser.getId();

        //リクエストパラメータで取得した日付をLocalDate型に変換
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        //日付から年を算出
        int year = localDate.getYear();
        //日付から年を算出
        int month = localDate.getMonthValue();

        //idが画面から渡されているかで条件分岐
        //idが渡されてきた場合はバリデーション後に勤怠編集画面に遷移
        if (id != null) {
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
            int loginUserId = loginUser.getId();
            int userId = dateAttendanceService.findDateAttendanceById(Integer.parseInt(id)).getUserId();
            int attendance = dateAttendanceService.findDateAttendanceById(Integer.parseInt(id)).getAttendance();
            int attendanceStatus = monthAttendanceService.findByUserIdAndMonth(loginUserId, year, month).getAttendanceStatus();

            if (loginUserId != userId /*|| attendance == 0*/ || attendanceStatus != 0) {
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

        //idが渡されていない場合は、勤怠登録画面に遷移
        }else{
            //勤怠（月）の存在チェック
            //ユーザIDと現在年から勤怠（月）情報を取得
            MonthAttendanceForm monthAttendanceForm = monthAttendanceService.findByUserIdAndYear(UserID, year);
            //戻り値がnullの場合は勤怠（月）を作成
            if(monthAttendanceForm == null){
                monthAttendanceService.saveNewMonth(UserID, year);
            }

            //日付と休憩時間のデフォルト値を詰めたformModelを入れる
            DateAttendanceForm dateAttendance = new DateAttendanceForm();
            dateAttendance.setDate(date);
            dateAttendance.setBreakTime("00:00");
            mav.addObject("formModel", dateAttendance);
            mav.setViewName("/new_attendance");
            return mav;
        }
    }

    /*
     *　IDが空で渡ってきた場合
     *
    @GetMapping("/editAttendance/")
    public ModelAndView returnEditAttendance(RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();
        //バリデーション
        List<String> errorMessages = new ArrayList<String>();
        errorMessages.add("・不正なパラメータが入力されました");
        redirectAttributes.addFlashAttribute("parameterErrorMessages", errorMessages);
        mav.setViewName("redirect:/");
        return mav;
    }*/

    /*
     * 新規勤怠登録処理
     */
    @PostMapping("/newAttendance")
    public ModelAndView postNewAttendance(@ModelAttribute(name = "formModel") @Validated DateAttendanceForm reqAttendance,
                                          BindingResult result) throws ParseException {
        //ログインユーザ情報から社員番号取得
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        String employeeNumber = loginUser.getEmployeeNumber();

        /*
        //現在日付を取得
        LocalDate today = LocalDate.now();
        // 現在の月と年を取得
        int month = today.getMonthValue();
        int year = today.getYear();
         */
        //表示している日時を取得
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(accessDate);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        //上記で取得した年月をFormにセット
        reqAttendance.setMonth(month);
        reqAttendance.setYear(year);

        //バリデーション
        List<String> errorMessages = new ArrayList<>();
        Date date = reqAttendance.getDate();
        LocalTime startTime = reqAttendance.getWorkTimeStart();
        LocalTime finishTime = reqAttendance.getWorkTimeFinish();
        String breakTime = reqAttendance.getBreakTime();
        int attendanceNumber = reqAttendance.getAttendance();

        //勤務区分が休日の場合
        if (reqAttendance.getAttendance() == 5) {
            reqAttendance.setWorkTimeStart(LocalTime.parse("00:00"));
            reqAttendance.setWorkTimeFinish(LocalTime.parse("00:00"));
            reqAttendance.setBreakTime("00:00");
        }

        if (Objects.isNull(startTime) && attendanceNumber != 5) {
            errorMessages.add("・開始時刻を入力してください");
        }
        if (Objects.isNull(finishTime) && attendanceNumber != 5) {
            errorMessages.add("・終了時刻を入力してください");
        }
        if (attendanceNumber == 0) {
            errorMessages.add("・勤怠区分を登録してください");
        }
        if (attendanceNumber == 5 && (Objects.nonNull(startTime) || Objects.nonNull(finishTime) || !breakTime.equals("00:00"))) {
            errorMessages.add("・無効な入力です");
        }
        if (Objects.nonNull(startTime) && Objects.nonNull(finishTime) && !startTime.isBefore(finishTime)) {
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

        if (result.hasErrors()) {
            //エラーがあったら、エラーメッセージを格納する
            //エラーメッセージの取得
            for (FieldError error : result.getFieldErrors()) {
                String message = error.getDefaultMessage();
                //取得したエラーメッセージをエラーメッセージのリストに格納
                errorMessages.add(message);
            }
        }
        if (!errorMessages.isEmpty()) {
            ModelAndView mav = new ModelAndView();
            mav.addObject("formModel", reqAttendance);
            mav.addObject("errorMessages", errorMessages);
            mav.setViewName("/new_attendance");
            return mav;
        }
        
        //勤怠登録処理
        dateAttendanceService.postNew(reqAttendance, employeeNumber);
        logService.newLog(reqAttendance, employeeNumber);
        return new ModelAndView("redirect:/");
    }


    /*
     * 勤怠編集処理
     */
    @PostMapping("/editAttendance")
    public ModelAndView postEditAttendance(@ModelAttribute(name = "formModel") @Validated DateAttendanceForm reqAttendance,
                                           BindingResult result, @RequestParam(name = "id") Integer id,
                                           @RequestParam(name = "month") Integer month) throws ParseException, IllegalAccessException {
        ModelAndView mav = new ModelAndView();
        //バリデーション
        //エラーメッセージの準備
        List<String> errorMessages = new ArrayList<>();
        //Formから業務開始・終了時間、休憩時間、勤怠区分を取得
        LocalTime startTime = reqAttendance.getWorkTimeStart();
        LocalTime finishTime = reqAttendance.getWorkTimeFinish();
        String breakTime = reqAttendance.getBreakTime();
        int attendanceNumber = reqAttendance.getAttendance();

        //勤務区分が休日の場合
        if (reqAttendance.getAttendance() == 5) {
            reqAttendance.setWorkTimeStart(LocalTime.parse("00:00"));
            reqAttendance.setWorkTimeFinish(LocalTime.parse("00:00"));
            reqAttendance.setBreakTime("00:00");
        }

        //各バリデーション
        if (Objects.isNull(startTime) && attendanceNumber != 5) {
            errorMessages.add("・開始時刻を入力してください");
        }
        if (Objects.isNull(finishTime) && attendanceNumber != 5) {
            errorMessages.add("・終了時刻を入力してください");
        }
        if (attendanceNumber == 0) {
            errorMessages.add("・勤怠区分を登録してください");
        }
        if (attendanceNumber == 5 && (!startTime.equals(LocalTime.parse("00:00")) || !finishTime.equals(LocalTime.parse("00:00"))
                || (!breakTime.equals("00:00:00") && !breakTime.equals("00:00")))) {
            errorMessages.add("・無効な入力です");
        }
        if (attendanceNumber != 5 && Objects.nonNull(startTime) && Objects.nonNull(finishTime) && !startTime.isBefore(finishTime)) {
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

        if (result.hasErrors()) {
            //Formでエラーがあったら、エラーメッセージを格納する
            //エラーメッセージの取得
            for (FieldError error : result.getFieldErrors()) {
                String message = error.getDefaultMessage();
                //取得したエラーメッセージをエラーメッセージのリストに格納
                errorMessages.add(message);
            }
        }
        //エラーメッセージが１つでもあった場合は、画面にエラーメッセージをセットし、勤怠編集画面に遷移
        if (!errorMessages.isEmpty()) {
            mav.addObject("formModel", reqAttendance);
            mav.addObject("errorMessages", errorMessages);
            mav.setViewName("/edit_attendance");
            return mav;
        }

        //ログインユーザ情報から社員番号取得
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        String employeeNumber = loginUser.getEmployeeNumber();
        reqAttendance.setId(id);

        //変更前の勤怠情報を持ってくる
        DateAttendanceForm preAttendance = dateAttendanceService.findDateAttendanceById(id);
        dateAttendanceService.updateAttendance(reqAttendance, employeeNumber, month);
        //変更後に勤怠履歴を残す
        logService.editLog(preAttendance, reqAttendance, employeeNumber);
        return new ModelAndView("redirect:/");
    }

    /*
     * 勤怠一括登録/編集画面表示
     */
    @GetMapping("/all_update_attendance")
    public ModelAndView allUpdateAttendance() throws ParseException {
        ModelAndView mav = new ModelAndView();
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        String employeeNumber = loginUser.getEmployeeNumber();
        String name = loginUser.getName();
        int loginUserId = loginUser.getId();

        //祝日の取得
        List<String> holidays = holidayCsvParser.parse().stream()
                .map(holiday -> holiday.getDate().toString()) // LocalDateを文字列に変換
                .collect(Collectors.toList());

//        mav.setViewName("/show_users");
//        //空のformModelを入れる
//        DateAttendanceForm dateAttendance = new DateAttendanceForm();
//        dateAttendance.setBreakTime("00:00");
//        mav.addObject("formModel", dateAttendance);
        //【追加⑤】
        Calendar calender = Calendar.getInstance();
        calender.setTime(accessDate);
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

        //プルダウン用の表示リスト作成
        List<String> pullDown = new ArrayList<>();
        Calendar pullDownStart = Calendar.getInstance();
        pullDownStart.setTime(startDate);
        Calendar pullDownEnd = Calendar.getInstance();
        pullDownEnd.setTime(endDate);

        for(int i = -6; i <= 6; i++){
            pullDownStart.add(Calendar.MONTH, i);
            pullDownEnd.add(Calendar.MONTH, i);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
            String startPullDown = sdf2.format(pullDownStart.getTime());
            String endPullDown = sdf2.format(pullDownEnd.getTime());
            //「2024年12月1日～2024年12月31日」の文字列を作成
            String allPullDown = startPullDown + "～" + endPullDown;
            //プルダウン用の表示リストに格納
            pullDown.add(allPullDown);
            //startDateとendDateセットし直す（次の繰り返し処理で、-6カ月した月からさらに-5カ月になってしまうため）
            pullDownStart.setTime(startDate);
            pullDownEnd.setTime(endDate);
        }
        
//        // リストに勤怠情報を追加
//        List<DateAttendanceListForm.Attendance> attendances = List.of(attendance1, attendance2);
        //勤怠記録の取得
        //個々の引数は動的に変わるようにする
        List<DateAttendanceListForm.Attendance> attendances = dateAttendanceService.findAllAttendancesList(year, month, loginUserId);

        // AttendanceFormにリストを設定
        DateAttendanceListForm formModel = new DateAttendanceListForm();
        formModel.setAttendances(attendances);

        //勤怠記録の取得
        List<DateAttendanceForm> dateAttendances = dateAttendanceService.findALLAttendances (year, month, loginUserId);

        mav.addObject("monthDates", dates);
        mav.addObject("formModel", formModel);
        mav.addObject("dateAttendances", dateAttendances);
        mav.addObject("pullDown", pullDown);
        mav.addObject("holidays", holidays);
        mav.addObject("loginUser", loginUser);
        mav.setViewName("/all_update_attendance");
        return mav;
    }

    /*
     * プルダウンで期間選択・「前月」「翌月」リンク押下時
     */
    @GetMapping("/selectMonthAll")
    public ModelAndView selectMonthAll(@RequestParam(name = "selectMonthAll") Integer selectMonthAll) {
        ModelAndView mav = new ModelAndView();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(accessDate);
        //リクエストパラメータ「selectMonth」で受け取った数字分加算・減算する処理
        calendar.add(Calendar.MONTH, selectMonthAll);
        accessDate = calendar.getTime();

        return new ModelAndView("redirect:/all_update_attendance");
    }

    /*
     * 勤怠一括登録/編集処理
     */
    @PostMapping("/updateAll")
    public ModelAndView updateAll(@ModelAttribute @Validated DateAttendanceListForm formModel,
                                  BindingResult result,
                                  @RequestParam(name = "dates", required = false) List<Date> dates,
                                  RedirectAttributes redirectAttributes) throws ParseException {
        ModelAndView mav = new ModelAndView();
        DateAttendanceListForm listForm = new DateAttendanceListForm();
        //ログインユーザ情報から社員番号取得
        UserForm loginUser = (UserForm) session.getAttribute("loginUser");
        String employeeNumber = loginUser.getEmployeeNumber();
        int loginUserId = loginUser.getId();

        //表示している日時を取得
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(accessDate);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        int i = 0;

        //フォームから送信された複数の勤怠情報を処理
        List<DateAttendanceListForm.Attendance> attendances = formModel.getAttendances();
        for (DateAttendanceListForm.Attendance attendance : attendances) {

            //バリデーション(何かしらの項目に入力があった時のみ)
            if (attendance.getAttendance() != 0 || attendance.getWorkTimeStart() != null || attendance.getWorkTimeFinish() != null || attendance.getBreakTime() != "" || attendance.getMemo() != "") {

                //勤務区分が休日の場合
                if (attendance.getAttendance() == 5) {
                    attendance.setWorkTimeStart(LocalTime.parse("00:00"));
                    attendance.setWorkTimeFinish(LocalTime.parse("00:00"));
                    attendance.setBreakTime("00:00");
                }

                //エラーメッセージの準備
                List<String> errorMessages = new ArrayList<>();
                //Formから業務開始・終了時間、休憩時間、勤怠区分を取得
                LocalTime startTime = attendance.getWorkTimeStart();
                LocalTime finishTime = attendance.getWorkTimeFinish();
                String breakTime = attendance.getBreakTime();
                int attendanceNumber = attendance.getAttendance();
                //各バリデーション
                if (Objects.isNull(startTime) && attendanceNumber != 5) {
                    errorMessages.add("・開始時刻を入力してください");
                }
                if (Objects.isNull(finishTime) && attendanceNumber != 5) {
                    errorMessages.add("・終了時刻を入力してください");
                }
                if (attendanceNumber == 0) {
                    errorMessages.add("・勤怠区分を登録してください");
                }
                if (attendanceNumber == 5 && (!startTime.equals(LocalTime.parse("00:00")) || !finishTime.equals(LocalTime.parse("00:00"))
                        || (!breakTime.equals("00:00:00") && !breakTime.equals("00:00")))) {
                    errorMessages.add("・無効な入力です");
                }
                if (attendanceNumber != 5 && Objects.nonNull(startTime) && Objects.nonNull(finishTime) && !startTime.isBefore(finishTime)) {
                    errorMessages.add("・無効な入力です");
                }
                if (attendance.getMemo().length() > 60) {
                    errorMessages.add("・60文字以下で入力してください");
                }

                //休憩時間のバリデーション
                //労働開始/終了時間がnullだとエラーになるためnullじゃない時のみ処理を行う
                if (attendance.getWorkTimeStart() != null && attendance.getWorkTimeFinish() != null) {
                    try {
                        //労働時間を計算し変数に代入
                        String totalWorkTime = dateAttendanceService.calculateWorkTimeList(attendance);
                        //労働時間と休憩時間をLocalTime型に変換
                        LocalTime workTimeParsed = LocalTime.parse(totalWorkTime);
                        LocalTime breakTimeParsed = LocalTime.parse(attendance.getBreakTime());

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

                if (result.hasErrors()) {
                    //Formでエラーがあったら、エラーメッセージを格納する
                    //エラーメッセージの取得
                    for (FieldError error : result.getFieldErrors()) {
                        String message = error.getDefaultMessage();
                        //取得したエラーメッセージをエラーメッセージのリストに格納
                        errorMessages.add(message);
                    }
                }
                //エラーメッセージが１つでもあった場合は、画面にエラーメッセージをセットし、勤怠編集画面に遷移
                if (!errorMessages.isEmpty()) {
                    //                mav.addObject("formModel", reqAttendance);
                    // AttendanceFormにリストを設定
                    listForm.setAttendances(attendances);
                    mav.addObject("formModel", formModel);
                    mav.addObject("loginUser", loginUser);
                    mav.addObject("errorMessages", errorMessages);

                    //エラー時にフォワードして入力値を保持させるためにプルダウンと前月/翌月の記述を書く
                    Calendar calendar2 = Calendar.getInstance();
                    calendar2.setTime(accessDate);
//                    int month = calendar.get(Calendar.MONTH) + 1;
//                    int year = calendar.get(Calendar.YEAR);
                    calendar2.set(Calendar.DAY_OF_MONTH, 1);
                    calendar2.set(Calendar.HOUR_OF_DAY, 0);
                    calendar2.set(Calendar.MINUTE, 0);
                    calendar2.set(Calendar.SECOND, 0);
                    //Date型のフォーマット揃える（dateAttendancesのdateと）
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.s");
                    String start = sdf.format(calendar2.getTime());
                    Date startDate = sdf.parse(start);
                    //Date startDate = calender.getTime();
                    int endDay = calendar2.getActualMaximum(Calendar.DAY_OF_MONTH);
                    calendar2.set(Calendar.DAY_OF_MONTH, endDay);
                    //Date型のフォーマット揃える（dateAttendancesのdateと）
                    String end = sdf.format(calendar2.getTime());
                    Date endDate = sdf.parse(end);
                    //Date endDate = calender.getTime();
                    List<Date> dates2 = new ArrayList<Date>();
                    Calendar calendar3 = new GregorianCalendar();
                    calendar3.setTime(startDate);
                    while (calendar3.getTime().before(endDate))
                    {
                        Date result2 = calendar3.getTime();
                        dates2.add(result2);
                        calendar3.add(Calendar.DATE, 1);
                    }
                    dates2.add(endDate);

                    //プルダウン用の表示リスト作成
                    List<String> pullDown = new ArrayList<>();
                    Calendar pullDownStart = Calendar.getInstance();
                    pullDownStart.setTime(startDate);
                    Calendar pullDownEnd = Calendar.getInstance();
                    pullDownEnd.setTime(endDate);

                    for(int j = -6; j <= 6; j++){
                        pullDownStart.add(Calendar.MONTH, i);
                        pullDownEnd.add(Calendar.MONTH, i);
                        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
                        String startPullDown = sdf2.format(pullDownStart.getTime());
                        String endPullDown = sdf2.format(pullDownEnd.getTime());
                        //「2024年12月1日～2024年12月31日」の文字列を作成
                        String allPullDown = startPullDown + "～" + endPullDown;
                        //プルダウン用の表示リストに格納
                        pullDown.add(allPullDown);
                        //startDateとendDateセットし直す（次の繰り返し処理で、-6カ月した月からさらに-5カ月になってしまうため）
                        pullDownStart.setTime(startDate);
                        pullDownEnd.setTime(endDate);
                    }


                    mav.addObject("monthDates", dates2);
                    mav.addObject("pullDown", pullDown);
                    mav.setViewName("/all_update_attendance");

                    //勤怠記録の取得の記述も書いてフォワード先に渡す
                    List<DateAttendanceForm> dateAttendances = dateAttendanceService.findALLAttendances (year, month, loginUserId);
                    mav.addObject("dateAttendances", dateAttendances);

                    //祝日の取得も同様に
                    List<String> holidays = holidayCsvParser.parse().stream()
                            .map(holiday -> holiday.getDate().toString()) // LocalDateを文字列に変換
                            .collect(Collectors.toList());

                    mav.addObject("holidays", holidays);

//                    redirectAttributes.addFlashAttribute("formModel", formModel);
//                    redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
//                    mav.setViewName("redirect:/all_update_attendance");

                    return mav;
                }
            }

            //登録か編集かの条件分岐(メモ以外の全ての項目が入力された時のみ)
            if (attendance.getAttendance() != 0 && attendance.getWorkTimeStart() != null && attendance.getWorkTimeFinish() != null && attendance.getBreakTime() != "") {
                if (attendance.getId() == 0) {
                    //上記で取得した年月をFormにセット
                    attendance.setMonth(month);
                    attendance.setYear(year);

                    attendance.setDate(dates.get(i));
                    i++;
                    //登録処理
                    dateAttendanceService.postListNew(attendance, employeeNumber);
                } else {
                    //編集処理
                    dateAttendanceService.updateAllAttendances(attendance, employeeNumber, 12);
                }
            }
        }
        mav.setViewName("redirect:/all_update_attendance");
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