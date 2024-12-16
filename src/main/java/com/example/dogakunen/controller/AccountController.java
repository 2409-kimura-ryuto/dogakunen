package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.UserService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.dogakunen.controller.AttendanceController.accessDate;

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
        // 画面遷移先を指定
        mav.setViewName("/login");
        //エラーメッセージ表示
        List<String> errorMessage = (List<String>) session.getAttribute("errorMessages");
        String employeeNumber = (String) session.getAttribute("employeeNumber");
        if (errorMessage != null) {
            mav.addObject("errorMessages", errorMessage);
            mav.addObject("employeeNumber", employeeNumber);
            session.invalidate();
        }

        //ログインフィルターのエラーメッセージをmavに詰めてセッション削除
        List<String> filterErrorMessages = (List<String>) session.getAttribute("filterErrorMessages");
        mav.addObject("filterErrorMessages", filterErrorMessages);
        session.removeAttribute("filterErrorMessages");

        return mav;
    }

    /*
     * ログイン処理
     */
    @GetMapping("/loginUser")
    public ModelAndView login(@RequestParam(name = "employeeNumber", required = false) String employeeNumber,
                              @RequestParam(name = "password", required = false) String password) {
        //バリデーション
        //エラーメッセージの準備
        List<String> errorMessages = new ArrayList<String>();
        //社員番号入力チェック
        if (employeeNumber.isBlank()) {
            errorMessages.add("・社員番号を入力してください");
        }
        //パスワード入力チェック
        if (password.isBlank()) {
            errorMessages.add("・パスワードを入力してください");
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
            errorMessages.add("・ログインに失敗しました");
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
        return new ModelAndView("redirect:/");
    }

    /*
     * ログアウト処理
     */
    @GetMapping("/logout")
    public ModelAndView logout() {
        //セッションのログイン情報を破棄
        session.invalidate();

        //【追加】
        accessDate = new Date();

        //ログイン画面にフォワード
        ModelAndView mav = new ModelAndView();
        mav.setViewName("/login");
        return mav;

    }

    /*
     * 設定画面表示
     */
    @GetMapping("/setting")
    public ModelAndView setting() {
        ModelAndView mav = new ModelAndView();
        // 画面遷移先を指定
        mav.setViewName("/setting");
        //社員番号を表示
        //セッションからログインユーザ情報を取得
        UserForm loginUser = (UserForm)session.getAttribute("loginUser");
        //ログインユーザ情報から社員番号のみを取り出す
        String employeeNumber = loginUser.getEmployeeNumber();
        //画面に社員番号をセット
        mav.addObject("employeeNumber", employeeNumber);
        //エラーメッセージ表示
        //セッションからエラーメッセージを取得
        List<String> errorMessage = (List<String>)session.getAttribute("errorMessages");
        //エラーメッセージが存在した場合
        if (errorMessage != null) {
            //画面にエラーメッセージをセット
            mav.addObject("errorMessages", errorMessage);
            //セッションからエラーメッセージを削除
            session.removeAttribute("errorMessages");
        }
        return mav;
    }

    /*
     * パスワード変更処理
     */
    @PutMapping("/settingProcess")
    public ModelAndView settingProcess(@RequestParam(name = "password", required = false) String password, @RequestParam(name = "passwordConfirmation", required = false) String passwordConfirmation) {
        ModelAndView mav = new ModelAndView();
        //エラーメッセージの準備
        List<String> errorMessages = new ArrayList<String>();
        //バリデーション
        //パスワードと確認用パスワードが一致しない場合
        if(!password.equals(passwordConfirmation)) {
            //エラーメッセージを設定
            errorMessages.add("・パスワードと確認用パスワードが一致しません");
        }
        //入力されたパスワードが空欄または記号を含む半角文字6文字以上20文字以下でない場合
        if(!password.matches("^[!-~]{6,20}$") || StringUtils.isBlank(password)){
            //エラーメッセージを設定
            errorMessages.add("・パスワードは6文字以上20文字以下で入力してください");
        }
        //エラーメッセージが存在する場合
        if(errorMessages.size() != 0){
            //セッションにエラーメッセージを設定
            session.setAttribute("errorMessages", errorMessages);
            //設定画面にリダイレクト
            return new ModelAndView("redirect:/setting");
        }
        //パスワードの変更処理
        //リクエストから取得したパスワードの暗号化
        String encodedPwd = BCrypt.hashpw(password, BCrypt.gensalt());
        //セッションからログインユーザ情報を取得
        UserForm loginUser = (UserForm)session.getAttribute("loginUser");
        //ログインユーザ情報のパスワードを入力されたパスワードに変更
        loginUser.setPassword(encodedPwd);
        //変更後のloginUserを引数にDBをupdate
        userService.saveUser(loginUser);

        //画面遷移先を指定
        return new ModelAndView("redirect:/");
    }

}


