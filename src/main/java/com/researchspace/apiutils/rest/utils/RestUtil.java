/*
 * 
 */
package com.researchspace.apiutils.rest.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import com.researchspace.apiutils.ApiError;
import com.researchspace.apiutils.ApiErrorCodes;
import com.researchspace.core.util.JacksonUtil;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.UnknownHttpStatusCodeException;

/** <pre>
Copyright 2016 ResearchSpace

Utility methods for REST clients

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
</pre>
*/
public class RestUtil {

    public static boolean isError(HttpStatus status) {
        HttpStatus.Series series = status.series();
        return (HttpStatus.Series.CLIENT_ERROR.equals(series)
                || HttpStatus.Series.SERVER_ERROR.equals(series));
    }
   /**
    * Generates an ApiError from a Throwable. If the throwable is an HttpStatusCodeException
    *  ( superclass of Spring Rest exception), then the exception response is converted to an ApiERror
    * <em>Note</em> This method should only be called by clients calling a webservice that will return serialized APiError
    *  objects.
    */
   public static ApiError fromException(Throwable e) {

		if (e instanceof HttpStatusCodeException) {
			HttpStatusCodeException ce = (HttpStatusCodeException) e;
			return JacksonUtil.fromJson(ce.getResponseBodyAsString(), ApiError.class);
		} else if (e instanceof ResourceAccessException) {
			return resourceAccessApiError(e);
		} else if (e instanceof UnknownHttpStatusCodeException) {
			return unknownHttpStatusApiError(e);
		} else {
			return defaultApiError(e);
		}
	}

	public static ApiError resourceAccessApiError(Throwable e) {
		return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCodes.GENERAL_ERROR.getCode(),
				"Error Service Unreachable", e.getMessage());
	}

	public static ApiError unknownHttpStatusApiError(Throwable e) {
		return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCodes.GENERAL_ERROR.getCode(),
				"Error an Unknown Http Status Code was Encountered", e.getMessage());
	}

	public static  ApiError defaultApiError(Throwable e) {
		return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCodes.GENERAL_ERROR.getCode(),
				"An exception was thrown from the service that could not be interpreted", e.getMessage());
	}
}
