package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.sql.routing.RoutingStrategy;

public interface DDLClause<C extends DDLClause<C>> {

	/**
	 * @return 执行语句
	 */
	int execute();

	DDLClause<C> withRouting(RoutingStrategy routing);
}
