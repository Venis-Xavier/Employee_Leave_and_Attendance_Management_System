package com.employee.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRecordDto {
    private int recordId;
    private Integer employeeId;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private double workHours;
    private String status; // Using String for AttendanceStatus
    private LocalDate date;
}
