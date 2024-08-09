package com.github.xuse.querydsl.sql;

import com.querydsl.core.types.Operator;

/**
 * 
 * This operator means this operation is a independent SQL statement.
 *
 */
public interface Statement extends Operator{
	@Override
	default Class<?> getType() {
		return Void.class;
	}
}
