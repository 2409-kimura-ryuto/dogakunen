package com.example.dogakunen.controller.form;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserForm {
    private int id;
    private String password;
    private String name;
    private Integer employeeNumber;
    private int positionId;
    private int isStopped;
    private Date createdDate;
    private Date updatedDate;

    //内部結合用に追加
    private String positionName;
    private Integer month;
    private int attendanceStatus;
}
