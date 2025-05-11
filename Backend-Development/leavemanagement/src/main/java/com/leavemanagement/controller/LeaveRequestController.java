package com.leavemanagement.controller;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leavemanagement.dto.LeaveRequestDto;
import com.leavemanagement.exception.EmployeeNotFoundException;
import com.leavemanagement.service.LeaveRequestService;
import com.leavemanagement.utils.ResultResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller class for handling leave requests.
 * Provides endpoints to create, update, view, and delete leave requests.
 */
@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/leaveRequest")
public class LeaveRequestController {

	@Autowired
	private LeaveRequestService leaveRequestService;
	
	/**
     * Endpoint to send a leave request.
     *
     * @param leaveRequestDto The details of the leave request to be created.
     * @return ResponseEntity containing a ResultResponse object with success or error details.
     *         - 200 OK if the leave request is successfully created.
     *         - 400 BAD REQUEST if there's an error during processing.
     */
	@PostMapping("/sendLeaveRequest")
	public ResponseEntity<ResultResponse<?>> requestingLeave(HttpServletRequest request, @RequestBody LeaveRequestDto leaveRequestDto) {
		log.info("Entered into requestLeave method in LeaveRequest controller");
		String employeeId = (String) request.getAttribute("employeeId");
		
		if (employeeId == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<List<LeaveRequestDto>>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(employeeId, leaveRequestDto.getEmployeeId().toString())) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", employeeId, leaveRequestDto.getEmployeeId().toString());
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<LeaveRequestDto>>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
	    
		ResultResponse<LeaveRequestDto> leaveRequest = leaveRequestService.requestingLeave(leaveRequestDto);
		return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequest);
	}
	
	@GetMapping("/showLeaveRequest/employeeId/{employeeId}")
	public ResponseEntity<ResultResponse<?>> showLeaveRequestOfEmployee(@PathVariable("employeeId") Integer employeeId){
		ResultResponse<List<LeaveRequestDto>> leaveRequest = leaveRequestService.showLeaveRequestsOfEmployee(employeeId);
		return ResponseEntity.status(HttpStatus.OK).body(leaveRequest);
	}
	
	
	
	
	
	
	
	/**
     * Endpoint to approve or reject a leave request.
     *
     * @param employeeId The ID of the employee whose leave is being approved or rejected.
     * @param leaveRequestDto The details of the leave request for approval or rejection.
     * @return ResponseEntity containing a ResultResponse object with success or error details.
     *         - 200 OK if the leave request is successfully processed.
     *         - 400 BAD REQUEST if there's an error during processing.
     */
	@PatchMapping("/leaveApprovalOrReject/employeeId/{employeeId}")
	public ResponseEntity<ResultResponse<?>> approveOrRejectLeave(HttpServletRequest request,@PathVariable Integer employeeId, @RequestBody LeaveRequestDto leaveRequestDto) {
		log.info("Entered into approveOrReject method in LeaveRequest controller");
		ResultResponse<LeaveRequestDto> leaveRequest = leaveRequestService.approveOrRejectLeave(employeeId, leaveRequestDto);
		return ResponseEntity.ok(leaveRequest);
	}
	
	/**
     * Endpoint to view all leave requests assigned to a manager.
     *
     * @param managerId The ID of the manager whose employees' leave requests are being fetched.
     * @return ResponseEntity containing a ResultResponse object with the list of leave requests.
     *         - 200 OK if the leave requests are successfully retrieved.
     *         - 400 BAD REQUEST if there's an error during processing.
     */
	@GetMapping("/getAllLeaveRequest/managerId/{employeeId}")
	public ResponseEntity<ResultResponse<?>> showAllLeaveRequest(HttpServletRequest request, @PathVariable Integer employeeId) {
	    
	    String id = (String) request.getAttribute("employeeId"); // Directly read attribute
	  
		log.info("Logged-in Employee ID: {}", employeeId);
	    log.info("Requested Manager ID: {}", id);


	    if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<List<LeaveRequestDto>>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(employeeId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", employeeId, id);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<LeaveRequestDto>>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }

	    log.info("Fetching all leave requests for manager ID: {}", id);
	    ResultResponse<List<LeaveRequestDto>> leaveRequest = leaveRequestService.showAllLeaveRequest(employeeId);

	    return ResponseEntity.ok(leaveRequest);
	}


	/**
     * Endpoint to cancel a leave request.
     *
     * @param employeeId The ID of the employee who made the leave request.
     * @param leaveRequestId The ID of the leave request to be canceled.
     * @return ResponseEntity containing a ResultResponse object with success or error details.
     *         - 200 OK if the leave request is successfully canceled.
     *         - 400 BAD REQUEST if there's an error during processing.
     */
	@DeleteMapping("/cancelLeaveRequest/employeeId/{employeeId}/leaveRequestId/{leaveRequestId}")
	public ResponseEntity<ResultResponse<?>> cancelLeaveRequest(HttpServletRequest request, @PathVariable Integer employeeId, @PathVariable Integer leaveRequestId) {
		log.info("Entered into cancelLeaveRequest method in LeaveRequest controller");
		String id = (String) request.getAttribute("employeeId");
		if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<List<LeaveRequestDto>>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(employeeId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", employeeId, id);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<LeaveRequestDto>>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
		ResultResponse<LeaveRequestDto> cancelLeaveRequest = leaveRequestService.cancelLeaveRequest(employeeId, leaveRequestId);
		return ResponseEntity.ok(cancelLeaveRequest);
	}
	
	/**
     * Endpoint to retrieve leave requests for an employee within a specific date range.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return List of leave requests (LeaveRequestDto) within the specified date range.
     */
	@GetMapping("/leaveDetailsOfEmployeeInDateRange/StartDate/{startDate}/EndDate/{endDate}")
	public ResponseEntity<List<LeaveRequestDto>> leaveDetailsOfEmployeeInDateRange
	(@PathVariable  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)  LocalDate startDate,
			@PathVariable  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
		log.info("Entered into leaveDetailsOfEmployeeInDateRange method in LeaveRequest controller");
		List<LeaveRequestDto> leaveDetails = leaveRequestService.leaveDetailsOfEmployeeInDateRange(startDate, endDate);
		return ResponseEntity.ok().body(leaveDetails);
	}
 
	
}
