package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.DateAttendanceForm;
import com.example.dogakunen.controller.form.MonthAttendanceForm;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.repository.entity.DateAttendance;
import com.example.dogakunen.repository.entity.User;
import com.example.dogakunen.service.DateAttendanceService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.MonthAttendanceService;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

        /*
        Date n;
        while(startDate.before(endDate)) {
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(startDate);
            calendar1.add(Calendar.DAY_OF_MONTH, 1);
            List<Date> monthdates = new ArrayList<>();
            monthdates.add(calendar1.getTime());

        }
         */
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
            String allPullDown = startPullDown + "～" + endPullDown;
            pullDown.add(allPullDown);
            pullDownStart.setTime(startDate);
            pullDownEnd.setTime(endDate);
        }

        //Mapの宣言
        Map<Integer, String> map = new HashMap<>();

        int i = -6;
        for(String str : pullDown) {
            // MapにListの値を追加
            map.put(i, str);
            i++;
        }
        // キーでソートする
        Object[] mapkey = map.keySet().toArray();
        Arrays.sort(mapkey);





        //ログインユーザ情報取得
        UserForm loginUser =(UserForm) session.getAttribute("loginUser");
        Integer loginId = loginUser.getId();

        //勤怠月取得
        MonthAttendanceForm monthAttendanceForm = monthAttendanceService.findByUserIdAndMonth(loginId, month);
        //勤怠記録の取得
        List<DateAttendanceForm> dateAttendances = dateAttendanceService.findALLAttendances (month, loginId);
        //サンプルで一時的に追加（あとで消します）
        //Date sampleDate = dateAttendances.get(0).getDate();
        //int sample = sampleDate.compareTo(dates.get(0));

        //勤怠状況ステータスによって申請ボタンの表示を切り替えるために勤怠状況ステータスを取得
        //int attendanceStatus = monthAttendanceService.findByUserIdAndMonth(loginUser.getId(), 12).getAttendanceStatus();
        //【追加③】勤怠記録ステータスはデフォルトで0(申請前)を設定。monthAttendanceFormがnullじゃない時、int attendanceStatusを取得
        int attendanceStatus = 0;
        if(monthAttendanceForm != null) {
            attendanceStatus = monthAttendanceForm.getAttendanceStatus();
        }
        //【追加④】勤怠記録と、勤怠(月)の情報が無いとき（勤怠登録全くしていない）はそれぞれ空のFormを設定する
        if(dateAttendances == null) {
            dateAttendances = new ArrayList<>();
            /*
            //dateAttendanceにデフォルト値の設定
            DateAttendanceForm dateAttendance = new DateAttendanceForm();
            dateAttendance.setAttendance(0);
            dateAttendance.setWorkTimeStart(LocalTime.parse("00:00"));
            dateAttendance.setWorkTimeFinish(LocalTime.parse("00:00"));
            dateAttendance.setBreakTime("00:00");
            dateAttendance.setWorkTime("00:00");
            dateAttendance.setMemo("");

            dateAttendances.add(dateAttendance);
            */


        }
        if(monthAttendanceForm == null) {
            monthAttendanceForm = new MonthAttendanceForm();
        }



        //承認者orシステム管理者フィルターのエラーメッセージをmavに詰めてセッション削除
        List<String> filterErrorMessages = (List<String>) session.getAttribute("filterErrorMessages");
        mav.addObject("filterErrorMessages", filterErrorMessages);
        session.removeAttribute("filterErrorMessages");

        //情報をセット
        mav.addObject("attendances",dateAttendances);
        mav.addObject("monthAttendance", monthAttendanceForm);
        mav.addObject("loginUser", loginUser);
        mav.addObject("attendanceStatus", attendanceStatus);
        //【追加】月の日付を画面にバインド
        mav.addObject("monthDates", dates);
        mav.addObject("map", map);
        mav.addObject("pullDown", pullDown);
        mav.setViewName("/home");
        return mav;
    }

    /*
     * 「前月へ」リンク押下時
     */
    @GetMapping("/prevMonth")
    public ModelAndView prevMonth() {
        ModelAndView mav = new ModelAndView();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(accessDate);
        calendar.add(Calendar.MONTH, -1);
        accessDate = calendar.getTime();

        return new ModelAndView("redirect:/");
    }

    /*
     * 「次月へ」リンク押下時
     */
    @GetMapping("/nextMonth")
    public ModelAndView nextMonth() {
        ModelAndView mav = new ModelAndView();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(accessDate);
        calendar.add(Calendar.MONTH, 1);
        accessDate = calendar.getTime();

        return new ModelAndView("redirect:/");
    }

    /*
     * プルダウンで期間選択時
     */
    @GetMapping("/selectMonth")
    public ModelAndView selectMonth(@RequestParam(name = "selectMonth") Integer selectMonth) {
        ModelAndView mav = new ModelAndView();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(accessDate);
        calendar.add(Calendar.MONTH, selectMonth);
        accessDate = calendar.getTime();

        return new ModelAndView("redirect:/");
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