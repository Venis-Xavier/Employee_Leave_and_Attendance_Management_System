package com.example.shiftmanagement.dto;

import java.sql.Date;
import java.sql.Time;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftDto {
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    private String shiftName;
}
