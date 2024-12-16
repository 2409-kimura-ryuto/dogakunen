package com.example.dogakunen.controller.form;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.Interval;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.TemporalAmount;
import java.util.Date;

@Getter
@Setter
public class DateAttendanceForm {
    private int id;

    private int userId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private int month;

    private int year;

    private int attendance;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime workTimeStart;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime workTimeFinish;

    private String breakTime;

    private String workTime;

    @Size(max = 60, message = "・60文字以下で入力してください")
    private String memo;

    private Date createdDate;

    private Date updatedDate;

    private String userName;

    private String employeeNumber;
}
