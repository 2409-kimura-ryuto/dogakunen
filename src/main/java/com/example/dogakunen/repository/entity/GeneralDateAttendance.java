package com.example.dogakunen.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.time.Duration;
import java.util.Date;

@Entity
@Getter
@Setter

public class GeneralDateAttendance {
    //参照時の使用するentity
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column
    private Date date;

    @Column
    private Integer month;

    @Column
    private Integer attendance;

    @Column(name = "work_time_start")
    private Time workTimeStart;

    @Column(name = "work_time_finish")
    private Time workTimeFinish ;

    //Durationに戻す
    @Column(name = "break_time")
    private String breakTime;

    @Column(name = "work_time")
    private String workTime;

    @Column
    private String memo;

    @Column(name = "created_date", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "updated_date", insertable = true, updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    //内部結合用に追加
    @Column(name = "attendance_status")
    private Integer attendanceStatus;
}
