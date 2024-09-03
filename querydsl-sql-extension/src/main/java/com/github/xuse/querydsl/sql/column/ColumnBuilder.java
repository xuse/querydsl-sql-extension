package com.github.xuse.querydsl.sql.column;

public class ColumnBuilder<T> extends ColumnBuilderBase<T, ColumnBuilder<T>> {
	public ColumnBuilder(PathMapping p) {
		super(p);
		super.q = this;
	}
}
