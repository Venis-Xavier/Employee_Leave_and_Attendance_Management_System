package com.employee.exception;

public class CheckOutBeforeCheckInException extends RuntimeException{
	public CheckOutBeforeCheckInException(String msg) {
		super(msg);
	}
}

