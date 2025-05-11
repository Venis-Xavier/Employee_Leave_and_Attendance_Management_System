package com.employee.controller;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.employee.dto.AttendanceDto;
import com.employee.dto.AttendanceRecordDto;
import com.employee.entity.AttendanceRecords;
import com.employee.entity.AttendanceRecords.AttendanceStatus;
import com.employee.service.AttendanceService;
import com.employee.utils.ResultResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import com.employee.exception.*;
@RestController
@RequestMapping("/attendance")
@Slf4j
public class AttendanceController {
	@Autowired
	public AttendanceService service;
	
	/**
    * Records an employee's attendance based on their clock-in and clock-out times.
    * 
    * Example request:
    * POST /attendance/giveattendance/{id}
    * 
    * @param id Path variable representing the employee ID.
    * @param attendanceDTO Request body containing clock-in and clock-out times.
    * @return ResultResponse object indicating success or failure of the attendance submission.
    * 
    * Example request body:
    * {
    *     "clockInTime": "2025-03-19T08:00:00",
    *     "clockOutTime": "2025-03-19T18:00:00"
    * }
    * 
    * Possible responses:
    * - 200 OK: Attendance recorded successfully.
    * - 400 Bad Request: Exception (CheckOutBeforeCheckInException, InvalidAttendanceDateException, DuplicateEntryException).
    */
	@PostMapping("/giveattendance/{id}")
	@CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<ResultResponse<?>> postAttendance(HttpServletRequest request,
    	    @RequestBody AttendanceDto attendanceDTO,
    	    @PathVariable Integer id) {
		
		String tokenid = (String) request.getAttribute("employeeId");
		if (tokenid == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<AttendanceRecords>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(id.toString(), tokenid)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", tokenid, id);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<AttendanceRecords>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
		
       
		try {
			
			ResultResponse<AttendanceRecordDto> resultResponse = service.submitAttendance(id, attendanceDTO);
            return ResponseEntity.ok(resultResponse);
        }
        
        catch (CheckOutBeforeCheckInException  | InvalidAttendanceDateException | DuplicateEntryException ex) {
            log.error("Exception caught in Controller: {}", ex.getMessage());
            ResultResponse<Object> errorResponse = ResultResponse.builder()
                    .message(ex.getMessage())
                    .success(false).timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        } 
    }
	
	
	
	
	/**
     * Endpoint to fetch attendance records within a given date range.
     * 
     * Example request:
     * GET /attendance/records?startDate=2025-03-17&endDate=2025-03-19
     * 
     * @param startDate Start date for the range (YYYY-MM-DD format).
     * @param endDate End date for the range (YYYY-MM-DD format).
     * @return List of attendance records within the specified date range.
     */
	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/records")
	public ResponseEntity<List<Map<String, Object>>> getAttendanceRecordsByDateRange(
	        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
	        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
	       ) {

	    log.info("Entered getAttendanceRecordsByDateRange method");

	    // Fetch attendance records from the service
	    List<Map<String, Object>> records = service.getAttendanceWithinDateRange(startDate, endDate);

	    // Return the records wrapped in ResponseEntity
	    return ResponseEntity.ok(records);
	}



}
