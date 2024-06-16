package com.github.xuse.querydsl.sql.ddl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.column.ColumnFeature;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.ColumnMetadataExImpl;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.dialect.SpecialFeature;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.DDLOps;
import com.querydsl.core.types.DDLOps.AlterTableOps;
import com.querydsl.core.types.DDLOps.Basic;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.DDLExpressions;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlterTableQuery extends AbstractDDLClause<AlterTableQuery> {
	private boolean allowIndexDrop = true;

	private boolean allowConstraintDrop = true;

	private boolean allowColumnDrop;

	private boolean simulate;

	private boolean logSQL;

	private MetadataQuerySupport metadata;

	public AlterTableQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPathEx<?> path) {
		super(connection, configuration, path);
	}

	@Override
	protected List<String> generateSQLs() {
		SchemaAndTable acutalTable = metadata.asInCurrentSchema(table.getSchemaAndTable());
		List<ColumnDef> columns = metadata.getColumns(acutalTable);
		if (columns.isEmpty()) {
			// 无表，变为创建
			return asCreateTable();
		}
		CompareResult difference = new CompareResult();
		compareColumns(difference, columns);
		compareConstraints(difference, metadata.getIndexes(acutalTable, MetadataQuerySupport.INDEX_POILCY_MERGE_CONSTRAINTS));
		compareTableAttributes(difference,this.table,metadata.getTable(acutalTable));
		
		if (difference.isEmpty()) {
			log.info("TABLE [{}] compare finished, there's no difference between database and java definitions.",
					table);
			return Collections.emptyList();
		}
		List<String> sqls = new ArrayList<>();

		CompareResult independentOperations = new CompareResult();

		if (configuration.has(SpecialFeature.ONE_COLUMN_IN_SINGLE_DDL)) {
			for (ColumnMapping cp : difference.getAddColumns()) {
				SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
				if (serializer.serializeAlterTable(this.table, difference.ofAddSingleColumn(cp),
						independentOperations) > 0) {
					sqls.add(serializer.toString());
				}
			}

			for (String cp : difference.getDropColumns()) {
				SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
				if (serializer.serializeAlterTable(this.table, difference.ofDropSingleColumn(cp),
						independentOperations) > 0) {
					sqls.add(serializer.toString());
				}

			}
			for (ColumnModification cp : difference.getChangeColumns()) {
				if (cp.getChanges().size() == 1) {
					SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
					if (serializer.serializeAlterTable(this.table, difference.ofSingleChangeColumn(cp),
							independentOperations) > 0) {
						sqls.add(serializer.toString());
					}
				} else {
					for (ColumnChange cg : cp.getChanges()) {
						SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
						ColumnModification param = cp.ofSingleChange(cg);
						if (serializer.serializeAlterTable(this.table, difference.ofSingleChangeColumn(param),
								independentOperations) > 0) {
							sqls.add(serializer.toString());
						}
					}
				}
			}
			for (Constraint c : difference.getDropConstraints()) {
				SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
				if (serializer.serializeAlterTable(this.table, difference.ofDropSingleConstraint(c),
						independentOperations) > 0) {
					sqls.add(serializer.toString());
				}
			}
			for (Constraint c : difference.getAddConstraints()) {
				SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
				if (serializer.serializeAlterTable(this.table, difference.ofAddSingleConstraint(c),
						independentOperations) > 0) {
					sqls.add(serializer.toString());
				}
				;
			}
		} else {
			SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
			if (serializer.serializeAlterTable(this.table, difference, independentOperations) > 0) {
				sqls.add(serializer.toString());
			}
		}

		// 开始处理Alter table不支持的索引和约束
		for (Constraint c : independentOperations.getDropConstraints()) {
			SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
			serializer.serialzeConstraintIndepentDrop(this.table, c);
			sqls.add(serializer.toString());
		}

		for (Constraint c : independentOperations.getAddConstraints()) {
			SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
			serializer.serialzeConstraintIndepentCreate(this.table, c);
			sqls.add(serializer.toString());
		}
		if (simulate || logSQL) {
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

	private void compareTableAttributes(CompareResult difference, RelationalPath<?> table, TableInfo tableInfo) {
		if (table instanceof RelationalPathEx) {
			RelationalPathEx<?> entity = (RelationalPathEx<?>) table;
			if(configuration.getTemplates().supports(DDLOps.COMMENT)) {
				if(!stringEquals(entity.getComment(), tableInfo.getRemarks(),false)){
					difference.setTableCommentChange(entity.getComment());
				}	
			}
			if(configuration.getTemplates().supports(DDLOps.COLLATE)) {
				if(!stringEquals(entity.getCollate().name(), tableInfo.getAttribute("COLLATE"),true)){
					difference.setTableCollation(entity.getCollate().name());
				}
			}
		}
	}

	private boolean stringEquals(String comment, String remarks, boolean ignoreCase) {
		if (comment == remarks) {
			return true;
		}
		if(StringUtils.isEmpty(comment) && StringUtils.isEmpty(remarks)) {
			return true;
		}
		return comment == null ? false : ignoreCase ? comment.equalsIgnoreCase(remarks) : comment.equals(remarks);
	}

	private void compareConstraints(CompareResult differenceContainer, List<Constraint> dbConstraints) {
		if (table instanceof RelationalPathEx) {
			SQLTemplatesEx templates=configuration.getTemplates();
			RelationalPathEx<?> entity = (RelationalPathEx<?>) table;
			List<Constraint> toCreate = new ArrayList<>(entity.getConstraints());
			List<Constraint> toDrop = new ArrayList<>(dbConstraints);
			// 对比两个列表，去重后，位于toCreate里的就是需要创建的，位于toDrop的就是需要删除的。
			for (Iterator<Constraint> iter = toCreate.iterator(); iter.hasNext();) {
				Constraint constraint = iter.next();
				if (constraint.getConstraintType().isIgnored()||templates.notSupports(constraint.getConstraintType())) {
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
				if (d.getConstraintType().isIgnored()||templates.notSupports(d.getConstraintType())) {
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
	 * @param defined
	 * @param toDrop
	 * @return
	 */
	private Constraint findDuplicateConstraint(Constraint defined, List<Constraint> toDrop) {
		for (Constraint c : toDrop) {
			if (defined.contentEquals(c)) {
				return c;
			}
		}
		return null;
	}

	private List<String> asCreateTable() {
		List<String> sqls = new ArrayList<>();
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		List<Constraint> others = serializer.serializeTableCreate(table);
		{
			String sql = serializer.toString();
			// log.info(sql);
			sqls.add(sql);
		}
		for (Constraint c : others) {
			SQLSerializerAlter serializer2 = new SQLSerializerAlter(configuration, true);
			serializer2.serialzeConstraintIndepentCreate(table, c);
			String sql = serializer2.toString();
			// log.info(sql);
			sqls.add(sql);
		}
		return sqls;
	}

	@Override
	protected String generateSQL() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 设置是否允许删除Column，默认false
	 * 
	 * @param flag
	 * @return this
	 */
	public AlterTableQuery dropColumns(boolean flag) {
		this.allowColumnDrop = flag;
		return this;
	}

	/**
	 * @param flag 是否删除索引
	 * @return this
	 */
	public AlterTableQuery dropIndexes(boolean flag) {
		this.allowColumnDrop = flag;
		return this;
	}

	/**
	 * @param flag 是否删除约束
	 * @return this
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
		// 新增列
		Map<String, ColumnMapping> defined = getColumnMapLower((RelationalPathEx<?>) this.table);
		List<String> toBeDropped = new ArrayList<String>();
		List<ColumnModification> changed = new ArrayList<ColumnModification>();

		// 比较差异
		for (ColumnDef c : columns) {
			String columnKey = c.getColumnName().toLowerCase();
			ColumnMapping definedColumn = defined.remove(columnKey);
			// is dropped column
			if (definedColumn == null) {
				if (allowColumnDrop && supportsChangeDelete) {
					toBeDropped.add(c.getColumnName());
				}
				continue;
			}
			// 创建Java侧的列定义。
			// 为什么不直接用PathMapping对象，因为该定义在数据库方言下，部分字段的类型和长度可能被转化为其他数据库类型。
			Path<?> path = definedColumn.getPath();
			ColumnDef javaColumn = getTemplates().getColumnDataType(definedColumn.getJdbcType(),
					definedColumn.getSize(), definedColumn.getDigits());
			ColumnMetadataExImpl javaSide = new ColumnMetadataExImpl(
					definedColumn.getColumn().ofType(javaColumn.getJdbcType()).withSize(javaColumn.getColumnSize())
							.withDigits(javaColumn.getDecimalDigit()));
			javaSide.setComment(definedColumn.getComment());
			javaSide.setDefaultExpression(definedColumn.getDefaultExpression());
			javaSide.setFeatures(definedColumn.getFeatures());
			javaSide.setUnsigned(definedColumn.isUnsigned());

			// 创建Database侧的列定义
			ColumnMetadataExImpl dbSide = toColumnMetadata(c);
			// 比较差异
			List<ColumnChange> changes = compareDataType(javaSide, dbSide, javaColumn, c);
			if (!changes.isEmpty()) {
				// 这里必须填写definedColumn
				changed.add(new ColumnModification(path, dbSide, changes, definedColumn));
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
		ColumnMetadata querydslColumn = ColumnMetadata.named(c.getColumnName()).withSize(c.getColumnSize())
				.withIndex(c.getOrdinal()).withDigits(c.getDecimalDigit()).ofType(c.getJdbcType());
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
	private List<ColumnChange> compareDataType(ColumnMetadataExImpl c1, ColumnMetadataExImpl c2, ColumnDef java,
			ColumnDef db) {
		List<ColumnChange> result = new ArrayList<ColumnChange>();
		// 忽略字段顺序和列名称，仅对比其他8个属性
		if (dataTypeChanged(c1, c2) || c1.isAutoIncreament() != c2.isAutoIncreament()) {
			Expression<?> from = DDLExpressions.dataType(db.getDataType(), c2.isNullable(), c2.isUnsigned(), null);
			Expression<?> to = DDLExpressions.dataType(java.getDataType(), c1.isNullable(), c1.isUnsigned(), null);
			log.info("changge {} ->{}", from, to);
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
		if (!configuration.getTemplates().notSupports(DDLOps.COMMENT)) {
			if (!Objects.equals(c1.getComment(), c2.getComment())) {
				Expression<String> exp1 = StringUtils.isEmpty(c1.getComment()) ? null
						: ConstantImpl.create(c1.getComment());
				Expression<String> exp2 = StringUtils.isEmpty(c2.getComment()) ? null
						: ConstantImpl.create(c2.getComment());
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
		Operator ops=Ops.EQ;
		if (type == Types.TIME || type == Types.TIMESTAMP || type == Types.TIME_WITH_TIMEZONE
				|| type == Types.TIMESTAMP_WITH_TIMEZONE) {
			ops = Basic.TIME_EQ;
		}
		Expression<Boolean> compareExpression = Expressions.simpleOperation(Boolean.class, ops, c1Default,
				c2Default);
		s.handle(DDLExpressions.simple(Basic.SELECT_VALUES, compareExpression,
				Expressions.path(Object.class, null, dummyTable)));
		String sql = s.toString();
		final SQLBindings qSql = new SQLBindings(sql, s.getConstants());
		try {
			return metadata.doSQLQuery(e -> e.querySingle(qSql, r -> r.getBoolean(1)), "CompareDefault");
		} catch (Exception e) {
			log.error("Compare default value bwtween [{}] = [{}] error", c1Default, c2Default, e);
		}
		return false;
	}

	private boolean dataTypeChanged(ColumnMetadataExImpl c1, ColumnMetadataExImpl c2) {
		String name = c1.getName();
		if (c1.getJdbcType() != c2.getJdbcType()) {
			System.out.println(name + " -jdbc" + c1.getJdbcType() + " != " + c2.getJdbcType());
			return true;
		}
		if (c1.hasSize() && c1.getSize() != c2.getSize()) {
			System.out.println(name + " -size" + c1.getSize() + " != " + c2.getSize());
			return true;
		}
		if (SQLTypeUtils.isNumeric(c1.getJdbcType())) {
			if (c1.hasDigits() && c1.getDigits() != c2.getDigits()) {
				System.out.println(name + " -digits" + c1.getDigits() + " != " + c2.getDigits());
				return true;
			}
		}
		if (configuration.getTemplates().supports(DDLOps.UNSIGNED) && c1.isUnsigned() != c2.isUnsigned()) {
			System.out.println(name + " -isUnsigned" + c1.isUnsigned() + " != " + c2.isUnsigned());
			return true;
		}
		return false;
	}

	public AlterTableQuery simulate(boolean flag) {
		simulate = flag;
		return this;
	}
	
	@Override
	protected void finished(List<String> sqls) {
		log.info("Alter table {} finished, {} sqls executed.",table.getSchemaAndTable(),sqls.size());
	}

}
