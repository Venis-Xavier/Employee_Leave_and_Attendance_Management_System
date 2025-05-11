package com.empleavemanagement.reportsmodule.serviceTest;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
 
import com.empleavemanagement.reportsmodule.client.AttendanceFeignClient;
import com.empleavemanagement.reportsmodule.client.EmployeeFeignClient;
import com.empleavemanagement.reportsmodule.client.LeaveFeignClient;
import com.empleavemanagement.reportsmodule.client.ShiftFeignClient;
import com.empleavemanagement.reportsmodule.dto.AttendanceReportDTO;
import com.empleavemanagement.reportsmodule.dto.LeaveReportDTO;
import com.empleavemanagement.reportsmodule.dto.ShiftReportDTO;
import com.empleavemanagement.reportsmodule.service.EmployeeReportService;
 
public class EmployeeReportServiceTest {
 
    @Mock
    private AttendanceFeignClient attendanceClient;
 
    @Mock
    private ShiftFeignClient shiftFeignClient;
 
    @Mock
    private LeaveFeignClient leaveFeignClient;
 
    @Mock
    private EmployeeFeignClient employeeFeignClient;
 
    @InjectMocks
    private EmployeeReportService employeeReportService;
 
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
 
    /**
     * Test for getAttendanceReports method
     */
    @Test
    void testGetAttendanceReports() {
        // Mock data
        LocalDate startDate = LocalDate.of(2023, 4, 1);
        LocalDate endDate = LocalDate.of(2023, 4, 7);
        List<AttendanceReportDTO> mockAttendance = Arrays.asList(
                new AttendanceReportDTO(1, LocalDateTime.of(2023, 4, 1, 9, 0), LocalDateTime.of(2023, 4, 1, 17, 0), 8.0, "PRESENT", startDate),
                new AttendanceReportDTO(2, LocalDateTime.of(2023, 4, 2, 9, 0), LocalDateTime.of(2023, 4, 2, 17, 0), 8.0, "PRESENT", startDate.plusDays(1)),
                new AttendanceReportDTO(1, LocalDateTime.of(2023, 4, 3, 9, 0), LocalDateTime.of(2023, 4, 3, 15, 30), 6.5, "PRESENT", startDate.plusDays(2))
        );
 
        ResponseEntity<List<AttendanceReportDTO>> response = ResponseEntity.ok(mockAttendance);
        when(attendanceClient.getAttendanceRecordsByDateRange(startDate, endDate)).thenReturn(response);
 
        // Call the method
        List<AttendanceReportDTO> result = employeeReportService.getAttendanceReports(1, startDate, endDate);
 
        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size()); // Employee 1 has 2 attendance records
        assertEquals(1, result.get(0).getEmployeeId());
    }
 
    /**
     * Test for getLeaveReports method
     */
    @Test
    void testGetLeaveReports() {
        // Mock data
        LocalDate startDate = LocalDate.of(2023, 4, 1);
        LocalDate endDate = LocalDate.of(2023, 4, 7);
        List<LeaveReportDTO> mockLeaveReports = Arrays.asList(
                new LeaveReportDTO(1, "SICK_LEAVE", Date.valueOf(startDate), Date.valueOf(startDate.plusDays(1)), "APPROVED"),
                new LeaveReportDTO(2, "CASUAL_LEAVE", Date.valueOf(startDate.plusDays(2)), Date.valueOf(startDate.plusDays(3)), "PENDING"),
                new LeaveReportDTO(1, "PAID_LEAVE", Date.valueOf(startDate.plusDays(4)), Date.valueOf(startDate.plusDays(5)), "APPROVED")
        );
 
        ResponseEntity<List<LeaveReportDTO>> response = ResponseEntity.ok(mockLeaveReports);
        when(leaveFeignClient.leaveDetailsOfEmployeeInDateRange(startDate, endDate)).thenReturn(response);
 
        // Call the method
        List<LeaveReportDTO> result = employeeReportService.getLeaveReports(1, startDate, endDate);
 
        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size()); // Only APPROVED leave reports for employee 1
        assertEquals("APPROVED", result.get(0).getLeaveStatus());
    }
 
    /**
     * Test for getShiftReports method
     */
    @Test
    void testGetShiftReports() {
        // Mock data
        LocalDate startDate = LocalDate.of(2023, 4, 1);
        LocalDate endDate = LocalDate.of(2023, 4, 7);
        List<ShiftReportDTO> mockShiftReports = Arrays.asList(
                new ShiftReportDTO(1, Date.valueOf(startDate), Date.valueOf(startDate.plusDays(1)), Time.valueOf("09:00:00"), Time.valueOf("17:00:00"), "Morning Shift"),
                new ShiftReportDTO(2, Date.valueOf(startDate.plusDays(2)), Date.valueOf(startDate.plusDays(3)), Time.valueOf("21:00:00"), Time.valueOf("05:00:00"), "Night Shift"),
                new ShiftReportDTO(1, Date.valueOf(startDate.plusDays(4)), Date.valueOf(startDate.plusDays(5)), Time.valueOf("13:00:00"), Time.valueOf("21:00:00"), "Evening Shift")
        );
 
        ResponseEntity<List<ShiftReportDTO>> response = ResponseEntity.ok(mockShiftReports);
        when(shiftFeignClient.getShiftsByDateRange(startDate, endDate)).thenReturn(response);
 
        // Call the method
        List<ShiftReportDTO> result = employeeReportService.getShiftReports(1, startDate, endDate);
 
        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size()); // Employee 1 has 2 shift reports
        assertEquals("Morning Shift", result.get(0).getShiftName());
    }
 
    /**
     * Test for getAllNamesAndIds method
     */
    @Test
    void testGetAllNamesAndIds() {
        // Mock data
        Map<Integer, String> mockNamesAndIds = Map.of(
                1, "John Doe",
                2, "Jane Smith"
        );
 
        when(employeeFeignClient.getAllNamesAndIds(100)).thenReturn(mockNamesAndIds);
 
        // Call the method
        Map<Integer, String> result = employeeReportService.getAllNamesAndIds(100);
 
        // Assertions
        assertNotNull(result);
        assertEquals(2, result.size()); // Verify size
        assertEquals("John Doe", result.get(1)); // Verify name for employeeId 1
    }
}