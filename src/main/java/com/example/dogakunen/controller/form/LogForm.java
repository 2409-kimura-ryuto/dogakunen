package com.example.dogakunen.controller.form;

import com.example.dogakunen.repository.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class LogForm {
    private int id;

    private int userId;

    private String operation;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String field;

    private String content;

    private String userName;

    private String employeeNumber;

    private Date createdDate;

    private Date updatedDate;
}
