package com.example.shiftmanagement.service;

import com.example.shiftmanagement.entity.ShiftRequest;
import com.example.shiftmanagement.entity.UpdatedRequests;
import com.example.shiftmanagement.utils.ResultResponse;

import lombok.extern.slf4j.Slf4j;

import com.example.shiftmanagement.client.EmployeeManagementClient;
import com.example.shiftmanagement.dao.UpdatedRequestsDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UpdatedRequestsService {
	
	@Autowired
	private EmployeeManagementClient employeeManagementClient;
    
    private final UpdatedRequestsDao updatedRequestsDao;

    public UpdatedRequestsService(UpdatedRequestsDao updatedRequestsDao) {
        this.updatedRequestsDao = updatedRequestsDao;
    }

    // Method to fetch shift request history for an employee
    public ResultResponse<List<UpdatedRequests>> getUpdatedRequestsByEmployeeId(Integer employeeId) {
        List<UpdatedRequests> result =  updatedRequestsDao.findByEmployeeId(employeeId);
        return ResultResponse.<List<UpdatedRequests>>builder()
                .data(result)
                .success(true)
                .message("All shifts retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public ResultResponse<List<List<UpdatedRequests>>> getUpdatedRequestsofEmployees(Integer managerId){
    	List<Integer> EmployeeunderManager= employeeManagementClient.employeesUnderManager(managerId);

    	log.info("ids: "+EmployeeunderManager);
    	List<List<UpdatedRequests>> request = new ArrayList<>();
    	if(EmployeeunderManager.isEmpty()) {
    		log.info("employees ids empty");
    		return ResultResponse.<List<List<UpdatedRequests>>>builder()
                    .data(null)
                    .success(true)
                    .message("employee ids not fetching properly")
                    .timestamp(LocalDateTime.now())
                    .build();
    	}
    	for(Integer id: EmployeeunderManager) {
    		List<UpdatedRequests> ex = example(id);
    		if(!ex.isEmpty()) {
    			request.add(ex);
    		}
    		
    		
    	}    	
    	return ResultResponse.<List<List<UpdatedRequests>>>builder()
                .data(request)
                .success(true)
                .message("All shifts retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public List<UpdatedRequests> example(Integer managerId){
    	return updatedRequestsDao.findByEmployeeId(managerId);
    }
}
