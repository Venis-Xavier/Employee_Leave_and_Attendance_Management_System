package com.employeemanagement.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.employeemanagement.dao.EmployeeDao;
import com.employeemanagement.dto.EmployeeDto;
import com.employeemanagement.dto.UserDto;
import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Role;
import com.employeemanagement.entity.UserPrincipal;
import com.employeemanagement.exception.EmployeeNotFoundException;
import com.employeemanagement.exception.InvalidLoginException;
import com.employeemanagement.exception.PasswordIncorrectException;

import com.employeemanagement.utils.ResultResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class for managing employee login operations.
 * Provides methods to employee login and converting object to Dto.
 */
@Slf4j
@Service
public class EmployeeService implements UserDetailsService{
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Autowired
	private JWTService jwtService;
	@Lazy
	@Autowired
	private AuthenticationManager authManager;
	
	@Lazy
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	
	/**
     * This method checks the login credentials of an employee based on either employee ID or email.
     * 
     * @param employeeDto Contains the employee's login credentials (ID/Email and password).
     * @return A ResultResponse object with user details if login is successful, otherwise throws an exception.
     */
	/*public ResultResponse<UserDto> checkLogin(EmployeeDto employeeDto) {
		if(employeeDto.getEmployeeId()!=null) {
			Optional<Employee> employeeDetails = employeeDao.findByEmployeeId(employeeDto.getEmployeeId());
			if(employeeDetails.isPresent()) {
				if(employeeDto.getPassword() != null) {
					if(employeeDto.getPassword().equals(employeeDetails.get().getPassword())){
						UserDto userDetails = convertToUserDto(employeeDetails.get());
						ResultResponse result = ResultResponse.builder().data(userDetails).success(true).message("Login Successfull").timeStamp(LocalDateTime.now()).build();
						return result;
					}else {
						throw new PasswordIncorrectException("Password Incorrect");
					}
				}else {
					throw new InvalidLoginException("Invalid Login - Enter both Username and Password");
				}
				
			}else {
				throw new EmployeeNotFoundException("Employee not found - Invalid Employee ID");
			}
		}
		else if(employeeDto.getEmail() != null) {
			Optional<Employee> employeeDetails = employeeDao.findByEmailId(employeeDto.getEmail());
			if(employeeDetails.isPresent()) {
				if(employeeDto.getPassword() != null) {
					if(employeeDto.getPassword().equals(employeeDetails.get().getPassword())){
						UserDto userDetails = convertToUserDto(employeeDetails.get());
						ResultResponse result = ResultResponse.builder().data(userDetails).success(true).message("Login Successfull").timeStamp(LocalDateTime.now()).build();
						return result;
						
					}else {
						throw new PasswordIncorrectException("Password Incorrect");
					}
				}else {
					throw new InvalidLoginException("Invalid Login - Enter both Username and Password");
				}
				
			}else {
				throw new EmployeeNotFoundException("Employee not found - Invalid Email");
			}
		}else {
			throw new InvalidLoginException("Invalid Login - Enter both Username and Password");
		}
		
	}*/
	
	/**
     * Converts an Employee entity to a UserDto object for external representation.
     * 
     * @param e The Employee entity to be converted.
     * @return A UserDto object containing the employee's information.
     */
	public UserDto convertToUserDto(Employee e) {
		UserDto output = new UserDto();
		output.setEmployeeId(e.getEmployeeId());
		output.setFirstName(e.getFirstName());
		output.setLastName(e.getLastName());
		output.setEmail(e.getEmail());
		output.setGender(e.getGender());
		output.setManagerId(e.getManager().getEmployeeId());
		output.setRole(e.getRole());
		output.setDepartment(e.getDepartment());
		
		return output;
	}
	
	public UserDto convertToManagerDto(Employee e) {
		UserDto output = new UserDto();
		output.setEmployeeId(e.getEmployeeId());
		output.setFirstName(e.getFirstName());
		output.setLastName(e.getLastName());
		output.setEmail(e.getEmail());
		output.setGender(e.getGender());
		output.setRole(e.getRole());
		output.setDepartment(e.getDepartment());
		
		return output;
	}
	
	@Override
	public UserDetails loadUserByUsername(String useremail) throws EmployeeNotFoundException {
		Optional<Employee> user = employeeDao.findByEmailId(useremail);
 
		if(user == null) {
			throw new EmployeeNotFoundException("User Not Found....");
		}
		
		return new UserPrincipal(user.get());
	}
	
	public ResultResponse<String> verify(EmployeeDto employeeDto) {
		log.info("enterd in to verify");
		Authentication authentication= null;
		if(employeeDto.getEmail() != null) {
			authentication = authManager
		            .authenticate(new UsernamePasswordAuthenticationToken(employeeDto.getEmail(), employeeDto.getPassword()));
			log.info("enterd mail and password");
		}else if(employeeDto.getEmployeeId() != null) {
			Employee emp = employeeDao.findByEmployeeId(employeeDto.getEmployeeId()).get();
			log.info("enterd id and password");
			if(emp == null) {
				throw new EmployeeNotFoundException("Employee Not Found");
			}
						
			authentication = authManager
		            .authenticate(new UsernamePasswordAuthenticationToken(emp.getEmail(), employeeDto.getPassword()));
			
		}
		else {
			log.error("invalid login");
			throw new InvalidLoginException("Invalid Login");
		}
	    
	    if (!authentication.isAuthenticated()) {
	        throw new EmployeeNotFoundException("Invalid Login Credentials!");
	    }
	    Employee authUser = null;
	    if(employeeDto.getEmail() != null) {
		    authUser = employeeDao.findByEmailId(employeeDto.getEmail()).get();
	    }else if(employeeDto.getEmployeeId() != null) {
	    	authUser = employeeDao.findByEmployeeId(employeeDto.getEmployeeId()).get();
	    }

	    ResultResponse loggedin = ResultResponse.builder().data(jwtService.getToken(authUser.getEmployeeId().toString(), authUser.getRole(), authUser.getEmail())).success(true).message("Logged in successfully").timeStamp(LocalDateTime.now()).build();
	    return loggedin;
	}
	
	
	public List<Integer> employeesUnderManager(Integer managerId){
		log.info("employee under manager service class" + managerId);
		List<Integer> result = employeeDao.findEmployeesByManagerId(managerId);
		log.info("result: "+result);
		return result;
	}
	
	public ResultResponse<UserDto> employeeDetails(Integer employeeId) {
		log.info("employee details method in service");
		Employee empDetails = employeeDao.findByEmployeeId(employeeId).get();
		if(empDetails.getRole() == Role.EMPLOYEE) {
			ResultResponse result = ResultResponse.builder().data(convertToUserDto(empDetails)).success(true).message("Profile fetched Successfully").timeStamp(LocalDateTime.now()).build();
			return result;
		}
		ResultResponse result =  ResultResponse.builder().data(convertToManagerDto(empDetails)).success(true).message("Profile fetched Successfully").timeStamp(LocalDateTime.now()).build();
		return result;
	}
	
	
	public List<Integer> getAllEmployeeIds(){
		return employeeDao.getAllEmployeeIds();
	}
	
	public Integer findManagerId(Integer employeeId) {
		return employeeDao.findManagerId(employeeId);
	}
	
//	public Map<Integer, String> getAllNamesAndIds(Integer managerId) {
//        List<Object[]> results = employeeDao.getEmployeeIdToFullNameMap(managerId);
//        return results.stream()
//                .collect(Collectors.toMap(
//                        result -> (Integer) result[0], // Key: employeeId
//                        result -> (String) result[1]   // Value: fullName
//                ));
//	}
	
	public Map<Integer, String> getAllNamesAndIds(Integer managerId) {
        List<Object[]> results = employeeDao.getEmployeeIdToFullNameMap(managerId);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0], // Key: employeeId
                        result -> (String) result[1]   // Value: fullName
                ));
	}
	public List<UserDto> employeeDetailsUnderManager(Integer managerId){
		List<Integer> empids = employeesUnderManager(managerId);
		List<UserDto> result = new ArrayList<>();
		for(Integer id: empids) {
			result.add(employeeDetails(id).getData());
		}
		return result;
	}
}