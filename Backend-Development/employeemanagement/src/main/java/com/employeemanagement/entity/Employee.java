package com.employeemanagement.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
	
	@Id
	@Column(name="EmployeeId")
	private Integer employeeId;
	
	@ManyToOne
	@JoinColumn(name="ManagerId",referencedColumnName="EmployeeId")
	@JsonManagedReference
	private Employee manager;
	
	@Column(name="FirstName", nullable=false, length=100)
	private String firstName;
	
	@Column(name="LastName",nullable=false,length=100)
	private String lastName;
	
	@Enumerated(EnumType.STRING)
	@Column(name="Gender")
	private Gender gender;
	
	@Column(name="Email",nullable=false,length=100,unique=true)
	private String email;
	
	@Column(name="Department",length=100)
	private String department;
	
	@Column(name="Password",nullable=false)
	private String password;
	
	@Enumerated(EnumType.STRING)
	@Column(name="Role")
	private Role role;
	
	@JsonBackReference
	@OneToMany(mappedBy="manager")
	private List<Employee> employees;
	
}
