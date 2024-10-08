package com.github.xuse.querydsl.repository;

import com.github.xuse.querydsl.lambda.PathCache;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.sql.RelationalPath;

public class LambdaQuery<T,R> extends QueryWrapper<T,R,LambdaQuery<T,R>>{

	public LambdaQuery() {
		super(null, new DefaultQueryMetadata());
	}
	
	public LambdaQuery(Class<T> clz) {
		super(PathCache.get(clz, null),new DefaultQueryMetadata());
	}

	protected LambdaQuery(RelationalPath<T> table, DefaultQueryMetadata mixin) {
		super(table,mixin);
	}

	@Override
	protected LambdaQuery<T,R> subchain() {
		return new LambdaQuery<>(table,new DefaultQueryMetadata());
	}

}
