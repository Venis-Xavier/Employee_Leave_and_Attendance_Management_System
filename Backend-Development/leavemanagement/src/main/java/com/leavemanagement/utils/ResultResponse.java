package com.leavemanagement.utils;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse<T> {
	private T data;
	private boolean success;
	private String message;
	private LocalDateTime timeStamp;

}
