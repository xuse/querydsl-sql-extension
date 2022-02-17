/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.querydsl.core.group.GroupExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.Visitor;

/**
 * 
 * 1 query DSL的 {@link QBean} 另一种实现方式。使用ASM生成的动态类来加速对Bean的构造和存取。
 * 2、首次使用的时候需要生成动态类。
 * 
 * 默认实现需要在构造时反射来判断拼装对象结构，是一个耗时比较大的构造过程，因此Projections.bean/field的结果应该要缓存起来 2
 * 对于Qxxx生成类，内部已经缓存了默认的Projection。
 */
public class QBeanEx<T> extends FactoryExpressionBase<T> {

	private static final long serialVersionUID = -8210214512730989778L;

	private final BeanCodec beanCodec;

	public BeanCodec getBeanCodec() {
		return beanCodec;
	}

	private static Map<String, Expression<?>> createBindings(Expression<?>... args) {
		Map<String, Expression<?>> rv = new LinkedHashMap<>();
		for (Expression<?> expr : args) {
			if (expr instanceof Path<?>) {
				Path<?> path = (Path<?>) expr;
				rv.put(path.getMetadata().getName(), expr);
			} else if (expr instanceof Operation<?>) {
				Operation<?> operation = (Operation<?>) expr;
				if (operation.getOperator() == Ops.ALIAS && operation.getArg(1) instanceof Path<?>) {
					Path<?> path = (Path<?>) operation.getArg(1);
					if (isCompoundExpression(operation.getArg(0))) {
						rv.put(path.getMetadata().getName(), operation.getArg(0));
					} else {
						rv.put(path.getMetadata().getName(), operation);
					}
				} else {
					throw new IllegalArgumentException("Unsupported expression " + expr);
				}

			} else {
				throw new IllegalArgumentException("Unsupported expression " + expr);
			}
		}
		return rv;
	}

	private static boolean isCompoundExpression(Expression<?> expr) {
		return expr instanceof FactoryExpression || expr instanceof GroupExpression;
	}

	private final Map<String, Expression<?>> bindings;

	/**
	 * Create a new QBean instance
	 *
	 * @param type type of bean
	 * @param args properties to be populated
	 */
	protected QBeanEx(Class<? extends T> type, Expression<?>... args) {
		this(type, createBindings(args));
	}
	
	/**
	 * 构造
	 * @param type
	 * @param ex
	 */
	protected QBeanEx(Class<? extends T> type, IRelationPathEx<?> ex) {
		super(type);
		Map<String, Expression<?>> bindings = new LinkedHashMap<>();
		for(Path<?> p:ex.getColumns()) {
			bindings.put(p.getMetadata().getName(),(Expression<?>)	p);
		}
		this.bindings=Collections.unmodifiableMap(bindings);
		this.beanCodec=BeanCodecManager.getInstance().getPopulator(this.getType(), new ArrayList<>(this.bindings.keySet()));
	}

	/**
	 * Create a new QBean instance
	 *
	 * @param type        type of bean
	 * @param fieldAccess true, for field access and false, for property access
	 * @param bindings    bindings
	 */
	protected QBeanEx(Class<? extends T> type, Map<String, ? extends Expression<?>> bindings) {
		super(type);
		this.bindings = Collections.unmodifiableMap(bindings);
		this.beanCodec=BeanCodecManager.getInstance().getPopulator(this.getType(), new ArrayList<>(this.bindings.keySet()));

	}

	protected void typeMismatch(Class<?> type, Expression<?> expr) {
		final String msg = expr.getType().getName() + " is not compatible with " + type.getName();
		throw new IllegalArgumentException(msg);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T newInstance(Object... a) {
		return (T) beanCodec.newInstance(a);
	}
	 /**
     * Create an alias for the expression
     *
     * @return this as alias
     */
    public Expression<T> as(Path<T> alias) {
        return ExpressionUtils.operation(getType(),Ops.ALIAS, this, alias);
    }

    /**
     * Create an alias for the expression
     *
     * @return this as alias
     */
    public Expression<T> as(String alias) {
        return as(ExpressionUtils.path(getType(), alias));
    }

    @Override
    public <R,C> R accept(Visitor<R,C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof QBean<?>) {
            QBean<?> c = (QBean<?>) obj;
            return getArgs().equals(c.getArgs()) && getType().equals(c.getType());
        } else {
            return false;
        }
    }

    @Override
    public List<Expression<?>> getArgs() {
        return new ArrayList<>(bindings.values());
    }
    
	public <K> StreamExpressionWrapper<T, K> map(Function<T, K> function, Class<K> clz) {
		return new StreamExpressionWrapper<>(this, function, clz);
	}
}
