package com.github.xuse.querydsl.sql.ddl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.column.ColumnBuilderHandler;
import com.github.xuse.querydsl.sql.column.ColumnFeature;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.ColumnMetadataExImpl;
import com.github.xuse.querydsl.sql.column.PathMapping;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterColumnOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.Basic;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.TypeUtils;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlterTableQuery extends AbstractDDLClause<AlterTableQuery> {

	private boolean allowIndexDrop = true;

	private boolean allowConstraintDrop = true;

	private boolean allowColumnDrop;

	private boolean simulate;

	private MetadataQuerySupport metadata;

	private final RelationalPathExImpl<?> table;
	
	//K = old column, V = new column
	private final Map<String, String> columnRenameMapping = new HashMap<>();

	public AlterTableQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		super(connection, configuration, RelationalPathExImpl.toRelationPathEx(path));
		this.table=RelationalPathExImpl.valueOf(path);
	}

	@Override
	protected List<String> generateSQLs() {
		SchemaAndTable actualTable = metadata.asInCurrentSchema(table.getSchemaAndTable());
		if (routing != null) {
			actualTable = routing.getOverride(actualTable, configuration);
		}
		List<ColumnDef> columns = metadata.getColumns(actualTable);
		
		
		DDLMetadataBuilder builder=new DDLMetadataBuilder(configuration,table,routing);
		if (columns.isEmpty()) {
			// 无表，变为创建
			builder.serializeTableCreate(false);
			return builder.getSqls();
		}
		
		CompareResult difference = new CompareResult();
		compareColumns(difference, columns);
		compareConstraints(difference, metadata.getIndexes(actualTable, MetadataQuerySupport.INDEX_POLICY_MERGE_CONSTRAINTS));
		compareTableAttributes(difference, table, metadata.getTable(actualTable));
		if (difference.isEmpty()) {
			log.info("TABLE [{}] compare finished, there's no difference between database and java definitions.", table);
			return Collections.emptyList();
		}
		builder.serializeAlterTable(difference);
		List<String> sqls = builder.getSqls();
		if (simulate) {
			log.info("Alter table SQL{}:", sqls.size());
			for (String s : sqls) {
				log.info(s);
			}
		}
		if (simulate) {
			sqls.clear();
		}
		return sqls;
	}

	/**
	 *  修改数据库中的数据库主键。
	 *  @implSpec
	 *   仅用于对数据库进行修改，Java模型中的数据库主键不会发生变化。
	 *  @param paths 主键列。
	 *  @return this
	 */
	public AlterTableQuery changePrimaryKey(Path<?>... paths) {
		table.createPrimaryKey(paths);
		return this;
	}

	/**
	 * 为表增加索引
	 * @implSpec
	 *  等效于 createConstraint(name, ConstraintType.KEY, paths)
	 * @param name name
	 * @param paths paths
	 * @return this
	 */
	public AlterTableQuery createIndex(String name, Path<?>... paths) {
		table.createConstraint(name, ConstraintType.KEY, paths);
		return this;
	}

	/**
	 * 为表增加唯一约束
	 * @implSpec
	 *  等效于 createConstraint(name, ConstraintType.UNIQUE, paths)
	 * @param name name
	 * @param paths paths
	 * @return this
	 */
	public AlterTableQuery createUnique(String name, Path<?>... paths) {
		table.createConstraint(name, ConstraintType.UNIQUE, paths);
		return this;
	}

	/**
	 * 删除指定名称的索引或约束
	 * @param name name
	 * @return this
	 */
	public AlterTableQuery removeConstraintOrIndex(String name) {
		int count = 0;
		for (Iterator<Constraint> iter = table.getConstraints().iterator(); iter.hasNext(); ) {
			Constraint c = iter.next();
			if (name.equals(c.getName())) {
				iter.remove();
				count++;
			}
		}
		if (count == 0) {
			throw Exceptions.illegalArgument("There is no Index or Constraint named {}", name);
		}
		return this;
	}

	/**
	 * Adding a column / 增加一个列
	 * @param <A> type of the column
	 * @param column 列定义，参见 {@link ColumnMetadata}
	 * @param type Java数据类型
	 * @return ColumnBuilderHandler，可配置该列的缺省值等信息。
	 */
	public <A> ColumnBuilderHandler<A, AlterTableQuery> addColumn(ColumnMetadata column, Class<A> type) {
		String name = column.getName();
		Path<A> path = table.createPath(name, type);
		PathMapping cb = table.addMetadataDynamic(path, column);
		return new ColumnBuilderHandler<A, AlterTableQuery>(cb, this);
	}
	
	/**
	 * Change a column. / 修改列。如果前后列名不一样，会对列重命名。
	 * @param <A> 新的Java映射类型，如果是和静态模型映射，使用Java字段类型即可。
	 * @param path 字段
	 * @param column column metadata.
	 * @param type type 
	 * @return ColumnBuilderHandler，可配置该列的缺省值等信息。
	 */
	public <A> ColumnBuilderHandler<A, AlterTableQuery> changeColumn(Path<?> path, Class<A> type, ColumnMetadata column) {
		ColumnMapping mapping = table.removeColumn(path);
		if(mapping==null) {
			throw new IllegalArgumentException("The path '"+column+"' is not belong to current table.");
		}
		if(!mapping.getName().equals(column.getName())){
			//Column rename
			this.columnRenameMapping.put(mapping.getName().toLowerCase(), column.getName().toLowerCase());
			//如果列改名，Path需要更换为指向当前对象的Path，否则会在序列化时导致列名计算不正确
			path=TypeUtils.createPathByType(type, path.getMetadata().getName(), table);
		}
		PathMapping cb = table.addMetadataDynamic(path, column);
		return new ColumnBuilderHandler<A, AlterTableQuery>(cb, this);
	}
	
	/**
	 * drop one column by the path name.
	 * @param pathName
	 * @return this
	 */ 
	public AlterTableQuery removeColumn(String pathName) {
		Path<?> p=table.getColumn(pathName);
		if(p==null) {
			throw new IllegalArgumentException("There's no column path named '"+pathName+"'.");
		}
		table.removeColumn(p);
		return this;
	}
	
	public AlterTableQuery removeColumn(Path<?> column) {
		Path<?> thisPath=table.getColumn(column.getMetadata().getName());
		if (thisPath != column) {
			throw new IllegalArgumentException("The path '"+column+"' is not belong to current table.");
		}
		table.removeColumn(column);
		return this;
	}
	

	/**
	 *  为表增加索引或约束
	 *  @param name 名称
	 *  @param type 约束/索引类型
	 *  @param paths 字段
	 *  @return this
	 */
	public AlterTableQuery createConstraint(String name, ConstraintType type, Path<?>... paths) {
		table.createConstraint(name, type, paths);
		return this;
	}

	/**
	 *  为表增加一个检查约束
	 *  @param name 名称
	 *  @param expr 检查表达式
	 *  @return this
	 */
	public AlterTableQuery createCheck(String name, Expression<Boolean> expr) {
		table.createCheck(name, expr);
		return this;
	}

	/**
	 * 设置是否允许删除Column，默认false
	 *
	 * @param flag flag
	 * @return this
	 */
	public AlterTableQuery dropColumns(boolean flag) {
		this.allowColumnDrop = flag;
		return this;
	}

	/**
	 *  @param flag 是否删除索引
	 *  @return this
	 */
	public AlterTableQuery dropIndexes(boolean flag) {
		this.allowIndexDrop = flag;
		return this;
	}

	/**
	 *  @param flag 是否删除约束
	 *  @return this
	 */
	public AlterTableQuery dropConstraint(boolean flag) {
		this.allowConstraintDrop = flag;
		return this;
	}

	protected boolean preExecute(MetadataQuerySupport metadata) {
		this.metadata = metadata;
		return true;
	}

	/*
	 * 按ColumnName小写索引
	 */
	private Map<String, ColumnMapping> getColumnMapLower(RelationalPathEx<?> meta) {
		Map<String, ColumnMapping> map = new HashMap<String, ColumnMapping>();
		for (Path<?> p : meta.getColumns()) {
			ColumnMapping mapping = meta.getColumnMetadata(p);
			map.put(mapping.getColumn().getName().toLowerCase(), mapping);
		}
		return map;
	}

	private void compareColumns(CompareResult sqls, List<ColumnDef> columns) {
		SQLTemplatesEx dialect = configuration.getTemplates();
		boolean supportsChangeDelete = !dialect.notSupports(AlterTableOps.DROP_COLUMN);
		if (!supportsChangeDelete) {
			log.warn("Current database [{}] doesn't support alter table column.", dialect.getClass().getName());
		}
		PrimaryKey<?> keys= this.table.getPrimaryKey();
		//defined - 当前内存模型中的列定义 
		Map<String, ColumnMapping> defined = getColumnMapLower(this.table);
		List<String> toBeDropped = new ArrayList<String>();
		List<ColumnModification> changed = new ArrayList<ColumnModification>();
		
		//比较差异, columns=数据库中的实际列定义
		for (ColumnDef c : columns) {
			String columnKey = c.getColumnName().toLowerCase();
			String originalName=null;
			if(columnRenameMapping.get(columnKey)!=null) {
				originalName = c.getColumnName();
				columnKey = columnRenameMapping.get(columnKey);
			}
			ColumnMapping definedColumn = defined.remove(columnKey);
			// is drop column
			if (definedColumn == null) {
				if (allowColumnDrop && supportsChangeDelete) {
					toBeDropped.add(c.getColumnName());
				}
				continue;
			}
			// 创建Java侧的列定义(不可以直接用PathMapping中的ColumnMetadata？因为该定义在数据库方言下，部分字段的类型和长度可能被转化为其他类型。)
			ColumnDef javaColumn = getTemplates().getColumnDataType(definedColumn.getJdbcType(), definedColumn.getSize(), definedColumn.getDigits());
			ColumnMetadata columnMetadata = definedColumn.getColumn().ofType(javaColumn.getJdbcType()).withSize(javaColumn.getColumnSize()).withDigits(javaColumn.getDecimalDigit());
			
			Path<?> path = definedColumn.getPath();
			boolean isPk = keys == null ? false : keys.getLocalColumns().contains(path);
			if(isPk) {
				columnMetadata = columnMetadata.notNull();
			}
			ColumnMetadataExImpl javaSide = new ColumnMetadataExImpl(columnMetadata);
			javaSide.setComment(definedColumn.getComment());
			javaSide.setDefaultExpression(definedColumn.getDefaultExpression());
			javaSide.setFeatures(definedColumn.getFeatures());
			javaSide.setUnsigned(definedColumn.isUnsigned());
			
			// 创建Database侧的列定义
			ColumnMetadataExImpl dbSide = toColumnMetadata(c);
			// 比较差异
			List<ColumnChange> modifications = compareDataType(javaSide, dbSide, javaColumn, c);
			if (originalName!=null || !modifications.isEmpty()) {
				changed.add(new ColumnModification(path, dbSide, modifications, definedColumn,originalName));
			}
		}
		Map<String, ColumnMapping> insert = new HashMap<String, ColumnMapping>();
		for (Map.Entry<String, ColumnMapping> e : defined.entrySet()) {
			String columnName = configuration.getColumnOverride(table.getSchemaAndTable(), e.getKey());
			insert.put(columnName, e.getValue());
		}
		sqls.setDropColumns(toBeDropped);
		sqls.setAddColumns(insert.values());
		sqls.setChangeColumns(changed);
	}

	private ColumnMetadataExImpl toColumnMetadata(ColumnDef c) {
		ColumnMetadata querydslColumn = ColumnMetadata.named(c.getColumnName()).withSize(c.getColumnSize()).withIndex(c.getOrdinal()).withDigits(c.getDecimalDigit()).ofType(c.getJdbcType());
		if (!c.isNullable()) {
			querydslColumn = querydslColumn.notNull();
		}
		ColumnMetadataExImpl column = new ColumnMetadataExImpl(querydslColumn);
		if (StringUtils.isNotEmpty(c.getColumnDef())) {
			Template tt = TemplateFactory.DEFAULT.create(c.getColumnDef());
			column.setDefaultExpression(Expressions.simpleTemplate(Object.class, tt, Collections.emptyList()));
		}
		if (c.isAutoIncrement()) {
			column.setFeatures(new ColumnFeature[] { ColumnFeature.AUTO_INCREMENT });
		}
		if (StringUtils.isNotEmpty(c.getRemarks())) {
			column.setComment(c.getRemarks());
		}
		if (SQLTypeUtils.isNumeric(c.getJdbcType())) {
			column.setUnsigned(c.getDataType().toUpperCase().contains("UNSIGNED"));
		}
		return column;
	}

	/*
	 * @param c1 from java class.
	 * 
	 * @param c2 from database.
	 */
	private List<ColumnChange> compareDataType(ColumnMetadataExImpl c1, ColumnMetadataExImpl c2, ColumnDef java, ColumnDef db) {
		List<ColumnChange> result = new ArrayList<ColumnChange>();
		// 忽略字段顺序和列名称，仅对比其他8个属性
		if (dataTypeChanged(c1, c2) || c1.isAutoIncreament() != c2.isAutoIncreament()) {
			Expression<?> from = DDLExpressions.dataType(DDLOps.DATA_TYPE, db.getDataType(), c2.isNullable(), c2.isUnsigned(), null);
			Expression<?> to = DDLExpressions.dataType(AlterColumnOps.SET_DATATYPE,java.getDataType(), c1.isNullable(), c1.isUnsigned(), null);
			
			log.info("CHANGE: {},null:{},unsign:{} -> {},null:{},unsign:{}", db.getDataType(), c2.isNullable(),c2.isUnsigned(), java.getDataType(), c1.isNullable(), c1.isUnsigned());
			result.add(ColumnChange.dataType(from, to));
		}
		// NULL修改
		if (c1.isNullable() != c2.isNullable()) {
			if (c1.isNullable()) {
				result.add(ColumnChange.toNull());
			} else {
				result.add(ColumnChange.toNotNull());
			}
		}
		// 对比缺省值
		Expression<?> c1Default = c1.getDefaultExpression();
		Expression<?> c2Default = c2.getDefaultExpression();
		if (c1Default != c2Default) {
			if (c1Default == null || c2Default == null) {
				result.add(ColumnChange.changeDefault(c2.getDefaultExpression(), c1.getDefaultExpression()));
			} else {
				// 因为c1是java侧建模。需要unwrap后重新计算, c2是已经做过处理的
				if (c1Default instanceof Constant<?>) {
					Object o = ((Constant<?>) c1Default).getConstant();
					String exp = SQLTypeUtils.serializeLiteral(String.valueOf(o), c1.getJdbcType());
					c1Default = Expressions.template(Object.class, exp);
				}
				String c1Str = c1Default.toString();
				String c2Str = c2Default.toString();
				if (!c1Str.equals(c2Str)) {
					if (!compareExpressionViaDb(c1Default, c2Default, c1.getJdbcType())) {
						result.add(ColumnChange.changeDefault(c2.getDefaultExpression(), c1.getDefaultExpression()));
					}
				}
			}
		}
		// 对比Comments
		if (configuration.getTemplates().supports(DDLOps.COMMENT_ON_COLUMN)) {
			if (!Objects.equals(c1.getComment(), c2.getComment())) {
				Expression<String> exp1 = StringUtils.isEmpty(c1.getComment()) ? null : ConstantImpl.create(c1.getComment());
				Expression<String> exp2 = StringUtils.isEmpty(c2.getComment()) ? null : ConstantImpl.create(c2.getComment());
				result.add(ColumnChange.comment(exp2, exp1));
			}
		}
		return result;
	}

	/*
	 * MYSQL：column time(3) default '12:00:00'。got 12:00:00.000' from the database metadata. 两个数值对比(select '12:00:00' =
	 * when comparing in database, 12:00:00 is not equal to 12:00:00.000.
	 * 为此，定义了Basic.TIME_EQ来做时间戳比较。
	 */
	private boolean compareExpressionViaDb(Expression<?> c1Default, Expression<?> c2Default, int type) {
		SQLSerializer s = new SQLSerializer(configuration.get());
		s.setUseLiterals(true);
		String dummyTable = configuration.getTemplates().getDummyTable();
		Operator ops = Ops.EQ;
		if (type == Types.TIME || type == Types.TIMESTAMP || type == Types.TIME_WITH_TIMEZONE || type == Types.TIMESTAMP_WITH_TIMEZONE) {
			ops = Basic.TIME_EQ;
		}
		Expression<Boolean> compareExpression = Expressions.simpleOperation(Boolean.class, ops, c1Default, c2Default);
		Expression<?> dualPath=StringUtils.isEmpty(dummyTable)?DDLExpressions.empty(): Expressions.path(Object.class, dummyTable);
		s.handle(DDLExpressions.simple(Basic.SELECT_VALUES, compareExpression, dualPath));
		String sql = s.toString();
		final SQLBindings qSql = new SQLBindings(sql, s.getConstants());
		try {
			return metadata.doSQLQuery(e -> e.querySingle(qSql, r -> r.getBoolean(1)), "CompareDefault");
		} catch (Exception e) {
			log.error("Compare default value between [{}] = [{}] error", c1Default, c2Default, e);
		}
		return false;
	}

	private boolean dataTypeChanged(ColumnMetadataExImpl c1, ColumnMetadataExImpl c2) {
		if (c1.getJdbcType() != c2.getJdbcType()) {
			return true;
		}
		if (c1.hasSize() && c1.getSize() != c2.getSize()) {
			return true;
		}
		if (SQLTypeUtils.isNumeric(c1.getJdbcType())) {
			if (c1.hasDigits() && c1.getDigits() != c2.getDigits()) {
				return true;
			}
		}
		return configuration.getTemplates().supports(DDLOps.UNSIGNED) && c1.isUnsigned() != c2.isUnsigned();
	}

	/**
	 *  模拟操作：打印出SQL语句，实际不操作。
	 *  @param flag this
	 *  @return this
	 */
	public AlterTableQuery simulate(boolean flag) {
		simulate = flag;
		return this;
	}

	@Override
	protected int finished(List<String> sqls) {
		log.info("Alter table {} finished, {} sqls executed.", table.getSchemaAndTable(), sqls.size());
		return sqls.size();
	}

	private void compareTableAttributes(CompareResult difference, RelationalPath<?> table, TableInfo tableInfo) {
		if (table instanceof RelationalPathEx) {
			RelationalPathEx<?> entity = (RelationalPathEx<?>) table;
			if (configuration.getTemplates().supports(DDLOps.COMMENT_ON_TABLE)) {
				if (!stringEquals(entity.getComment(), tableInfo.getRemarks(), false)) {
					difference.setTableCommentChange(entity.getComment());
				}
			}
			if (configuration.getTemplates().supports(DDLOps.COLLATE) && entity.getCollate() != null) {
				String collateName = entity.getCollate().name();
				if (!stringEquals(collateName, tableInfo.getAttribute("COLLATE"), true)) {
					difference.setTableCollation(collateName);
				}
			}
		}
	}

	private boolean stringEquals(String comment, String remarks, boolean ignoreCase) {
		if (comment == remarks) {
			return true;
		}
		if (StringUtils.isEmpty(comment) && StringUtils.isEmpty(remarks)) {
			return true;
		}
		return comment != null && (ignoreCase ? comment.equalsIgnoreCase(remarks) : comment.equals(remarks));
	}

	private void compareConstraints(CompareResult differenceContainer, List<Constraint> dbConstraints) {
		if (table != null) {
			SQLTemplatesEx templates = configuration.getTemplates();
			RelationalPathEx<?> entity = table;
			List<Constraint> toCreate = new ArrayList<>(entity.getConstraints());
			List<Constraint> toDrop = new ArrayList<>(dbConstraints);
			// 对比两个列表，去重后，位于toCreate里的就是需要创建的，位于toDrop的就是需要删除的。
			for (Iterator<Constraint> iter = toCreate.iterator(); iter.hasNext(); ) {
				Constraint constraint = iter.next();
				if (constraint.getConstraintType().isIgnored() || templates.notSupports(constraint.getConstraintType())) {
					// 当前框架还不支持的类型，不处理
					iter.remove();
					continue;
				}
				Constraint duplicate = findDuplicateConstraint(constraint, toDrop);
				if (duplicate != null) {
					toDrop.remove(duplicate);
					iter.remove();
				}
			}
			// 处理主键
			Constraint pk = Constraint.valueOf(entity.getPrimaryKey());
			if (pk != null) {
				Constraint duplicate = findDuplicateConstraint(pk, toDrop);
				if (duplicate != null) {
					toDrop.remove(duplicate);
				} else {
					toCreate.add(pk);
				}
			}
			// Filter toDrop
			List<Constraint> toDropFiltered = new ArrayList<>();
			for (Constraint d : toDrop) {
				if (d.getConstraintType().isIgnored() || templates.notSupports(d.getConstraintType())) {
					continue;
				}
				if (d.getConstraintType().isIndex()) {
					if (allowIndexDrop) {
						toDropFiltered.add(d);
					}
				} else if (allowConstraintDrop) {
					toDropFiltered.add(d);
				}
			}
			differenceContainer.setDropConstraints(toDropFiltered);
			differenceContainer.setAddConstraints(toCreate);
		}
	}

	/**
	 * 不是根据约束名称去判断，而是根据内容去判断
	 *
	 * @param defined defined
	 * @param toDrop toDrop
	 * @return Constraint
	 */
	private Constraint findDuplicateConstraint(Constraint defined, List<Constraint> toDrop) {
		for (Constraint c : toDrop) {
			if (defined.contentEquals(c)) {
				return c;
			}
		}
		return null;
	}

	@Override
	protected String generateSQL() {
		throw new UnsupportedOperationException();
	}
}
