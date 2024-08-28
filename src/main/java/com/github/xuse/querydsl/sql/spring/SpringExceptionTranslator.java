package com.github.xuse.querydsl.sql.spring;

import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import com.querydsl.core.QueryException;

public class SpringExceptionTranslator implements com.querydsl.sql.SQLExceptionTranslator {

	private final SQLExceptionTranslator translator;

	public SpringExceptionTranslator() {
		this.translator = new SQLStateSQLExceptionTranslator();
	}

	public SpringExceptionTranslator(SQLExceptionTranslator translator) {
		this.translator = translator;
	}

	@Override
	public RuntimeException translate(String sql, List<Object> bindings, SQLException e) {
		RuntimeException ex = translator.translate("", sql, e);
		return ex == null ? new QueryException(e) : ex;
	}

	@Override
	public RuntimeException translate(SQLException e) {
		RuntimeException ex = translator.translate("", null, e);
		return ex == null ? new QueryException(e) : ex;
	}

}