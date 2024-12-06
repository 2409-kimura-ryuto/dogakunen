package com.example.dogakunen.controller.form;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.Interval;

import java.sql.Time;
import java.time.Duration;
import java.util.Date;

@Getter
@Setter
public class DateAttendanceForm {
    private int id;
    private int userId;
    private Date date;
    private int month;
    private int attendance;
    private Time workTimeStart;
    private Time workTimeFinish;
    private Duration breakTime;
    private Duration workTime;
    private String memo;
    private Date createdDate;
    private Date updatedDate;
    private String userName;
    private Integer employeeNumber;
}
