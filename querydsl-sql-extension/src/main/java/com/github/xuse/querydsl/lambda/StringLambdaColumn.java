package com.github.xuse.querydsl.lambda;

import com.querydsl.core.types.dsl.StringExpression;

public interface StringLambdaColumn<B> extends LambdaColumn<B, String>,ExprString{
    default StringExpression mixin(){
    	return (StringExpression) PathCache.getPath(this);
    }
}
