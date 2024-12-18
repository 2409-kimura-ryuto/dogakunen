package com.example.dogakunen.controller.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class DateAttendanceListForm {
    @Valid
    private List<Attendance> attendances;

    @Getter
    @Setter
    public static class Attendance {
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

        private String memo;

        private Date createdDate;

        private Date updatedDate;

        private String userName;

        private String employeeNumber;
    }
}
