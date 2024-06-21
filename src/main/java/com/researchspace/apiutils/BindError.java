package com.researchspace.apiutils;

import java.util.function.Supplier;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Stores data related to Spring Binding result. Any of all of these fields can
 * be empty.
 */
@ToString
@EqualsAndHashCode
@Getter
public class BindError {

	private String field;
	private Object rejectedValue;
	private String objectName;
	private String message;

	/**
	 * Sets the error's default message to be the message
	 *
   */
	public BindError(FieldError error) {
		this(error, error::getDefaultMessage);
	}

	/**
	 * Sets the error's default message to be the message
	 *
   */
	public BindError(ObjectError error) {
		this(error, error::getDefaultMessage);
	}

	/**
	 * Sets the error's default message to that resolved by messageResolver
	 *
   */
	public BindError(FieldError error, Supplier<String> messageResolver) {
		this.field = error.getField();
		this.rejectedValue = error.getRejectedValue();
		this.message = messageResolver.get();
		this.objectName = error.getObjectName();
	}

	/**
	 * Sets the error's default message to that resolved by messageResolver
	 *
   */
	public BindError(ObjectError error, Supplier<String> messageResolver) {
		this.message = messageResolver.get();
		this.objectName = error.getObjectName();
	}

}
