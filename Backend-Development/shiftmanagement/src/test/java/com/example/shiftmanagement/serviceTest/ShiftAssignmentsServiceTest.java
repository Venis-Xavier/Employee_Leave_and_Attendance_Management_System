package com.example.shiftmanagement.serviceTest;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.sql.Time;
import java.text.SimpleDateFormat;
import com.example.shiftmanagement.client.EmployeeManagementClient;
import com.example.shiftmanagement.dao.ShiftAssignmentsDao;
import com.example.shiftmanagement.dao.ShiftRequestDao;
import com.example.shiftmanagement.dto.ShiftDto;
import com.example.shiftmanagement.entity.ShiftAssignments;
import com.example.shiftmanagement.entity.ShiftRequest;
import com.example.shiftmanagement.service.ShiftAssignmentsService;
import com.example.shiftmanagement.utils.ResultResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

class ShiftAssignmentsServiceTest {

    @InjectMocks
    private ShiftAssignmentsService shiftAssignmentsService;

    @Mock
    private ShiftAssignmentsDao shiftAssignmentsDao;

    @Mock
    private ShiftRequestDao shiftRequestDao;

    @Mock
    private EmployeeManagementClient employeeManagementClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAssignmentsByEmployeeId() {
        ShiftAssignments shift = new ShiftAssignments();
        shift.setEmployeeId(1);

        when(shiftAssignmentsDao.findByEmployeeId(1)).thenReturn(shift);

        ResultResponse<ShiftAssignments> response = shiftAssignmentsService.getAssignmentsByEmployeeId(1);

        assertTrue(response.isSuccess());
        assertEquals(shift, response.getData());
    }

    @Test
    void testAssignShift() {
        ShiftDto shiftDto = new ShiftDto();
        shiftDto.setStartDate(new java.sql.Date(System.currentTimeMillis()));
        shiftDto.setEndDate(new java.sql.Date(System.currentTimeMillis() + 86400000));
        shiftDto.setShiftName("Morning Shift");

        ShiftAssignments shift = new ShiftAssignments();
        shift.setEmployeeId(1);
        shift.setStartDate(shiftDto.getStartDate());
        shift.setEndDate(shiftDto.getEndDate());
        shift.setShiftName(shiftDto.getShiftName());

        when(shiftAssignmentsDao.save(any(ShiftAssignments.class))).thenReturn(shift);

        ResultResponse<ShiftAssignments> response = shiftAssignmentsService.assignShift(1, shiftDto);

        assertTrue(response.isSuccess());
        assertEquals("Shift is assigned successfully", response.getMessage());
        assertEquals(shift, response.getData());
    }

    @Test
    void testUpdatetable() throws Exception {
        // Convert String times to Time objects
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        java.util.Date startDateParsed = sdf.parse("09:00 AM");
        java.util.Date endDateParsed = sdf.parse("05:00 PM");

        Time startTime = new Time(startDateParsed.getTime());
        Time endTime = new Time(endDateParsed.getTime());

        ShiftRequest shiftRequest = new ShiftRequest();
        shiftRequest.setStartTime(startTime);
        shiftRequest.setEndTime(endTime);
        shiftRequest.setShiftRequestedName("Updated Shift");

        ShiftAssignments shiftAssignment = new ShiftAssignments();
        shiftAssignment.setEmployeeId(1);
        shiftAssignment.setStartTime(new Time(sdf.parse("08:00 AM").getTime()));
        shiftAssignment.setEndTime(new Time(sdf.parse("04:00 PM").getTime()));
        shiftAssignment.setShiftName("Original Shift");

        when(shiftRequestDao.findMostRecentApprovedShiftRequest(1)).thenReturn(Optional.of(shiftRequest));
        when(shiftAssignmentsDao.findTopByEmployeeIdOrderByEndDateDesc(1)).thenReturn(Optional.of(shiftAssignment));

        ShiftAssignments updatedAssignment = shiftAssignmentsService.updatetable(1);

        assertEquals("Updated Shift", updatedAssignment.getShiftName());
        assertEquals(startTime, updatedAssignment.getStartTime());
        assertEquals(endTime, updatedAssignment.getEndTime());
    }

    @Test
    void testGetAllShifts() {
        List<Integer> empIds = Arrays.asList(1, 2);
        ShiftAssignments shift1 = new ShiftAssignments();
        shift1.setEmployeeId(1);
        ShiftAssignments shift2 = new ShiftAssignments();
        shift2.setEmployeeId(2);

        when(employeeManagementClient.employeesUnderManager(100)).thenReturn(empIds);
        when(shiftAssignmentsDao.findByEmployeeId(1)).thenReturn(shift1);
        when(shiftAssignmentsDao.findByEmployeeId(2)).thenReturn(shift2);

        ResultResponse<List<ShiftAssignments>> response = shiftAssignmentsService.getAllShifts(100);

        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size());
    }

    @Test
    void testDeleteTable() throws Exception {
        ShiftRequest request = new ShiftRequest();
        when(shiftRequestDao.findMostRecentRejectedShiftRequest(1)).thenReturn(Optional.of(request));

        shiftAssignmentsService.deletetable(1);

        verify(shiftRequestDao, times(1)).delete(request);
    }
    @Test
    void testAddShift() {
        ShiftAssignments shift = new ShiftAssignments();
        shift.setShiftName("Morning Shift");

        when(shiftAssignmentsDao.save(shift)).thenReturn(shift);

        ShiftAssignments savedShift = shiftAssignmentsService.addShift(shift);

        assertEquals("Morning Shift", savedShift.getShiftName());
    }
    
    @Test
    void testGetShiftById() {
        ShiftAssignments shift = new ShiftAssignments();
        shift.setShiftName("Night Shift");

        when(shiftAssignmentsDao.findById(1)).thenReturn(Optional.of(shift));

        ShiftAssignments fetchedShift = shiftAssignmentsService.getShiftById(1);

        assertNotNull(fetchedShift);
        assertEquals("Night Shift", fetchedShift.getShiftName());
    }
    

    
    @Test
    void testGetEmployeeDataUnderManager() {
        List<Integer> empIds = Arrays.asList(1, 2);
        ShiftAssignments shift1 = new ShiftAssignments();
        shift1.setEmployeeId(1);
        ShiftAssignments shift2 = new ShiftAssignments();
        shift2.setEmployeeId(2);

        when(employeeManagementClient.employeesUnderManager(100)).thenReturn(empIds);
        when(shiftAssignmentsDao.findByEmployeeId(1)).thenReturn(shift1);
        when(shiftAssignmentsDao.findByEmployeeId(2)).thenReturn(shift2);

        List<ShiftAssignments> result = shiftAssignmentsService.getemployeedataundermanager(100);

        assertEquals(2, result.size());
    }
    
    

}
