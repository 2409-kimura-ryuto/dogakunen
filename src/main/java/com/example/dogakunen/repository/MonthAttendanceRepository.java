package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.MonthAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthAttendanceRepository extends JpaRepository<MonthAttendance, Integer> {

}