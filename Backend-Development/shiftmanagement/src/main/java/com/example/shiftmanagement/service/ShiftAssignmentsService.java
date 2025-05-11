package com.example.shiftmanagement.service;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.shiftmanagement.client.EmployeeManagementClient;
import com.example.shiftmanagement.dao.ShiftAssignmentsDao;
import com.example.shiftmanagement.dao.ShiftRequestDao;
import com.example.shiftmanagement.dto.ShiftDto;
import com.example.shiftmanagement.entity.ShiftAssignments;
import com.example.shiftmanagement.entity.ShiftRequest;
import com.example.shiftmanagement.exception.AssigningDateError;
import com.example.shiftmanagement.exception.DateError;
import com.example.shiftmanagement.utils.ResultResponse;
import lombok.extern.slf4j.Slf4j;
/**
 * The ShiftAssignmentsService class is a service layer component responsible for managing
 * shift assignments, employee validations, and shift-related operations. It handles 
 * business logic and interactions with the data access layer (DAOs).
 * 
 * Annotations:
 * - @Service: Marks this class as a service component in the Spring framework, 
 *   allowing it to be detected and managed as a Spring bean.
 * - @Slf4j: Enables logging for debugging and monitoring the behavior of the service.
 * 
 * Dependencies:
 * - ShiftAssignmentsDao: Handles database operations for ShiftAssignments entities.
 * - EmployeeDao: Handles database operations for Employee entities.
 * - ShiftRequestDao: Handles database operations for ShiftRequest entities.
 */
@Slf4j
@Service
public class ShiftAssignmentsService {

    @Autowired
    private ShiftAssignmentsDao shiftAssignmentsDao;

//    @Autowired
//    private EmployeeDao employeeDao;
    @Autowired
    private ShiftRequestDao shiftRequestDao;
    
    @Autowired
    private EmployeeManagementClient employeeManagementClient;

    // Fetch assignments by employee ID
    /**
     * Fetches shift assignments for a specific employee.
     * 
     * @param employeeId The ID of the employee whose shift assignments are to be retrieved.
     * @return An Optional containing the ShiftAssignments object if available, or empty otherwise.
     */
    public ResultResponse<ShiftAssignments> getAssignmentsByEmployeeId(int employeeId) {
    	ResultResponse result=ResultResponse.builder().data(shiftAssignmentsDao.findByEmployeeId(employeeId)).success(true).message("Shift Assignement is fetched").timestamp(LocalDateTime.now()).build();
        return result;
    }
    
    public ResultResponse<List<ShiftAssignments>> getShiftAssignmentsByEmployeeId(int employeeId) {
    	ResultResponse result=ResultResponse.builder().data(shiftAssignmentsDao.findByShiftsEmployeeId(employeeId)).success(true).message("Shift Assignement is fetched").timestamp(LocalDateTime.now()).build();
        return result;
    }
 
    // Assign a shift to an employee
    /**
     * Assigns a new shift to an employee.
     * 
     * @param employeeId The ID of the employee to whom the shift is assigned.
     * @param shift A ShiftDto containing shift details (startDate, endDate, startTime, endTime, etc.).
     * @return A ResultResponse containing the assigned shift and a success message.
     * @throws Exception If the employee is not found or the date validation fails.
     */
    public ResultResponse<ShiftAssignments> assignShift(Integer employeeId, ShiftDto shift) {
        log.info("entered into service for employeeId: {}", employeeId);

        ShiftAssignments shifts = new ShiftAssignments();
        shifts.setEmployeeId(employeeId);
        shifts.setStartDate(shift.getStartDate());
        shifts.setEndDate(shift.getEndDate());
        shifts.setStartTime(shift.getStartTime());
        shifts.setEndTime(shift.getEndTime());
        shifts.setShiftName(shift.getShiftName());

        // Validate that startDate is not null and is before endDate
        if (shifts.getEndDate() != null && shifts.getStartDate() != null && shifts.getEndDate().before(shifts.getStartDate())) {
            log.error("Validation failed: Start date is after end date for employeeId: {}", employeeId);
            return ResultResponse.<ShiftAssignments>builder()
                    .data(null)
                    .success(false)
                    .message("Start Date must always be before End date!!")
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        // Fetch the most recent previous shift for the employee
        Optional<ShiftAssignments> lastAssignedShift = shiftAssignmentsDao.findTopByEmployeeIdOrderByEndDateDesc(employeeId);

        if (lastAssignedShift.isPresent()) {
            // Get the last shift's end date
            java.sql.Date lastEndDate = lastAssignedShift.get().getEndDate();
            // Validate that the new start date is after the last shift's end date
            if (shifts.getStartDate() != null && lastEndDate != null && !shifts.getStartDate().after(lastEndDate)) {
                log.error("Validation failed: New start date is not after the last assigned end date for employeeId: {}", employeeId);
                return ResultResponse.<ShiftAssignments>builder()
                        .data(null)
                        .success(false)
                        .message("The new assigning shift start date must be after the last assigned end date")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
        }

        try {
            // Save the new shift assignment
            ShiftAssignments savedShift = shiftAssignmentsDao.save(shifts);
            log.info("Shift assigned successfully for employeeId: {}", employeeId);
            return ResultResponse.<ShiftAssignments>builder()
                    .data(savedShift)
                    .success(true)
                    .message("Shift is assigned successfully")
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("Error saving shift assignment for employeeId: {}: {}", employeeId, e.getMessage());
            return ResultResponse.<ShiftAssignments>builder()
                    .data(null)
                    .success(false)
                    .message("Error assigning shift: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
    
    //updating the assignments table when the shift request is approved
    /**
     * Updates a shift assignment table based on an approved shift request.
     * 
     * @param id The ID of the employee whose shift assignment is updated.
     * @return The updated ShiftAssignments object.
     * @throws Exception If no approved shift request or assignment is found.
     */
    public ShiftAssignments updatetable(Integer id) throws Exception {
        // Fetch the most recent approved ShiftRequest for the employee
        ShiftRequest request = shiftRequestDao.findMostRecentApprovedShiftRequest(id).orElseThrow(() -> new Exception("No approved shift request found for employee ID: " + id));
        log.info("Fetched ShiftRequest: {}", request);
        // Fetch the most recent ShiftAssignment for the employee
        ShiftAssignments assignment = shiftAssignmentsDao.findTopByEmployeeIdOrderByEndDateDesc(id).orElseThrow(() -> new Exception("No shift assignment found for employee ID " + id));
        log.info("Before Update - ShiftAssignments: {}", assignment);
        // Update fields in ShiftAssignments based on the ShiftRequest
        boolean updated = false; // Flag to track if any field is updated
        if (request.getStartTime() != null && !request.getStartTime().equals(assignment.getStartTime())) {
            assignment.setStartTime(request.getStartTime());
            log.info("start: "+assignment.getStartDate());
            updated = true;
        }
        if (request.getEndTime() != null && !request.getEndTime().equals(assignment.getEndTime())) {
            assignment.setEndTime(request.getEndTime());
            log.info("end: "+assignment.getEndTime());
            updated = true;
        }
        if (request.getShiftRequestedName() != null && !request.getShiftRequestedName().equals(assignment.getShiftName())) {
            assignment.setShiftName(request.getShiftRequestedName());
            updated = true;
        }
        // Save only if there are actual updates
        if (updated) {
            shiftAssignmentsDao.save(assignment);
            log.info("After Update - ShiftAssignments: {}", assignment);
        } else {
            log.info("No changes detected in ShiftAssignments. Save operation skipped.");
        }
        shiftRequestDao.delete(request);      
        
        return assignment;
    }
    
    //deleting the record which is rejected
    /**
     * Deletes a shift assignment table for a rejected shift request.
     * 
     * @param id The ID of the employee whose rejected shift request is handled.
     * @throws Exception If no employee or rejected shift request is found.
     */
    public void deletetable(Integer id) throws Exception {
        // Fetch the most recent approved ShiftRequest for the employee
        ShiftRequest request = shiftRequestDao.findMostRecentRejectedShiftRequest(id).get();
        log.info("Fetched ShiftRequest: {}", request);

        // Fetch the most recent ShiftAssignment for the employee
        shiftRequestDao.delete(request);      
    }
    
    // Get all shifts
    /**
     * Retrieves all shift assignments.
     * 
     * @return A List containing all ShiftAssignments objects.
     */
    public ResultResponse<List<ShiftAssignments>> getAllShifts(Integer managerId) {
    	List<Integer> empids = employeeManagementClient.employeesUnderManager(managerId);
    	List<ShiftAssignments> resultList = new ArrayList<>();
    	log.info("entered into the service");
    	for(Integer id : empids) {
    		log.info("fetching the shifts of each employee");
    		ShiftAssignments assign = shiftAssignmentsDao.findByEmployeeId(id);
    		if(assign!=null)
    	resultList.add(assign);
    	}
    	ResultResponse result=ResultResponse.builder().data(resultList).success(true).message("shifts of all employees is Fetched.").timestamp(LocalDateTime.now()).build();
        return result;
    }
    
    // Add a new shift
    /**
     * Adds a new shift assignment.
     * 
     * @param shift The ShiftAssignments object to be added.
     * @return The saved ShiftAssignments object.
     */
    public ShiftAssignments addShift(ShiftAssignments shift) {
        return shiftAssignmentsDao.save(shift);
    }
    
    // Get shift by ID
    /**
     * Retrieves a shift assignment by its ID.
     * 
     * @param shiftId The unique ID of the shift assignment.
     * @return A ShiftAssignments object if found, or null otherwise.
     */
    public ShiftAssignments getShiftById(int shiftId) {
        return shiftAssignmentsDao.findById(shiftId).orElse(null);
    }
    
    //getting the assigned shifts in the specified date range
    /**
     * Retrieves shifts within a specific date range and exports them to a CSV file.
     * 
     * @param startDate The start date for filtering shifts.
     * @param endDate The end date for filtering shifts.
     * @return A List of Map objects representing the filtered shifts.
     * @throws Exception If an error occurs during data fetching or file writing.
     */
    public List<Map<String, Object>> getShiftsByDateRange(LocalDate startDate, LocalDate endDate) throws Exception {
        // Fetch data using the repository
        //List<Object[]> shifts = shiftAssignmentsDao.findByDateBetween(startDate, endDate);
        List<Map<String, Object>> shifts = shiftAssignmentsDao.findByDateBetween(startDate, endDate);
        writeToCsv(shifts, "C:/Project/shiftmanagement/shifts.csv");
        return shifts;
    }
    
    //writing the retrieved data into the csv file
    /**
     * Writes shift data to a CSV file.
     * 
     * @param data The List of Map objects containing shift data.
     * @param fileName The name and path of the CSV file to be created.
     */
    public static void writeToCsv(List<Map<String, Object>> data, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            // Write the header
            writer.append("employeeId,startDate,endDate,startTime,endTime,shiftName\n");
            // Write the rows
            for (Map<String, Object> row : data) {
                writer.append(row.get("employeeId").toString()).append(",")
                      .append(row.get("startDate").toString()).append(",")
                      .append(row.get("endDate").toString()).append(",")
                      .append(row.get("startTime").toString()).append(",")
                      .append(row.get("endTime").toString()).append(",")
                      .append(row.get("shiftName").toString()).append("\n");
            }

            System.out.println("CSV file created successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<ShiftAssignments> getemployeedataundermanager(Integer managerId){
    	List<Integer> EmployeeunderManager= employeeManagementClient.employeesUnderManager(managerId);
    	List<ShiftAssignments> result = new ArrayList<>();
    	for(Integer e : EmployeeunderManager) {
    		ShiftAssignments empshift = shiftAssignmentsDao.findByEmployeeId(e);
    		if(empshift!=null) {
        		result.add(empshift);
    		}

    	}
    	return result;
    }
}

