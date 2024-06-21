package com.researchspace.apiutils;

import java.util.List;

import lombok.Value;

@Value
public class BindErrorList {
	List<BindError> validationErrors;

}
