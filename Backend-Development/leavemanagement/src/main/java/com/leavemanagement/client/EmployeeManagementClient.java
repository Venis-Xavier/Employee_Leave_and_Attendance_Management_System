package com.leavemanagement.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.leavemanagement.security.FeignClientConfiguration;


@FeignClient(name="employeemanagement",configuration=FeignClientConfiguration.class)
public interface EmployeeManagementClient {
	
	@GetMapping("/employee/employeesUnderManager/managerId/{managerId}")
	public List<Integer> employeesUnderManager(@PathVariable Integer managerId);
	

}
