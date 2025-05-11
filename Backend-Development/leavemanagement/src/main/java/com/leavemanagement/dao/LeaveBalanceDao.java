package com.leavemanagement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.leavemanagement.entity.LeaveBalance;
import com.leavemanagement.entity.LeaveTypes;


@Repository
public interface LeaveBalanceDao extends JpaRepository<LeaveBalance, Integer>{
	
	// Fetching data leave balance data by using employee id and leave type 
	@Query("select l from LeaveBalance as l where l.employeeId = :employeeId and l.leaveType = :leaveType")
	LeaveBalance findByEmployeeIdAndLeaveType(
	    @Param("employeeId") Integer employeeId, 
	    @Param("leaveType") LeaveTypes leaveType
	);
	
	// Fetching employee leave balance data by using employee id
	@Query("select l from LeaveBalance as l where l.employeeId = :employeeId")
	List<LeaveBalance> findByEmployeeId(@Param("employeeId") Integer employeeId);
}
