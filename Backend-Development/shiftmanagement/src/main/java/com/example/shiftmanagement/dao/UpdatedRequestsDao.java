package com.example.shiftmanagement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.shiftmanagement.entity.UpdatedRequests;

public interface UpdatedRequestsDao extends JpaRepository<UpdatedRequests, Integer>{
	
	@Query("select u from UpdatedRequests u where u.employeeId = :employeeId")
	List<UpdatedRequests> findByEmployeeId(@Param("employeeId") Integer employeeId);

}
