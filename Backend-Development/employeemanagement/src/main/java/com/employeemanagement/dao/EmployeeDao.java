package com.employeemanagement.dao;

import com.employeemanagement.dto.EmployeeDto;
import com.employeemanagement.dto.UserDto;
import com.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeDao extends JpaRepository<Employee, Integer> {

    @Query("select e from Employee e where e.employeeId = :employeeId")
    Optional<Employee> findByEmployeeId(@Param("employeeId") Integer employeeId);

    @Query("select e from Employee e where e.email= :email")
    Optional<Employee> findByEmailId(@Param("email") String email); // Use Spring Data JPA's built-in naming convention

    @Query("select e.employeeId from Employee e where e.manager.employeeId = :managerId")
    List<Integer> findEmployeesByManagerId(@Param("managerId") Integer managerId);

    	
//	@Query("select e.email from Employee as e where e.employeeId = :employeeId")
//	EmployeeDto findEmailById(@Param("employeeId") Integer employeeId);
	
	@Query("select e.employeeId from Employee e")
	List<Integer> getAllEmployeeIds();
	
	@Query("select e.manager.employeeId from Employee e where e.employeeId= :employeeId")
	Integer findManagerId(@Param("employeeId") Integer employeeId);
	
//	@Query("SELECT e.employeeId AS id, CONCAT(e.firstName, ' ', e.lastName) AS fullName FROM Employee e where e.manager.employeeId=:managerId")
//    List<Object[]> getEmployeeIdToFullNameMap(@Param("managerId") Integer managerId);
	@Query("SELECT e.employeeId AS id, CONCAT(e.firstName, ' ', e.lastName) AS fullName " +
		       "FROM Employee e " +
		       "WHERE e.manager.id = :managerId")
		List<Object[]> getEmployeeIdToFullNameMap(@Param("managerId") Integer managerId);
}
