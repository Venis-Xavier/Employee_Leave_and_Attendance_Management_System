package com.leavemanagement.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.leavemanagement.client.EmployeeManagementClient;
import com.leavemanagement.dao.LeaveRequestDao;
import com.leavemanagement.dto.LeaveBalanceDto;
import com.leavemanagement.dto.LeaveRequestDto;
import com.leavemanagement.entity.LeaveRequest;
import com.leavemanagement.entity.LeaveStatus;
import com.leavemanagement.entity.Role;
import com.leavemanagement.exception.EmployeeNotFoundException;
import com.leavemanagement.exception.InsufficientBalanceException;
import com.leavemanagement.exception.InvalidDateException;
import com.leavemanagement.exception.LeaveRequestAvailable;
import com.leavemanagement.exception.LeaveRequestNotFound;
import com.leavemanagement.utils.ResultResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing leave requests.
 * Provides methods to create, approve/reject, cancel, and fetch leave requests.
 */
@Slf4j
@Service
public class LeaveRequestService {
	
	@Autowired
	private EmployeeManagementClient employeeManagementClient;
	
	@Autowired
	private LeaveBalanceService leaveBalanceService;
	
	@Autowired
	private LeaveRequestDao leaveRequestDao;
	
	
	/**
     * Converts a LeaveRequest entity to LeaveRequestDto.
     * 
     * @param leaveRequest The LeaveRequest entity to convert.
     * @return A LeaveRequestDto object containing the leave request details.
     */
	public LeaveRequestDto convertToLeaveRequestDto(LeaveRequest leaveRequest) {
		LeaveRequestDto leaveRequestDto = new LeaveRequestDto();
		leaveRequestDto.setEmployeeId(leaveRequest.getEmployeeId());
		leaveRequestDto.setLeaveType(leaveRequest.getLeaveType());
		leaveRequestDto.setLeaveStatus(leaveRequest.getLeaveStatus());
		leaveRequestDto.setDaysRequested(leaveRequest.getDaysRequested());
		leaveRequestDto.setStartDate(leaveRequest.getStartDate());
		leaveRequestDto.setEndDate(leaveRequest.getEndDate());
		return leaveRequestDto;
		
	}

	/**
     * Handles the process of requesting leave for an employee.
     * Validates dates, calculates the number of days requested, and saves the leave request.
     * 
     * @param leaveRequestDto The details of the leave request.
     * @return A ResultResponse containing the leave request details and a success message.
     * @throws InvalidDateException If the dates are invalid (e.g., start date after end date).
     * @throws InvalidDateFormatException If the provided dates are in an incorrect format.
     * @throws EmployeeNotFoundException If the employee is not found.
     */
	public ResultResponse<LeaveRequestDto> requestingLeave(LeaveRequestDto leaveRequestDto) {
		LeaveRequest leaveRequest = new LeaveRequest();
		log.info("entered into leave request service");
		//Optional<Employee> e = //employeeService.getEmployeeDetails(leaveRequestDto.getEmployeeId());
			//Employee emp = e.get();
		Date startDate ;
		Date endDate;
		try {
			startDate = Date.valueOf(leaveRequestDto.getStartDate());
			endDate = Date.valueOf(leaveRequestDto.getEndDate());
		}catch(Exception exception) {
			log.error("date format error");
			throw new InvalidDateException("Invalid Date Format - Check your Date Format Again");
		}
		Date today = new Date(System.currentTimeMillis());
		if(startDate.before(today) || endDate.before(today)) {
			log.error("start date or end date is before today's date");
			throw new InvalidDateException("Invalid Date - check your date's properly");
		}
		if(startDate.after(endDate)) {
			log.error("end date is before start date");
			throw new InvalidDateException("Invalid Date - Start Date should be before End Date");
		}
		if(alreadyRequested(leaveRequestDto.getEmployeeId(), leaveRequestDto.getStartDate(), leaveRequestDto.getEndDate())) {
			log.error("Leave already request in the same range of start date and end date");
			throw new LeaveRequestAvailable("Already Leave Request Before with in range of start Date and end date");
		}
		long differenceInMillis = endDate.getTime() - startDate.getTime();
        // Convert milliseconds to days
        long totalDaysRequested = TimeUnit.MILLISECONDS.toDays(differenceInMillis);
        log.info("no of days requested calculated");
        List<LeaveBalanceDto> leavebalance = leaveBalanceService.checkBalanceOfAllTypes(leaveRequestDto.getEmployeeId()).getData();
        for(LeaveBalanceDto lb: leavebalance) {
        	if(lb.getLeaveTypes()==leaveRequestDto.getLeaveType()) {
        		if(lb.getBalance()<=0) {
        			throw new InsufficientBalanceException("Your "+leaveRequestDto.getLeaveType()+" balance is 0");
//	        		}else if(lb.getBalance()<(int)totalDaysRequested) {
//	        			throw new InsufficientBalanceException("Your "+leaveRequestDto.getLeaveType()+" balance is low");
        		}else if(lb.getBalance()<(int)totalDaysRequested) {
        			throw new InsufficientBalanceException("Your balance is too low");
        		}
        	}
        }			
		leaveRequest.setEmployeeId(leaveRequestDto.getEmployeeId());
	    leaveRequest.setStartDate(leaveRequestDto.getStartDate());
        leaveRequest.setEndDate(leaveRequestDto.getEndDate());				
        leaveRequest.setDaysRequested((int)totalDaysRequested);
        leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
        
		leaveRequestDao.save(leaveRequest);
		ResultResponse result=ResultResponse.builder().data(convertToLeaveRequestDto(leaveRequest)).success(true).message("Leave request sent").timeStamp(LocalDateTime.now()).build();
		return result;
	}
	
	/**
     * Approves or rejects a leave request for an employee.
     * Updates the leave balance if the request is approved.
     * 
     * @param employeeId The ID of the employee whose leave request is being handled.
     * @param leaveRequestDto The details of the leave request, including the new status.
     * @return A ResultResponse containing the updated leave request details.
     * @throws EmployeeNotFoundException If the employee is not found.
     * @throws LeaveRequestNotFound If no leave request exists for the employee.
     */
	public ResultResponse<LeaveRequestDto> approveOrRejectLeave(Integer employeeId, LeaveRequestDto leaveRequestDto) {
		LeaveRequest leaveRequest = leaveRequestDao.findByEmployeeId(employeeId);
		if(leaveRequest != null) {
			if(leaveRequest.getLeaveStatus()==LeaveStatus.PENDING){
				leaveRequest.setLeaveStatus(leaveRequestDto.getLeaveStatus());
				if(leaveRequestDto.getLeaveStatus()==LeaveStatus.APPROVED) {
					leaveBalanceService.updateLeaveBalance(employeeId, leaveRequest.getLeaveType(), leaveRequest.getDaysRequested());
				}
			}
		}else {
			log.error("No leaves reqeusted by employee "+ employeeId);
			throw new LeaveRequestNotFound("No leave's request by employee "+employeeId);
		}
		leaveRequestDao.save(leaveRequest);
		ResultResponse result = ResultResponse.builder().data(convertToLeaveRequestDto(leaveRequest)).success(true).message("Leave "+leaveRequestDto.getLeaveStatus()).timeStamp(LocalDateTime.now()).build();
		return result;
	}
	
	/**
     * Fetches all pending leave requests under a specific manager.
     * 
     * @param managerId The ID of the manager.
     * @return A ResultResponse containing the list of leave requests.
     * @throws EmployeeNotFoundException If the manager is not found or not a manager.
     */
	public ResultResponse<List<LeaveRequestDto>> showAllLeaveRequest(Integer managerId){
		log.info("Entered into service ");
		List<Integer> employeesUnderManager = employeeManagementClient.employeesUnderManager(managerId);//employeeService.getEmployeesUnderManager(managerId);
		log.info("employee data is fetched"+employeesUnderManager);
		List<LeaveRequestDto> employeesLeaveRequest = new ArrayList<LeaveRequestDto>();
		log.info("new list is created");
		
		for (Integer e : employeesUnderManager) {
			LeaveRequest leaveRequestOfAnEmployee = leaveRequestDao.findByEmployeeId(e);
			if (leaveRequestOfAnEmployee != null && leaveRequestOfAnEmployee.getLeaveStatus() == LeaveStatus.PENDING) {
	            log.info("employee id:" + e);
	            employeesLeaveRequest.add(convertToLeaveRequestDto(leaveRequestOfAnEmployee));
	        }
		}
		log.info("employee leave data is added to list");
		ResultResponse result = ResultResponse.builder().data(employeesLeaveRequest).success(true).message("All Leave Request under manager "+managerId+" is fetched").timeStamp(LocalDateTime.now()).build();
		return result;
	}
	
	public ResultResponse<List<LeaveRequestDto>> showLeaveRequestsOfEmployee(Integer employeeId){
		ResultResponse result = ResultResponse.builder().data(leaveRequestDao.findAllLeaveRequestOfEmployee(employeeId)).message("Leave Request of employee "+employeeId+" fetched successfully").success(true).timeStamp(LocalDateTime.now()).build();
		return result;
	}
	
    /**
     * Cancels a leave request for an employee.
     * 
     * @param employeeId The ID of the employee requesting cancellation.
     * @param leaveRequestId The ID of the leave request to cancel.
     * @return A ResultResponse containing the canceled leave request details.
     * @throws EmployeeNotFoundException If the employee is not found.
     */
	public ResultResponse<LeaveRequestDto> cancelLeaveRequest(Integer employeeId, Integer leaveRequestId) {
		
		LeaveRequestDto leaveRequestDto = new LeaveRequestDto();
		LeaveRequest leaveRequest = leaveRequestDao.findById(leaveRequestId).get();
		if(leaveRequest.getLeaveStatus()==LeaveStatus.PENDING) {
			leaveRequestDto = convertToLeaveRequestDto(leaveRequest);
			leaveRequestDto.setLeaveStatus(LeaveStatus.CANCELLED);
			leaveRequestDao.delete(leaveRequest);
		}else {
			ResultResponse result = ResultResponse.builder().data(convertToLeaveRequestDto(leaveRequest)).message("Leave Request is already "+leaveRequest.getLeaveStatus()).success(true).timeStamp(LocalDateTime.now()).build();
			return result;
		}
		ResultResponse result = ResultResponse.builder().data(leaveRequestDto).message("Request Deleted Sucessfully").success(true).timeStamp(LocalDateTime.now()).build();
		return result;
	}
	
    /**
     * Retrieves the leave details for an employee within a specific date range.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of LeaveRequestDto objects containing leave details within the specified date range.
     * @throws LeaveRequestNotFound If no leaves are found within the date range.
     */
	public List<LeaveRequestDto> leaveDetailsOfEmployeeInDateRange(LocalDate startDate,  LocalDate endDate){
		log.info("entred into service");
		List<LeaveRequestDto> leaveRequestDetails = new ArrayList<>();
		List<LeaveRequest> leaveDetails = leaveRequestDao.findLeaveDetailsByEmployeeIdAndDates(startDate, endDate);
		if(leaveDetails != null) {
			log.info("data is present");
			//ResultResponse result = ResultResponse.builder().data(leaveDetails.).success(true).message("Leave Details Fetched Successfully").timeStamp(LocalDateTime.now()).build();
			for(LeaveRequest lr : leaveDetails) {
				LeaveRequestDto lrdto = new LeaveRequestDto();
				lrdto.setEmployeeId(lr.getEmployeeId());
				lrdto.setStartDate(lr.getStartDate());
				lrdto.setEndDate(lr.getEndDate());
				lrdto.setLeaveType(lr.getLeaveType());
				lrdto.setLeaveStatus(lr.getLeaveStatus());
				leaveRequestDetails.add(lrdto);
			}
			
			return leaveRequestDetails;
		}else {
			log.error("No leaves requested from "+startDate+" to "+ endDate);
			throw new LeaveRequestNotFound("No leaves requested from "+startDate+" to "+endDate);
		}
	}
 
	
	public boolean alreadyRequested(Integer employeeId, LocalDate startDate, LocalDate endDate) {
		log.info("entered into leave reqeusted function");
		Optional<List<LeaveRequest>> leaveRequests = leaveRequestDao.findAlreadyRequestedLeavesByDates(employeeId, startDate, endDate);
		if(!leaveRequests.get().isEmpty()) {
			System.out.println(leaveRequests.get());
			log.info("Previously requested leave in the same dates");
			return true;
		}
		return false;
	}


}


