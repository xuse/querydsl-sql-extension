package com.github.xuse.querydsl.lambda;

import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Function;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.ComparableExpression;

/**
 * @author jiyi
 *
 * @param <B> the type of the bean, where this column come from.
 * @param <T> the type of the column
 */
@FunctionalInterface
public interface LambdaColumn<B,T extends Comparable<T>> extends Function<B, T>, Serializable,Path<T>,ComparablePath<T> {

	default PathMetadata getMetadata() {
		return PathCache.getPath(this).getMetadata();
    }

    default Path<?> getRoot(){
    	return PathCache.getPath(this).getRoot();
    }

    default AnnotatedElement getAnnotatedElement() {
    	return PathCache.getPath(this).getAnnotatedElement();
    }
    
    default <R,C> R accept(Visitor<R,C> v, C context) {
    	return v.visit(PathCache.getPath(this), context);
    }
    
    default  Class<? extends T> getType(){
    	return PathCache.getPath(this).getType();
    }
    
    default ComparableExpression<T> mixin(){
    	return PathCache.getPathAsExpr(this);
    }
}
