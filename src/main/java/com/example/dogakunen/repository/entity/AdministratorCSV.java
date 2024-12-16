package com.example.dogakunen.repository.entity;

import com.opencsv.bean.CsvBindByName;

import lombok.Data;

@Data
public class AdministratorCSV {

    @CsvBindByName(column = "氏名", required = true)
    private String name;

    @CsvBindByName(column = "社員番号", required = true)
    private String employeeNumber;

    @CsvBindByName(column = "総労働時間", required = true)
    private String totalWorkTime;

    @CsvBindByName(column = "総時間外労働", required = true)
    private String totalOverTime;
}
