package com.researchspace.apiutils;

/**
 * RSpace-specific error codes for more detailed error reporting that Http statuses alone<br>
 * Convention is that 1st 3 digits match the Http status code, then a 2 digit suffix is used to provide more specificity
 *
 */
public enum ApiErrorCodes {
	//client errors
	INVALID_METHOD_ARGUMENT(400_01),
	INVALID_FIELD(400_02), TYPE_MISMATCH(400_03), MISSING_MULTIPART(400_04),
	MISSING_REQ_PARAMETER(400_05), METHOD_MISMATCH(400_06), CONSTRAINT_VIOLATION(400_07),
	BINDING(400_08),
	UNREADABLE(400_09),
	AUTH(401_01),
	NO_HANDLER(404_01),
	RESOURCE_NOT_FOUND(404_02),
	NOT_ALLOWED(405_01),
    EDIT_CONFLICT(409_01),
	MEDIATYPE(415_01),
	ILLEGAL_ARGUMENT(422_01),
	TOOMANY_REQUESTS(429_01),
	MAX_FILE_UPLOAD_RATE_EXCEEDED(429_02),
	
	// server errors
	GENERAL_ERROR(500_01),
	IO(500_02), 
	INTERNAL_ARGUMENT_CONVERSION(500_03), 
	BATCH_LAUNCH(500_04),
	CONFIGURED_UNAVAILABLE(503_01)
	;
	
	private final int code;
	public int getCode() {
		return code;
	}
	
	ApiErrorCodes(int code) {
		this.code = code;
	}
}
