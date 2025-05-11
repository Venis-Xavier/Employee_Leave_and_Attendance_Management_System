package com.leavemanagement.serviceTest;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
 
import com.leavemanagement.client.EmployeeManagementClient;
import com.leavemanagement.dao.LeaveRequestDao;
import com.leavemanagement.dto.LeaveBalanceDto;
import com.leavemanagement.dto.LeaveRequestDto;
import com.leavemanagement.entity.LeaveRequest;
import com.leavemanagement.entity.LeaveStatus;
import com.leavemanagement.entity.LeaveTypes;
import com.leavemanagement.exception.InsufficientBalanceException;
import com.leavemanagement.exception.InvalidDateException;
import com.leavemanagement.exception.LeaveRequestAvailable;
import com.leavemanagement.service.LeaveBalanceService;
import com.leavemanagement.service.LeaveRequestService;
import com.leavemanagement.utils.ResultResponse;
 
public class LeaveRequestTest {
 
    @Mock
    private EmployeeManagementClient employeeManagementClient;
 
    @Mock
    private LeaveBalanceService leaveBalanceService;
 
    @Mock
    private LeaveRequestDao leaveRequestDao;
 
    @InjectMocks
    private LeaveRequestService leaveRequestService;
 
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }
 
    @Test
    public void testRequestingLeave_Success() {
        // Mock data
        LeaveRequestDto requestDto = new LeaveRequestDto();
        requestDto.setEmployeeId(1);
        requestDto.setStartDate(LocalDate.now().plusDays(1));
        requestDto.setEndDate(LocalDate.now().plusDays(3));
        requestDto.setLeaveType(LeaveTypes.CASUAL_LEAVE);
 
        LeaveBalanceDto balanceDto = new LeaveBalanceDto();
        balanceDto.setLeaveTypes(LeaveTypes.CASUAL_LEAVE);
        balanceDto.setBalance(5);
        when(leaveBalanceService.checkBalanceOfAllTypes(1))
        .thenReturn(ResultResponse.<List<LeaveBalanceDto>>builder()
            .data(Arrays.asList(balanceDto))
            .build());
 
 
        when(leaveRequestDao.findAlreadyRequestedLeavesByDates(anyInt(), any(), any()))
                .thenReturn(Optional.of(Collections.emptyList()));
 
        // Call the method
        ResultResponse<LeaveRequestDto> response = leaveRequestService.requestingLeave(requestDto);
 
        // Assertions
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Leave request sent", response.getMessage());
        verify(leaveRequestDao, times(1)).save(any(LeaveRequest.class));
    }
 
    @Test
    public void testRequestingLeave_InvalidDates() {
        // Mock data
        LeaveRequestDto requestDto = new LeaveRequestDto();
        requestDto.setStartDate(LocalDate.now().minusDays(1)); // Past date
        requestDto.setEndDate(LocalDate.now());
 
        // Call the method and expect exception
        assertThrows(InvalidDateException.class, () -> leaveRequestService.requestingLeave(requestDto));
    }
 
    @Test
    public void testRequestingLeave_AlreadyRequested() {
        // Mock data
        LeaveRequestDto requestDto = new LeaveRequestDto();
        requestDto.setEmployeeId(1);
        requestDto.setStartDate(LocalDate.now().plusDays(1));
        requestDto.setEndDate(LocalDate.now().plusDays(3));
 
        LeaveRequest leaveRequest = new LeaveRequest();
        when(leaveRequestDao.findAlreadyRequestedLeavesByDates(anyInt(), any(), any()))
                .thenReturn(Optional.of(Arrays.asList(leaveRequest)));
 
        // Call the method and expect exception
        assertThrows(LeaveRequestAvailable.class, () -> leaveRequestService.requestingLeave(requestDto));
    }
 
    @Test
    public void testShowAllLeaveRequests_Success() {
        // Mock data
        List<Integer> employees = Arrays.asList(1, 2);
        when(employeeManagementClient.employeesUnderManager(1)).thenReturn(employees);
 
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(1);
        leaveRequest.setLeaveStatus(LeaveStatus.PENDING);
 
        when(leaveRequestDao.findByEmployeeId(1)).thenReturn(leaveRequest);
        when(leaveRequestDao.findByEmployeeId(2)).thenReturn(null);
 
        // Call the method
        ResultResponse<List<LeaveRequestDto>> response = leaveRequestService.showAllLeaveRequest(1);
 
        // Assertions
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
    }
    
    
    @Test
    public void testCancelLeaveRequest_Success() {
        // Mock data
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setLeaveStatus(LeaveStatus.PENDING);
 
        when(leaveRequestDao.findById(1)).thenReturn(Optional.of(leaveRequest));
 
        // Call the method
        ResultResponse<LeaveRequestDto> response = leaveRequestService.cancelLeaveRequest(1, 1);
 
        // Assertions
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Request Deleted Sucessfully", response.getMessage());
        verify(leaveRequestDao, times(1)).delete(leaveRequest);
    }
 
    @Test
    public void testApproveLeaveRequest_Success() {
        // Mock data
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(1);
        leaveRequest.setLeaveStatus(LeaveStatus.PENDING);
        leaveRequest.setLeaveType(LeaveTypes.CASUAL_LEAVE);
        leaveRequest.setDaysRequested(5);
 
        when(leaveRequestDao.findByEmployeeId(1)).thenReturn(leaveRequest);
 
        LeaveRequestDto requestDto = new LeaveRequestDto();
        requestDto.setLeaveStatus(LeaveStatus.APPROVED);
 
        // Call the method
        ResultResponse<LeaveRequestDto> response = leaveRequestService.approveOrRejectLeave(1, requestDto);
 
        // Assertions
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Leave APPROVED", response.getMessage());
        verify(leaveBalanceService, times(1)).updateLeaveBalance(1, LeaveTypes.CASUAL_LEAVE, 5);
    }
 
    @Test
    public void testRejectLeaveRequest_Success() {
        // Mock data
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(1);
        leaveRequest.setLeaveStatus(LeaveStatus.PENDING);
 
        when(leaveRequestDao.findByEmployeeId(1)).thenReturn(leaveRequest);
 
        LeaveRequestDto requestDto = new LeaveRequestDto();
        requestDto.setLeaveStatus(LeaveStatus.REJECTED);
 
        // Call the method
        ResultResponse<LeaveRequestDto> response = leaveRequestService.approveOrRejectLeave(1, requestDto);
 
        // Assertions
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Leave REJECTED", response.getMessage());
    }
 
    @Test
    public void testLeaveDetailsOfEmployeeInDateRange() {
        // Mock data
        LocalDate startDate = LocalDate.of(2023, 4, 1);
        LocalDate endDate = LocalDate.of(2023, 4, 10);
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployeeId(1);
        leaveRequest.setStartDate(startDate);
        leaveRequest.setEndDate(endDate);
 
        when(leaveRequestDao.findLeaveDetailsByEmployeeIdAndDates(startDate, endDate))
            .thenReturn(Arrays.asList(leaveRequest));
 
        // Call the method
        List<LeaveRequestDto> leaveDetails = leaveRequestService.leaveDetailsOfEmployeeInDateRange(startDate, endDate);
 
        // Assertions
        assertNotNull(leaveDetails);
        assertEquals(1, leaveDetails.size());
        assertEquals(1, leaveDetails.get(0).getEmployeeId());
    }
 
 
    
}