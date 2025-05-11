package com.employeemanagement.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.employeemanagement.utils.ResultResponse;


@ControllerAdvice
public class GobalException {

	@ExceptionHandler(EmployeeNotFoundException.class)
	public ResponseEntity<?> EmployeeNotFoundException(Exception e){
		ResultResponse<Object> errorResponse = ResultResponse.builder()
				.message(e.getMessage())
				.success(false).timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}
	@ExceptionHandler(InvalidLoginException.class)
	public ResponseEntity<?> InvalidLoginException(Exception e){
		ResultResponse<Object> errorResponse = ResultResponse.builder()
				.message(e.getMessage())
				.success(false).timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}
	
	@ExceptionHandler(PasswordIncorrectException.class)
	public ResponseEntity<?> PasswordIncorrectException(Exception e){
		ResultResponse<Object> errorResponse = ResultResponse.builder()
				.message(e.getMessage())
				.success(false).timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorResponse);
	}
	
}
