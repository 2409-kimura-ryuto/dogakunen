package com.example.dogakunen.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.Interval;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;

@Entity
@Table(name = "date_attendances")
@Getter
@Setter

public class DateAttendance {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column
    private Date date;

    @Column
    private Integer month;

    @Column
    private Integer attendance;

    @Column(name = "work_time_start")
    private LocalTime workTimeStart;

    @Column(name = "work_time_finish")
    private LocalTime workTimeFinish ;

    @Column(name = "break_time")
    private String breakTime;

    @Column(name = "work_time")
    private String workTime;

    @Column(name = "memo")
    private String memo;

    @Column(name = "created_date", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "updated_date", insertable = true, updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

}
