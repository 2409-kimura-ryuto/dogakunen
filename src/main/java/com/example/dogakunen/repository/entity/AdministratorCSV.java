package com.example.dogakunen.repository.entity;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class AdministratorCSV {

    @CsvBindByName(column = "①氏名", required = true)
    private String name;

    @CsvBindByName(column = "②社員番号", required = true)
    private String employeeNumber;

    @CsvBindByName(column = "③所定労働時間", required = true)
    private String prescribedWorkTime;

    @CsvBindByName(column = "④総労働時間", required = true)
    private String totalWorkTime;

    @CsvBindByName(column = "⑤総残業時間", required = true)
    private String totalOverTime;
}
