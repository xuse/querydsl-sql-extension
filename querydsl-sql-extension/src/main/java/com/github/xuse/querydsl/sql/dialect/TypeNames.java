package com.github.xuse.querydsl.sql.dialect;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.util.StringUtils;

/**
 * QueryDSL中，本来也有JDBC type &lt;-&gt; Database Data type映射关系，存储在
 *
 * private final Map&lt;String, Integer&gt; typeNameToCode = new HashMap&lt;&gt;();
 *
 * private final Map&lt;Integer, String&gt; codeToTypeName = new HashMap&lt;&gt;();
 * 两个字段中，但没有使用的原因是：无法格式化成 varchar2(n)之类的形式，而且会使用最大的数据类型覆盖。
 * 在受限于无法修改QueryDSL源码情况下，只能另行实现。
 */
final class TypeNames {

	private final Map<Integer, Map<Integer, DataTypePolicy>> weighted = new HashMap<Integer, Map<Integer, DataTypePolicy>>();

	private final Map<Integer, DataTypePolicy> defaults = new HashMap<Integer, DataTypePolicy>();

	/**
	 *  返回该SQL类型在此数据库中的描述类型
	 *
	 *  @param typecode 要求的sql类型
	 *  @return Type
	 */
	public DataTypePolicy get(int typecode) {
		DataTypePolicy result = defaults.get(typecode);
		if (result == null)
			throw new IllegalArgumentException("No Dialect mapping for JDBC type: " + typecode);
		return result;
	}

	/**
	 *  返回该SQL类型在此数据库中的描述类型
	 *
	 *  @param typecode 要求的sql类型
	 *  @param size     数值长度 (精度) precision 大小
	 *  @param scale    小数位数
	 *  @return ColumnDef (仅填充其中的以下字段)
	 *  <ul>
	 *  <li>{@link ColumnDef#getDataType()}</li>
	 *  <li>{@link ColumnDef#getColumnSize()}</li>
	 *  <li>{@link ColumnDef#getDecimalDigit()}</li>
	 *  <li>{@link ColumnDef#getJdbcType()}</li>
	 *  </ul>
	 */
	public ColumnDef get(int typecode, int size, int scale) {
		if (size <= 0 && scale <= 0) {
			return generateLocalColumnDef(get(typecode), size, scale);
		}
		Map<Integer, DataTypePolicy> map = weighted.get(typecode);
		if (map != null && !map.isEmpty()) {
			for (Map.Entry<Integer, DataTypePolicy> entry : map.entrySet()) {
				if (size <= entry.getKey()) {
					return generateLocalColumnDef(entry.getValue(), size, scale);
				}
			}
		}
		return generateLocalColumnDef(get(typecode), size, scale);
	}

	private static final String[] REPLACE_SOURCE = { "$l", "$p", "$s" };

	private static ColumnDef generateLocalColumnDef(DataTypePolicy policy, int sizeDefined, int scaleDefined) {
		String template = policy.getTemplate();
		//Accept the policy adjusting on size.
		if (policy.getSize() != DataTypePolicy.UNCHANGED) {
			sizeDefined = policy.getSize();
		}
		if (policy.getDigits() != DataTypePolicy.UNCHANGED) {
			scaleDefined = policy.getSize();
		}
		String sizeStr = String.valueOf(sizeDefined);
		String scaleStr = String.valueOf(scaleDefined);
		ColumnDef result = new ColumnDef();
		result.setDataType(StringUtils.replaceEach(template, REPLACE_SOURCE, new String[] { sizeStr, sizeStr, scaleStr }));
		result.setColumnSize(sizeDefined);
		result.setDecimalDigit(scaleDefined);
		result.setJdbcType(policy.getSqlType());
		result.setColumnName("");
		result.setNullable(true);
		return result;
	}

	/**
	 *  注册一个类型
	 *
	 *  @param typecode   类型
	 *  @param capacity   容量（length或者precision）
	 *  @param value      SQL描述符，支持 $l $s $p三个宏
	 *  @return DataTypePolicy
	 */
	public DataTypePolicy put(int typecode, int capacity, String value) {
		Map<Integer, DataTypePolicy> map = weighted.get(typecode);
		if (map == null) {
			// add new ordered map
			weighted.put(typecode, map = new TreeMap<Integer, DataTypePolicy>());
		}
		DataTypePolicy result;
		map.put(capacity, result = new DataTypePolicy(typecode, value));
		return result;
	}

	/**
	 * 某类sql数据类型在本数据库上的实现
	 * @param sqlType sqlType
	 * @param template template
	 * @param alias      别名
	 * @return DataTypePolicy
	 */
	public DataTypePolicy put(int sqlType, String template, String... alias) {
		DataTypePolicy type = new DataTypePolicy(sqlType, template);
		type.setAlias(alias);
		defaults.put(sqlType, type);
		return type;
	}

	// /**
	// * 还原数据类型，根据一个数据类型描述，还原为java.sql.Types常量
	// *
	// * @return getTypeNameCodes
	// */
	// public Map<String, Integer> getTypeNameCodes() {
	// Map<String, Integer> result = new HashMap<String, Integer>();
	// for (ColumnDataType p : defaults.values()) {
	// result.put(StringUtils.substringBefore(p.getName().toUpperCase(), "("), p.getSqlType());
	// if (p.getAlias() != null && p.getAlias().length > 0) {
	// for (String alias : p.getAlias()) {
	// result.put(alias.toUpperCase(), p.getSqlType());
	// }
	// }
	// }
	// for (Map<Integer, ColumnDataType> m : weighted.values()) {
	// for (ColumnDataType p : m.values()) {
	// result.put(StringUtils.substringBefore(p.getName().toUpperCase(), "("), p.getSqlType());
	// if (p.getAlias() != null && p.getAlias().length > 0) {
	// for (String alias : p.getAlias()) {
	// result.put(alias.toUpperCase(), p.getSqlType());
	// }
	// }
	// }
	// }
	// return result;
	// }
	// 默认策略参照MYSQL
	public static TypeNames generateDefault() {
		TypeNames typeNames = new TypeNames();
		typeNames.put(Types.BOOLEAN, "boolean");
		// .size(64)
		typeNames.put(Types.BINARY, "binary($l)");
		// Numeric types
		typeNames.put(Types.BIT, "bit").noSize();
		typeNames.put(Types.FLOAT, "float").noSize();
		typeNames.put(Types.DOUBLE, "double").noSize();
		typeNames.put(Types.TINYINT, "tinyint").noSize();
		typeNames.put(Types.SMALLINT, "smallint").noSize();
		typeNames.put(Types.INTEGER, "int").noSize();
		typeNames.put(Types.BIGINT, "bigint").noSize();
		typeNames.put(Types.DECIMAL, "decimal($p,$s)");
		typeNames.put(Types.NUMERIC, "decimal($p,$s)");
		// Datetime types
		typeNames.put(Types.DATE, "date");
		typeNames.put(Types.TIME, "time");
		typeNames.put(Types.TIMESTAMP, "timestamp");
		// character types
		typeNames.put(Types.CHAR, "char($l)");
		typeNames.put(Types.VARCHAR, "varchar($l)");
		typeNames.put(Types.VARBINARY, "varbinary($l)");
		typeNames.put(Types.LONGVARCHAR, "varchar($l)").type(Types.VARCHAR);
		typeNames.put(Types.LONGVARBINARY, "varbinary($l)").type(Types.VARBINARY);
		return typeNames;
	}
}
