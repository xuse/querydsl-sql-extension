package com.github.xuse.querydsl.sql.ddl;

import com.github.xuse.querydsl.sql.log.ExceptionLogDetail;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;

public interface DDLClause<C extends DDLClause<C>> {

	/**
	 * @return 执行语句
	 */
	int execute();

	DDLClause<C> withRouting(RoutingStrategy routing);

	/**
	 * 设置本语句执行失败时的异常日志详细度。
	 *
	 * @param detail 详细度
	 * @return this
	 */
	DDLClause<C> exceptionLog(ExceptionLogDetail detail);
}
