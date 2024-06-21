package com.researchspace.apiutils;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
	
	private HttpStatus status;
	private int httpCode;
	private int internalCode;
	private String message;
	private String messageCode;
	private List<String> errors;
	private String iso8601Timestamp;

	/**
	 * optional, context-specific data
	 */
	private Object data;
	
	public ApiError(HttpStatus status, int internalCode, String message, String messageCode, List<String> errors, Object extraData) {
		this(status, status.value(), internalCode, message, messageCode, errors, getTimestamp(), extraData);
	}

	public ApiError(HttpStatus status, int internalCode, String message, String messageCode, String error) {
		this(status, internalCode, message, messageCode, Collections.singletonList(error), null);
	}

	public ApiError(HttpStatus status, int internalCode, String message, List<String> errors, Object extraData) {
		this(status, internalCode, message, null, errors, extraData);
	}

	public ApiError(HttpStatus status, int internalCode, String message, String error, Object extraData) {	
		this(status, internalCode, message, null, Collections.singletonList(error), extraData);
	}

	public ApiError(HttpStatus status, int internalCode, String message, List<String> errors) {
		this(status, internalCode, message, errors, null);
	}
	
	public ApiError(HttpStatus status, int internalCode, String message, String error) {	
		this(status, internalCode, message, (String) null, error);
	}

	private static String getTimestamp() {
		return Instant.now().toString();
	}

}
