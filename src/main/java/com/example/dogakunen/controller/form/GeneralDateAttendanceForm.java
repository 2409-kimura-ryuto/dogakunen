package com.example.dogakunen.controller.form;

import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.time.Duration;
import java.util.Date;

@Getter
@Setter
public class GeneralDateAttendanceForm {
    private int id;
    private int userId;
    private Date date;
    private int month;
    private int attendance;
    private Time workTimeStart;
    private Time workTimeFinish;
    //Durationに戻す
    private Time breakTime;
    private Time workTime;
    private String memo;
    private Date createdDate;
    private Date updatedDate;

    //内部結合用に追加
//    private Integer attendanceStatus;
}
