package com.example.shiftmanagement.serviceTest;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.example.shiftmanagement.client.EmployeeManagementClient;
import com.example.shiftmanagement.dao.ShiftAssignmentsDao;
import com.example.shiftmanagement.dao.ShiftRequestDao;
import com.example.shiftmanagement.dao.UpdatedRequestsDao;
import com.example.shiftmanagement.dto.ShiftRequestDto;
import com.example.shiftmanagement.entity.ShiftAssignments;
import com.example.shiftmanagement.entity.ShiftRequest;
import com.example.shiftmanagement.entity.UpdatedRequests;
import com.example.shiftmanagement.exception.CannotUpdateStatusException;
import com.example.shiftmanagement.service.ShiftAssignmentsService;
import com.example.shiftmanagement.service.ShiftRequestService;
import com.example.shiftmanagement.utils.ResultResponse;

class ShiftRequestServiceTest {

    @InjectMocks
    private ShiftRequestService shiftRequestService;

    @Mock
    private ShiftRequestDao shiftRequestDao;

    @Mock
    private UpdatedRequestsDao updatedRequestsDao;

    @Mock
    private ShiftAssignmentsService assignService;

    @Mock
    private ShiftAssignmentsDao shiftAssignmentsDao;

    @Mock
    private EmployeeManagementClient employeeManagementClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateShiftRequest() {
        ShiftRequestDto requestDto = new ShiftRequestDto();
        requestDto.setShiftRequestedName("Night Shift");

        ShiftAssignments assignedShift = new ShiftAssignments();
        assignedShift.setShiftName("Morning Shift");

        ResultResponse<ShiftAssignments> assignmentResponse =
                ResultResponse.<ShiftAssignments>builder()
                        .data(assignedShift)
                        .success(true)
                        .message("Shift found")
                        .timestamp(LocalDateTime.now())
                        .build();

        when(assignService.getAssignmentsByEmployeeId(1)).thenReturn(assignmentResponse);
        when(shiftRequestDao.save(any(ShiftRequest.class))).thenReturn(new ShiftRequest());

        ResultResponse<ShiftRequest> response = shiftRequestService.createShiftRequest(1, requestDto);

        assertTrue(response.isSuccess());
        assertEquals("Shift request created successfully", response.getMessage());
    }

    @Test
    void testGetRequestsByEmployeeId() {
        ShiftRequest shiftRequest = new ShiftRequest();
        shiftRequest.setShiftRequestedName("Evening Shift");

        when(shiftRequestDao.findByEmployeeId(1)).thenReturn(shiftRequest);

        ResultResponse<ShiftRequest> response = shiftRequestService.getRequestsByEmployeeId(1);

        assertTrue(response.isSuccess());
        assertEquals("Evening Shift", response.getData().getShiftRequestedName());
    }

    @Test
    void testGetAllShifts() {
        List<Integer> employeeIds = Arrays.asList(1, 2);
        ShiftRequest shift1 = new ShiftRequest();
        shift1.setEmployeeId(1);
        ShiftRequest shift2 = new ShiftRequest();
        shift2.setEmployeeId(2);

        when(employeeManagementClient.employeesUnderManager(100)).thenReturn(employeeIds);
        when(shiftRequestDao.findByEmployeeId(1)).thenReturn(shift1);
        when(shiftRequestDao.findByEmployeeId(2)).thenReturn(shift2);

        ResultResponse<List<ShiftRequest>> response = shiftRequestService.getAllShifts(100);

        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size());
    }

    @Test
    void testApproveOrRejectLeave() throws CannotUpdateStatusException {
        ShiftRequest shiftRequest = new ShiftRequest();
        shiftRequest.setStatus(ShiftRequest.RequestStatus.PENDING);
        shiftRequest.setShiftRequestedName("Updated Shift");

        // Convert LocalDateTime to java.sql.Date
        Date startDate = Date.valueOf(LocalDateTime.now().minusDays(5).toLocalDate());
        Date endDate = Date.valueOf(LocalDateTime.now().plusDays(5).toLocalDate());

        ShiftAssignments shiftAssignment = new ShiftAssignments();
        shiftAssignment.setStartDate(startDate);
        shiftAssignment.setEndDate(endDate);

        ShiftRequestDto shiftRequestDto = new ShiftRequestDto();
        shiftRequestDto.setStatus(ShiftRequest.RequestStatus.APPROVED);

        when(shiftRequestDao.findByEmployeeId(1)).thenReturn(shiftRequest);
        when(shiftAssignmentsDao.findByEmployeeId(1)).thenReturn(shiftAssignment);
        when(shiftRequestDao.save(shiftRequest)).thenReturn(shiftRequest);

        ResultResponse<ShiftRequest> response = shiftRequestService.approveOrRejectLeave(1, shiftRequestDto);

        assertTrue(response.isSuccess());
        assertEquals(ShiftRequest.RequestStatus.APPROVED, response.getData().getStatus());
    }
}