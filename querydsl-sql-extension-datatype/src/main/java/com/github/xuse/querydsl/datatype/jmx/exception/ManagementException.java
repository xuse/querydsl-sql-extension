package com.github.xuse.querydsl.datatype.jmx.exception;

public class ManagementException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ManagementException() {
	}

	public ManagementException(String message) {
		super(message);
	}

	public ManagementException(Throwable cause) {
		super(cause);
	}

	public ManagementException(String message, Throwable cause) {
		super(message, cause);
	}
}
