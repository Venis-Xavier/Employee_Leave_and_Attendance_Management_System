package com.example.shiftmanagement.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.shiftmanagement.security.FeignClientConfiguration;

@FeignClient(name="employeemanagement",configuration=FeignClientConfiguration.class)
public interface EmployeeManagementClient {
	
	@GetMapping("/employee/employeesUnderManager/managerId/{managerId}")
	public List<Integer> employeesUnderManager(@PathVariable Integer managerId);
	
	@GetMapping("/employee/managerIdfromEmployeeId/{employeeId}")
	public Integer findManagerId(@PathVariable Integer employeeId);
 
}