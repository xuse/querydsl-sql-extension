package com.querydsl.core.types;

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
