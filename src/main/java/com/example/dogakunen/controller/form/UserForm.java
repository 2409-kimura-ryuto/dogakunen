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
    private int employeeNumber;
    private int positionId;
    private int isStopped;
    private Date createdDate;
    private Date updatedDate;
}
