package com.example.dogakunen.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.Interval;

import java.sql.Time;
import java.util.Date;

@Entity
@Table(name = "month_attendances")
@Getter
@Setter

public class MonthAttendance {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column
    private Integer month;

    @Column(name = "attendance_status")
    private Integer attendanceStatus;

    @Column(name = "created_date", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "updated_date", insertable = true, updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    @PreUpdate
    public  void  onPreUpdate () {
        Date date = new Date();
        this .setUpdatedDate(date);
    }

}
