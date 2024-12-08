package com.example.dogakunen.controller.form;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.Interval;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
public class DateAttendanceForm {
    private int id;
    private int userId;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;
    private int month;
    private int attendance;
    private LocalTime workTimeStart;
    private LocalTime workTimeFinish;
    private LocalTime breakTime;
    private LocalTime workTime;
    private String memo;
    private Date createdDate;
    private Date updatedDate;
    private String userName;
    private Integer employeeNumber;
}
