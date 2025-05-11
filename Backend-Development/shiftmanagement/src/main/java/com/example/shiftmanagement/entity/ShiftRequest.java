package com.example.shiftmanagement.entity;

import java.sql.Time;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ShiftRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ShiftRequestedID")
    private int shiftRequestedId;

    @Column(name = "ShiftRequestedName", length = 255)
    private String shiftRequestedName;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private RequestStatus status;

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
    private Integer employeeId;

    private Integer AssignedShiftId;
    
    private String AssignedShiftName;
    @Column(name = "StartTime")
    private Time startTime;

    @Column(name = "EndTime")
    private Time endTime;
    
    private LocalDateTime timestamp;
    
    public enum RequestStatus {
        APPROVED,
        REJECTED,
        PENDING
    }
}
