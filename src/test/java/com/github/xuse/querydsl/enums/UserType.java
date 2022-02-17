package com.github.xuse.querydsl.enums;

import com.github.xuse.querydsl.types.CodeEnum;

public enum UserType implements CodeEnum<UserType> {
	FREE(1), PAID(2);

	private int code;

	UserType(int code) {
		this.code = code;
	}

	@Override
	public int getCode() {
		return code;
	}

}
