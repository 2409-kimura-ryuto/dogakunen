package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.DateAttendanceService;
import com.example.dogakunen.service.MonthAttendanceService;
import com.example.dogakunen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminController {
    //システム管理者権限関係のController

    @Autowired
    UserService userService;

    @Autowired
    DateAttendanceService dateAttendanceService;

    @Autowired
    MonthAttendanceService monthAttendanceService;

    /*
     *システム管理画面表示処理
     */
    @GetMapping("/systemManage")
    public ModelAndView systemManage() {
        ModelAndView mav = new ModelAndView();

        // ユーザーを全件取得
        List<UserForm> userData = userService.findAllUser();

        //ユーザーデータオブジェクトを保管
        mav.addObject("users", userData);

        //画面遷移先を指定
        mav.setViewName("/system_manage");

        //画面に遷移
        return mav;
    }

    /*
     *新規アカウント登録画面表示処理
     */
    @GetMapping("/newUser")
    public ModelAndView newUser(){
        ModelAndView mav = new ModelAndView();

        //空のユーザー情報をセット
        UserForm newUser = new UserForm();

        //画面にバインド
        mav.addObject("user", newUser);

        //画面遷移先を指定
        mav.setViewName("/new_user");

        //画面に遷移
        return mav;
    }

    /*
     *アカウント登録処理
     */
    @PutMapping("/newUser")
    public ModelAndView entryUser(@ModelAttribute("user") @Validated UserForm userForm, BindingResult result,
                                  @RequestParam(name="position") Integer positionId, @RequestParam(name="employeeNumber") String employeeNumberStr){

        ModelAndView mav = new ModelAndView();

        employeeNumberStr = employeeNumberStr.replaceAll(",$", "");

        //バリデーション　必須チェック
        List<String> errorMessages = new ArrayList<String>();
        if (userForm.getEmployeeNumber() == null || (Integer.toString(userForm.getEmployeeNumber()).isBlank())){
            errorMessages.add("・社員番号を入力してください");
        }
        if (userForm.getPassword().isBlank()){
            errorMessages.add("・パスワードを入力してください");
        }
        if (userForm.getName().isBlank()){
            errorMessages.add("・氏名を入力してください");
        }
        if (Integer.toString(positionId).isBlank()){
            errorMessages.add("・役職を選択してください");
        }

        //妥当性チェック　パスワードと確認用パスワードが同一か
        if (!userForm.getPassword().equals(userForm.getPasswordConfirmation())){
            errorMessages.add("・パスワードと確認用パスワードが一致しません");
        }

        //重複チェック
        if(userForm.getEmployeeNumber() != null) {
            UserForm selectedAccount = userService.findByEmployeeNumber(userForm.getEmployeeNumber());
            if (selectedAccount != null){
                errorMessages.add("・アカウントが重複しています");
            }
        }


        //社員番号・パスワードの文字数チェック（アノテーションができなかった時用)
        if((userForm.getEmployeeNumber() != null) && (!Integer.toString(userForm.getEmployeeNumber()).isBlank()) && !employeeNumberStr.matches("^[0-9]{7}+$")) {
            errorMessages.add("・社員番号は半角数字7文字で入力してください");
        }
        if((!userForm.getPassword().isBlank()) && !userForm.getPassword().matches("^[!-~]{6,20}+$")) {
            errorMessages.add("・パスワードは半角文字かつ6文字以上20文字以下で入力してください");
        }

        if(!errorMessages.isEmpty()) {
            //エラーメッセージに値があれば、エラーメッセージを画面にバインド
            mav.addObject("errorMessages", errorMessages);

            //入力情報の保持
            userForm.setPositionId(positionId);
            mav.addObject("user", userForm);
            mav.setViewName("/new_user");
            return mav;
        }

        //登録処理
        //リクエストから取得したパスワードの暗号化
        String encodedPwd = BCrypt.hashpw(userForm.getPassword(), BCrypt.gensalt());
        //登録するユーザのパスワードを暗号化されたパスワードに変更
        userForm.setPassword(encodedPwd);
        userForm.setPositionId(positionId);
        userService.saveUser(userForm);

        //勤怠マスタ(日)の作成(日付挿入）
        UserForm newUser = userService.selectLoginUser(userForm.getEmployeeNumber());
        int newUserId = newUser.getId();
        dateAttendanceService.saveNewDate(newUserId);

        //勤怠マスタ(月)の作成
        monthAttendanceService.saveNewMonth(newUserId);

        //ユーザー管理画面へリダイレクト
        return new ModelAndView("redirect:/systemManage");
    }
}
