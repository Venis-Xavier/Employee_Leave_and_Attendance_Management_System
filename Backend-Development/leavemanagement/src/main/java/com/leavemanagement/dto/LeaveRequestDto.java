package com.leavemanagement.dto;

import java.time.LocalDate;

import com.leavemanagement.entity.LeaveStatus;
import com.leavemanagement.entity.LeaveTypes;

//import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestDto {
	
	private Integer employeeId; 
		 
  @Enumerated(EnumType.STRING)
  private LeaveTypes leaveType;

  private LocalDate startDate;

  private LocalDate endDate;
  
  @Enumerated(EnumType.STRING)
  private LeaveStatus leaveStatus;
  
  private int DaysRequested;
    
 
}