package com.github.xuse.querydsl.sql.expression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.util.CollectionUtils;

public class QStringMap extends FactoryExpressionBase<Map<String, ?>> {

	private final List<Expression<?>> args;
	
    /**
     * Create a new QMap instance
     *
     * @param args
     */
    @SuppressWarnings("unchecked")
    protected QStringMap(Expression<?>... args) {
        super((Class) Map.class);
        this.args = CollectionUtils.unmodifiableList(Arrays.asList(args));
    }


    /**
     * Create a new QMap instance
     *
     * @param args
     */
    protected QStringMap(List<Expression<?>> args) {
        super((Class) Map.class);
        this.args = CollectionUtils.unmodifiableList(args);
    }

	@Override
	public List<Expression<?>> getArgs() {
		 return args;
	}

	@Override
	public @Nullable Map<String, ?> newInstance(Object... args) {
		Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
           // map.put(args.get(i), args[i]);
        }
        return map;
	}

	@Override
	public <R, C> @Nullable R accept(Visitor<R, C> v, @Nullable C context) {
		// TODO Auto-generated method stub
		return null;
	}

}
