package com.researchspace.apiutils;



import static com.researchspace.core.util.TransformerUtils.toList;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.researchspace.core.util.JacksonUtil;

import lombok.AllArgsConstructor;
import lombok.Data;

public class ApiErrorTest {

	@Test
	public void testTimestampinIS8601() throws ParseException {
		ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.ILLEGAL_ARGUMENT.getCode(),"message", "errorMsg");
		String tsString = error.getIso8601Timestamp();
		assertNotNull(DateTimeFormatter.ISO_DATE_TIME.parse(tsString));
	}
	@Data
	@AllArgsConstructor
	static class SomeDataList {
		List<SomeData> dataList;
		
	}
	@Data
	@AllArgsConstructor
	static class SomeData {
		String fieldName;
		String message;	
	}
	
	@Test
	public void extraData() throws ParseException {
		SomeData d1 = new SomeData("name", "message");
		SomeData d2 = new SomeData("name2", "message2");
		SomeDataList errors = new SomeDataList(toList(d1,d2));
		ApiError error = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.ILLEGAL_ARGUMENT.getCode(),"message", "errorMsg", errors);
		assertTrue(JacksonUtil.toJsonWithoutEmptyFields(error).contains("\"dataList\":[{\"fieldName\":\"name\",\"message\":\"message\"}"));
		
	}


}
