package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.BooleanExpression;

public interface BooleanLambdaColumn<B> extends LambdaColumn<B, Boolean>,ExprBoolean{
    default BooleanExpression mixin(){
    	return (BooleanExpression) PathCache.getPath(this);
    }
}
