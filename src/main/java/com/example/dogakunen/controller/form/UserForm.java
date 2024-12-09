package com.example.dogakunen.controller.form;

import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserForm {
    private int id;
    private String password;

    //パスワード確認用のフィールド
    @Transient
    private String passwordConfirmation;

    @Size(max = 10, message = "・氏名は10文字以下で入力してください")
    private String name;
    private String employeeNumber;
    private int positionId;

    private int isStopped;

    private Date createdDate;

    private Date updatedDate;

    //内部結合用に追加
    private String positionName;
    private Integer month;
    private int attendanceStatus;
}
