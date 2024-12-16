package com.example.dogakunen.controller.form;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
public class MonthAttendanceForm {
    private int id;
    private int userId;
    private int month;
    private int year;
    private int attendanceStatus;
    private Date createdDate;
    private Date updatedDate;
}