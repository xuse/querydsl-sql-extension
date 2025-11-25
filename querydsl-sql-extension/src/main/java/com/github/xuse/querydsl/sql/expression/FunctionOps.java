package com.github.xuse.querydsl.sql.expression;

import com.querydsl.core.types.Operator;

/**
 * 扩展QueryDSL的一些常用函数
 * 
 * @author Joey
 *
 */
public enum FunctionOps implements Operator {
	IF_NULL(Object.class)
	;

	final Class<?> type;

	FunctionOps(Class<?> type) {
		this.type = type;
	}

	@Override
	public Class<?> getType() {
		return type;
	}
}
