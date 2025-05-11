package com.leavemanagement.entity;

public enum LeaveTypes {
	SICK_LEAVE(3), CASUAL_LEAVE(5), PAID_LEAVE(2);
	
	private int defaultDays;
	
	LeaveTypes(int defaultDays){
			this.defaultDays = defaultDays;
		}
		
		public int getDefaultDays() {
			return defaultDays;
		}
		public void setDefaultDays(int defaultDays) {
			this.defaultDays=defaultDays;
		}
}
