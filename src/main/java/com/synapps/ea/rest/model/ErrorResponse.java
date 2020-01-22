package com.synapps.ea.rest.model;

/**
 * @author Mark Billingham
 *
 */
public class ErrorResponse {
	private String errorMessage;
	private String advice;
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getAdvice() {
		return advice;
	}
	public void setAdvice(String advice) {
		this.advice = advice;
	}
}
