package com.researchspace.apiutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * General exception handlers for  RestControllers, converting standard exceptions and Spring MVC to ApiError objects.<br>
 * Projects should extend this class and annotate i with appropriate <code>@ControllerAdvice</code> annotations, overriding or adding additional handlers as needed.
 * Exceptions to HttpStatus codes are as following:
 * <table>
 * <tr> 
 *  <th> Exceptions</th><th>HttpStatus</th><th>Code</th></tr>
 *  <tr> <td> o.s.web.bind.MethodArgumentNotValidException</td><td>BAD_REQUEST</td><th>400</td></tr>
 *  <tr> <td> o.s.validation.BindException</td><td>BAD_REQUEST</td><th>400</td></tr>
 *  <tr> <td> o.s.beans.TypeMismatchException</td><td>BAD_REQUEST</td><th>400</td></tr>
 *  <tr> <td>o.s.web.multipart.support.MissingServletRequestPartException</td><td>BAD_REQUEST</td><th>400</td></tr>
 *  <tr> <td>o.s.web.bind.MissingServletRequestParameterException</td><td>BAD_REQUEST</td><th>400</td></tr>
 *  <tr> <td>o.s.web.bind.ServletRequestBindingException</td><td>BAD_REQUEST</td><th>400</td></tr>
 *  <tr> <td>o.s.web.method.annotation.MethodArgumentTypeMismatchException</td><td>BAD_REQUEST</td><th>400</td></tr>
 *  <tr> <td>javax.validation.ConstraintViolationException</td><td>BAD_REQUEST</td><th>400</td></tr>
 *  <tr> <td>o.s.http.converter.HttpMessageNotReadableException</td><td>BAD_REQUEST</td><th>400</td></tr>
 * <tr> <td>o.s.web.servlet.NoHandlerFoundException</td><td>NOT_FOUND</td><th>404</td></tr>
 *  <tr> <td>o.s.web.HttpRequestMethodNotSupportedException</td><td>METHOD_NOT_ALLOWED</td><th>405</td></tr>
 * <tr> <td>o.s.web.HttpRequestMethodNotSupportedException</td><td>UNSUPPORTED_MEDIA_TYPE</td><th>415</td></tr>
 * <tr> <td>java.lang.IllegalArgumentException</td><td>UNPROCESSABLE_ENTITY</td><th>422</td></tr>
 * <tr> <td>java.io.IOException</td><td>INTERNAL_SERVER_ERROR</td><th>500</td></tr>
 * <tr> <td>o.s.web.method.annotation.MethodArgumentConversionNotSupportedException</td><td>INTERNAL_SERVER_ERROR</td><th>500</td></tr>
 * <tr> <td>java.lang.Exception</td><td>INTERNAL_SERVER_ERROR</td><th>500</td></tr>
 *  </table>
 * 
 */
@Slf4j
public abstract class RestControllerAdvice extends ResponseEntityExceptionHandler {
	// 400
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		logException(ex);
		final List<String> errors = new ArrayList<>();
		for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.add(error.getField() + ": " + error.getDefaultMessage());
		}
		for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
		}
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.INVALID_METHOD_ARGUMENT.getCode(),
				ex.getLocalizedMessage(), errors);
		return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
	}


	@Override
	protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers,
			final HttpStatus status, final WebRequest request) {

		logException(ex);
		final List<String> errors = new ArrayList<>();
		List<BindError> bindErrors = new ArrayList<>();
		for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.add(error.getField() + ": " + error.getDefaultMessage());
			bindErrors.add(new BindError(error));	
		}

		for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
			bindErrors.add(new BindError(error));
		}
		BindErrorList errorList = new BindErrorList(bindErrors);

		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.INVALID_FIELD.getCode(),
				"Errors detected : " + ex.getErrorCount(), errors, errorList);
		return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
	}

	@Override

	protected ResponseEntity<Object> handleTypeMismatch(final TypeMismatchException ex, final HttpHeaders headers,
			final HttpStatus status, final WebRequest request) {

		logException(ex);
		final String error = ex.getValue() + " value for " + ex.getPropertyName() + " should be of type "
				+ ex.getRequiredType();
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.TYPE_MISMATCH.getCode(),
				ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestPart(final MissingServletRequestPartException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		log.info(ex.getClass().getName());
		final String error = ex.getRequestPartName() + " part is missing";
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.MISSING_MULTIPART.getCode(),
				ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
			final MissingServletRequestParameterException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		logException(ex);
		final String error = ex.getParameterName() + " parameter is missing";
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.MISSING_REQ_PARAMETER.getCode(),
				ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@ExceptionHandler({ MethodArgumentTypeMismatchException.class })
	public ResponseEntity<Object> handleMethodArgumentTypeMismatch(final MethodArgumentTypeMismatchException ex,
			final WebRequest request) {
		logException(ex);
		final String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.METHOD_MISMATCH.getCode(),
				ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());

	}

	@ExceptionHandler({ ConstraintViolationException.class })
	public ResponseEntity<Object> handleConstraintViolation(final ConstraintViolationException ex,
			final WebRequest request) {
		logException(ex);
		final List<String> errors = new ArrayList<>();
		for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
			errors.add(violation.getRootBeanClass().getName() + " " + violation.getPropertyPath() + ": "
					+ violation.getMessage());
		}
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.CONSTRAINT_VIOLATION.getCode(),
				ex.getLocalizedMessage(), errors);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		logException(ex);
		String error = "Either JSON syntax is invalid, or RSpace could not parse an expected date or numeric field";
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.UNREADABLE.getCode(),
				ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	// 404
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		logException(ex);
		final String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();
		final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ApiErrorCodes.NO_HANDLER.getCode(),
				ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	// 405
	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
			final HttpRequestMethodNotSupportedException ex, final HttpHeaders headers, final HttpStatus status,
			final WebRequest request) {
		log.info(ex.getClass().getName());
		final StringBuilder builder = new StringBuilder();
		builder.append(ex.getMethod());
		builder.append(" method is not supported for this request. Supported methods are ");
		ex.getSupportedHttpMethods().forEach(t -> builder.append(t).append(" "));

		final ApiError apiError = new ApiError(HttpStatus.METHOD_NOT_ALLOWED, ApiErrorCodes.NOT_ALLOWED.getCode(),
				ex.getLocalizedMessage(), builder.toString());
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	// 415
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex,
			final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
		logException(ex);
		final StringBuilder builder = new StringBuilder();
		builder.append(ex.getContentType());
		builder.append(" media type is not supported. Supported media types are ");
		ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(" "));

		final ApiError apiError = new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ApiErrorCodes.MEDIATYPE.getCode(),
				ex.getLocalizedMessage(), builder.substring(0, builder.length() - 2));
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	// 422
	@ExceptionHandler({ IllegalArgumentException.class })
	public ResponseEntity<Object> handleIllegalArgumen(final IllegalArgumentException ex, final WebRequest request) {
		log.error("error", ex);
		final ApiError apiError = new ApiError(HttpStatus.UNPROCESSABLE_ENTITY,
				ApiErrorCodes.ILLEGAL_ARGUMENT.getCode(), ex.getLocalizedMessage(), ex.getMessage());
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}

	// 500
	@ExceptionHandler({ IOException.class })
	public ResponseEntity<Object> handleIO(final Exception ex, final WebRequest request) {
		return handle500Error(ex, ApiErrorCodes.IO, "I/O error");
	}

	// 500
	@ExceptionHandler({ MethodArgumentConversionNotSupportedException.class })
	public ResponseEntity<Object> handleArgumentConversion(final Exception ex, final WebRequest request) {
		return handle500Error(ex, ApiErrorCodes.INTERNAL_ARGUMENT_CONVERSION, "Error converting argument");
	}

	protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		String error = "Missing required header or parameter - have you supplied an 'apiKey' header?";
		final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ApiErrorCodes.BINDING.getCode(),
				ex.getLocalizedMessage(), error);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());

	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> handleAll(final Exception ex, final WebRequest request) {
		return handle500Error(ex, ApiErrorCodes.GENERAL_ERROR, "General server error");
	}

	protected ResponseEntity<Object> handle500Error(final Exception ex, ApiErrorCodes code, String msg) {
		logException(ex);
		log.error("error", ex);
		final ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, code.getCode(),
				ex.getLocalizedMessage(), msg);
		return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
	}
	
	protected void logException(final Exception ex) {
		log.info(ex.getClass().getName());
	}

}
