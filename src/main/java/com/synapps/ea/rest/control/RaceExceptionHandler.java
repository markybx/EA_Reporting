package com.synapps.ea.rest.control;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.synapps.ea.rest.model.ErrorResponse;

/**
 * @author Mark Billingham
 *
 */
@ControllerAdvice
public class RaceExceptionHandler extends ResponseEntityExceptionHandler {
	Logger logger = Logger.getLogger(this.getClass());
	/**
	 * @param e
	 * @param request
	 * @return
	 */
	@ExceptionHandler({ RuntimeException.class })
	protected ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
		logger.error("Request error handled " + e.getMessage());
		logger.debug("Request error handled " + e.getMessage(), e);
		ErrorResponse response = new ErrorResponse();
		response.setErrorMessage(e.toString());
		response.setAdvice("Try again");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return handleExceptionInternal(e, response, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
	}
}
