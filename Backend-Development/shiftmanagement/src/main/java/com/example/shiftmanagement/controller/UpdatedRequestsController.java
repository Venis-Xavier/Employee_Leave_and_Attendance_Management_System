package com.example.shiftmanagement.controller;

import com.example.shiftmanagement.entity.ShiftRequest;
import com.example.shiftmanagement.entity.UpdatedRequests;
import com.example.shiftmanagement.service.UpdatedRequestsService;
import com.example.shiftmanagement.utils.ResultResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@CrossOrigin(origins="http://localhost:3000/")
@RequestMapping("/updatedrequests")
public class UpdatedRequestsController {

    private final UpdatedRequestsService updatedRequestsService;

    public UpdatedRequestsController(UpdatedRequestsService updatedRequestsService) {
        this.updatedRequestsService = updatedRequestsService;
    }

    // API to get shift history for an employee
    // employee
    @GetMapping("/{employeeId}")
    public ResponseEntity<ResultResponse<List<UpdatedRequests>>> getUpdatedRequests(HttpServletRequest request, @PathVariable Integer employeeId) {
    	log.info("entered into updatedrequest controller");
    	String id = (String) request.getAttribute("employeeId");
        if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<List<UpdatedRequests>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(employeeId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, employeeId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<UpdatedRequests>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
        ResultResponse<List<UpdatedRequests>> updatedRequests = updatedRequestsService.getUpdatedRequestsByEmployeeId(employeeId);
        return ResponseEntity.ok(updatedRequests);
    }
    
    //manager
    @GetMapping("/managerId/{managerId}")
    public ResponseEntity<ResultResponse<List<List<UpdatedRequests>>>> getUpdatedRequestsofEmployees(HttpServletRequest request, @PathVariable Integer managerId){
    	String id = (String) request.getAttribute("employeeId");
        if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<List<List<UpdatedRequests>>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(managerId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, managerId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<List<UpdatedRequests>>>builder()
	                        .timestamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
    	ResultResponse<List<List<UpdatedRequests>>> updatedRequestsofemployee = updatedRequestsService.getUpdatedRequestsofEmployees(managerId);
    	return ResponseEntity.ok(updatedRequestsofemployee);
    }
    
    
}	
