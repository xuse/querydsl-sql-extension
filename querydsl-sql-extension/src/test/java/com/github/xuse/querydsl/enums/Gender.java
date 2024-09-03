package com.github.xuse.querydsl.enums;

import com.github.xuse.querydsl.types.CodeEnum;

public enum Gender implements CodeEnum<Gender>{
	MALE,
	FEMALE;

	@Override
	public int getCode() {
		return ordinal();
	}
	
	
	public static void main(String[] args) {
	}
}
