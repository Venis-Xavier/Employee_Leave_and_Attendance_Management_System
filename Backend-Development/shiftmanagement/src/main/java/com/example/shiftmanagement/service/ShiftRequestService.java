package com.example.shiftmanagement.service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.shiftmanagement.entity.ShiftAssignments;
import com.example.shiftmanagement.entity.ShiftRequest;
import com.example.shiftmanagement.entity.ShiftRequest.RequestStatus;
import com.example.shiftmanagement.entity.UpdatedRequests;
import com.example.shiftmanagement.exception.CannotUpdateStatusException;
import com.example.shiftmanagement.exception.EmployeeNotFoundException;
import com.example.shiftmanagement.exception.SameNameException;
import com.example.shiftmanagement.client.EmployeeManagementClient;
import com.example.shiftmanagement.dao.ShiftAssignmentsDao;
import com.example.shiftmanagement.dao.ShiftRequestDao;
import com.example.shiftmanagement.dao.UpdatedRequestsDao;
import com.example.shiftmanagement.dto.ShiftRequestDto;
import com.example.shiftmanagement.utils.ResultResponse;

import lombok.extern.slf4j.Slf4j;
/**
 * The ShiftRequestService class provides business logic for managing shift requests 
 * made by employees. This class handles the creation, retrieval, approval, and 
 * rejection of shift requests while ensuring data integrity and validation.
 * 
 * Annotations:
 * - @Service: Marks this class as a Spring service component, allowing it to be 
 *   detected and managed as a Spring bean.
 * - @Autowired: Injects dependencies such as DAOs and services needed for operations.
 * 
 * Dependencies:
 * - ShiftRequestDao: Handles database operations for ShiftRequest entities.
 * - EmployeeDao: Handles database operations for Employee entities.
 * - ShiftAssignmentsService: Provides shift assignment-related services.
 */
@Service
@Slf4j
public class ShiftRequestService {

    @Autowired
    private ShiftRequestDao shiftRequestDao;

    @Autowired
    private UpdatedRequestsDao updatedRequestsDao;
    @Autowired
    private ShiftAssignmentsService assignService;
    @Autowired
    private ShiftAssignmentsDao shiftAssignmentsDao;
    
    @Autowired
    private EmployeeManagementClient employeeManagementClient;
    public ResultResponse<ShiftRequest> createShiftRequest(Integer employeeId, ShiftRequestDto request) {
        ShiftRequest newRequest = new ShiftRequest();
        log.info("entered into create shift request in service class for employeeId: {}", employeeId);

        try {
            ResultResponse<ShiftAssignments> assignmentResponse = assignService.getAssignmentsByEmployeeId(employeeId);

            if (!assignmentResponse.isSuccess()) {
                log.error("Could not retrieve assigned shift for employeeId: {}. Reason: {}", employeeId, assignmentResponse.getMessage());
                return ResultResponse.<ShiftRequest>builder()
                        .data(null)
                        .success(false)
                        .message("Could not retrieve assigned shift details.")
                        .timestamp(LocalDateTime.now())
                        .build();
            }

            ShiftAssignments assignedShift = assignmentResponse.getData();
            log.info("Assigned shift data for employeeId {}: {}", employeeId, assignedShift);

            newRequest.setEmployeeId(employeeId);

            if (request.getShiftRequestedName() != null) {
                // Check if the requested name is the same as the assigned name
                if (assignedShift != null && request.getShiftRequestedName().equals(assignedShift.getShiftName())) {
                    log.warn("Requested shift name is the same as assigned shift name for employeeId: {}", employeeId);
                    return ResultResponse.<ShiftRequest>builder()
                            .data(null)
                            .success(false)
                            .message("The assigned shift name and requested shift name are the same!")
                            .timestamp(LocalDateTime.now())
                            .build();
                }
                newRequest.setShiftRequestedName(request.getShiftRequestedName());
            }

            if (request.getStartTime() != null) {
                newRequest.setStartTime(request.getStartTime());
            }

            if (request.getEndTime() != null) {
                newRequest.setEndTime(request.getEndTime());
            }

            newRequest.setStatus(RequestStatus.PENDING);

            if (assignedShift != null) {
                newRequest.setAssignedShiftId(assignedShift.getShiftAssignmentId());
                newRequest.setAssignedShiftName(assignedShift.getShiftName());
            }

            newRequest.setTimestamp(LocalDateTime.now());
            ShiftRequest savedRequest = shiftRequestDao.save(newRequest);

            return ResultResponse.<ShiftRequest>builder()
                    .data(savedRequest)
                    .success(true)
                    .message("Shift request created successfully")
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error creating shift request for employeeId: {}. Error: {}", employeeId, e.getMessage());
            return ResultResponse.<ShiftRequest>builder()
                    .data(null)
                    .success(false)
                    .message("Failed to create shift request: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    //getting request by Employee id
    /**
     * Retrieves shift requests for a specific employee by their ID.
     * 
     * @param employeeId The ID of the employee whose shift request is to be retrieved.
     * @return ResultResponse containing the ShiftRequest object and a success message.
     * @throws EmployeeNotFoundException If the employee is not found.
     */
    public ResultResponse<ShiftRequest> getRequestsByEmployeeId(int employeeId) {
        ShiftRequest shiftRequest = shiftRequestDao.findByEmployeeId(employeeId);
        return ResultResponse.<ShiftRequest>builder()
                .data(shiftRequest)
                .success(true)
                .message("Shift request retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    //getting the requests placed by employees
    /**
     * Retrieves all shift requests in the system.
     * 
     * @return ResultResponse containing a List of all ShiftRequest objects and a success message.
     */
    public ResultResponse<List<ShiftRequest>> getAllShifts(Integer managerId) {
    	List<Integer> ids = employeeManagementClient.employeesUnderManager(managerId);
    	log.info("ids: "+ids);
    	List<ShiftRequest> result = new ArrayList<>();
    	for(Integer id: ids) {
    		ShiftRequest shift = shiftRequestDao.findByEmployeeId(id); // Use the employee's ID, not the manager's
    		if(shift != null) {
    			result.add(shift);
    		}
    	}
        return ResultResponse.<List<ShiftRequest>>builder()
                .data(result)
                .success(true)
                .message("All shifts retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    //approval or rejection of the request placed by the employee
    /**
     * Approves or rejects a shift request based on the provided status.
     * 
     * @param employeeId The ID of the employee whose shift request is being updated.
     * @param shiftRequestDto A ShiftRequestDto containing the new status (APPROVED or REJECTED).
     * @return ResultResponse containing the updated ShiftRequest object and a success message.
     * @throws CannotUpdateStatusException If the shift request status cannot be updated.
     */
    public ResultResponse<ShiftRequest> approveOrRejectLeave(Integer employeeId, ShiftRequestDto shiftRequestDto) throws CannotUpdateStatusException {
        // Find the shift request by employee ID
        ShiftRequest shiftRequest = shiftRequestDao.findByEmployeeId(employeeId);

        if (shiftRequest == null) {
            return ResultResponse.<ShiftRequest>builder()
                    .data(null)
                    .success(false)
                    .message("No shift request found for employee ID: " + employeeId)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // Check if the request status is pending
        if (shiftRequest.getStatus() == ShiftRequest.RequestStatus.PENDING) {
            // Fetch `startDate` and `endDate` from `ShiftAssignment`
            ShiftAssignments shiftAssignment = shiftAssignmentsDao.findByEmployeeId(employeeId);
            
            if (shiftAssignment == null) {
                return ResultResponse.<ShiftRequest>builder()
                        .data(null)
                        .success(false)
                        .message("No shift assignment found for employee ID: " + employeeId)
                        .timestamp(LocalDateTime.now())
                        .build();
            }

            // Update the status from the ShiftRequestDto
            shiftRequest.setStatus(shiftRequestDto.getStatus());

            // Save the updated shift request
            ShiftRequest updatedShiftRequest = shiftRequestDao.save(shiftRequest);
            
            // ✅ Store update history before deletion
            UpdatedRequests history = new UpdatedRequests();
            history.setEmployeeId(updatedShiftRequest.getEmployeeId());
            history.setShiftRequestedName(updatedShiftRequest.getShiftRequestedName());
            history.setStartTime(updatedShiftRequest.getStartTime());
            history.setEndTime(updatedShiftRequest.getEndTime());
            history.setStartDate(shiftAssignment.getStartDate()); // ✅ Fetch from shift assignment
            history.setEndDate(shiftAssignment.getEndDate()); // ✅ Fetch from shift assignment
            history.setStatus(UpdatedRequests.RequestStatus.valueOf(updatedShiftRequest.getStatus().name()));

            updatedRequestsDao.save(history);
            
            return ResultResponse.<ShiftRequest>builder()
                    .data(updatedShiftRequest)
                    .success(true)
                    .message("Shift request is UPDATED successfully")
                    .timestamp(LocalDateTime.now())
                    .build();
        } else {
            throw new CannotUpdateStatusException("Cannot update the status!!");
        }
    }

}