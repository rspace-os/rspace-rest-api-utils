package com.researchspace.apiutils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.request.WebRequest;


class RestControllerAdviceTest {
	 WebRequest request;
	@BeforeEach
	void setupmocks (){
		request = Mockito.mock(WebRequest.class);
	}
	
	@Test
	void testHandleBindException (){
		RestControllerAdvice advice = new RestControllerAdvice() {};
		BindException be = createTwoBindErrors();
		ResponseEntity<Object> resultEntity = advice.handleBindException(be, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
		ApiError error = (ApiError)resultEntity.getBody();
		assertEquals(2, error.getErrors().size());
		BindErrorList dataBindErrorList = (BindErrorList)error.getData();
		assertEquals(2, dataBindErrorList.getValidationErrors().size());
	}

	private BindException createTwoBindErrors() {
		Object toValidate = "somethingToValidate";
		BindingResult br = new BeanPropertyBindingResult(toValidate, "x");
		br.addError(new FieldError("x", "f1", "f1-error"));
		br.addError(new ObjectError("x",  "f1-error"));
		BindException be = new BindException(br);
		return be;
	}

}
