package com.github.xuse.querydsl.sql.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.github.xuse.querydsl.spring.core.resource.Util;
import com.github.xuse.querydsl.types.CodeEnum;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.lang.Primitives;
import com.querydsl.core.FilteredClause;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.types.Null;
import com.querydsl.sql.types.Type;

import lombok.SneakyThrows;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SQLTypeUtils {
	private final static Map<Class<?>, BiFunction<Class<?>, PathMetadata, Path<?>>> PathCreators = new HashMap<>();

	private static final BiFunction<Class<?>, PathMetadata, Path<?>> StringCreator = (a, b) -> Expressions
			.stringPath(b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> NumberCreator = (a, b) -> Expressions
			.numberPath((Class) a, b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> PrimitiveNumberCreator = (a, b) -> Expressions
			.numberPath((Class) Primitives.toWrapperClass(a), b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> DateCreator = (a, b) -> Expressions
			.datePath(a.asSubclass(Date.class), b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> DateTimeCreator = (a, b) -> Expressions
			.dateTimePath(a.asSubclass(Date.class), b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> TimeCreator = (a, b) -> Expressions
			.timePath(a.asSubclass(Date.class), b);

	private static final BiFunction<Class<?>, PathMetadata, Path<?>> BooleanCreator = (a, b) -> Expressions
			.booleanPath(b);
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> SimpleCreator = Expressions::simplePath;
	
	private static final BiFunction<Class<?>, PathMetadata, Path<?>> ArrayCreator = (a, b) -> Expressions.arrayPath(a, b);
	
	static {
		PathCreators.put(byte[].class, ArrayCreator);
		PathCreators.put(Byte[].class, ArrayCreator);
		PathCreators.put(long[].class, ArrayCreator);
		PathCreators.put(Long[].class, ArrayCreator);
		PathCreators.put(float[].class, ArrayCreator);
		PathCreators.put(Float[].class, ArrayCreator);
		PathCreators.put(double[].class, ArrayCreator);
		PathCreators.put(Double[].class, ArrayCreator);
		PathCreators.put(short[].class, ArrayCreator);
		PathCreators.put(Short[].class, ArrayCreator);
		PathCreators.put(char[].class, ArrayCreator);
		PathCreators.put(Character[].class, ArrayCreator);
		PathCreators.put(boolean[].class, ArrayCreator);
		PathCreators.put(Boolean[].class, ArrayCreator);
		PathCreators.put(int[].class, ArrayCreator);
		PathCreators.put(Integer[].class, ArrayCreator);
		
		PathCreators.put(String.class, StringCreator);
		PathCreators.put(CharSequence.class, StringCreator);

		PathCreators.put(Long.class, NumberCreator);
		PathCreators.put(Short.class, NumberCreator);
		PathCreators.put(Integer.class, NumberCreator);
		PathCreators.put(Float.class, NumberCreator);
		PathCreators.put(Double.class, NumberCreator);
		
		PathCreators.put(BigInteger.class, NumberCreator);
		PathCreators.put(BigDecimal.class, NumberCreator);

		PathCreators.put(Long.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Short.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Integer.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Float.TYPE, PrimitiveNumberCreator);
		PathCreators.put(Double.TYPE, PrimitiveNumberCreator);

		PathCreators.put(java.sql.Date.class, DateCreator);
		PathCreators.put(LocalDate.class, (a, b) -> Expressions.datePath(a.asSubclass(LocalDate.class), b));

		PathCreators.put(java.sql.Time.class, TimeCreator);
		PathCreators.put(LocalTime.class, (a, b) ->Expressions.timePath(a.asSubclass(LocalTime.class), b));

		
		PathCreators.put(Instant.class, (a, b) -> Expressions.dateTimePath(a.asSubclass(Instant.class), b));
		PathCreators.put(java.util.Date.class, DateTimeCreator);
		PathCreators.put(java.sql.Timestamp.class, DateTimeCreator);
		PathCreators.put(ZonedDateTime.class, (a, b) -> Expressions.dateTimePath(a.asSubclass(ZonedDateTime.class), b));
		PathCreators.put(LocalDateTime.class, (a, b) -> Expressions.dateTimePath(a.asSubclass(LocalDateTime.class), b));

		PathCreators.put(Boolean.class, BooleanCreator);
		PathCreators.put(Boolean.TYPE, BooleanCreator);
	}


	/*
	 * crate the path object according to the java type.
	 */
	public static Path<?> createPathByType(Class<?> type, String name, Path<?> parent) {
		PathMetadata metadata = PathMetadataFactory.forProperty(parent, name);
		BiFunction<Class<?>, PathMetadata, Path<?>> creator = PathCreators.get(type);
		if (creator != null) {
			return creator.apply(type, metadata);
		}
		if (Enum.class.isAssignableFrom(type)) {
			return Expressions.enumPath((Class<? extends Enum>) type, metadata);
		}
		return SimpleCreator.apply(type, metadata);
	}
	
	@SneakyThrows
	public static RelationalPath<?> getMetaModel(Class<?> clz){
		for (Field field : Util.getDeclaredFields(clz)) {
			if ((field.getModifiers() & Modifier.STATIC) > 0 && field.getType() == clz) {
				RelationalPath<?> obj = (RelationalPath<?>) field.get(null);
				return obj;
			}
		}
		return null;
	}

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
		if(CodeEnum.class.isAssignableFrom(type)) {
			return Types.TINYINT;
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
	 * @param type Generic type
	 * @return instance of the clz;
	 * @throws InstantiationException    InstantiationException
	 * @throws IllegalAccessException    IllegalAccessException
	 * @throws InvocationTargetException InvocationTargetException
	 */
	@SuppressWarnings("rawtypes")
	public static Type<?> createInstance(Class<? extends Type> clz, String[] parameters, Class<?> fieldType,java.lang.reflect.Type type)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
		int size = parameters.length;
		
		if(type==null) {
			type=fieldType;
		}
		
		Class[] ptypes=new Class[size];
		for(int i=0;i<size;i++) {
			ptypes[i]=String.class;
		}
		Type o=null;
		
		//Step1.
		Object[] mixedParams=new Object[size+1];
		System.arraycopy(parameters, 0, mixedParams, 1, size);
		{
			Class[] ptypesWithType=new Class[size+1];
			ptypesWithType[0]=java.lang.reflect.Type.class;
			System.arraycopy(ptypes, 0, ptypesWithType, 1, size);

			mixedParams[0]=type;
			createWith(clz,ptypesWithType,mixedParams);
		}
		//Step2.
		if(o==null) {
			Class[] ptypesWithClass=new Class[size+1];
			ptypesWithClass[0]=java.lang.Class.class;
			System.arraycopy(ptypes, 0, ptypesWithClass, 1, size);
			
			mixedParams[0]=fieldType;
			o=createWith(clz,ptypesWithClass,mixedParams);
		}
		//Step3.
		if(o==null) {
			o=createWith(clz,ptypes,parameters);
		}
		if(o==null) {
			throw new IllegalArgumentException("Unable to Instant type " + clz.getName() + ".");	
		}
		return o;
	}

	@SuppressWarnings("rawtypes")
	private static Type createWith(Class<? extends Type> clz, Class[] types, Object[] params) {
		try {
			Constructor<? extends Type> c= clz.getDeclaredConstructor(types);
			c.setAccessible(true);
			return c.newInstance(params);
		} catch (Exception e) {
			return null;
		}
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
