package com.github.xuse.querydsl.sql.ddl;

public interface DDLClause<C extends DDLClause<C>> {
	
	
	/**
	 * 执行语句
	 */
	void execute();

}
