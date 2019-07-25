package com.github.xuse.querydsl.sql.result;

import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.sql.SQLListenerContext;

/**
 * 结果处理器
 * @author jiyi
 *
 */
public interface Projection<T> {
	
	List<T> convert(ResultSet rs)throws InstantiationException, IllegalAccessException, InvocationTargetException, SQLException;

	CloseableIterator<T> iterator(PreparedStatement stmt, ResultSet rs, SQLListenerContext context);
	
}
