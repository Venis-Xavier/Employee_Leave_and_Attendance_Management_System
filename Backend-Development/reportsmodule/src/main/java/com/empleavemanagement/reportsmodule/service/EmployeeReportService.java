package com.empleavemanagement.reportsmodule.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.empleavemanagement.reportsmodule.client.AttendanceFeignClient;
import com.empleavemanagement.reportsmodule.client.EmployeeFeignClient;
import com.empleavemanagement.reportsmodule.client.LeaveFeignClient;
import com.empleavemanagement.reportsmodule.client.ShiftFeignClient;
import com.empleavemanagement.reportsmodule.dto.AttendanceReportDTO;
import com.empleavemanagement.reportsmodule.dto.LeaveReportDTO;
import com.empleavemanagement.reportsmodule.dto.ShiftReportDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmployeeReportService {

    @Autowired
    private AttendanceFeignClient attendanceClient;

    @Autowired
    private ShiftFeignClient shiftFeignClient;

    @Autowired
    private LeaveFeignClient leaveFeignClient;
    
    @Autowired
    private EmployeeFeignClient employeeFeignClient;
    
    
    public List<AttendanceReportDTO> getAttendanceReports(int employeeId,LocalDate startDate,LocalDate endDate) {
        log.info("Fetching weekly attendance reports for employeeId: {}", employeeId);

        log.debug("Date range attendance report: {} to {}", startDate, endDate);
        ResponseEntity<List<AttendanceReportDTO>> response = attendanceClient.getAttendanceRecordsByDateRange(
                startDate, endDate);

        return filterAttendanceByEmployeeId(response.getBody(), employeeId);
    }
    
    public List<LeaveReportDTO> getLeaveReports(int employeeId,LocalDate startDate,LocalDate endDate) {
        log.info("Fetching leave reports for employeeId: {}", employeeId);

        log.debug("Date range leave report: {} to {}", startDate, endDate);
        ResponseEntity<List<LeaveReportDTO>> response = leaveFeignClient.leaveDetailsOfEmployeeInDateRange(
                startDate, endDate);
        List<LeaveReportDTO> reportDTOs = response.getBody().stream()
        	    .filter(records -> !records.getLeaveStatus().equalsIgnoreCase("PENDING"))
        	    .collect(Collectors.toList());

        return filterLeaveByEmployeeId(reportDTOs, employeeId);
    }
    
    public List<ShiftReportDTO> getShiftReports(int employeeId,LocalDate startDate,LocalDate endDate) {
        log.info("Fetching shift reports for employeeId: {}", employeeId);

        log.debug("Date range shift report: {} to {}", startDate, endDate);
        ResponseEntity<List<ShiftReportDTO>> response = shiftFeignClient.getShiftsByDateRange(
                startDate, endDate);

        return filterShiftByEmployeeId(response.getBody(), employeeId);
    }
    
	public Map<Integer, String> getAllNamesAndIds(Integer managerId) {
		return employeeFeignClient.getAllNamesAndIds(managerId);
	}
    
    private List<AttendanceReportDTO> filterAttendanceByEmployeeId(List<AttendanceReportDTO> records, int employeeId) {
        log.debug("Filtering attendance records by employeeId: {}", employeeId);
        
        List<AttendanceReportDTO> filteredRecords = new ArrayList<>();
        int initialSize = records.size(); // For logging how many records were processed

        for (AttendanceReportDTO record : records) {
            if (record.getEmployeeId() == employeeId) {
                filteredRecords.add(record);
            }
        }
        log.debug("Filtered {} records out of {} for employeeId: {}", filteredRecords.size(), initialSize, employeeId);

        return filteredRecords;
    }
    
    private List<LeaveReportDTO> filterLeaveByEmployeeId(List<LeaveReportDTO> records, int employeeId) {
        log.debug("Filtering leave records for employeeId: {}", employeeId);

        List<LeaveReportDTO> filteredRecords = new ArrayList<>();
        int initialSize = records.size(); // For logging how many records were processed

        // Loop through the records and filter based on employee ID
        for (LeaveReportDTO record : records) {
            if (record.getEmployeeId() == employeeId) {
                filteredRecords.add(record);
            }
        }

        log.debug("Filtered {} records out of {} for employeeId: {}", filteredRecords.size(), initialSize, employeeId);
        return filteredRecords;
    }
    
    private List<ShiftReportDTO> filterShiftByEmployeeId(List<ShiftReportDTO> records, int employeeId) {
        log.debug("Filtering shift records for employeeId: {}", employeeId);

        List<ShiftReportDTO> filteredRecords = new ArrayList<>();

        // Filter records based on employee ID
        for (ShiftReportDTO record : records) {
            if (record.getEmployeeId() == employeeId) {
                filteredRecords.add(record);
            }
        }

        log.debug("Filtered {} shift records for employeeId: {}", filteredRecords.size(), employeeId);
        return filteredRecords;
    }
    
}