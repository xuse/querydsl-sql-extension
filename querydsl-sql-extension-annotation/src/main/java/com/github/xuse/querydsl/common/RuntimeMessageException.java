package com.github.xuse.querydsl.common;

import lombok.Getter;

@Getter
public class RuntimeMessageException extends RuntimeException{
	private int code;
	
	public RuntimeMessageException(String message) {
		super(message);
	}
	
	public RuntimeMessageException(int code, String message) {
		super(message);
		this.code = code;
	}

	@Override
	public Throwable fillInStackTrace() {
		return this;
	}
}
