package com.github.xuse.querydsl.lambda;

import java.lang.reflect.AnnotatedElement;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.Visitor;

public interface LambdaColumnBase<B, T> extends Path<T>{
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
}
