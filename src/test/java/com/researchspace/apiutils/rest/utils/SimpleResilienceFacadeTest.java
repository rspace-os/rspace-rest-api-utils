package com.researchspace.apiutils.rest.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.researchspace.apiutils.ApiError;
import com.researchspace.apiutils.ApiErrorCodes;
import com.researchspace.core.util.JacksonUtil;

import io.vavr.control.Either;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

public class SimpleResilienceFacadeTest {
	
	int calls = 0;

	@BeforeEach
	public void setup () {
		calls = 0;
	}
	@Test
	public void retriesAreTriggeredFor5xxExceptions() {
		// make a call every 10ms that will fail
		SimpleResilienceFacade facade = new SimpleResilienceFacade(10, 10);
		Supplier<ResponseEntity<String>> exceptionThrowingCall = this::createFailed500ApiCall;
		Either<ApiError,String> resp = facade.makeApiCall(exceptionThrowingCall);
		assertTrue(resp.isLeft());
		assertEquals(3, calls);
	}
	
	@Test
	public void retriesAreNotTriggeredFor4xxExceptions() {
		// make a call every 10ms that will fail
		SimpleResilienceFacade facade = new SimpleResilienceFacade(10, 10);
		Supplier<ResponseEntity<String>> exceptionThrowingCall = this::createFailed400ApiCall;
		Either<ApiError,String> resp = facade.makeApiCall(exceptionThrowingCall);
		assertTrue(resp.isLeft());
		assertEquals(1, calls);
	}

	@Test
	public void resourceAccessExceptionReturnsCorrectApiError() {
		SimpleResilienceFacade facade = new SimpleResilienceFacade(10, 10);
		Supplier<ResponseEntity<String>> exceptionThrowingCall = this::createResourceAccessException;
		Either<ApiError,String> resp = facade.makeApiCall(exceptionThrowingCall);
		assertTrue(resp.isLeft());
		assertEquals(1, calls);
		assertEquals("Error Service Unreachable", resp.getLeft().getMessage());
	}

	@Test
	public void unknownHttpStatusCodeExceptionReturnsCorrectApiError() {
		SimpleResilienceFacade facade = new SimpleResilienceFacade(10, 10);
		Supplier<ResponseEntity<String>> exceptionThrowingCall = this::createUnknownHttpException;
		Either<ApiError,String> resp = facade.makeApiCall(exceptionThrowingCall);
		assertTrue(resp.isLeft());
		assertEquals(1, calls);
		assertEquals("Error an Unknown Http Status Code was Encountered", resp.getLeft().getMessage());
	}

	ResponseEntity<String> createFailed500ApiCall() {
		calls++;
		throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "some error",
				createAnApiError(HttpStatus.INTERNAL_SERVER_ERROR), Charset.defaultCharset());
	}
	
	ResponseEntity<String> createFailed400ApiCall() {
		calls++;
		throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "some error",
				createAnApiError(HttpStatus.BAD_REQUEST), Charset.defaultCharset());
	}

	ResponseEntity<String> createResourceAccessException() {
		calls++;
		throw new ResourceAccessException("", new IOException());
	}

	ResponseEntity<String> createUnknownHttpException() {
		calls++;
		throw new UnknownHttpStatusCodeException(1000, "", null, null, null);
	}

	byte[] createAnApiError(HttpStatus status) {
		ApiError error = new ApiError(status, ApiErrorCodes.ILLEGAL_ARGUMENT.getCode(), "message", "errorMsg");
		return JacksonUtil.toJson(error).getBytes();
	}

}
