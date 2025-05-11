package com.employee;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.employee.client.EmployeeManagementClient;
import com.employee.dao.AttendanceRepository;
import com.employee.dto.AttendanceDto;
import com.employee.dto.AttendanceRecordDto;
import com.employee.entity.AttendanceRecords;
import com.employee.entity.AttendanceRecords.AttendanceStatus;
import com.employee.exception.CheckOutBeforeCheckInException;
import com.employee.exception.DuplicateEntryException;
import com.employee.exception.InvalidAttendanceDateException;
import com.employee.service.AttendanceService;
import com.employee.utils.ResultResponse;

class AttendanceServiceTest {

    @InjectMocks
    private AttendanceService attendanceService;

    @Mock
    private AttendanceRepository attendanceRepo;

    @Mock
    private EmployeeManagementClient employeeManagementClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

   

    @Test
    void testSubmitAttendance_duplicateEntry() {
        // Arrange
        int employeeId = 1;
        LocalDateTime clockInTime = LocalDateTime.of(2025, 4, 22, 9, 0);
        LocalDateTime clockOutTime = LocalDateTime.of(2025, 4, 22, 17, 0);
        AttendanceDto attendanceDto = new AttendanceDto(clockInTime, clockOutTime);

        AttendanceRecords existingRecord = new AttendanceRecords(1, employeeId, clockInTime, clockOutTime, 8, AttendanceStatus.PRESENT, LocalDate.now());
        when(attendanceRepo.findByEmployeeAndDate(employeeId, LocalDate.now())).thenReturn(existingRecord);

        // Act & Assert
        assertThrows(DuplicateEntryException.class, () -> attendanceService.submitAttendance(employeeId, attendanceDto));
        verify(attendanceRepo, never()).save(any(AttendanceRecords.class));
    }

    @Test
    void testSubmitAttendance_checkOutBeforeCheckIn() {
        // Arrange
        int employeeId = 1;
        LocalDateTime clockInTime = LocalDateTime.of(2025, 4, 22, 9, 0);
        LocalDateTime clockOutTime = LocalDateTime.of(2025, 4, 22, 8, 0);
        AttendanceDto attendanceDto = new AttendanceDto(clockInTime, clockOutTime);

        // Act & Assert
        assertThrows(CheckOutBeforeCheckInException.class, () -> attendanceService.submitAttendance(employeeId, attendanceDto));
        verify(attendanceRepo, never()).save(any(AttendanceRecords.class));
    }

    @Test
    void testMarkAbsentees() {
        // Arrange
        LocalDate previousDay = LocalDate.now().minusDays(1);
        List<Integer> allEmployeeIds = Arrays.asList(1, 2, 3);

        when(employeeManagementClient.getAllEmployeeIds()).thenReturn(allEmployeeIds);
        when(attendanceRepo.findByEmployeeAndDate(anyInt(), eq(previousDay))).thenReturn(null);

        // Act
        attendanceService.markAbsentees();

        // Assert
        verify(attendanceRepo, times(3)).save(any(AttendanceRecords.class));
    }

    @Test
    void testConvertEntityToDto() {
        // Arrange
        AttendanceRecords attendanceRecord = new AttendanceRecords(1, 1, LocalDateTime.of(2025, 4, 22, 9, 0),
                LocalDateTime.of(2025, 4, 22, 17, 0), 8, AttendanceStatus.PRESENT, LocalDate.now());

        // Act
        AttendanceRecordDto dto = attendanceService.convertEntityToDto(attendanceRecord);

        // Assert
        assertEquals(1, dto.getRecordId());
        assertEquals(1, dto.getEmployeeId());
        assertEquals(8, dto.getWorkHours());
        assertEquals("PRESENT", dto.getStatus());
    }
    
    
    
    
    @Test
    void testSubmitAttendance_success() {
        // Arrange
        int employeeId = 1;
        LocalDateTime clockInTime = LocalDateTime.of(2025, 4, 22, 9, 0);
        LocalDateTime clockOutTime = LocalDateTime.of(2025, 4, 22, 17, 0);
        AttendanceDto attendanceDto = new AttendanceDto(clockInTime, clockOutTime);

        AttendanceRecords savedRecord = new AttendanceRecords(1, employeeId, clockInTime, clockOutTime, 8, AttendanceStatus.PRESENT, LocalDate.now());
        
        // Mock repository calls
        when(attendanceRepo.findByEmployeeAndDate(employeeId, LocalDate.now())).thenReturn(null);
        when(attendanceRepo.save(any(AttendanceRecords.class))).thenReturn(savedRecord);

        // Act
        ResultResponse<AttendanceRecordDto> result = attendanceService.submitAttendance(employeeId, attendanceDto);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(savedRecord.getRecordId(), result.getData().getRecordId());
        assertEquals(savedRecord.getEmployeeId(), result.getData().getEmployeeId());
        verify(attendanceRepo, times(1)).save(any(AttendanceRecords.class));
    }

}
