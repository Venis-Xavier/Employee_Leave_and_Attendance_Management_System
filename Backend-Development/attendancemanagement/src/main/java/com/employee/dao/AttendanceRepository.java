package com.employee.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.employee.entity.AttendanceRecords;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecords,Integer>{
	@Query("SELECT a FROM AttendanceRecords a WHERE a.employeeId = :employeeId AND DATE(a.clockInTime) = :date")
	AttendanceRecords findByEmployeeAndDate(@Param("employeeId") Integer employeeId, @Param("date") LocalDate date);
	
	@Query("SELECT  a.employeeId,a.clockInTime, a.clockOutTime, a.workHours, a.status , a.date " +
		       "FROM AttendanceRecords a WHERE a.date BETWEEN :startDate AND :endDate")
	List<Object[]> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	@Query(value="select * from attendance_records as a where a.employee_id= :employeeId order by recordid desc limit 1",nativeQuery=true)
	AttendanceRecords getEmployeesTodayAttendace(@Param("employeeId") Integer employeeId);
	
}
