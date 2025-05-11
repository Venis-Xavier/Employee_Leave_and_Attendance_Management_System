package com.example.shiftmanagement.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.shiftmanagement.entity.ShiftAssignments;

public interface ShiftAssignmentsDao extends JpaRepository<ShiftAssignments, Integer>{
	//@Query("select s from ShifAssignments s where s.employeeId =: employeeId")
	@Query(value = "SELECT * FROM shift_assignments s WHERE s.employee_id = :employeeId ORDER BY s.end_date DESC LIMIT 1", nativeQuery = true)
	ShiftAssignments findByEmployeeId(@Param("employeeId") Integer employeeId);
	
	@Query(value = "SELECT * FROM shift_assignments s WHERE s.employee_id = :employeeId", nativeQuery = true)
	List<ShiftAssignments> findByShiftsEmployeeId(@Param("employeeId") Integer employeeId);

	//@Query("SELECT s FROM ShiftAssignments s WHERE s.employee.employeeId = :employeeId ORDER BY s.endDate DESC")
	@Query(value = "SELECT * FROM shift_assignments s WHERE s.employee_id = :employeeId ORDER BY s.end_date DESC LIMIT 1", nativeQuery = true)
    Optional<ShiftAssignments> findTopByEmployeeIdOrderByEndDateDesc(@Param("employeeId") Integer employeeId);
	
	@Query(value = "SELECT s.employee_id AS employeeId, s.start_date AS startDate, s.end_date AS endDate, s.start_time AS startTime, s.end_time AS endTime, s.shift_name AS shiftName FROM shift_assignments s WHERE s.start_date BETWEEN :startDate AND :endDate", nativeQuery = true)
	List<Map<String, Object>> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}