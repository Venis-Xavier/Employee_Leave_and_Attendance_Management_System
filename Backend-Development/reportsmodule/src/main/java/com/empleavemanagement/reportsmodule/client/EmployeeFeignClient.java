package com.empleavemanagement.reportsmodule.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.empleavemanagement.reportsmodule.security.FeignClientConfiguration;


@FeignClient(name = "employeemanagement",configuration=FeignClientConfiguration.class)
public interface EmployeeFeignClient {
	
	@GetMapping("/employee/getAllNamesAndIds/{managerId}")
	public Map<Integer,String> getAllNamesAndIds(@PathVariable Integer managerId);
}