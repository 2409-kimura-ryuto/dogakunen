package com.example.dogakunen.repository;

import com.example.dogakunen.repository.entity.DateAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DateAttendanceRepository extends JpaRepository<DateAttendance, Integer> {

}
