package com.github.xuse.querydsl.sql.dbmeta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.config.ConfigrationPackageExporter;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.spring.core.resource.Util;
import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.sql.ddl.DDLOps.Basic;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.github.xuse.querydsl.sql.dialect.SchemaPolicy;
import com.github.xuse.querydsl.sql.dialect.SizeParser;
import com.github.xuse.querydsl.sql.dialect.SpecialFeature;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.support.DistributedLock;
import com.github.xuse.querydsl.sql.support.QueryFunction;
import com.github.xuse.querydsl.util.ArrayUtils;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.TypeUtils;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.Column;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLListeners;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MetadataQuerySupport {

	private static final QueryMetadata metadata = new DefaultQueryMetadata();

	protected abstract ConfigurationEx getConfiguration();

	public abstract Connection getConnection();

	private final SQLListeners listeners;

	public DriverInfo getDriverInfo() {
		return ConfigrationPackageExporter.computeDriverInfo(getConfiguration(), this::generateDriverInfo);
	}
	
	private DriverInfo generateDriverInfo() {
		DriverInfo result = doConnectionAccess(c -> {
			DriverInfo r = new DriverInfo();
			DatabaseMetaData e=c.getMetaData();
			r.catalog = c.getCatalog();
			r.schema = c.getSchema();
			r.driverName = e.getDriverName();
			r.driverVersion = e.getDriverVersion() + " " + e.getDatabaseMinorVersion();
			r.databaseProductName = e.getDatabaseProductName();
			r.dataProductVersion = e.getDatabaseProductVersion() + " " + e.getDatabaseMinorVersion();
			r.defaultTxIsolation = e.getDefaultTransactionIsolation();
			r.setUrl(e.getURL());
			return r;
		});
		result.policy = getConfiguration().getTemplates().getSchemaPolicy();
		result.setDbTimeDelta(calcDbTimeDelta());
		if(log.isInfoEnabled()){
			log.info(result.toString());
		}
		return result;
	}
	
	private long calcDbTimeDelta() {
		SQLTemplatesEx templates = getConfiguration().getTemplates();
		String dummyTable = templates.getDummyTable();
		if(dummyTable==null) {
			dummyTable="";
		}
		SQLSerializer s = new SQLSerializer(this.getConfiguration().get());
		s.handle(DDLExpressions.simple(Basic.SELECT_VALUES, Expressions.currentTimestamp(), Expressions.path(Object.class, null, dummyTable)));
		String sql = s.toString();
		final SQLBindings qSql = new SQLBindings(sql, Collections.emptyList());
		long dbTimeDelta = doSQLQuery(e -> {
			Timestamp ts = e.querySingle(qSql, rs -> rs.getTimestamp(1));
			return ts.getTime() - System.currentTimeMillis();
		}, "getTime");
		return dbTimeDelta;
	}

	public MetadataQuerySupport() {
		listeners = new SQLListeners(getConfiguration().get().getListeners());
	}
	

	/**
	 *  得到当前数据库的时间，这一运算不是通过到数据库查询而得，而是和数据库每次心跳时都会刷新当前系统时间和数据库时间的差值，从而得到数据库时间。
	 *
	 *  @return 当前数据库时间
	 */
	public Date getDatabaseTime() {
		return getDriverInfo().getDatabaseTime();
	}

	/**
	 *  得到数据库中的表
	 *
	 *  @param tablename 要查找的表名，仅第一个参数有效
	 *  @return 表的信息
	 */
	public TableInfo getTable(String tablename) {
		return getTable(parseSchemaAndTable(tablename));
	}

	/**
	 * 得到数据库中的表
	 * @param table table
	 * @return 表的信息
	 */
	public TableInfo getTable(SchemaAndTable table) {
		List<TableInfo> tables = getDatabaseObject(ObjectType.TABLE, table, Ops.EQ);
		if (tables.isEmpty()) {
			return null;
		}
		return tables.get(0);
	}

	public boolean existsTable(SchemaAndTable table, RoutingStrategy routing) {
		table = routing.getOverride(table, getConfiguration());
		return !getDatabaseObject(ObjectType.TABLE, table, null).isEmpty();
	}

	/**
	 * 返回数据库中所有的表(当前schema下)
	 * @param catalog catalog
	 * @param schema schema
	 * @return 表信息
	 */
	public List<TableInfo> getTables(String catalog, String schema) {
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		return getDatabaseObject(ObjectType.TABLE, new SchemaAndTable(policy.toNamespace(catalog, schema), null), null);
	}

	/**
	 *  @param type  要查询的对象类型 "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY",
	 *               "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 *  @param table 匹配名称
	 *  @param oper  操作符，可以为null，为null时表示等于条件
	 *  @return 表/视图等数据库对象的信息
	 *  @see Operator
	 */
	public List<TableInfo> getDatabaseObject(ObjectType type, SchemaAndTable table, Ops oper) {
		SQLTemplatesEx templates = getConfiguration().getTemplates();
		table = getConfiguration().getOverride(table);
		final String qSchema = processNamespace(table.getSchema());
		final String qMatchName = processName(table.getTable(), oper);
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		List<TableInfo> result = doSQLQuery(q -> 
			templates.getSchemaAccessor().fetchTables(q, policy.asCatalog(qSchema), policy.asSchema(qSchema), qMatchName, type), "getTables");
		log.debug("getting {} {}.{}, result is {}", type, qSchema, qMatchName, result);
		return result;
	}

	/**
	 * 得到表的Column信息
	 * @param tableName tableName
	 * @return List&lt;ColumnDef&gt;
	 */
	public List<ColumnDef> getColumns(String tableName) {
		return getColumns(parseSchemaAndTable(tableName));
	}

	/**
	 * 得到表的Column信息
	 * @param schemaAndTable schemaAndTable
	 * @return List&lt;ColumnDef&gt;
	 */
	public List<ColumnDef> getColumns(SchemaAndTable schemaAndTable) {
		schemaAndTable = getConfiguration().getOverride(schemaAndTable);
		final String namespace = processNamespace(schemaAndTable.getSchema());
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		final String tableName = processName(schemaAndTable.getTable(), Ops.EQ);
		List<ColumnDef> columns = doMetadataQuery("getColumns[" + tableName + "]", c -> c.getColumns(policy.asCatalog(namespace), policy.asSchema(namespace), tableName, "%"), this::populateColumn);
		columns.sort(Comparator.comparingInt(ColumnDef::getOrdinal));
		log.debug("Table {} 's columns: {}", schemaAndTable, columns);
		return columns;
	}

	private ColumnDef populateColumn(ResultSet rs) throws SQLException {
		ColumnDef column = new ColumnDef();
		/*
		 * Notice: Oracle非常变态，当调用rs.getString("COLUMN_DEF")会经常抛出
		 * "Stream is already closed" Exception。 百思不得其解，google了半天有人提供了回避这个问题的办法
		 * （https://issues.apache.org/jira/browse/DDLUTILS-29），
		 * 就是将getString("COLUMN_DEF")作为第一个获取的字段， 非常神奇的就好了。叹息啊。。。
		 */
		String defaultVal = rs.getString("COLUMN_DEF");
		int jdbcType = rs.getInt("DATA_TYPE");
		SizeParser sizeParser = getConfiguration().getTemplates().getColumnSizeParser(jdbcType);
		int columnSize = rs.getInt("COLUMN_SIZE");
		int digits = rs.getInt("DECIMAL_DIGITS");
		column.setColumnName(rs.getString("COLUMN_NAME"));
		column.setJdbcType(jdbcType);
		column.setColumnSize(sizeParser.size(columnSize, digits));
		column.setDecimalDigit(sizeParser.digits(columnSize, digits));
		defaultVal = getConfiguration().getTemplates().translateDefault(defaultVal, jdbcType, column.getColumnSize(), column.getDecimalDigit());
		column.setTableCat(rs.getString("TABLE_CAT"));
		column.setTableSchema(rs.getString("TABLE_SCHEM"));
		column.setTableName(rs.getString("TABLE_NAME"));
		column.setOrdinal(rs.getInt("ORDINAL_POSITION"));
		column.setDataType(rs.getString("TYPE_NAME"));
		column.setCharOctetLength(rs.getInt("CHAR_OCTET_LENGTH"));
		int nullable = rs.getInt("NULLABLE");
		
		boolean nullAble1 = nullable == DatabaseMetaData.columnNullable;
		boolean nullAble2 = "YES".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
		if (nullAble1 != nullAble2) {
			logDriverProblem("column null ability",rs.getString("IS_NULLABLE"));
		}
		column.setNullable(nullAble2);
		column.setAutoIncrement("YES".equalsIgnoreCase(rs.getString("IS_AUTOINCREMENT")));
		column.setGenerated("YES".equalsIgnoreCase(rs.getString("IS_GENERATEDCOLUMN")));
		/*
		 * 计算defaultVal的合适值，null表示没有缺省值，""表示缺省值为空。
		 */
		if (defaultVal != null && getConfiguration().has(SpecialFeature.HAS_CRLF_IN_DEFAULT_VALUE_EXPRESSION)) {
			// / Oracle会在后面加上换行等怪字符。之前直接用了trimToNull，但这是不对的，会将default ' '这样的定义忽略掉。
			defaultVal = StringUtils.rtrim(defaultVal, '\r', '\n');
			if (defaultVal.length() == 0) {
				defaultVal = null;
			}
		}
		column.setColumnDef(defaultVal);
		// 这个操作容易出问题，一定要最后操作
		column.setRemarks(rs.getString("REMARKS"));
		return column;
	}

	private void logDriverProblem(String string, String params) {
		DriverInfo driverInfo=getDriverInfo();
		log.warn(driverInfo.getDriverVersion() + " " + driverInfo.getDriverVersion() + ":" + string+"="+params);
	}

	/**
	 * 返回当前schema下的所有数据库对象名称
	 * @param catalog catalog
	 * @param schema schema
	 * @param types   取以下参数{@link ObjectType}。可以省略，省略的情况下取Table
	 * @return 所有表名
	 */
	public List<String> getNames(String catalog, String schema, ObjectType... types) {
		if (types == null || types.length == 0) {
			types = new ObjectType[] { ObjectType.TABLE };
		}
		String[] ts = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			ts[i] = types[i].name();
		}
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		SchemaAndTable schemaAndTable = new SchemaAndTable(policy.toNamespace(catalog, schema), null);
		schemaAndTable = getConfiguration().getOverride(schemaAndTable);
		final String namespace = processNamespace(schemaAndTable.getSchema());
		List<String> names = this.doMetadataQuery("getObjects", m -> m.getTables(policy.asCatalog(namespace), policy.asSchema(namespace), null, ts), r -> r.getString("TABLE_NAME"));
		return names;
	}

	/**
	 *  a catalog cache for 5 seconds.
	 */
	private volatile Entry<Set<String>, Long> catalogCache = new Entry<>(null, 0L);

	private final Map<Class<?>, List<FieldOrder>> reflectCache = new HashMap<>(32);

	public Collection<String> getCatalogs() {
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		if (policy == SchemaPolicy.SCHEMA_ONLY) {
			return Collections.singletonList("");
		}
		Entry<Set<String>, Long> catalogCache = this.catalogCache;
		long currentTime = System.currentTimeMillis();
		if (catalogCache == null || currentTime - catalogCache.getValue() > 5000) {
			Set<String> catalog = new HashSet<>(// r.getString("TABLE_CAT")
			doMetadataQuery("AllCatalogs", DatabaseMetaData::getCatalogs, r -> r.getString(1)));
			this.catalogCache = new Entry<>(catalog, currentTime);
			return catalog;
		} else {
			return catalogCache.getKey();
		}
	}

	public Collection<String> getSchemas(String catalog) {
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		QueryFunction<ResultSet, String> schemaExt = r -> r.getString("TABLE_SCHEM");
		if (policy == SchemaPolicy.CATALOG_ONLY) {
			return getCatalogs().contains(catalog) ? Collections.singletonList("") : Collections.emptyList();
		} else if (policy == SchemaPolicy.SCHEMA_ONLY) {
			if (StringUtils.isNotEmpty(catalog)) {
				return Collections.emptyList();
			}
			return doMetadataQuery("AllSchemas", DatabaseMetaData::getSchemas, schemaExt);
		} else {
			return doMetadataQuery(catalog + "'schemas", m -> m.getSchemas(catalog, "%"), schemaExt);
		}
	}

	public Collection<String> getSchemas() {
		return getSchemas(null);
	}
	
	public List<DataType> getDataTypes(){
		return doMetadataQuery("getInfoType", meta->meta.getTypeInfo(), rs->getFromResultSet(rs, DataType.class));
	}

	public List<SequenceInfo> getSequenceInfo(String namespace, String seqName) {
		List<SequenceInfo> result = new ArrayList<SequenceInfo>(2);
		for (TableInfo table : getDatabaseObject(ObjectType.SEQUENCE, new SchemaAndTable(namespace, seqName), Ops.EQ)) {
			SequenceInfo e = new SequenceInfo();
			// TODO fill other infors.
			e.setName(table.getName());
			result.add(e);
		}
		return result;
	}

	/**
	 * 得到数据库中（当前schema下）所有视图
	 * @param namespace namespace
	 * @return 视图信息列表。 A collection of view info.
	 */
	public List<TableInfo> getViews(String namespace) {
		SchemaAndTable schemaAndTable = new SchemaAndTable(namespace, null);
		return getDatabaseObject(ObjectType.VIEW, schemaAndTable, Ops.EQ);
	}

	/**
	 * @param table 表名和Schema
	 * @return 表的主键
	 */
	public Constraint getPrimaryKey(SchemaAndTable table) {
		table = getConfiguration().getOverride(table);
		final String namespace = processNamespace(table.getSchema());
		final String tableName = processName(table.getTable(), Ops.EQ);
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		SchemaReader schemas=getConfiguration().getTemplates().getSchemaAccessor();
		return this.doSQLQuery(e->schemas.getPrimaryKey(policy.asCatalog(namespace), policy.asSchema(namespace), tableName,e),"getPrimryKey");
	}

	/**
	 *  获得外键（引用其他表的键）
	 *
	 *  @param table SchemaAndTable
	 *  @return ForeignKeyItem
	 */
	public List<ForeignKeyItem> getForeignKey(SchemaAndTable table) {
		table = getConfiguration().getOverride(table);
		final String namespace = processNamespace(table.getSchema());
		final String tableName = processName(table.getTable(), Ops.EQ);
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		List<ForeignKeyItem> result = doMetadataQuery("getForeignKey", m -> m.getImportedKeys(policy.asCatalog(namespace), policy.asSchema(namespace), tableName), r -> getFromResultSet(r, ForeignKeyItem.class));
		return result;
	}

	private static final class FieldOrder {
		private final int index;

		@SuppressWarnings("unused")
		private final String name;

		private final Field field;

		public FieldOrder(int index, String name, Field field) {
			this.index = index;
			this.name = name;
			this.field = field;
		}
	}

	private List<FieldOrder> generateAccessor(ResultSet rs, Class<?> clz) {
		Map<String, Integer> columnIndex = new HashMap<>();
		try {
			ResultSetMetaData rsMeta = rs.getMetaData();
			for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
				columnIndex.put(rsMeta.getColumnName(i), i);
			}
		} catch (SQLException e) {
			throw getConfiguration().get().translate(e);
		}
		List<FieldOrder> result = new ArrayList<>();
		for (Field field : Util.getDeclaredFields(clz)) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			Column columnAnno = field.getAnnotation(Column.class);
			if (columnAnno == null) {
				continue;
			}
			String columnName = field.getName();
			String name = StringUtils.trimToNull(columnAnno.value());
			if (name != null) {
				columnName = name;
			}
			field.setAccessible(true);
			Integer index = columnIndex.get(columnName);
			if (index == null) {
				throw Exceptions.illegalArgument("Column {} not in resultset.", columnName);
			}
			result.add(new FieldOrder(index, columnName, field));
		}
		return result;
	}

	public <T> T getFromResultSet(ResultSet rs, Class<T> clz) {
		List<FieldOrder> list = reflectCache.computeIfAbsent(clz, c -> generateAccessor(rs, c));
		try {
			T t = TypeUtils.newInstance(clz);
			for (FieldOrder field : list) {
				Object o = getConfiguration().get().get(rs, null, field.index, field.field.getType());
				if (field.field.getType().isPrimitive() && o == null) {
				} else {
					field.field.set(t, o);
				}
			}
			return t;
		} catch (Exception e) {
			throw Exceptions.toRuntime(e);
		}
	}

	public List<Constraint> getConstraints(SchemaAndTable table, boolean detail) {
		table = getConfiguration().getOverride(table);
		final String namespace = processNamespace(table.getSchema());
		final String tableName = processName(table.getTable(), Ops.EQ);
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		return getConstraints0(policy.asCatalog(namespace),policy.asSchema(namespace),tableName,detail);
	}

	private List<Constraint> getConstraints0(String catalog, String schema, String tableName, boolean detail) {
		SchemaReader template = getConfiguration().getTemplates().getSchemaAccessor();
		return doSQLQuery(q -> template.getConstraints(catalog,schema, tableName, q, detail), "getConstraints");
	}

	public List<PartitionInfo> getPartitions(SchemaAndTable table) {
		table = getConfiguration().getOverride(table);
		final String namespace = processNamespace(table.getSchema());
		final String tableName = processName(table.getTable(), Ops.EQ);
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		SchemaReader template = getConfiguration().getTemplates().getSchemaAccessor();
		List<PartitionInfo> partitions = doSQLQuery(q -> template.getPartitions(policy.asCatalog(namespace),policy.asSchema(namespace), tableName, q), "getPartitions");
		if (partitions == null) {
			throw new UnsupportedOperationException("Current database do not support PARTITION operation. " + getDriverInfo() .getDatabaseProductName());
		}
		if (partitions.size() == 1 && partitions.get(0).getMethod() == PartitionMethod.NOT_PARTITIONED) {
			return Collections.emptyList();
		}
		return partitions;
	}

	public static final int INDEX_POLICY_INDEX_ONLY = 0;

	public static final int INDEX_POLICY_ALL_INDEX = 1;

	public static final int INDEX_POLICY_MERGE_CONSTRAINTS = 2;

	/**
	 *  得到指定实体的所有索引(基于JDBC驱动实现，实际在不同数据库上可能不准确。)
	 *
	 *  @param table  指定实体类型
	 *  @param fetchPolicy {@link #INDEX_POLICY_INDEX_ONLY} 去除传统意义上的constraint （纯Index）
	 *                {@link #INDEX_POLICY_ALL_INDEX}
	 *                不去除Constraint。直接返回JDBC的getIndex的结果（单 JDBC）
	 *                {@link #INDEX_POLICY_MERGE_CONSTRAINTS}
	 *                连同已知的Constraint一起返回，Constraint的数据以Constraint为准（合并结果）
	 *
	 *  @see Constraint
	 *  @return Constraints
	 */
	public List<Constraint> getIndexes(SchemaAndTable table, int fetchPolicy) {
		table = getConfiguration().getOverride(table);
		String namespace = processNamespace(table.getSchema());
		final String tableName = processName(table.getTable(), Ops.EQ);
		SchemaPolicy policy = getConfiguration().getTemplates().getSchemaPolicy();
		final String catalog = policy.asCatalog(namespace);
		final String schema = policy.asSchema(namespace);
		
		List<Constraint> constraints = fetchPolicy == INDEX_POLICY_ALL_INDEX ? Collections.emptyList()
				: getConstraints0(catalog, schema, tableName, fetchPolicy == INDEX_POLICY_MERGE_CONSTRAINTS);
		Set<String> constraintsNames = collectConstraintNames(constraints);
		List<Constraint> result = new ArrayList<Constraint>();
		if (fetchPolicy == INDEX_POLICY_MERGE_CONSTRAINTS) {
			result.addAll(constraints);
		}
		
		for(Constraint index: getIndex0(catalog,schema,tableName)) {
			// 已经在约束数据中，从索引中去除
			if (constraintsNames.contains(index.getName())) {
				continue;
			}
			result.add(index);
		}
		return result;
	}
	
	private List<Constraint> getIndex0(String catalog, String schema, String tableName) {
		SchemaReader template = getConfiguration().getTemplates().getSchemaAccessor();
		return doSQLQuery(q -> template.getIndexes(catalog, schema, tableName, q), "getIndexes");
	}
	
	
	private Set<String> collectConstraintNames(Collection<Constraint> constraints) {
		if (constraints == null) {
			return Collections.emptySet();
		}
		Set<String> identifiers = constraints.stream().map(Constraint::getName).collect(Collectors.toSet());
		for (Constraint c : constraints) {
			if (StringUtils.isEmpty(c.getIndexQualifier())) {
				continue;
			}
			identifiers.add(c.getIndexQualifier());
		}
		return identifiers;
	}

	private static SchemaAndTable parseSchemaAndTable(String tableName) {
		String namespace = null;
		int n = tableName.indexOf('.');
		if (n > 0) {
			// 尝试从表名中计算schema
			namespace = tableName.substring(0, n);
			tableName = tableName.substring(n + 1);
		}
		return new SchemaAndTable(namespace, tableName);
	}

	private static String processName(String matchName, Ops oper) {
		if (oper != null && oper != Ops.EQ) {
			if (StringUtils.isEmpty(matchName)) {
				matchName = "%";
			} else if (oper == Ops.LIKE) {
				matchName = "%" + matchName + "%";
			} else if (oper == Ops.ENDS_WITH) {
				matchName = "%" + matchName;
			} else if (oper == Ops.STARTS_WITH) {
				matchName = matchName + "%";
			}
		}
		return matchName;
	}

	private static String processNamespace(String schema) {
		// 对“null”做特殊处理
		if ("null".equals(schema)) {
			return null;
		}
		return StringUtils.trimToNull(schema);
	}

	private SQLListenerContextImpl startContext(Connection connection) {
		SQLListenerContextImpl context = new SQLListenerContextImpl(metadata, connection, null);
		listeners.start(context);
		return context;
	}

	/*
	 * 仅限非Statement的操作
	 */
	private <R> R doConnectionAccess(QueryFunction<Connection, R> func) {
		Assert.notNull(func);
		Connection conn = getConnection();
		SQLListenerContextImpl context = startContext(conn);
		// long time = System.currentTimeMillis();
		try {
			R r = func.apply(conn);
			return r;
		} catch (SQLException e) {
			throw getConfiguration().get().translate(e);
		} finally {
			listeners.end(context);
		}
	}

	private <T> List<T> doMetadataQuery(String action, QueryFunction<DatabaseMetaData, ResultSet> func, QueryFunction<ResultSet, T> resultExtractor) {
		Assert.notNull(func);
		Assert.notNull(resultExtractor);
		Connection conn = getConnection();
		SQLListenerContextImpl context = startContext(conn);
		long time = System.currentTimeMillis();
		try {
			ConnectionWrapper q = new ConnectionWrapper(conn, getConfiguration());
			List<T> list = q.metadataQuery(func, resultExtractor);
			postExecuted(context, System.currentTimeMillis() - time, "(" + action + ")", list.size());
			return list;
		} finally {
			listeners.end(context);
		}
	}

	public <R> R doSQLQuery(java.util.function.Function<ConnectionWrapper, R> works, String sqlAction) {
		Connection conn = getConnection();
		SQLListenerContextImpl context = startContext(conn);
		long time = System.currentTimeMillis();
		try {
			ConnectionWrapper q = new ConnectionWrapper(conn, getConfiguration());
			R r = works.apply(q);
			int size = 1;
			if (r instanceof Collection) {
				size = ((Collection<?>) r).size();
			}
			postExecuted(context, System.currentTimeMillis() - time, "(" + sqlAction + ")", size);
			return r;
		} finally {
			listeners.end(context);
		}
	}

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, Object rst) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, String.valueOf(rst));
		context.setData(ContextKeyConstants.ACTION, action);
		if (getConfiguration().getSlowSqlWarnMillis() <= cost) {
			context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
		}
		listeners.executed(context);
	}

	public SchemaAndTable asInCurrentSchema(SchemaAndTable schemaAndTable) {
		if (processNamespace(schemaAndTable.getSchema()) == null) {
			schemaAndTable = new SchemaAndTable(getDriverInfo().getNamespace(), schemaAndTable.getTable());
		} else {
			schemaAndTable = getConfiguration().getOverride(schemaAndTable);
		}
		return schemaAndTable;
	}

	/**
	 * 指定一个SQL脚本文件运行
	 * @param url      the script file.
	 * @param endChars 命令结束字符
	 * @param charset charset
	 * @param ignoreErrors 出现错误继续执行
	 * @param exceptionCollector 执行错误的SQL的异常信息将被写入这个Map
	 * @return 所有指令返回的行数总和
	 */
	public int executeScriptFile(URL url, Charset charset, String endChars, boolean ignoreErrors, Map<String, RuntimeException> exceptionCollector) {
		Assert.notNull(url,"Script file url is null.");
		char[] ends = endChars.toCharArray();
		Connection conn = getConnection();
		SQLListenerContextImpl context = startContext(conn);
		ConnectionWrapper exe = new ConnectionWrapper(conn, getConfiguration());
		int statements = 0, count = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), charset))) {
			long time = System.currentTimeMillis();
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length() == 0 || line.startsWith("--")) {
					continue;
				}
				char end = line.charAt(line.length() - 1);
				if (ArrayUtils.contains(ends, end)) {
					sb.append(line, 0, line.length() - 1);
					String sql = sb.toString();
					sb.setLength(0);
					try {
						count += exe.executeQuery(sql, null, null);
						statements++;
					} catch (RuntimeException e) {
						if (exceptionCollector != null) {
							exceptionCollector.put(sql, e);
						}
						if (!ignoreErrors) {
							throw e;
						}
					}
				} else {
					sb.append(line).append('\n');
				}
			}
			if (sb.length() > 0) {
				String sql = sb.toString();
				try {
					count += exe.executeQuery(sql, null, null);
					statements++;
				} catch (RuntimeException e) {
					if (exceptionCollector != null) {
						exceptionCollector.put(sql, e);
					}
					if (!ignoreErrors) {
						throw e;
					}
				}
			}
			log.info("{} executed, sql={}, updated={}, time={}ms", url, statements, count, (System.currentTimeMillis() - time));
		} catch (IOException e) {
			throw Exceptions.toRuntime(e);
		} finally {
			listeners.end(context);
		}
		return count;
	}

	public abstract DistributedLock getLock(String lockName);
}
