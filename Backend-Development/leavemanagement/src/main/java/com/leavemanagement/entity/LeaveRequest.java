package com.leavemanagement.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequest {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="LeaveRequestId")
	private Integer leaveRequestId;
	
	@Column(name="EmployeeID")
	private Integer employeeId;
	
	@Enumerated(EnumType.STRING)
	@Column(name="LeaveType")
	private LeaveTypes leaveType;
	
	@Column(name="StartDate")
	private LocalDate startDate;
	
	@Column(name="EndDate")
	private LocalDate endDate;
	
	@Column(name="DaysRequested")
	private int daysRequested;
	
	@Enumerated(EnumType.STRING)
	@Column(name="LeaveStatus")
	private LeaveStatus leaveStatus = LeaveStatus.PENDING;
	

}
