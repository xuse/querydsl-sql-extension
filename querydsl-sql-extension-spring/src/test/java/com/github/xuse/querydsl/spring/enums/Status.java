package com.github.xuse.querydsl.spring.enums;

import com.github.xuse.querydsl.types.CodeEnum;

public enum Status implements CodeEnum<Status>{
	INIT, RUNNING, FAIL, SUCCESS;

	@Override
	public int getCode() {
		return ordinal();
	}

}
