package com.leavemanagement.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.leavemanagement.utils.ResultResponse;

@ControllerAdvice
public class GlobalException {
	
	
	@ExceptionHandler(InvalidDateException.class)
	public ResponseEntity<?> InvalidDateException(Exception e){
		ResultResponse<Object> errorResponse = ResultResponse.builder()
				.message(e.getMessage())
				.success(false).timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorResponse);
	}
	
	@ExceptionHandler(LeaveRequestAvailable.class)
	public ResponseEntity<?> LeaveRequestAvailable(Exception e){
		ResultResponse<Object> errorResponse = ResultResponse.builder()
				.message(e.getMessage())
				.success(false).timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorResponse);
	}
	
	@ExceptionHandler(LeaveRequestNotFound.class)
	public ResponseEntity<?> LeaveRequestNotFound(Exception e){
		ResultResponse<Object> errorResponse = ResultResponse.builder()
				.message(e.getMessage())
				.success(false).timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
	}
	
	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<?> InsufficientBalanceException(Exception e){
		ResultResponse<Object> errorResponse = ResultResponse.builder()
				.message(e.getMessage())
				.success(false).timeStamp(LocalDateTime.now()).build();
		return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(errorResponse);
	}
}
