package com.github.xuse.querydsl.enums;

import org.springframework.expression.spel.ast.OpDec;

import com.github.xuse.querydsl.types.CodeEnum;

public enum Gender implements CodeEnum<Gender>{
	MALE,
	FEMALE;

	@Override
	public int getCode() {
		return ordinal();
	}
}
