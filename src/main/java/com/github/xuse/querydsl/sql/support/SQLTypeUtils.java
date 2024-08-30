package com.github.xuse.querydsl.sql.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.util.ArrayUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.FilteredClause;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.types.Null;
import com.querydsl.sql.types.Type;

public class SQLTypeUtils {

	public static boolean compareDate(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == Null.DEFAULT || b == Null.DEFAULT) {
			return false;
		}
		return ((Date) a).getTime() == ((Date) b).getTime();
	}

	public static int getDefaultSize(int sqlType, int size) {
		switch (sqlType) {
		case java.sql.Types.VARCHAR:
		case java.sql.Types.NVARCHAR:
			return size > 0 ? size : 64;
		case java.sql.Types.VARBINARY:
			return size > 0 ? size : 256;
		case java.sql.Types.CHAR:
		case java.sql.Types.BINARY:
		case java.sql.Types.NCHAR:
			return size > 0 ? size : 16;
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.LONGVARBINARY:
		case java.sql.Types.LONGNVARCHAR:
			return size > 0 ? size : 4096;
		default:
			return size;
		}
	}

	public static int calcJdbcType(int sqlType, Field field) {
		if (sqlType != Types.NULL) {
			return sqlType;
		}
		Class<?> type = field.getType();
		String name = type.getName();
		switch (name) {
		case "int":
		case "java.lang.Integer":
			return Types.INTEGER;
		case "java.lang.Long":
			return Types.BIGINT;
		case "java.lang.Double":
			return Types.DOUBLE;
		case "java.lang.Float":
			return Types.FLOAT;
		case "java.lang.String":
			return Types.VARCHAR;
		case "java.sql.Date":
		case "java.time.LocalDate":
			return Types.DATE;
		case "java.util.Date":
		case "java.sql.Timestamp":
		case "java.time.Instant":
			return Types.TIMESTAMP;
		case "java.sql.Time":
			return Types.TIME;
		case "java.time.LocalTime":
			return Types.TIME_WITH_TIMEZONE;
		case "java.time.LocalDateTime":
			return Types.TIMESTAMP_WITH_TIMEZONE;
		case "[B":
			return Types.VARBINARY;
		}
		throw Exceptions.illegalArgument("Please assign the jdbc data type of field {}, type={}", field, name);
	}

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
	 * @param columnDef columnDef
	 * @param type      type
	 * @return 针对用户自己写的String常量在数据库中的表达
	 */
	public static String serializeLiteral(String columnDef, int type) {
		if (columnDef == null) {
			// || columnDef.length()==0
			return null;
		}
		switch (type) {
		case Types.TIMESTAMP:
		case Types.TIME:
		case Types.DATE: {
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
			if (limit == size) {
				// No need to print...
				while (rs.next()) {
					size++;
				}
				break;
			}
		}
		sb.append("Total:").append(size).append(" record(s).");
		return sb.toString();
	}

	/**
	 * create the instance with string parameters.
	 * 
	 * @param clz        the class
	 * @param parameters parameters
	 * @param fieldType  fieldType
	 * @return instance of the clz;
	 * @throws InstantiationException    InstantiationException
	 * @throws IllegalAccessException    IllegalAccessException
	 * @throws InvocationTargetException InvocationTargetException
	 */
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
					&& isStringType(ArrayUtils.subArray(c.getParameterTypes(), 1, c.getParameterCount()))) {
				return (Type<?>) c.newInstance(ArrayUtils.addAllElement(new Object[] { fieldType }, parameters));
			}
		}
		throw new IllegalArgumentException("can not Instant type " + clz.getName() + ".");
	}

	public static void setWhere(List<Path<?>> mergeKey, FilteredClause<?> select, Map<Path<?>, Object> values) {
		for (Path<?> p : mergeKey) {
			SimpleExpression<?> key = (SimpleExpression<?>) p;
			Object value = values.get(p);
			// Set conditions, null value also be a condition.
			if (value == null) {
				select.where(key.isNull());
			} else {
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
	
	public static void close(ResultSet rs) {
		try {
			rs.close();
		} catch (SQLException e) {
			//do nothing
		}
	}
	public static void close(Statement st) {
		try {
			st.close();
		} catch (SQLException e) {
			//do nothing
		}
	}
}
