package com.example.shiftmanagement.entity;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;

import com.example.shiftmanagement.entity.ShiftRequest.RequestStatus;

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
public class UpdatedRequests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UpdatedID")
    private int updatesId;

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
    
    @Column(name = "StartTime")
    private Time startTime;

    @Column(name = "EndTime")
    private Time endTime;
    
    @Column(name = "StartDate")
    private Date startDate;

    @Column(name = "EndDate")
    private Date endDate;
    
    public enum RequestStatus {
        APPROVED,
        REJECTED,
        PENDING
    }
}
