package com.empleavemanagement.reportsmodule.dto;

import java.sql.Date;
import java.sql.Time;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftReportDTO {

	private int employeeId;
    private Date startDate;
    private Date endDate;
    private Time startTime;
    private Time endTime;
    private String shiftName;
}
