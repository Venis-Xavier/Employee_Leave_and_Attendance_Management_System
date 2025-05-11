package com.leavemanagement.dao;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.leavemanagement.entity.LeaveRequest;


@Repository
public interface LeaveRequestDao extends JpaRepository<LeaveRequest, Integer>{

	// Fetch the latest leave request of an employee by using employee id
	@Query(value="select * from leave_request as l where l.employeeid = :employeeId order by l.leave_request_id DESC LIMIT 1",nativeQuery=true)
	LeaveRequest findByEmployeeId(@Param("employeeId") Integer employeeId);
	
	@Query(value="select * from leave_request as l where l.employeeid = :employeeId order by l.leave_request_id DESC", nativeQuery=true)
	List<LeaveRequest> findAllLeaveRequestOfEmployee(@Param("employeeId") Integer employeeId);

	// Fetch the leave requests of all employees who are requested from startDate to endDate
		@Query(value = "select * from leave_request as l where l.start_date between :startDate and :endDate",nativeQuery=true)
		List<LeaveRequest> findLeaveDetailsByEmployeeIdAndDates( @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
	
	@Query(value= "select * from leave_request as l where l.employeeid= :employeeId and ((l.start_date between :startDate and :endDate) or (l.end_date between :startDate and :endDate) or (l.start_date <= :startDate and l.end_date>= :endDate))",nativeQuery=true)
	Optional<List<LeaveRequest>> findAlreadyRequestedLeavesByDates(@Param("employeeId") Integer employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

