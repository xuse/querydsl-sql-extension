package com.github.xuse.querydsl.sql.ddl;

import com.querydsl.sql.RoutingStrategy;

public interface DDLClause<C extends DDLClause<C>> {
	
	
	/**
	 * 执行语句
	 */
	int execute();
	
	DDLClause<C> withRouting(RoutingStrategy routing);

}
