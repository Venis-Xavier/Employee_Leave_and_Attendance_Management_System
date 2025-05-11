package com.empleavemanagement.reportsmodule.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceReportDTO {
    private int employeeId;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private double workHours;
    private String status;
    private LocalDate date;
}