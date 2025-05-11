package com.employee.exception;

public class DuplicateEntryException extends RuntimeException{
	public DuplicateEntryException(String msg) {
		super(msg);
	}
}

