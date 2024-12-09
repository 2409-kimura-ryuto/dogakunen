package com.example.dogakunen.filter;

import com.example.dogakunen.controller.form.UserForm;
import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApproverFilter implements Filter {

    @Autowired
    HttpSession httpSession;

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        //型変換
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        httpSession = httpRequest.getSession(false);

        UserForm userForm = (UserForm) httpSession.getAttribute("loginUser");
        if (userForm.getPositionId() == 2){
            chain.doFilter(httpRequest,httpResponse);
        } else {
            httpSession = httpRequest.getSession(true);
            //エラーメッセージをセット
            List<String> errorMessages = new ArrayList<>();
            errorMessages.add("無効なアクセスです");
            httpSession.setAttribute("filterErrorMessages", errorMessages);
            //ホーム画面にリダイレクト
            httpResponse.sendRedirect("/");
        }

    }

    @Override
    public void init(FilterConfig config) {
    }

    @Override
    public void destroy() {
    }
}