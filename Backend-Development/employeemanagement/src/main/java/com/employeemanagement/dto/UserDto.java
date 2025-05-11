package com.employeemanagement.dto;

import com.employeemanagement.entity.Gender;
import com.employeemanagement.entity.Role;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

	private Integer employeeId;
	
	private Integer managerId;
	
	private String firstName;
	
	private String lastName;
	
	@Enumerated(EnumType.STRING)
	private Gender gender;
	
	private String email;
	
	private String department;
		
	@Enumerated(EnumType.STRING)
	private Role role;
	
}
