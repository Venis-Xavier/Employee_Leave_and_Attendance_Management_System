package com.leavemanagement.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leavemanagement.dto.LeaveBalanceDto;
import com.leavemanagement.dto.LeaveRequestDto;
import com.leavemanagement.entity.LeaveTypes;
import com.leavemanagement.service.LeaveBalanceService;
import com.leavemanagement.utils.ResultResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
/**
 * Controller class responsible for handling requests related to leave balance operations.
 * Provides endpoints to check leave balance by type and display all leave balances for an employee.
 */
@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/leaveBalance")
public class LeaveBalanceController {
	
	@Autowired
	private LeaveBalanceService leaveBalanceService;
	
	/**
     * Endpoint to check the leave balance of a specific type for an employee.
     * 
     * @param employeeId The unique identifier of the employee.
     * @param leaveType The type of leave being checked (e.g., SICK, CASUAL, PAID).
     * @return ResponseEntity containing the leave balance of the requested type.
     *         - 200 OK if the leave balance is successfully retrieved.
     *         - 400 INTERNAL SERVER ERROR in case of an exception.
     */
	@GetMapping("/checkLeaveBalanceOfEmployee/employeeId/{employeeId}/leaveType/{leaveType}")
	public ResponseEntity<ResultResponse<?>> checkLeaveBalance(HttpServletRequest request, @PathVariable Integer employeeId,@PathVariable LeaveTypes leaveType) {
		log.info("Entered into checkLeaveBalance method in leaveBalance Controller class");
		
		
		ResultResponse<LeaveBalanceDto> leavebalance = leaveBalanceService.checkBalanceOfType(employeeId, leaveType);
		return ResponseEntity.ok().body(leavebalance);
	}
	
	/**
     * Endpoint to display all leave balances for an employee.
     * 
     * @param employeeId The unique identifier of the employee.
     * @return ResponseEntity containing a list of all leave balances for the employee.
     *         - 200 OK if the leave balances are successfully retrieved.
     *         - 500 INTERNAL SERVER ERROR in case of an exception.
     */
	@GetMapping("/showLeaveBalance/employeeId/{employeeId}")
	public ResponseEntity<ResultResponse<?>> showLeaveBalance(HttpServletRequest request,@PathVariable Integer employeeId){
		log.info("Entered into showLeaveBalance method in LeaveBalance controller");
		String id = (String) request.getAttribute("employeeId");
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
		ResultResponse<List<LeaveBalanceDto>> leaveBalance = leaveBalanceService.checkBalanceOfAllTypes(employeeId);
		return ResponseEntity.ok(leaveBalance);
	}
	
	@GetMapping("/showLeaveBalanceOfAllEmployeesUnderManager/{managerId}")
	public ResponseEntity<ResultResponse<?>> checkBalanceOfAllEmployeesUnderManager(HttpServletRequest request,@PathVariable Integer managerId){
		log.info("entered into check balanace of all employees under manager method in controller");
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

	    if (!Objects.equals(managerId.toString(), id)) {
	        log.error("Employee ID mismatch: Logged-in ID = {}, Requested Manager ID = {}", id, managerId);
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(ResultResponse.<List<LeaveRequestDto>>builder()
	                        .timeStamp(LocalDateTime.now())
	                        .success(false)
	                        .message("Unauthorized access. Employee ID mismatch.")
	                        .data(null)
	                        .build());
	    }
		ResultResponse<List<List<LeaveBalanceDto>>> leaveBalances = leaveBalanceService.checkBalanceOfAllEmployeesUnderManager(managerId);
		return ResponseEntity.ok(leaveBalances);
	}
	
	
	/**
	 * This scheduled task resets the leave balance for all employees on the first day of each month.
	 * The method runs automatically at midnight (00:00:00) on the 1st day of every month.
	 * Cron expression: "0 0 0 1 * ?"
	 * - 0 0 0: Indicates execution at 00:00:00 (midnight).
	 * - 1: Specifies the 1st day of the month.
	 * - *: Applies to every month.
	 * - ?: No specific day of the week.
	 */
	@Scheduled(cron=" 0 0 0 1 * ? ")
	public void resetLeaveBalanceMontly() {
		log.info("Leave Balance reset scheduler executed at: "+LocalDateTime.now());
		leaveBalanceService.resetAllLeaveBalance();
	}
}
