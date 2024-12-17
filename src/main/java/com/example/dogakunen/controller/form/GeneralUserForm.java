package com.example.dogakunen.controller.form;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class GeneralUserForm {
    private int id;
    private String password;
    private String name;
    private Integer employeeNumber;
    private Integer positionId;
    private int isStopped;
    private Date createdDate;
    private Date updatedDate;

    //内部結合用に追加
    private String positionName;
    private Integer month;
    private int attendanceStatus;
}
