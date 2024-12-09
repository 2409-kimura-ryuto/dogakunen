package com.example.dogakunen.filter;

import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginFilter implements Filter {

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

        if (httpSession != null && httpSession.getAttribute("loginUser") != null){
            chain.doFilter(httpRequest,httpResponse);
        } else {
            httpSession = httpRequest.getSession(true);
            //エラーメッセージをセット
            List<String> errorMessages = new ArrayList<>();
            errorMessages.add("ログインしてください");
            httpSession.setAttribute("filterErrorMessages", errorMessages);
            //ログイン画面にリダイレクト
            httpResponse.sendRedirect("/login");
        }

    }

    @Override
    public void init(FilterConfig config) {
    }

    @Override
    public void destroy() {
    }
}