package com.example.shiftmanagement.dao;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.shiftmanagement.entity.ShiftRequest;

@Repository
public interface ShiftRequestDao extends JpaRepository<ShiftRequest, Integer> {
	
    ShiftRequest findByEmployeeId(Integer employeeId);
    

    @Query("SELECT sr FROM ShiftRequest sr WHERE sr.employeeId = :employeeId AND sr.status = 'APPROVED' ORDER BY sr.timestamp DESC")
    Optional<ShiftRequest> findMostRecentApprovedShiftRequest(@Param("employeeId") Integer employeeId);
    
    @Query("SELECT sr FROM ShiftRequest sr WHERE sr.employeeId = :employeeId AND sr.status = 'REJECTED' ORDER BY sr.timestamp DESC")
    Optional<ShiftRequest> findMostRecentRejectedShiftRequest(@Param("employeeId") Integer employeeId);
}
