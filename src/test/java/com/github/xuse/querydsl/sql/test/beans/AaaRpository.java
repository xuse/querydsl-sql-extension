package com.github.xuse.querydsl.sql.test.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.support.AbstractCrudRepository;
import com.querydsl.sql.RelationalPath;


@Repository
public class AaaRpository extends AbstractCrudRepository<Aaa, Long>{
	@Autowired
	private SQLQueryFactory factory;
	
	@Override
	protected SQLQueryFactory getFactory() {
		return factory;
	}

	@Override
	protected RelationalPath<Aaa> getPath() {
		return QAaa.aaa;
	}
}
