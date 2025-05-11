package com.leavemanagement.exception;

public class LeaveRequestAvailable extends RuntimeException{
	
	public LeaveRequestAvailable(String message) {
		super(message);
	}

}
