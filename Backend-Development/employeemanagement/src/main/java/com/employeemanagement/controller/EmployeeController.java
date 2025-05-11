package com.employeemanagement.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.employeemanagement.dto.EmployeeDto;
import com.employeemanagement.dto.UserDto;
import com.employeemanagement.service.EmployeeService;
import com.employeemanagement.utils.ResultResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller class responsible for handling requests related to login.
 * Provides endpoints to check login for an employee.
 */
@Slf4j
@RestController
@RequestMapping("/employee")
@CrossOrigin(origins = "http://localhost:3000")
public class EmployeeController {
	
	@Autowired
	private EmployeeService employeeService;
	
	
    /**
     * This endpoint handles employee login requests.
     * 
     * @param employeeDto The request body containing login credentials (e.g., username and password).
     * @return A ResponseEntity encapsulating a ResultResponse object that indicates either login success or failure.
     */
	@PostMapping("/login")
	public ResponseEntity<?> checkLogin(@RequestBody EmployeeDto employeeDto) {
		log.info("Entered into login controller");
		ResultResponse<String> employeeLogin = employeeService.verify(employeeDto);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(employeeLogin);
	}
	
	@GetMapping("/employeesUnderManager/managerId/{managerId}")
	public List<Integer> employeesUnderManager(@PathVariable Integer managerId){
		log.info("employeeunder manager in controller ,employee under manager method "+managerId);
		return employeeService.employeesUnderManager(managerId);
	}
	
	@GetMapping("/profile/{employeeId}")
	public ResponseEntity<ResultResponse<?>> getProfileDetails(HttpServletRequest request, @PathVariable Integer employeeId) {
		log.info("employee details method in controller");
		String id = (String) request.getAttribute("employeeId");
		if (id == null) {
	        log.error("Missing employeeId in request attributes");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                .body(ResultResponse.<UserDto>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Missing employee information.")
	                        .data(null)
	                        .build());
	    }

	    if (!Objects.equals(employeeId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, employeeId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<UserDto>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
	    ResultResponse<UserDto> result = employeeService.employeeDetails(employeeId);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}
	
	@GetMapping("/getAllNamesAndIds/{managerId}")
	public Map<Integer,String> getAllNamesAndIds(@PathVariable Integer managerId){
		return employeeService.getAllNamesAndIds(managerId);
	}
	
	@GetMapping("/managerIdfromEmployeeId/{employeeId}")
	public Integer findManagerId(@PathVariable Integer employeeId) {
		return employeeService.findManagerId(employeeId);
	}
	
	@GetMapping("/employeeDetailsUnderManager/{managerId}")
	public ResponseEntity<List<UserDto>> employeeDetailsUnderManager(@PathVariable Integer managerId){
		List<UserDto> employees =  employeeService.employeeDetailsUnderManager(managerId);
		return ResponseEntity.ok().body(employees);
	}
	
	@GetMapping("/getAllEmployeeIds")
	public ResponseEntity<List<Integer>> getAllEmployeeIds(@RequestHeader("Authorization") String token){
	    //if (!jwtService.validateschedulerToken(token)) {
	    //    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
	    //}
		return ResponseEntity.ok().body(employeeService.getAllEmployeeIds());
	}
}