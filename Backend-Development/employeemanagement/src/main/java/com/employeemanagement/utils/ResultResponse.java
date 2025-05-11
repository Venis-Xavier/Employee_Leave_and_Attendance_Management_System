package com.employeemanagement.utils;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultResponse<T> {
    private T data;
    private boolean success;
    private String message;
    private LocalDateTime timeStamp;
}
