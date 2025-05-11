package com.leavemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="LeaveBalanceId")
	private Integer leaveBalanceId;
	
	@Column(name="EmployeeId")
	private Integer employeeId;
	
	@Enumerated(EnumType.STRING)
	@Column(name="LeaveType")
	private LeaveTypes leaveType;
	
	@Column(name="LeaveBalance")
	private int balance;


}
