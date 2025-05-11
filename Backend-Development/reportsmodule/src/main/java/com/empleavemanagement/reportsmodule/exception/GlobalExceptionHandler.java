package com.empleavemanagement.reportsmodule.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.empleavemanagement.reportsmodule.dto.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidMonthException.class)
    public ResponseEntity<Response<?>> handleInvalidMonthException(InvalidMonthException e) {
        log.warn("InvalidMonthException: {}", e.getMessage());
        return new ResponseEntity<>(new Response<>(false, HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<Response<?>> handleReportNotFoundException(ReportNotFoundException e) {
        log.error("ReportNotFoundException: {}", e.getMessage());
        return new ResponseEntity<>(new Response<>(false, HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<?>> handleGenericException(Exception e) {
        log.error("Unhandled exception: {}", e.getMessage());
        return new ResponseEntity<>(new Response<>(false, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurreds"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleInvalidDateFormat(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body("Invalid date format. Please provide a valid date in the format YYYY-MM-DD.");
    }
}
