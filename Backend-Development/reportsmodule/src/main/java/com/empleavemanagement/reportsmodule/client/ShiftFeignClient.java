package com.empleavemanagement.reportsmodule.client;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.empleavemanagement.reportsmodule.dto.ShiftReportDTO;
import com.empleavemanagement.reportsmodule.security.FeignClientConfiguration;

@FeignClient(name = "shiftmanagement",configuration=FeignClientConfiguration.class)
public interface ShiftFeignClient {

	@GetMapping("/shiftassignments/shifts")
	public ResponseEntity<List<ShiftReportDTO>> getShiftsByDateRange(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

}
