package com.github.xuse.querydsl.enums;

import com.github.xuse.querydsl.types.CodeEnum;

public enum TaskStatus implements CodeEnum<TaskStatus>{
	INIT, RUNNING, FAIL, SUCCESS;

	@Override
	public int getCode() {
		return ordinal();
	}

}
