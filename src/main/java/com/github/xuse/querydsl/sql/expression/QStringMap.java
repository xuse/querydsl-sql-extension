package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.util.CollectionUtils;

public class QStringMap extends FactoryExpressionBase<Map<String, ?>> {

	private final List<Expression<?>> args;
	private final List<String> argNames;
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected QStringMap(Expression<?>... args) {
        super((Class) Map.class);
		this.argNames = toNames(this.args = CollectionUtils.unmodifiableList(Arrays.asList(args)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public QStringMap(List<Expression<?>> args) {
        super((Class) Map.class);
        this.args = CollectionUtils.unmodifiableList(args);
        this.argNames=toNames(args);
    }

	private static List<String> toNames(List<Expression<?>> args) {
		List<String> rv = new ArrayList<>(args.size());
		for (Expression<?> expr : args) {
			if (expr instanceof Path<?>) {
				Path<?> path = (Path<?>) expr;
				rv.add(path.getMetadata().getName());
			} else if (expr instanceof Operation<?>) {
				Operation<?> operation = (Operation<?>) expr;
				if (operation.getOperator() == Ops.ALIAS && operation.getArg(1) instanceof Path<?>) {
					Path<?> path = (Path<?>) operation.getArg(1);
					rv.add(path.getMetadata().getName());
				} else {
					throw new IllegalArgumentException("Unsupported expression " + expr);
				}
			} else {
				throw new IllegalArgumentException("Unsupported expression " + expr);
			}
		}
		return rv;
	}

	@Override
	public List<Expression<?>> getArgs() {
		 return args;
	}

	@Override
	public @Nullable Map<String, ?> newInstance(Object... args) {
		List<String> argNames =this.argNames; 
		Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
           map.put(argNames.get(i), args[i]);
        }
        return map;
	}

	@Override
	public <R, C> @Nullable R accept(Visitor<R, C> v, @Nullable C context) {
		 return v.visit(this, context);
	}

}
