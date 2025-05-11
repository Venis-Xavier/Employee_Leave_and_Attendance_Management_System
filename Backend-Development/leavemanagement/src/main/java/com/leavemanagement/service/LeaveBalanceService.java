package com.leavemanagement.service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leavemanagement.client.EmployeeManagementClient;
import com.leavemanagement.dao.LeaveBalanceDao;
import com.leavemanagement.dto.LeaveBalanceDto;
import com.leavemanagement.entity.LeaveBalance;
import com.leavemanagement.entity.LeaveTypes;
import com.leavemanagement.exception.EmployeeNotFoundException;
import com.leavemanagement.utils.ResultResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing leave balance operations.
 * Provides methods to check, update, and add leave balances for employees.
 */
@Slf4j
@Service
public class LeaveBalanceService {

	    @Autowired
	    private LeaveBalanceDao leaveBalanceDao;
	    
	    @Autowired
	    private EmployeeManagementClient employeeManagementClient;

	    /**
	     * Converts a LeaveBalance entity to LeaveBalanceDto.
	     * 
	     * @param leaveBalance The LeaveBalance entity to convert.
	     * @return A LeaveBalanceDto object containing the converted data.
	     */
	    public LeaveBalanceDto convertToLeaveBalanceDto(LeaveBalance leaveBalance) {
	    	LeaveBalanceDto leavebalancedto = new LeaveBalanceDto();
	    	leavebalancedto.setEmployeeId(leaveBalance.getEmployeeId());
	    	leavebalancedto.setLeaveTypes(leaveBalance.getLeaveType());
	    	leavebalancedto.setBalance(leaveBalance.getBalance());
	    	return leavebalancedto;
	    }
	    
		
		/**
	     * Checks the leave balance of all types for a specific employee.
	     * If balances do not exist, default leave balances are added.
	     * 
	     * @param employeeId The unique identifier of the employee.
	     * @return A list of LeaveBalanceDto objects representing all leave types for the employee.
	     * @throws EmployeeNotFoundException If the employee is not found.
	     */
		public ResultResponse<List<LeaveBalanceDto>> checkBalanceOfAllTypes(Integer employeeId) {
			log.info("eneterd into service class");
			List<LeaveBalanceDto> result = new ArrayList<>();
			List<LeaveBalance> allLeaveTypesBalance = leaveBalanceDao.findByEmployeeId(employeeId);
			
			if(allLeaveTypesBalance.isEmpty()) {
				allLeaveTypesBalance.add(addDefaultLeaveBalance(employeeId, LeaveTypes.SICK_LEAVE));
				allLeaveTypesBalance.add(addDefaultLeaveBalance(employeeId, LeaveTypes.PAID_LEAVE));
				allLeaveTypesBalance.add(addDefaultLeaveBalance(employeeId, LeaveTypes.CASUAL_LEAVE));
			}else if(allLeaveTypesBalance.size()<3) {
				boolean sickleaveunavailable = true;
				boolean paidleaveunavailable = true;
				boolean casualleaveunavailable = true;
				
				for(LeaveBalance lb : allLeaveTypesBalance) {
					if(lb.getLeaveType().equals(LeaveTypes.SICK_LEAVE)) {
						sickleaveunavailable = false;
					}else if(lb.getLeaveType().equals(LeaveTypes.CASUAL_LEAVE)) {
						casualleaveunavailable = false;
					}else if(lb.getLeaveType().equals(LeaveTypes.PAID_LEAVE)) {
						paidleaveunavailable = false;
					}
				}
				
				if(sickleaveunavailable) {
					allLeaveTypesBalance.add(addDefaultLeaveBalance(employeeId, LeaveTypes.SICK_LEAVE));
				}
				if(paidleaveunavailable) {
					allLeaveTypesBalance.add(addDefaultLeaveBalance(employeeId, LeaveTypes.PAID_LEAVE));
				}
				if(casualleaveunavailable) {
					allLeaveTypesBalance.add(addDefaultLeaveBalance(employeeId, LeaveTypes.CASUAL_LEAVE));
				}
			}
	
			leaveBalanceDao.saveAll(allLeaveTypesBalance);
			for(LeaveBalance lb : allLeaveTypesBalance) {
				result.add(convertToLeaveBalanceDto(lb));
			}
			ResultResponse output=ResultResponse.builder().data(result).success(true).message("Leave Balance is Fetched.").timeStamp(LocalDateTime.now()).build();
			return output;
			//return result;
		}

		/**
	     * Checks the leave balance of a specific type for an employee.
	     * If the balance does not exist, a default leave balance is added.
	     * 
	     * @param employeeId The unique identifier of the employee.
	     * @param leaveType The type of leave to check.
	     * @return A LeaveBalanceDto object representing the leave balance for the requested type.
	     * @throws EmployeeNotFoundException If the employee is not found.
	     */
		public ResultResponse<LeaveBalanceDto> checkBalanceOfType(Integer employeeId, LeaveTypes leaveType) {
			
			LeaveBalance balanceDetails = leaveBalanceDao.findByEmployeeIdAndLeaveType(employeeId, leaveType);
			if(balanceDetails == null) {
				balanceDetails = addDefaultLeaveBalance(employeeId, leaveType);
				leaveBalanceDao.save(balanceDetails);
			}
			//return convertToLeaveBalanceDto(balanceDetails);
			ResultResponse result=ResultResponse.builder().data(convertToLeaveBalanceDto(balanceDetails)).success(true).message("Leave Balance is Fetched.").timeStamp(LocalDateTime.now()).build();
			return result;
		}
		
		public ResultResponse<List<List<LeaveBalanceDto>>> checkBalanceOfAllEmployeesUnderManager(Integer managerId){
			
			log.info("Entered into check balance of all employees under manager in service");
			List<List<LeaveBalanceDto>> resultList = new ArrayList<>();
			List<Integer> employeeIds = employeeManagementClient.employeesUnderManager(managerId);
			for(Integer id: employeeIds) {
				resultList.add(checkBalanceOfAllTypes(id).getData());
			}
			ResultResponse result=ResultResponse.builder().data(resultList).success(true).message("Leave Balance of all employees is Fetched.").timeStamp(LocalDateTime.now()).build();
			return result;
		}
		
		/**
	     * Updates the leave balance of a specific type for an employee.
	     * Subtracts the requested number of days from the existing balance.
	     * 
	     * @param employeeId The unique identifier of the employee.
	     * @param leaveType The type of leave to update.
	     * @param daysRequested The number of leave days requested.
	     * @throws EmployeeNotFoundException If the employee is not found.
	     */
		public void updateLeaveBalance(Integer employeeId, LeaveTypes leaveType, int daysRequested) {
			LeaveBalance leaveBalanceDetails = leaveBalanceDao.findByEmployeeIdAndLeaveType(employeeId, leaveType);
			if(leaveBalanceDetails == null) {
				leaveBalanceDetails = addDefaultLeaveBalance(employeeId, leaveType);
			}
			if(leaveBalanceDetails.getBalance()>daysRequested) {
				leaveBalanceDetails.setBalance(leaveBalanceDetails.getBalance()-daysRequested);
			}else if(leaveBalanceDetails.getBalance()-daysRequested<=0) {
				leaveBalanceDetails.setBalance(0);
			}
			
			leaveBalanceDao.save(leaveBalanceDetails);
		}
		
		/**
	     * Adds a default leave balance for a specified employee and leave type.
	     * 
	     * @param employeeId The unique identifier of the employee.
	     * @param leaveType The type of leave to add.
	     * @return A LeaveBalance object representing the default leave balance.
	     */
		public LeaveBalance addDefaultLeaveBalance(Integer employeeId, LeaveTypes leaveType) {
			LeaveBalance lb = new LeaveBalance();
			lb.setEmployeeId(employeeId);
			lb.setBalance(leaveType.getDefaultDays());
			lb.setLeaveType(leaveType);
			return lb;
		}
		
		/**
		 * This method resets the leave balance for all employees to the default value
		 * specified in their leave type. It operates within a transactional boundary to
		 * ensure that all operations either complete successfully or roll back in case of an error.
		 * 
		 * The @Transactional annotation ensures:
		 * - Atomicity: All operations inside the method are treated as a single unit of work.
		 * - Rollback: If any operation fails, changes made within this method are reverted.
		 */
		@Transactional
		public void resetAllLeaveBalance() {
			log.info("Leave Balance Reset process initiated");
			List<LeaveBalance> leaveBalanceDetails = leaveBalanceDao.findAll();
			for(LeaveBalance leave : leaveBalanceDetails) {
				leave.setBalance(leave.getLeaveType().getDefaultDays());
			}
			leaveBalanceDao.saveAll(leaveBalanceDetails);
			log.info("Leave Balance for all employee are successfully reseted");
		}
}
