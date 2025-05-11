package com.leavemanagement.serviceTest;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
 
import com.leavemanagement.client.EmployeeManagementClient;
import com.leavemanagement.dao.LeaveBalanceDao;
import com.leavemanagement.dto.LeaveBalanceDto;
import com.leavemanagement.entity.LeaveBalance;
import com.leavemanagement.entity.LeaveTypes;
import com.leavemanagement.service.LeaveBalanceService;
import com.leavemanagement.utils.ResultResponse;
 
class LeaveBalanceTest {
 
    @Mock
    private LeaveBalanceDao leaveBalanceDao;
 
    @Mock
    private EmployeeManagementClient employeeManagementClient;
    
    @Spy
    @InjectMocks
    private LeaveBalanceService leaveBalanceService;
 
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
 
    /**
     * Test for `convertToLeaveBalanceDto`
     */
    @Test
    void testConvertToLeaveBalanceDto() {
        // Mock data
        LeaveBalance leaveBalance = new LeaveBalance();
        leaveBalance.setEmployeeId(1); // Setting employee ID
        leaveBalance.setLeaveType(LeaveTypes.SICK_LEAVE); // Setting leave type
        leaveBalance.setBalance(10); // Setting balance
 
        // Call the method
        LeaveBalanceDto leaveBalanceDto = leaveBalanceService.convertToLeaveBalanceDto(leaveBalance);
 
        // Assertions
        assertEquals(leaveBalance.getEmployeeId(), leaveBalanceDto.getEmployeeId());
        assertEquals(leaveBalance.getLeaveType(), leaveBalanceDto.getLeaveTypes());
        assertEquals(leaveBalance.getBalance(), leaveBalanceDto.getBalance());
    }
 
 
    /**
     * Test for `checkBalanceOfAllTypes`
     */
    @Test
    void testCheckBalanceOfAllTypes_DefaultBalancesAdded() {
        // Mock data
        List<LeaveBalance> emptyList = new ArrayList<>();
        when(leaveBalanceDao.findByEmployeeId(1)).thenReturn(emptyList);
 
        // Create LeaveBalance objects using setters
        LeaveBalance sickLeave = new LeaveBalance();
        sickLeave.setEmployeeId(1);
        sickLeave.setLeaveType(LeaveTypes.SICK_LEAVE);
        sickLeave.setBalance(LeaveTypes.SICK_LEAVE.getDefaultDays());
 
        LeaveBalance paidLeave = new LeaveBalance();
        paidLeave.setEmployeeId(1);
        paidLeave.setLeaveType(LeaveTypes.PAID_LEAVE);
        paidLeave.setBalance(LeaveTypes.PAID_LEAVE.getDefaultDays());
 
        LeaveBalance casualLeave = new LeaveBalance();
        casualLeave.setEmployeeId(1);
        casualLeave.setLeaveType(LeaveTypes.CASUAL_LEAVE);
        casualLeave.setBalance(LeaveTypes.CASUAL_LEAVE.getDefaultDays());
 
        // Mock the DAO saveAll method to return the created objects
        when(leaveBalanceDao.saveAll(anyList())).thenReturn(Arrays.asList(sickLeave, paidLeave, casualLeave));
 
        // Call the method
        ResultResponse<List<LeaveBalanceDto>> result = leaveBalanceService.checkBalanceOfAllTypes(1);
 
        // Assertions
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(3, result.getData().size());
        assertEquals("Leave Balance is Fetched.", result.getMessage());
    }
 
 
    /**
     * Test for `checkBalanceOfType`
     */
    @Test
    void testCheckBalanceOfType_DefaultBalanceAdded() {
        // Mock data
        when(leaveBalanceDao.findByEmployeeIdAndLeaveType(1, LeaveTypes.PAID_LEAVE)).thenReturn(null);
 
        // Create LeaveBalance using default constructor and setters
        LeaveBalance defaultBalance = new LeaveBalance();
        defaultBalance.setEmployeeId(1); // Setting employee ID
        defaultBalance.setLeaveType(LeaveTypes.PAID_LEAVE); // Setting leave type
        defaultBalance.setBalance(LeaveTypes.PAID_LEAVE.getDefaultDays()); // Setting default balance
 
        // Mock the DAO save method
        when(leaveBalanceDao.save(any(LeaveBalance.class))).thenReturn(defaultBalance);
 
        // Call the method
        ResultResponse<LeaveBalanceDto> result = leaveBalanceService.checkBalanceOfType(1, LeaveTypes.PAID_LEAVE);
 
        // Assertions
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(LeaveTypes.PAID_LEAVE, result.getData().getLeaveTypes());
        assertEquals(LeaveTypes.PAID_LEAVE.getDefaultDays(), result.getData().getBalance());
    }
 
 
    /**
     * Test for `checkBalanceOfAllEmployeesUnderManager`
     */
    @Test
    void testCheckBalanceOfAllEmployeesUnderManager() {
        // Mock data for employees under the manager
        when(employeeManagementClient.employeesUnderManager(100)).thenReturn(Arrays.asList(1, 2));
 
        // Mock data for leave balances of each employee
        List<LeaveBalanceDto> employee1Balances = Arrays.asList(
            new LeaveBalanceDto(1, LeaveTypes.SICK_LEAVE, 5),
            new LeaveBalanceDto(1, LeaveTypes.CASUAL_LEAVE, 3)
        );
        List<LeaveBalanceDto> employee2Balances = Arrays.asList(
            new LeaveBalanceDto(2, LeaveTypes.PAID_LEAVE, 7),
            new LeaveBalanceDto(2, LeaveTypes.CASUAL_LEAVE, 4)
        );
 
        // Use `doReturn()` for stubbing methods on the spy object
        doReturn(ResultResponse.<List<LeaveBalanceDto>>builder()
                .data(employee1Balances)
                .build())
            .when(leaveBalanceService)
            .checkBalanceOfAllTypes(1);
 
        doReturn(ResultResponse.<List<LeaveBalanceDto>>builder()
                .data(employee2Balances)
                .build())
            .when(leaveBalanceService)
            .checkBalanceOfAllTypes(2);
 
        // Call the method under test
        ResultResponse<List<List<LeaveBalanceDto>>> result = leaveBalanceService.checkBalanceOfAllEmployeesUnderManager(100);
 
        // Assertions
        assertNotNull(result); // Verify the result is not null
        assertTrue(result.isSuccess()); // Ensure the success flag is true
        assertEquals(2, result.getData().size()); // Confirm there are two employees
        assertEquals("Leave Balance of all employees is Fetched.", result.getMessage()); // Verify the message
    }
 
 
 
 
    /**
     * Test for `updateLeaveBalance`
     */
    @Test
    void testUpdateLeaveBalance_SufficientBalance() {
        // Mock data
        LeaveBalance currentBalance = new LeaveBalance();
        currentBalance.setEmployeeId(1); // Setting employee ID
        currentBalance.setLeaveType(LeaveTypes.SICK_LEAVE); // Setting leave type
        currentBalance.setBalance(5); // Setting balance to 5
 
        // Mocking the DAO behavior
        when(leaveBalanceDao.findByEmployeeIdAndLeaveType(1, LeaveTypes.SICK_LEAVE)).thenReturn(currentBalance);
 
        // Call the service method
        leaveBalanceService.updateLeaveBalance(1, LeaveTypes.SICK_LEAVE, 3);
 
        // Assertions
        verify(leaveBalanceDao, times(1)).save(currentBalance); // Verify save was called once
        assertEquals(2, currentBalance.getBalance()); // Ensure balance is updated to 2
    }
 
 
    @Test
    void testUpdateLeaveBalance_InsufficientBalance() {
        // Mock data
        LeaveBalance currentBalance = new LeaveBalance();
        currentBalance.setEmployeeId(1); // Set employee ID
        currentBalance.setLeaveType(LeaveTypes.CASUAL_LEAVE); // Set leave type
        currentBalance.setBalance(2); // Set initial balance to 2
 
        // Mock behavior for DAO
        when(leaveBalanceDao.findByEmployeeIdAndLeaveType(1, LeaveTypes.CASUAL_LEAVE)).thenReturn(currentBalance);
 
        // Call the service method
        leaveBalanceService.updateLeaveBalance(1, LeaveTypes.CASUAL_LEAVE, 5);
 
        // Assertions
        verify(leaveBalanceDao, times(1)).save(currentBalance); // Verify save was called once
        assertEquals(0, currentBalance.getBalance()); // Ensure balance is updated to 0
    }
 
 
    /**
     * Test for `resetAllLeaveBalance`
     */
    @Test
    void testResetAllLeaveBalance() {
        // Mock data
        LeaveBalance balance1 = new LeaveBalance();
        balance1.setEmployeeId(1); // Setting employee ID
        balance1.setLeaveType(LeaveTypes.SICK_LEAVE); // Setting leave type
        balance1.setBalance(3); // Setting current balance
 
        LeaveBalance balance2 = new LeaveBalance();
        balance2.setEmployeeId(2); // Setting employee ID
        balance2.setLeaveType(LeaveTypes.CASUAL_LEAVE); // Setting leave type
        balance2.setBalance(0); // Setting current balance
 
        List<LeaveBalance> balances = Arrays.asList(balance1, balance2);
 
        // Mock behavior for findAll()
        when(leaveBalanceDao.findAll()).thenReturn(balances);
 
        // Call the service method
        leaveBalanceService.resetAllLeaveBalance();
 
        // Assertions
        verify(leaveBalanceDao, times(1)).saveAll(balances); // Verify saveAll is called once
        assertEquals(LeaveTypes.SICK_LEAVE.getDefaultDays(), balances.get(0).getBalance()); // Verify reset balance for Sick Leave
        assertEquals(LeaveTypes.CASUAL_LEAVE.getDefaultDays(), balances.get(1).getBalance()); // Verify reset balance for Casual Leave
    }
 
}