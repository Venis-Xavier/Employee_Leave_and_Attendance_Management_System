package com.employee.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.employee.jwtutil.FeignClientConfiguration;


 
@FeignClient(name="employeemanagement",configuration=FeignClientConfiguration.class)
public interface EmployeeManagementClient {
	@GetMapping("/employee/employeesUnderManager/managerId/{managerId}")
	public List<Integer> employeesUnderManager(@PathVariable Integer managerId);
	
	@GetMapping("/employee/getAllEmployeeIds")
    List<Integer> getAllEmployeeIds();
	//public List<Integer> getAllEmployeeIds();
}
