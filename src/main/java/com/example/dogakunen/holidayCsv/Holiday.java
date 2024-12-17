package com.example.dogakunen.holidayCsv;

import java.time.LocalDate;

public class Holiday {
    private LocalDate date;
    private String name;

    public Holiday(LocalDate date, String name) {
        this.date = date;
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }
}
