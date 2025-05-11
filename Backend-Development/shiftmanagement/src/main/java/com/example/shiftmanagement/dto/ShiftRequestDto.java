package com.example.shiftmanagement.dto;

import java.sql.Time;
import com.example.shiftmanagement.entity.ShiftRequest.RequestStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

//@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShiftRequestDto {
    public String getShiftRequestedName() {
		return shiftRequestedName;
	}
	public void setShiftRequestedName(String shiftRequestedName) {
		this.shiftRequestedName = shiftRequestedName;
	}
	public Time getStartTime() {
		return startTime;
	}
	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}
	public Time getEndTime() {
		return endTime;
	}
	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}
	public RequestStatus getStatus() {
		return status;
	}
	public void setStatus(RequestStatus status) {
		this.status = status;
	}
	public Integer getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}
	public String getShiftName() {
		return shiftName;
	}
	public void setShiftName(String shiftName) {
		this.shiftName = shiftName;
	}
	private String shiftRequestedName;
    private Time startTime;
  private Time endTime;

  @Enumerated(EnumType.STRING)
    private RequestStatus status;
    private Integer employeeId;
    private String shiftName;
}
