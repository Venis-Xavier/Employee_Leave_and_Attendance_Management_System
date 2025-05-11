package com.empleavemanagement.reportsmodule.dto;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class Response<E> {
	private Boolean success;
	private HttpStatus status;
	private E data;
	private String errorMessage = "";
	
	/**
     * Constructor for a successful response.
     *
     * @param success whether the operation was successful
     * @param status the HTTP status of the response
     * @param data the data to be included in the response
     */
	
    public Response(Boolean success, HttpStatus status, E data) {
        this.success = success;
        this.status = status;
        this.data = data;
        this.errorMessage = null;
    }
	
    /**
     * Constructor for an error response.
     *
     * @param success whether the operation was successful
     * @param status the HTTP status of the response
     * @param errorMessage the error message to include in the response
     */
    public Response(Boolean success, HttpStatus status, String errorMessage) {
        this.success = success;
        this.status = status;
        this.data = null;
        this.errorMessage = errorMessage;
    }
	
}
 