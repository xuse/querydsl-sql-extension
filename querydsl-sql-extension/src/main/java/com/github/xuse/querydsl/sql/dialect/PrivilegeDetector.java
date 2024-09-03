package com.github.xuse.querydsl.sql.dialect;

import com.github.xuse.querydsl.sql.SQLQueryFactory;

@FunctionalInterface
public interface PrivilegeDetector {
	boolean check(SQLQueryFactory connection,Privilege... privileges);
}
