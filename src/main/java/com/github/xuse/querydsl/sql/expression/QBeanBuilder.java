package com.github.xuse.querydsl.sql.expression;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;

public class QBeanBuilder {
	private final Expression<?>[] exprs;
	
	QBeanBuilder(Expression<?>[] expressions){
		this.exprs=expressions;
	}
	
	/**
	 * 构造
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T> QBeanEx<T> as(Class<T> type){
		return new QBeanEx<T>(type, exprs);
	}
	
	/**
	 * 构造
	 * @param <T>
	 * @param type
	 * @return
	 */
	public <T> QBeanEx<T> as(Path<? extends T> type){
		return new QBeanEx<T>(type.getType(), exprs);
	}
}
