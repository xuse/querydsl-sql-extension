package com.github.xuse.querydsl.sql.expression;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionBase;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Visitor;

public class StreamExpressionWrapper<T, K> extends ExpressionBase<K> implements FactoryExpression<K> {
	private static final long serialVersionUID = -90942587064687950L;

	private final FactoryExpression<T> expr;
	private Function<T, K> function;

	public StreamExpressionWrapper(FactoryExpression<T> expr, Function<T, K> function, Class<K> clz) {
		super(clz);
		Assert.notNull(function, "consumer must not be null.");
		this.expr = expr;
		this.function = function;
	}

	@Override
	public List<Expression<?>> getArgs() {
		return expr.getArgs();
	}

	@Nullable
	@Override
	public K newInstance(Object... args) {
		return function.apply(expr.newInstance(args));
	}

	@Nullable
	@Override
	public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
		return expr.accept(v, context);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof StreamExpressionWrapper) {
			return expr.equals(((StreamExpressionWrapper<?, ?>) o).expr);
		} else {
			return false;
		}
	}

}