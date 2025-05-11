package com.employee.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.employee.client.EmployeeManagementClient;
import com.employee.dao.AttendanceRepository;
import com.employee.dto.AttendanceDto;
import com.employee.dto.AttendanceRecordDto;
import com.employee.entity.AttendanceRecords;
import com.employee.exception.CheckOutBeforeCheckInException;
import com.employee.exception.DuplicateEntryException;
import com.employee.exception.InvalidAttendanceDateException;
//import com.Employee.jwtutil.JWTService;
import com.employee.jwtutil.JWTServiceNew;
import com.employee.utils.ResultResponse;

import lombok.extern.slf4j.Slf4j;

import com.employee.entity.AttendanceRecords.AttendanceStatus;

@Service
@Slf4j
public class AttendanceService {
	
	@Autowired
	public JWTServiceNew jwtService;
    
    @Autowired
    AttendanceRepository attendanceRepo;

    @Autowired
    EmployeeManagementClient employeeManagementClient;
    
    /**
     
      * Converts AttendanceRecords (entity) to AttendanceRecordDto (DTO).
     * 
     * @param attendanceRecords The attendance record entity to convert.
     * @return A DTO representation of the attendance record.
     */
    public AttendanceRecordDto convertEntityToDto(AttendanceRecords attendanceRecords) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setRecordId(attendanceRecords.getRecordId());
        dto.setEmployeeId(attendanceRecords.getEmployeeId());
        dto.setClockInTime(attendanceRecords.getClockInTime());
        dto.setClockOutTime(attendanceRecords.getClockOutTime());
        dto.setWorkHours(attendanceRecords.getWorkHours());
        dto.setStatus(attendanceRecords.getStatus().toString()); // Convert enum to String
        dto.setDate(attendanceRecords.getDate());
        return dto;
    }

    /**
     * Records attendance for a specific employee.
     * 
     * @param employeeId The ID of the employee whose attendance is being recorded.
     * @param attendanceDTO Contains the clock-in and clock-out times.
     * @return A ResultResponse containing the saved attendance record as DTO.
     */
    public ResultResponse<AttendanceRecordDto> submitAttendance(int employeeId, AttendanceDto attendanceDTO) {
        log.info("Entered into the submitAttendance method.");
        AttendanceRecords attendanceRecord = new AttendanceRecords();
        attendanceRecord.setEmployeeId(employeeId);
        attendanceRecord.setClockInTime(attendanceDTO.getClockInTime());
        attendanceRecord.setClockOutTime(attendanceDTO.getClockOutTime());

        LocalDate today = LocalDate.now();
        attendanceRecord.setDate(today);

        // Validate clock-in and clock-out times
        
        Duration timeDifference = Duration.between(attendanceDTO.getClockInTime(), attendanceDTO.getClockOutTime());
        if (timeDifference.toHours() > 24) {
            log.error("Clock-in and clock-out time difference exceeds 24 hours.");
            throw new InvalidAttendanceDateException("Invalid attendance data");
        }
        
        if (attendanceDTO.getClockOutTime() == null || attendanceDTO.getClockInTime() == null) {
            log.error("Clock in time or clock out time is null");
            throw new InvalidAttendanceDateException("Clock in and clock out times must be provided");
        }

        // Additional validation logic (weekends, duplicate entries, etc.)
        DayOfWeek dayOfWeek = attendanceDTO.getClockInTime().getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            log.error("Attendance cannot be marked on weekends.");
            throw new InvalidAttendanceDateException("Attendance cannot be marked on weekends.");
        }

        if (attendanceDTO.getClockOutTime().isBefore(attendanceDTO.getClockInTime())) {
            log.error("Clock-out time is before clock-in time.");
            throw new CheckOutBeforeCheckInException("Clock-out time cannot be before clock-in time.");
        }

        AttendanceRecords existingRecord = attendanceRepo.findByEmployeeAndDate(employeeId, today);
        if (existingRecord != null) {
            log.error("Duplicate attendance entry detected for employee ID: {}", employeeId);
            throw new DuplicateEntryException("Today's attendance has already been recorded.");
        }

        // Calculate work hours and set status
        attendanceRecord.setWorkHours(calculateWorkHours(attendanceDTO.getClockInTime(), attendanceDTO.getClockOutTime()));
        attendanceRecord.setStatus(attendanceRecord.getWorkHours() > 0 ? AttendanceStatus.PRESENT : AttendanceStatus.ABSENT);

        // Save the entity and convert it to DTO
        AttendanceRecords savedRecord = attendanceRepo.save(attendanceRecord);
        AttendanceRecordDto savedRecordDto = convertEntityToDto(savedRecord);

        return ResultResponse.<AttendanceRecordDto>builder()
                .data(savedRecordDto)
                .success(true)
                .message("Attendance recorded successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
            //check Employee not found exception

    /**
     * Calculates work hours between clock-in and clock-out times.
     * 
     * @param clockInTime The time the employee clocked in.
     * @param clockOutTime The time the employee clocked out.
     * @return The number of hours worked.
     */
    private double calculateWorkHours(LocalDateTime clockInTime, LocalDateTime clockOutTime) {
        log.info("Calculating work hours between {} and {}", clockInTime, clockOutTime);
        return java.time.Duration.between(clockInTime, clockOutTime).toHours();
    }
    
    /**
     * Marks employees as ABSENT if they have no attendance records for the current day.
     *
     * This method checks all employees in the system to see if they have an attendance
     * record for the current date. If no record exists, an absentee record is created
     * with `clockInTime`, `clockOutTime` as `null`, `workHours` as `0`, and
     * `status` as `ABSENT`.
     *
     * This method can be run daily to ensure absentee records are accurately tracked.
     * 
     * @throws EmployeeNotFoundException If the records or updates fail
     * logs exception . 
     */

    public List<Integer> getemployeeids(){
    	return employeeManagementClient.getAllEmployeeIds();
    }
    /*
     * marking absentees using scheduler
     * */
    
    @Scheduled(cron = "0 38 9 * * ?")
    public void markAbsentees() {
        log.info("Marking absentees for the previous day.");
        
      

        LocalDate previousDay = LocalDate.now().minusDays(1); //marking yesterday's attendance
        List<Integer> allEmployees = getemployeeids();

        for (Integer employee : allEmployees) {
            AttendanceRecords existingRecords = attendanceRepo.findByEmployeeAndDate(employee, previousDay);

            // If no attendance record exists for the previous day, mark as ABSENT
            if (existingRecords == null) {
                AttendanceRecords absenteeRecord = new AttendanceRecords();
                absenteeRecord.setEmployeeId(employee);
                absenteeRecord.setClockInTime(null);
                absenteeRecord.setClockOutTime(null);
                absenteeRecord.setWorkHours(0);
                absenteeRecord.setStatus(AttendanceStatus.ABSENT);
                absenteeRecord.setDate(previousDay);
                attendanceRepo.save(absenteeRecord);
                log.info("Marked employee ID {} as ABSENT for {}", employee, previousDay);
            }
        }
    }



    /**
     * Retrieves attendance records within a given date range for all employees.
     * 
     * @param startDate The start date of the range (YYYY-MM-DD).
     * @param endDate The end date of the range (YYYY-MM-DD).
     * @return A list of AttendanceRecords within the specified date range.
     * 
     * Throws:
     * - IllegalArgumentException if the start date is after the end date.
     */
    public List<Map<String, Object>> getAttendanceWithinDateRange(LocalDate startDate, LocalDate endDate) {
        // Fetch raw results from the repository
        List<Object[]> rawResults = attendanceRepo.findByDateRange(startDate, endDate);

        // Convert raw results to labeled data
        List<Map<String, Object>> labeledResults = new ArrayList<>();
        for (Object[] row : rawResults) {
            Map<String, Object> labeledRow = new LinkedHashMap<>(); // Maintain order of fields
           labeledRow.put("employeeId", row[0]);
            labeledRow.put("clockInTime", row[1]);
            labeledRow.put("clockOutTime", row[2]);
            labeledRow.put("workHours", row[3]);
            labeledRow.put("status", row[4]);
            labeledRow.put("date", row[5]);
            labeledResults.add(labeledRow);
        }

        // Return the labeled results
        return labeledResults;
    }

}

