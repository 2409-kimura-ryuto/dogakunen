package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AccountController {

    @Autowired
    HttpSession session;
    @Autowired
    UserService userService;


    /*
     * ログイン画面表示
     */
    @GetMapping("/login")
    public ModelAndView loginView() {
        ModelAndView mav = new ModelAndView();
        // form用の空のentityを準備
        UserForm userForm = new UserForm();
        // 画面遷移先を指定
        mav.setViewName("/login");
        // 準備した空のFormを画面にバインド
        //mav.addObject("formModel", userForm);
        //エラーメッセージ表示
        List<String> errorMessage = (List<String>) session.getAttribute("errorMessages");
        Integer employeeNumber = (Integer) session.getAttribute("employeeNumber");
        if (errorMessage != null) {
            mav.addObject("errorMessages", errorMessage);
            mav.addObject("employeeNumber", employeeNumber);
            session.invalidate();
        }
        return mav;
    }

    /*
     * ログイン処理
     */
    @GetMapping("/loginUser")
    public ModelAndView login(@RequestParam(name = "employeeNumber", required = false) Integer employeeNumber,
                              @RequestParam(name = "password", required = false) String password) {
        //バリデーション
        //エラーメッセージの準備
        List<String> errorMessages = new ArrayList<String>();
        //社員番号入力チェック
        if (employeeNumber == null) {
            errorMessages.add("社員番号を入力してください");
        }
        //パスワード入力チェック
        if (password.isBlank()) {
            errorMessages.add("パスワードを入力してください");
        }
        //エラーメッセージが１つ以上ある場合
        if (errorMessages.size() != 0) {
            //セッションにエラーメッセージと社員番号を設定
            session.setAttribute("errorMessages", errorMessages);
            session.setAttribute("employeeNumber", employeeNumber);
            //ログイン画面にリダイレクト
            return new ModelAndView("redirect:/login");
        }
        //リクエストから取得した社員番号をもとにユーザ情報を取得
        UserForm loginUser = userService.selectLoginUser(employeeNumber);
        //バリデーション
        //ユーザが存在しないか停止中またはパスワードが違えばエラーメッセージをセット
        if (loginUser == null || loginUser.getIsStopped() == 1 || !BCrypt.checkpw(password, loginUser.getPassword())) {
            errorMessages.add("ログインに失敗しました");
        }
        //エラーメッセージが１つ以上ある場合
        if (errorMessages.size() != 0) {
            //セッションにエラーメッセージと社員番号を設定
            session.setAttribute("errorMessages", errorMessages);
            session.setAttribute("employeeNumber", employeeNumber);
            //ログイン画面にリダイレクト
            return new ModelAndView("redirect:/login");
        }
        //セッションにログインユーザ情報を詰める
        session.setAttribute("loginUser", loginUser);
        return new ModelAndView("redirect:/home");
    }

    /*
     * ログアウト処理
     */
    @GetMapping("/logout")
    public ModelAndView logout() {
        //セッションのログイン情報を破棄
        session.invalidate();
        //ログイン画面にフォワード
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/login");
        return mav;

    }

}


