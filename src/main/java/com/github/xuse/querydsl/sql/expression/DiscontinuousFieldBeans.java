package com.github.xuse.querydsl.sql.expression;

import java.util.List;
import java.util.Map;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Visitor;
import com.querydsl.sql.RelationalPath;

import lombok.AllArgsConstructor;

public abstract class DiscontinuousFieldBeans<T> extends FactoryExpressionBase<T> {
	private static final long serialVersionUID = -4411839816134215923L;

	protected final Map<RelationalPath<?>, QValuesBuildBean> qBeans;

	private final List<Expression<?>> expressions;

	@AllArgsConstructor
	static class QValuesBuildBean {
		final int[] indexes;
		final QBeanEx<?> qbean;

		public Object newInstance(Object... args) {
			int[] indexes = this.indexes;
			int length = indexes.length;
			Object[] values = new Object[length];
			for (int i = 0; i < length; i++) {
				values[i] = args[indexes[i]];
			}
			return qbean.newInstance(values);
		}
	}

	public DiscontinuousFieldBeans(Class<T> type,List<Expression<?>> expressions, Map<RelationalPath<?>, QValuesBuildBean> beans) {
		super(type);
		this.expressions = expressions;
		this.qBeans = beans;
	}


	@Override
	public <R, C> R accept(Visitor<R, C> v, C context) {
		return v.visit(this, context);
	}

	@Override
	public List<Expression<?>> getArgs() {
		return expressions;
	}
	
	
}
