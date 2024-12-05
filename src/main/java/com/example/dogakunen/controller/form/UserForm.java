package com.example.dogakunen.controller.form;

import jakarta.persistence.Transient;
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

    private String name;
    private int employeeNumber;
    private int positionId;
    private int isStopped;
    private Date createdDate;
    private Date updatedDate;

    private String positionName;
}
