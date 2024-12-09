package com.example.dogakunen.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    //ログインフィルター
    @Bean
    public FilterRegistrationBean<LoginFilter> loginFilter() {
        FilterRegistrationBean<LoginFilter> bean = new FilterRegistrationBean<>();

        bean.setFilter(new LoginFilter());
        //ホーム画面、勤怠登録画面、勤怠編集画面、設定画面にフィルターを設定
        bean.addUrlPatterns("/home");
        bean.addUrlPatterns("/newAttendance");
        bean.addUrlPatterns("/editAttendance/*");
        bean.addUrlPatterns("/setting");
        bean.setOrder(1);
        return bean;
    }

    //承認者権限フィルター
    @Bean
    public FilterRegistrationBean<ApproverFilter> approverFilter() {
        FilterRegistrationBean<ApproverFilter> bean = new FilterRegistrationBean<>();

        bean.setFilter(new ApproverFilter());
        //承認対象者一覧画面、勤怠状況確認画面にフィルターを設定
        bean.addUrlPatterns("/show_users");
        bean.addUrlPatterns("/check_attendance/*");
        bean.setOrder(2);
        return bean;
    }

    //システム管理者権限フィルター
    @Bean
    public FilterRegistrationBean<AdminFilter> adminFilter() {
        FilterRegistrationBean<AdminFilter> bean = new FilterRegistrationBean<>();

        bean.setFilter(new AdminFilter());
        //システム管理画面、アカウント登録画面、アカウント編集画面にフィルターを設定
        bean.addUrlPatterns("/systemManage");
        bean.addUrlPatterns("/newUser");
        bean.addUrlPatterns("/editUser/*");
        bean.setOrder(3);
        return bean;
    }
}