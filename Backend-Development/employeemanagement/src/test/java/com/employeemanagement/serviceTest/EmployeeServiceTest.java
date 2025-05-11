package com.employeemanagement.serviceTest;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
import java.util.*;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
 
import com.employeemanagement.dao.EmployeeDao;
import com.employeemanagement.dto.EmployeeDto;
import com.employeemanagement.dto.UserDto;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Role;
import com.employeemanagement.exception.EmployeeNotFoundException;
import com.employeemanagement.exception.InvalidLoginException;
import com.employeemanagement.exception.PasswordIncorrectException;
import com.employeemanagement.service.EmployeeService;
import com.employeemanagement.service.JWTService;
import com.employeemanagement.utils.ResultResponse;
 
public class EmployeeServiceTest {
 
    @Mock
    private EmployeeDao employeeDao;
 
    @Mock
    private JWTService jwtService;
 
    @Mock
    private AuthenticationManager authManager;
 
    @InjectMocks
    private EmployeeService employeeService;
 
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
 
    /**
     * Test for verify method with valid email and password
     */
    @Test
    void testVerifyWithValidEmail() {
        // Mock data
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmail("test@example.com");
        employeeDto.setPassword("password123");
 
        Employee employee = new Employee();
        employee.setEmployeeId(1);
        employee.setEmail("test@example.com");
        employee.setRole(Role.EMPLOYEE);
 
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
 
        when(authManager.authenticate(any())).thenReturn(authentication);
        when(employeeDao.findByEmailId("test@example.com")).thenReturn(Optional.of(employee));
        when(jwtService.getToken("1", Role.EMPLOYEE, "test@example.com")).thenReturn("mockToken");
 
        // Call the method
        ResultResponse<String> result = employeeService.verify(employeeDto);
 
        // Assertions
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("mockToken", result.getData());
        assertEquals("Logged in successfully", result.getMessage());
    }
 
    /**
     * Test for verify method with invalid login
     */
    @Test
    void testVerifyWithInvalidLogin() {
        // Mock data
        EmployeeDto employeeDto = new EmployeeDto();
        employeeDto.setEmail("test@example.com");
        employeeDto.setPassword("wrongpassword");
 
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
 
        when(authManager.authenticate(any())).thenReturn(authentication);
 
        // Call the method and assert exception
        assertThrows(EmployeeNotFoundException.class, () -> employeeService.verify(employeeDto));
    }
 
    /**
     * Test for employeeDetails method with valid employee ID
     */
    @Test
    void testEmployeeDetailsValidId() {
        // Mock data
    	Employee manager = new Employee();
    	manager.setEmployeeId(11);
    	manager.setDepartment("IT");
    	manager.setFirstName("Akhila");
    	manager.setLastName("Nidumolu");
    	manager.setRole(Role.MANAGER);
        Employee employee = new Employee();
        employee.setEmployeeId(1);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setRole(Role.EMPLOYEE);
        employee.setManager(manager);
 
        when(employeeDao.findByEmployeeId(1)).thenReturn(Optional.of(employee));
 
        // Call the method
        ResultResponse<UserDto> result = employeeService.employeeDetails(1);
 
        // Assertions
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Profile fetched Successfully", result.getMessage());
        assertEquals(1, result.getData().getEmployeeId());
    }
 
    /**
     * Test for employeeDetails method with invalid employee ID
     */
    @Test
    void testEmployeeDetailsInvalidId() {
        // Mock behavior
        when(employeeDao.findByEmployeeId(999)).thenReturn(Optional.empty());
 
        // Call the method and assert exception
        assertThrows(NoSuchElementException.class, () -> employeeService.employeeDetails(999));
    }
 
    
 
    /**
     * Test for employeesUnderManager method
     */
    @Test
    void testEmployeesUnderManager() {
        // Mock data
        when(employeeDao.findEmployeesByManagerId(100)).thenReturn(Arrays.asList(1, 2, 3));
 
        // Call the method
        List<Integer> employees = employeeService.employeesUnderManager(100);
 
        // Assertions
        assertNotNull(employees);
        assertEquals(3, employees.size());
        assertTrue(employees.contains(1));
        assertTrue(employees.contains(2));
        assertTrue(employees.contains(3));
    }
 
    /**
     * Test for getAllNamesAndIds method
     */
    @Test
    void testGetAllNamesAndIds() {
        // Mock data
        List<Object[]> results = Arrays.asList(
            new Object[]{1, "John Doe"},
            new Object[]{2, "Jane Smith"}
        );
        when(employeeDao.getEmployeeIdToFullNameMap(100)).thenReturn(results);
 
        // Call the method
        Map<Integer, String> namesAndIds = employeeService.getAllNamesAndIds(100);
 
        // Assertions
        assertNotNull(namesAndIds);
        assertEquals(2, namesAndIds.size());
        assertEquals("John Doe", namesAndIds.get(1));
        assertEquals("Jane Smith", namesAndIds.get(2));
    }
}