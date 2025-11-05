package com.github.xuse.querydsl.lambda;

import org.jetbrains.annotations.Nullable;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

public interface ExprBoolean extends ExprComparable<Boolean> {
	BooleanExpression mixin();

	default BooleanExpression and(@Nullable Predicate right) {
		return mixin().and(right);
	}

	default BooleanExpression not() {
		return mixin().not();
	}

	default BooleanExpression or(@Nullable Predicate right) {
		return mixin().or(right); 
	}
	
	default BooleanExpression isTrue() {
		return mixin().eq(true);
    }

	default BooleanExpression isFalse() {
		return mixin().eq(false);
    }
}
