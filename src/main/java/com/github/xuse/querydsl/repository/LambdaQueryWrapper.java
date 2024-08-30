package com.github.xuse.querydsl.repository;

import java.util.function.Consumer;
import java.util.function.Function;

import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.sql.expression.AliasMapBeans;
import com.github.xuse.querydsl.sql.expression.QAliasBeansDiscontinuous;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.group.QPair;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.RelationalPath;

public class LambdaQueryWrapper<T> extends LambdaQuery<T, T> {

	public LambdaQueryWrapper() {
	}
	
	public LambdaQueryWrapper(Class<T> clz) {
		super(clz);
	}
	
	protected LambdaQueryWrapper(RelationalPath<T> table, DefaultQueryMetadata mixin) {
		super(table,mixin);
	}

	@Override
	protected LambdaQueryWrapper<T> subchain() {
		return new LambdaQueryWrapper<>(table,new DefaultQueryMetadata());
	}
	
	public <C extends Comparable<C>> LambdaQuery<T,C> selectSingleColumn(LambdaColumn<T, C> expr) {
		mixin.setProjection(expr);
		LambdaQuery<T,C> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	public <C extends Comparable<C>,X> LambdaQuery<T,X> selectSingleColumn(LambdaColumn<T, C> expr,Function<LambdaColumn<T, C>,Expression<X>> func) {
		mixin.setProjection(func.apply(expr));
		LambdaQuery<T,X> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	public <K extends Comparable<K>,V extends Comparable<V>> LambdaQuery<T,Pair<K,V>> selectPair(LambdaColumn<T, K> expr1,LambdaColumn<T, V> expr2) {
		mixin.setProjection(new QPair<K,V>(expr1,expr2));
		LambdaQuery<T,Pair<K,V>> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	public <K extends Comparable<K>,V extends Comparable<V>> LambdaQuery<T,Pair<K,V>> selectPair(Expression<K> expr1,Expression<V> expr2) {
		mixin.setProjection(new QPair<K,V>(expr1,expr2));
		LambdaQuery<T,Pair<K,V>> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	public <R> LambdaQuery<T,R> select(Function<SelectBuilder<T>,Expression<R>> factoryExpression) {
		mixin.setProjection(factoryExpression.apply(new SelectBuilder<T>()));
		LambdaQuery<T,R> ch=new LambdaQuery<>(table,mixin);
		return ch;
	}
	
	public LambdaQuery<T,AliasMapBeans> selectBeans(Consumer<QAliasBeansDiscontinuous.Builder> consumer) {
		QAliasBeansDiscontinuous.Builder builder = QAliasBeansDiscontinuous.builder();
		consumer.accept(builder);
		mixin.setProjection(builder.build());
		LambdaQuery<T, AliasMapBeans> ch = new LambdaQuery<>(table, mixin);
		return ch;
	}
}
