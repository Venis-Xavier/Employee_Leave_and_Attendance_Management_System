package com.empleavemanagement.reportsmodule.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveReportDTO {
	private Integer employeeId; 
	private String leaveType;
    private Date startDate;
    private Date endDate;
    private String leaveStatus;

}
