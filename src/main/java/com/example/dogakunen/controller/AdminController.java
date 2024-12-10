package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.DateAttendanceService;
import com.example.dogakunen.service.MonthAttendanceService;
import com.example.dogakunen.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @Autowired
    HttpSession session;

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

        //【追加】セッションからログインユーザーのユーザーIdを取得
        int loginUserId = ((UserForm) session.getAttribute("loginUser")).getId();
        mav.addObject("loginUserId", loginUserId);

        //画面遷移先を指定
        mav.setViewName("/system_manage");

        //画面に遷移
        return mav;
    }

    /*
     *アカウント停止・復活処理
     */
    @GetMapping("/accountStop/{isStoppedId}")
    public ModelAndView accountStop(@PathVariable Integer isStoppedId, @RequestParam(name="userId") Integer userId) {
        ModelAndView mav = new ModelAndView();

        if(isStoppedId == 0) {
            isStoppedId = 1;
        } else if (isStoppedId == 1) {
            isStoppedId = 0;
        }

        //アカウント停止・復活の更新処理
        userService.editIsStopped(isStoppedId, userId);

        //システム管理画面へリダイレクト
        return new ModelAndView("redirect:/systemManage");
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
                                  @RequestParam(name="position") Integer positionId){

        ModelAndView mav = new ModelAndView();

        //バリデーション　必須チェック
        List<String> errorMessages = new ArrayList<String>();
        if (userForm.getEmployeeNumber().isBlank()){
            errorMessages.add("・社員番号を入力してください");
        }
        if((!userForm.getEmployeeNumber().isBlank()) && !userForm.getEmployeeNumber().matches("^[0-9]{7}+$")) {
            errorMessages.add("・社員番号は半角数字7文字で入力してください");
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
        if(!userForm.getEmployeeNumber().isBlank()) {
            UserForm selectedEmployeeNumber = userService.findByEmployeeNumber(userForm.getEmployeeNumber());
            if (selectedEmployeeNumber != null){
                errorMessages.add("・社員番号が重複しています");
            }
        }

        //パスワードの文字数チェック
        if((!userForm.getPassword().isBlank()) && !userForm.getPassword().matches("^[!-~]{6,20}+$")) {
            errorMessages.add("・パスワードは半角文字かつ6文字以上20文字以下で入力してください");
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

        if(!errorMessages.isEmpty()) {
            //エラーメッセージに値があれば、エラーメッセージを画面にバインド
            mav.addObject("errorMessages", errorMessages);

            //入力情報の保持
            userForm.setPositionId(positionId);
            mav.addObject("user", userForm);
            mav.setViewName("/new_user");
            return mav;
        }

        //リクエストから取得したパスワードの暗号化
        String encodedPwd = BCrypt.hashpw(userForm.getPassword(), BCrypt.gensalt());
        //登録するユーザのパスワードを暗号化されたパスワードに変更
        userForm.setPassword(encodedPwd);
        userForm.setPositionId(positionId);
        //登録処理
        userService.saveUser(userForm);

        //勤怠マスタ(日)の作成(日付挿入）
        UserForm newUser = userService.findByEmployeeNumber(userForm.getEmployeeNumber());
        int newUserId = newUser.getId();
        dateAttendanceService.saveNewDate(newUserId);

        //勤怠マスタ(月)の作成
        monthAttendanceService.saveNewMonth(newUserId);

        //ユーザー管理画面へリダイレクト
        return new ModelAndView("redirect:/systemManage");
    }

    /*
     *アカウント編集画面表示
     */
    @GetMapping ("/editUser/{id}")
    public ModelAndView editUser(@PathVariable String id, RedirectAttributes redirectAttributes) {
        ModelAndView mav = new ModelAndView();

        //idの数字チェック
        List<String> errorMessages = new ArrayList<>();
        if(!id.matches("^[0-9]+$")) {
            errorMessages.add("・不正なパラメータが入力されました");
            redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
            //システム管理画面に遷移
            return new ModelAndView("redirect:/systemManage");
        }


        //編集ユーザー情報を取得
        Integer editUserId = Integer.parseInt(id);
        UserForm editUser = userService.selectEditUser(editUserId);

        //idの存在チェック
        if(editUser == null) {
            errorMessages.add("・不正なパラメータが入力されました");
            //エラーメッセージを格納して、ユーザー管理画面へ遷移
            redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
            //ユーザー管理画面にリダイレクト
            return new ModelAndView("redirect:/systemManage");
        }


        //編集するユーザー情報を画面にバインド
        mav.addObject("user", editUser);

        //【追加】セッションからユーザIDを取得・画面にバインド
        int loginUserId = ((UserForm) session.getAttribute("loginUser")).getId();
        mav.addObject("loginUserId", loginUserId);

        //画面遷移先を指定
        mav.setViewName("/edit_user");

        //画面に遷移
        return mav;
    }

    /*
     *編集画面のidが空の場合
     */
    @GetMapping("/editUser/")
    public ModelAndView editUserInvalid(RedirectAttributes redirectAttributes) {
        List<String> errorMessages = new ArrayList<String>();
        errorMessages.add("・不正なパラメータです");
        //エラーメッセージを格納して、ユーザー管理画面へ遷移
        redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
        //ユーザー管理画面にリダイレクト
        return new ModelAndView("redirect:/systemManage");
    }

    /*
     *アカウント編集処理
     */
    @PutMapping("/update/{id}")
    public ModelAndView updateUser(@PathVariable Integer id,
                                   @ModelAttribute("user") @Validated UserForm userForm, BindingResult result,
                                   @RequestParam(name="position") Integer positionId){
        ModelAndView mav = new ModelAndView();

        //バリデーション　必須チェック
        List<String> errorMessages = new ArrayList<>();
        if (userForm.getName().isBlank()){
            errorMessages.add("・氏名を入力してください");
        }
        if (Integer.toString(positionId).isBlank()){
            errorMessages.add("・役職を選択してください");
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

        //idからユーザ情報参照
        UserForm editUserForm = userService.selectEditUser(id);

        if(!errorMessages.isEmpty()) {
            //エラーメッセージに値があれば、エラーメッセージを画面にバインド
            mav.addObject("errorMessages", errorMessages);
            //入力した値を保管
            userForm.setPositionId(positionId);
            userForm.setEmployeeNumber(editUserForm.getEmployeeNumber());
            mav.addObject("user", userForm);

            //アカウント編集画面へフォワード処理
            mav.setViewName("/edit_user");
            return mav;
        }


        //更新処理
        userForm.setId(id);
        userForm.setPositionId(positionId);
        userForm.setEmployeeNumber(editUserForm.getEmployeeNumber());
        userForm.setPassword(editUserForm.getPassword());
        userForm.setIsStopped(editUserForm.getIsStopped());
        userService.saveUser(userForm);

        //システム管理画面へリダイレクト
        return new ModelAndView("redirect:/systemManage");

    }

}
