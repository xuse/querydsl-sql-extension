package com.github.xuse.querydsl.repository;

import com.github.xuse.querydsl.lambda.PathCache;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.sql.RelationalPath;

public class LambdaQueryWrapper<T> extends QueryWrapper<T, LambdaQueryWrapper<T>> {

	public LambdaQueryWrapper() {
		super(new DefaultQueryMetadata());
		super.table = null;
	}
	
	public LambdaQueryWrapper(Class<T> clz) {
		super(new DefaultQueryMetadata());
		super.table=PathCache.get(clz);
	}

	private LambdaQueryWrapper(RelationalPath<T> table) {
		super(new DefaultQueryMetadata());
		super.table=table;
	}

	@Override
	protected LambdaQueryWrapper<T> subchain() {
		return new LambdaQueryWrapper<>(table);
	}
}
