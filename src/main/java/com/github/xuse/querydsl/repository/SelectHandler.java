package com.github.xuse.querydsl.repository;

import java.util.List;

import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.mysema.commons.lang.Pair;

public class SelectHandler<T> {
	final SQLQueryAlter<T> select;

	public SelectHandler(SQLQueryAlter<T> select) {
		this.select = select;
	}
	
	public SelectHandler<T> distinct() {
		this.select.distinct();
		return this;
	}

	public List<T> fetch() {
		return select.fetch();
	}
	
	public T fetchOne() {
		return select.fetchOne();
	}
	
	public int fetchCount() {
		return (int)select.fetchCount();
	}
	
	public Pair<Integer,List<T>> fetchAndCount(){
		return select.fetchAndCount();
	}
	
}
