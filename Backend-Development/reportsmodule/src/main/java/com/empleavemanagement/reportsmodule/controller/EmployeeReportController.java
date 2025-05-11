package com.empleavemanagement.reportsmodule.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.empleavemanagement.reportsmodule.dto.AttendanceReportDTO;
import com.empleavemanagement.reportsmodule.dto.LeaveReportDTO;
import com.empleavemanagement.reportsmodule.dto.ShiftReportDTO;
import com.empleavemanagement.reportsmodule.exception.ReportNotFoundException;
import com.empleavemanagement.reportsmodule.service.EmployeeReportService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@CrossOrigin(origins="http://localhost:3000")
@RequestMapping("employee/{employeeId}/reports")
public class EmployeeReportController {

    private final EmployeeReportService reportService;

    public EmployeeReportController(EmployeeReportService reportService) {
        this.reportService = reportService;
    }
    
    
    @GetMapping("/attendance")
    public ResponseEntity<?> getAttendanceReports(@PathVariable int employeeId,
    		@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("Fetching weekly attendance reports for employeeId:"+ employeeId);
            List<AttendanceReportDTO> reports = reportService.getAttendanceReports(employeeId,startDate,endDate);
            log.debug("Attendance reports fetched: {}", reports);
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } catch (ReportNotFoundException e) {
            log.error("ReportNotFoundException while fetching weekly attendance reports: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
    
    @GetMapping("/leave")
    public ResponseEntity<?> getLeaveReports(@PathVariable int employeeId,
    		@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("Fetching leave reports for employeeId: ", employeeId);
            List<LeaveReportDTO> reports = reportService.getLeaveReports(employeeId,startDate,endDate);
            log.debug("Leave reports fetched: {}", reports);
            return new ResponseEntity<>(reports, HttpStatus.OK);
        } catch (ReportNotFoundException e) {
            log.error("ReportNotFoundException while fetching leave reports: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
        @GetMapping("/shift")
        public ResponseEntity<?> getShiftReports(@PathVariable int employeeId,
        		@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
            try {
                log.info("Fetching Shift reports for employeeId: {}", employeeId);
                List<ShiftReportDTO> reports = reportService.getShiftReports(employeeId,startDate,endDate);
                log.debug("Shift reports fetched: {}", reports);
                return new ResponseEntity<>(reports, HttpStatus.OK);
            } catch (ReportNotFoundException e) {
                log.error("ReportNotFoundException while fetching shift reports: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected exception: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
            }
    }
        @GetMapping("/getAllNamesAndIds")
    	public ResponseEntity<Map<Integer,String>> getAllNamesAndIds(@PathVariable int employeeId){
    		return ResponseEntity.ok().body(reportService.getAllNamesAndIds(employeeId));
    	}
    
}