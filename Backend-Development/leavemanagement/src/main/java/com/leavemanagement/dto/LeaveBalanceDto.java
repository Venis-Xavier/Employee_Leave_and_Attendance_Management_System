package com.leavemanagement.dto;


import com.leavemanagement.entity.LeaveTypes;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceDto {
	
	private Integer employeeId;

	@Enumerated(EnumType.STRING)
	private LeaveTypes leaveTypes;
	
	private int balance;
	

}
