package com.example.shiftmanagement.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.shiftmanagement.client.EmployeeManagementClient;
import com.example.shiftmanagement.dto.ShiftRequestDto;
import com.example.shiftmanagement.service.ShiftAssignmentsService;
import com.example.shiftmanagement.service.ShiftRequestService;
import com.example.shiftmanagement.utils.ResultResponse;

import jakarta.servlet.http.HttpServletRequest;

import com.example.shiftmanagement.entity.ShiftAssignments;
import com.example.shiftmanagement.entity.ShiftRequest;
import com.example.shiftmanagement.entity.ShiftRequest.RequestStatus;
import lombok.extern.slf4j.Slf4j;

/**
 * The ShiftRequestController class is a REST controller responsible for managing 
 * shift requests for employees. It provides endpoints for creating, retrieving, 
 * and updating shift requests. It interacts with both the ShiftRequestService 
 * and ShiftAssignmentsService to handle business logic.
 * 
 * Annotations:
 * - @RestController: Marks this class as a REST controller for handling HTTP requests.
 * - @RequestMapping: Maps HTTP requests to the base path `/shift-requests`.
 * - @Slf4j: Enables logging for debugging and monitoring the behavior of the controller.
 * 
 * Dependencies:
 * - ShiftRequestService: Handles the business logic for shift requests.
 * - ShiftAssignmentsService: Handles updates related to shift assignments.
 */
@Slf4j
@RestController
@CrossOrigin(origins="http://localhost:3000/")
@RequestMapping("/shift-requests")
public class ShiftRequestController {
    @Autowired
    private ShiftRequestService shiftRequestService;
    
    @Autowired
    private ShiftAssignmentsService shiftAssignmentsService;
    
    @Autowired
    private EmployeeManagementClient employeeManagementClient;

    // Create a shift request for an employee
    /**
     * Creates a shift request for a specific employee.
     * 
     * Endpoint: /shift-requests/create/{employeeId}
     * HTTP Method: POST   
     * 
     * @param employeeId The ID of the employee for whom the shift request is created.
     * @param request The shift request details, provided in the request body.
     * @return ResponseEntity containing the created shift request wrapped in a ResultResponse object.
     * - Returns 200 OK if the request is successfully created.
     * - Throws a RuntimeException in case of errors during creation.
     * 
     * Log Statements:
     * - Logs entering and exiting the method with request details.
     * - Logs errors encountered during the creation process.
     */// employee access chestdu
    @PostMapping("/create/{employeeId}")
    public ResponseEntity<ResultResponse<ShiftRequest>> createShiftRequest(HttpServletRequest hrequest, @PathVariable Integer employeeId, @RequestBody ShiftRequestDto request) {
        log.info("Entering createShiftRequest method with employeeId: {} and request: {}", employeeId, request);
        String id = (String) hrequest.getAttribute("employeeId");
        if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<ShiftRequest>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(employeeId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, employeeId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<ShiftRequest>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
        try {
            ResultResponse<ShiftRequest> shiftRequest = shiftRequestService.createShiftRequest(employeeId, request);
            log.info("Exiting createShiftRequest method with shiftRequest: {}", shiftRequest);
            return ResponseEntity.ok().body(shiftRequest);
        } catch (Exception e) {
            log.error("Error creating shift request for employeeId: {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Error creating shift request: " + e.getMessage());
        }
    }

    // Get all shift requests by employee ID
    /**
     * Retrieves all shift requests for a specific employee.
     * 
     * Endpoint: /shift-requests/employee/{employeeId}
     * HTTP Method: GET
     * 
     * @param employeeId The ID of the employee whose shift requests are to be retrieved.
     * @return ResponseEntity containing the shift requests wrapped in a ResultResponse object.
     * - Returns 200 OK with the employee's shift requests.
     * - Throws a RuntimeException in case of errors during retrieval.
     * 
     * Log Statements:
     * - Logs entering and exiting the method with the retrieved shift requests.
     * - Logs errors encountered during the retrieval process.
     *///manager access chestadu
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ResultResponse<ShiftRequest>> getRequestsByEmployeeId(HttpServletRequest request, @PathVariable Integer employeeId) {
        log.info("Entering getRequestsByEmployeeId method with employeeId: {}", employeeId);
        String id = (String) request.getAttribute("employeeId");
        if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<ShiftRequest>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(employeeId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, employeeId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<ShiftRequest>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
        try {
            ResultResponse<ShiftRequest> shiftRequest = shiftRequestService.getRequestsByEmployeeId(employeeId);
            log.info("Exiting getRequestsByEmployeeId method with shiftRequest: {}", shiftRequest);
            return ResponseEntity.ok().body(shiftRequest);
        } catch (Exception e) {
            log.error("Error fetching shift requests for employeeId: {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Error fetching shift requests: " + e.getMessage());
        }
    }

    // Get all shift requests
    /**
     * Retrieves all shift requests.
     * 
     * Endpoint: /shift-requests/all
     * HTTP Method: GET
     * 
     * @return ResponseEntity containing all shift requests wrapped in a ResultResponse object.
     * - Returns 200 OK with the list of shift requests.
     * 
     * Log Statements:
     * - Logs entering and exiting the method with the number of shift requests retrieved.
     */ // manager accessed
    @GetMapping("/all/{managerId}")
    public ResponseEntity<ResultResponse<List<ShiftRequest>>> getAllShifts(HttpServletRequest request, @PathVariable Integer managerId) {
        log.info("Entering getAllShifts method");
        String id = (String) request.getAttribute("employeeId");
        log.info("id: "+id);
        if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<List<ShiftRequest>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(managerId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, managerId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<ShiftRequest>>builder(	)
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
        ResultResponse<List<ShiftRequest>> shifts = shiftRequestService.getAllShifts(managerId);
        log.info("Exiting getAllShifts method with {} shift requests", shifts.getData().size());
        return ResponseEntity.ok().body(shifts);
    }

    // Approve or reject a shift request
    /**
     * Approves or rejects a shift request for a specific employee.
     * 
     * Endpoint: /shift-requests/shiftApprovalOrReject/{employeeId}
     * HTTP Method: PUT
     * 
     * @param employeeId The ID of the employee whose shift request is being updated.
     * @param shiftRequestDto The shift request details including the approval/rejection status.
     * @return ResponseEntity containing the updated shift request wrapped in a ResultResponse object.
     * - Returns 200 OK if the request is successfully updated.
     * - Throws a RuntimeException in case of errors during the update process.
     * 
     * Additional Operations:
     * - Updates the shift assignments if the request is approved.
     * - Removes related shift assignments if the request is rejected.
     * 
     * Log Statements:
     * - Logs entering and exiting the method with shift request details.
     * - Logs errors encountered during the approval/rejection process.
     */ 
    
    //manager access- only
    @PutMapping("/shiftApprovalOrReject/{employeeId}")
    public ResponseEntity<ResultResponse<ShiftRequest>> approveOrRejectLeave( HttpServletRequest request,
            @PathVariable Integer employeeId, 
            @RequestBody ShiftRequestDto shiftRequestDto) throws Exception {
        log.info("Entering approveOrRejectLeave method with employeeId: {} and shiftRequestDto: {}", employeeId, shiftRequestDto);
        
        Integer managerId = employeeManagementClient.findManagerId(employeeId);
    	log.info("managerid: "+managerId);
    	String id = (String) request.getAttribute("employeeId");
        if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<ShiftRequest>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(managerId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, managerId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<ShiftRequest>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
        
        try {
            ResultResponse<ShiftRequest> shiftRequest = shiftRequestService.approveOrRejectLeave(employeeId, shiftRequestDto);
            if(shiftRequest.getData().getStatus()==RequestStatus.APPROVED)
            	shiftAssignmentsService.updatetable(employeeId);
            else if(shiftRequest.getData().getStatus()==RequestStatus.REJECTED)
            	shiftAssignmentsService.deletetable(employeeId);
            log.info("Exiting approveOrRejectLeave method with shiftRequest: {}", shiftRequest);
            return ResponseEntity.ok().body(shiftRequest);
        } catch (Exception e) {
            log.error("Error approving or rejecting leave for employeeId: {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Error approving or rejecting leave: " + e.getMessage());
        }
    }

}
