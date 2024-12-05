package com.example.dogakunen.controller;

import com.example.dogakunen.controller.form.UserForm;
import com.example.dogakunen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class AdminController {
    //システム管理者権限関係のController

    @Autowired
    UserService userService;

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

}
