package com.employee.exception;
public class InvalidAttendanceDateException extends RuntimeException{
	public InvalidAttendanceDateException(String msg) {
		super(msg);
	}
}
