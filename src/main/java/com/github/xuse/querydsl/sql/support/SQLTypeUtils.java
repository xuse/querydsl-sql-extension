package com.github.xuse.querydsl.sql.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.querydsl.core.FilteredClause;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.types.Type;

public class SQLTypeUtils {
	public static boolean isNumeric(int type) {
		switch (type) {
		case java.sql.Types.NUMERIC:
		case java.sql.Types.DECIMAL:
		case java.sql.Types.BIT:
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.BIGINT:
		case java.sql.Types.REAL:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.FLOAT:
			return true;
		default:
			return false;
		}
	}
	
	public static boolean isCharBinary(int type) {
		switch (type) {
		case java.sql.Types.VARBINARY:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.CHAR:
		case java.sql.Types.BINARY:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.LONGVARBINARY:
		case java.sql.Types.NCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGNVARCHAR:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * @param columnDef
	 * @param type
	 * @return 针对用户自己写的String常量在数据库中的表达
	 */
	public static String serializeLiteral(String columnDef, int type) {
		if(columnDef==null) {// || columnDef.length()==0
			return null;
		}
		switch (type) {
		case Types.TIMESTAMP:
		case Types.TIME: 
		case Types.DATE:{
			char first = columnDef.charAt(0);
			if (Character.isDigit(first)) {
				return "'" + columnDef + "'";
			}
			break;
		}
		case Types.CHAR:
		case Types.CLOB:
		case Types.LONGNVARCHAR:
		case Types.LONGVARCHAR:
		case Types.NCHAR:
		case Types.NCLOB:
		case Types.NVARCHAR:
		case Types.VARCHAR:
			return "'" + columnDef + "'";
		}
		return columnDef;
	}

	public static String toString(ResultSet rs) throws SQLException {
		StringBuilder sb = new StringBuilder(64);
		int limit = 200;
		ResultSetMetaData meta = rs.getMetaData();
		int count = meta.getColumnCount();

		sb.append(meta.getColumnLabel(1));
		for (int i = 2; i <= count; i++) {
			sb.append(", ");
			sb.append(meta.getColumnLabel(i));
		}
		sb.append('\n');
		int size = 0;
		while (rs.next()) {
			size++;
			sb.append('[');
			sb.append(rs.getObject(1));
			for (int i = 2; i <= count; i++) {
				sb.append(", ");
				sb.append(rs.getObject(i));
			}
			sb.append("]\n");
			if (limit == size) {// No need to print...
				while (rs.next()) {
					size++;
				}
				break;
			}
		}
		sb.append("Total:").append(size).append(" record(s).");
		return sb.toString();
	}
	
	

	@SuppressWarnings("rawtypes")
	public static Type<?> createInstance(Class<? extends Type> clz, String[] parameters, Class<?> fieldType)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
		Constructor<?>[] constructors = clz.getConstructors();
		int size = parameters.length;
		for (Constructor<?> c : constructors) {
			if (size == 0 && c.getParameterCount() == 1 && c.getParameterTypes()[0] == Class.class) {
				return (Type<?>) c.newInstance(fieldType);
			} else if (c.getParameterCount() == size && isStringType(c.getParameterTypes())) {
				return (Type<?>) c.newInstance((Object[]) parameters);
			} else if (c.getParameterCount() == size + 1
					&& isStringType(ArrayUtils.subarray(c.getParameterTypes(), 1, c.getParameterCount()))) {
				return (Type<?>) c.newInstance(ArrayUtils.addAll(new Object[] { fieldType }, (Object[]) parameters));
			}
		}
		throw new IllegalArgumentException("can not Instant type " + clz.getName() + ".");
	}
	
	public static void setWhere(List<Path<?>> mergeKey, FilteredClause<?> select, Map<Path<?>, Object> values) {
		for(Path<?> p:mergeKey) {
			SimpleExpression<?> key=(SimpleExpression<?>)p;
			Object value=values.get(p);
			//Set conditions, null value also be a condition. 
			if(value == null) {
				select.where(key.isNull());	
			}else {
				select.where(key.eq(ConstantImpl.create(value)));
			}
		}
	}
	
	private static boolean isStringType(Class<?>[] parameterTypes) {
		for (int i = 0; i < parameterTypes.length; i++) {
			if (parameterTypes[i] != String.class) {
				return false;
			}
		}
		return true;
	}
}
