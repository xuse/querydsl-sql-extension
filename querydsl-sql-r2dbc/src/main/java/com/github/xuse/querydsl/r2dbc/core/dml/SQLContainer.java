package com.github.xuse.querydsl.r2dbc.core.dml;

import java.util.List;

import com.querydsl.sql.SQLBindings;

public interface SQLContainer {
	List<SQLBindings> getSQL();
}
