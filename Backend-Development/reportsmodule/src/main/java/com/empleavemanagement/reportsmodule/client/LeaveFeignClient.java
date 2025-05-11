package com.empleavemanagement.reportsmodule.client;


import java.time.LocalDate;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.empleavemanagement.reportsmodule.dto.LeaveReportDTO;
import com.empleavemanagement.reportsmodule.security.FeignClientConfiguration;

@FeignClient(name = "leavemanagement",configuration=FeignClientConfiguration.class)
public interface LeaveFeignClient {
	
	@GetMapping("/leaveRequest/leaveDetailsOfEmployeeInDateRange/StartDate/{startDate}/EndDate/{endDate}")
	public ResponseEntity<List<LeaveReportDTO>> leaveDetailsOfEmployeeInDateRange
	(@PathVariable  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,@PathVariable  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
 
}
