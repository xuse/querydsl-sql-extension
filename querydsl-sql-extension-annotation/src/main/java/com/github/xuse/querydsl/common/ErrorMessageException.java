package com.github.xuse.querydsl.common;

import lombok.Getter;

@Getter
public final class ErrorMessageException extends Exception{
	private int code;
	
	public ErrorMessageException(String message) {
		super(message);
	}
	
	public ErrorMessageException(int code, String message) {
		super(message);
		this.code = code;
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
