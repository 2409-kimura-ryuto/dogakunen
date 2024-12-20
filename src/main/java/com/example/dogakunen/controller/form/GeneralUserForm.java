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

    @Column
    private String password;

    @Column
    private String name;

    @Column(name = "employee_number")
    private Integer employeeNumber;

    @Column(name = "position_id")
    private Integer positionId;

    @Column(name = "is_stopped")
    private int isStopped;

    @Column(name = "created_date", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "updated_date", insertable = true, updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate;

    //内部結合用に追加
    @Column(name = "position_name")
    private String positionName;

    private Integer month;

    @Column(name = "attendance_status")
    private int attendanceStatus;
}
