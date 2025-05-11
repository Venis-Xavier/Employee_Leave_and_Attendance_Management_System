package com.example.shiftmanagement.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.shiftmanagement.client.EmployeeManagementClient;
import com.example.shiftmanagement.dto.ShiftDto;
import com.example.shiftmanagement.entity.ShiftAssignments;
import com.example.shiftmanagement.service.ShiftAssignmentsService;
import com.example.shiftmanagement.utils.ResultResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * The ShiftAssignmentsController class is a REST controller responsible for managing 
 * shift assignments for employees. It provides endpoints for assigning shifts, 
 * retrieving shifts by various criteria, and querying shift-related data.
 * 
 * Annotations:
 * - @RestController: Marks this class as a REST controller for handling HTTP requests.
 * - @RequestMapping: Maps HTTP requests to the base path `/shiftassignments`.
 * - @Slf4j: Enables logging for debugging and monitoring the behavior of the controller.
 * 
 * Dependencies:
 * - ShiftAssignmentsService: Injected using @Autowired for managing the business logic.
 */

@Slf4j
@RestController
@RequestMapping("/shiftassignments")
public class ShiftAssignmentsController {
    @Autowired
    private ShiftAssignmentsService shiftAssignmentsService;
    
    @Autowired
    private EmployeeManagementClient employeeManagementClient;
    
    // Assign a shift to an employee
    /**
     * Assigns a shift to an employee.
     * 
     * Endpoint: /shiftassignments/assign/{employeeId}
     * HTTP Method: POST
     * 
     * @param employeeId The ID of the employee to whom the shift will be assigned.
     * @param shift The details of the shift being assigned, provided in the request body.
     * @return ResponseEntity containing the assigned shift wrapped in a ResultResponse object.
     * - Returns 200 OK if the shift is successfully assigned.
     * - Throws a RuntimeException in case of errors during assignment.
     * 
     * Log Statements:
     * - Logs entering and exiting the method with the shift details.
     * - Logs errors encountered during the assignment process.
     */
    
    //only manager uses. security change
    @PostMapping("/assign/{employeeId}")
    public ResponseEntity<ResultResponse<?>> assignShift(HttpServletRequest request,
            @PathVariable Integer employeeId,
            @RequestBody ShiftDto shift) {
    	log.info("enter into controller");
    	Integer managerId = employeeManagementClient.findManagerId(employeeId);
    	log.info("managerid: "+managerId);
    	String id = (String) request.getAttribute("employeeId");
    	if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<ShiftAssignments>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }
 
	    if (!Objects.equals(managerId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, managerId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<ShiftAssignments>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
        log.info("Entering assignShift method with employeeId: {} and shift details: {}", employeeId, shift);
        try {
        	ResultResponse<ShiftAssignments> assignedShift = shiftAssignmentsService.assignShift(employeeId, shift);
            log.info("Exiting assignShift method with assigned shift: {}", assignedShift);
            return ResponseEntity.ok().body(assignedShift);
        } catch (Exception e) {
        	ResultResponse errorrespone= ResultResponse.builder().data(null).message(e.getMessage()).success(false).timestamp(LocalDateTime.now()).build();
            log.error("Error assigning shift for employeeId: {}: {}", employeeId, e.getMessage());
            throw new RuntimeException("Error assigning shift: " + e.getMessage());
        }
    }
    
 // Get all shifts
    /**
     * Retrieves all shift assignments.
     * 
     * Endpoint: /shiftassignments/all
     * HTTP Method: GET
     * 
     * @return ResponseEntity containing the list of all shift assignments.
     * - Returns 200 OK with the list of shifts.
     * 
     * Log Statements:
     * - Logs entering and exiting the method with the number of shifts retrieved.
     */
    @GetMapping("/getShiftAssignmentsOfEmployeesUnderManager/{managerId}")
    public ResponseEntity<ResultResponse<List<ShiftAssignments>>> getAllShifts(HttpServletRequest request, @PathVariable Integer managerId) {
        log.info("Entering getAllShifts method");
        String id = (String) request.getAttribute("employeeId");
        if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<List<ShiftAssignments>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(managerId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, managerId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<ShiftAssignments>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
        
        ResultResponse<List<ShiftAssignments>> shifts = shiftAssignmentsService.getAllShifts(managerId);
        log.info("Exiting getAllShifts method with {} shifts", shifts.getData().size());
        return ResponseEntity.ok().body(shifts);
    }


    // Get assignments by employee ID
    /**
     * Retrieves shift assignments for a specific employee by their ID.
     * 
     * Endpoint: /shiftassignments/employee/{employeeId}
     * HTTP Method: GET
     * 
     * @param employeeId The ID of the employee whose assignments are to be retrieved.
     * @return ResponseEntity containing the optional shift assignments for the employee.
     * - Returns 200 OK with the assignments.
     * 
     * Log Statements:
     * - Logs entering and exiting the method with the retrieved assignments.
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ResultResponse<List<ShiftAssignments>>> getAssignmentsByEmployeeId(HttpServletRequest request, @PathVariable Integer employeeId) {
    	log.info("enterd into service");
    	//Integer managerId = employeeManagementClient.findManagerId(employeeId);
    	//log.info("managerid: "+managerId);
    	String id = (String) request.getAttribute("employeeId");
        if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<List<ShiftAssignments>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(employeeId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, employeeId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<ShiftAssignments>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }

        log.info("Entering getAssignmentsByEmployeeId method with employeeId: {}", employeeId);
        ResultResponse<List<ShiftAssignments>> assignments = shiftAssignmentsService.getShiftAssignmentsByEmployeeId(employeeId);
        log.info("Exiting getAssignmentsByEmployeeId method with assignments: {}", assignments);
        return ResponseEntity.ok().body(assignments);
    }
    
    /**
     * Retrieves shifts within a specific date range.
     * 
     * Endpoint: /shiftassignments/shifts
     * HTTP Method: GET
     * 
     * @param startDate The start date of the date range (ISO format).
     * @param endDate The end date of the date range (ISO format).
     * @return ResponseEntity containing the list of shifts within the specified date range.
     * - Returns 200 OK if shifts are found.
     * - Returns 500 Internal Server Error if an error occurs.
     * 
     * Log Statements:
     * - Logs entering and exiting the method with the number of shifts found.
     * - Logs an error if fetching shifts fails.
     */
    
    @GetMapping("/shifts")///implementation of manager id and employee id seperately
    public ResponseEntity<List<Map<String, Object>>> getShiftsByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("Entering getShiftsByDateRange method with startDate: {}, endDate: {}", startDate, endDate);

        try {
            // Fetch shifts from the service
            List<Map<String, Object>> shifts = shiftAssignmentsService.getShiftsByDateRange(startDate, endDate);

            log.info("Exiting getShiftsByDateRange method with {} shifts found", shifts.size());
            return ResponseEntity.ok().body(shifts);
        } catch (Exception e) {
            log.error("Error fetching shifts for startDate: {} and endDate: {}: {}", startDate, endDate, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
    
    @GetMapping("/employeesundermanager/{managerId}")
    public List<ShiftAssignments> getemployeedataundermanager(@PathVariable Integer managerId){
    	return shiftAssignmentsService.getemployeedataundermanager(managerId);
    }

}