package com.employee.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attendance_records")
public class AttendanceRecords {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RecordID")
    private int recordId;
    
    
    private Integer employeeId;
//    @Transient
//    private Integer managerId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "ClockInTime")
    private LocalDateTime clockInTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "ClockOutTime")
    private LocalDateTime clockOutTime;
    
    @Column(name = "WorkHours")
    private double workHours;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private AttendanceStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "Date")
    private LocalDate date;  // Added LocalDate field to store attendance date
    
    public enum AttendanceStatus {
        PRESENT,
        ABSENT
    }
    
 // Include employeeId in the JSON response
  //  @JsonInclude
  //  public Integer getEmployeeId() {
  //      return employee != null ? employee.getEmployeeId() : null;
 //   }
}
