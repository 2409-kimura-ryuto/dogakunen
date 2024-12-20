package com.example.dogakunen.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter

public class User {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String password;

    @Column
    private String name;

    @Column(name = "employee_number")
    private String employeeNumber;

    //システム管理画面表示
    //Positionと多対一でリレーションを形成
    @ManyToOne
    @JoinColumn(name="position_id")
    private Position position;

    @Column(name = "is_stopped")
    private int isStopped;

    @Column(name = "created_date", insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "updated_date", insertable = true, updatable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedDate = new Date();

    //DateAttendancesとリレーション形成
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<DateAttendance> dateAttendances;
}
