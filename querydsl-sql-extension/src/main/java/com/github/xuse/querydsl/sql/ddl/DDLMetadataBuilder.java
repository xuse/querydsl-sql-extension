package com.github.xuse.querydsl.sql.ddl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.column.ColumnFeature;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.ColumnMetadataEx;
import com.github.xuse.querydsl.sql.column.ColumnMetadataExImpl;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterColumnOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableConstraintOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTableOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTablePartitionOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.DropStatement;
import com.github.xuse.querydsl.sql.ddl.DDLOps.OtherStatement;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionDefineOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.github.xuse.querydsl.sql.dialect.SpecialFeature;
import com.github.xuse.querydsl.sql.partitions.PartitionAssigned;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.routing.TableRouting;
import com.github.xuse.querydsl.sql.routing.WrapdRoutingStrategy;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DDLMetadataBuilder {
	final private ConfigurationEx configuration;
	final private RelationalPath<?> table;
	final private RoutingStrategy routing;
	final private List<DDLMetadata> result = new ArrayList<>();

	public DDLMetadataBuilder(ConfigurationEx configuration, RelationalPath<?> table, RoutingStrategy routing) {
		this.configuration = configuration;
		this.table = table;
		this.routing = routing == null ? RoutingStrategy.DEFAULT : routing;
	}

	public void serialzeConstraintIndepentDrop(Constraint c) {
		DDLMetadata meta = new DDLMetadata(false, true);
		Assert.isNotEmpty(c.getName());
		SchemaAndTable name = generateConstraintName(c.getName(), table, false);
		Expression<?> dropExpr = DDLExpressions.simple(DropStatement.DROP_INDEX,
				Expressions.path(Object.class, name.getTable()));
		meta.addExpression(dropExpr);
		result.add(meta);
	}

	public void serilizeSimple(Operator ops, Expression<?>... exprs) {
		DDLMetadata meta = new DDLMetadata(true, true);
		meta.addExpression(DDLExpressions.simple(ops, exprs));
		result.add(meta);
	}
	
	public void serializeTableCreate(boolean processPartition) {
		final RelationalPathEx<?> tableEx = (table instanceof RelationalPathEx) ? (RelationalPathEx<?>) table : null;
		SQLTemplatesEx template = configuration.getTemplates();
		//计算要不要创建分区
		processPartition = processPartition && isPartitionedTable(tableEx);
		boolean ignoreKeysOnPartitionedTable = processPartition && configuration.has(SpecialFeature.NO_KEYS_ON_PARTITION_TABLE);
		//For postgresql. remove index / constraints from the partitioned table. 
		if(ignoreKeysOnPartitionedTable) {
			log.warn("Curentdatabase do not support primary key on partition table, All keys and constraints will not set to table in database. {}",table.getSchemaAndTable());
		}
		
		// The main Statement
		DDLMetadata meta = new DDLMetadata(true, true);
		result.add(meta);
		meta.setAction(template.getCreateTable());
		List<Expression<?>> tableDefExpressions = new ArrayList<>();
		// Add columns
		PrimaryKey<?> keys = table.getPrimaryKey();
		for (Path<?> p : table.getColumns()) {
			ColumnMetadata c = table.getMetadata(p);
			ColumnMetadataEx cx = tableEx == null ? new ColumnMetadataExImpl(c) : tableEx.getColumnMetadata(p);
			boolean isPk = keys == null ? false : keys.getLocalColumns().contains(p);
			tableDefExpressions.add(generateColumnDefinition(p, cx, isPk));
		}
		// Add Primary key.
		{
			if (keys != null && !keys.getLocalColumns().isEmpty()) {
				if(!ignoreKeysOnPartitionedTable) {
					Expression<?> columns = DDLExpressions.wrap(ExpressionUtils.list(Tuple.class, keys.getLocalColumns()));
					tableDefExpressions.add(DDLExpressions.constraintDefinition(ConstraintType.PRIMARY_KEY, table,
							new SchemaAndTable(null, ""), columns));	
				}
			}
		}
		// Add Constraint or index
		if(!ignoreKeysOnPartitionedTable) {
			if (tableEx != null && CollectionUtils.isNotEmpty(tableEx.getConstraints())) {
				for (Constraint constraint : tableEx.getConstraints()) {
					if (template.supportCreateInTableDefinition(constraint.getConstraintType())) {
						tableDefExpressions.add(generateConstraintDefinition(constraint, table));
					} else {
						addIndependConstraintMeta(constraint);
					}
				}
			}	
		}
		Expression<?> tableCreateExpression = DDLExpressions.tableDefinitionList(tableDefExpressions, true);
		
		//Add collate engine and etc..
		if (tableEx != null) {
			tableCreateExpression = DDLExpressions.charsetAndCollate(tableCreateExpression, tableEx.getCollate());
			if (StringUtils.isNotEmpty(tableEx.getComment())) {
				Expression<String> content = ConstantImpl.create(tableEx.getComment());
				if (configuration.has(SpecialFeature.INDEPENDENT_COMMENT_STATEMENT)) {
					createCommentStatement(content, null);
				} else {
					tableCreateExpression = DDLExpressions.simple(DDLOps.COMMENT_ON_TABLE, tableCreateExpression,
							content);
				}
			}

		}
		meta.addExpression(tableCreateExpression);
		// 处理建表时的Partition定义
		if (processPartition) {
			PartitionBy partition=tableEx.getPartitionBy();
			PartitionMethod method = partition.getMethod();
			if (template.supports(PartitionDefineOps.PARTITION_BY) && template.supports(method)) {
				meta.addExpression(getPartitionByExpression(tableEx, tableEx.getPartitionBy(), 
						configuration.has(SpecialFeature.PARTITION_KEY_MUST_IN_PRIMARY) ,ignoreKeysOnPartitionedTable));
			}else {
				log.warn("Current database do not support create a [PARTITION BY {}] table.", method);
			}
		}
	}

	private boolean isPartitionedTable(RelationalPathEx<?> tableEx) {
		SQLTemplatesEx template = configuration.getTemplates();
		if (tableEx != null && tableEx.getPartitionBy() != null) {
			PartitionMethod method = tableEx.getPartitionBy().getMethod();
			return template.supports(PartitionDefineOps.PARTITION_BY) && template.supports(method); 
		}
		return false;
	}

	private DDLMetadata addIndependConstraintMeta(Constraint c) {
		ConstraintType type = c.getConstraintType();
		if (type == null) {
			type = ConstraintType.KEY;
		}
		if (configuration.getTemplates().supports(type.getIndependentCreateOps())) {
			DDLMetadata meta = new DDLMetadata(false, true);
			result.add(meta);
			Operator ops = type.getIndependentCreateOps();
			Expression<?> defExp;
			if (type.isColumnList()) {
				defExp = DDLExpressions.wrapList(c.getPaths());
			} else {
				defExp = c.getCheckClause();
			}
			meta.addExpression(new ConstraintOperation(generateConstraintName(c.getName(), table, true),
					(ops == null ? type : ops), table, defExp));
			return meta;
		} else {
			if(c.isAllowIgnore()) {
				log.warn("[CREATION IGNORED] The constraint {} is not supported on current database.", c);
				return null;	
			}else {
				throw Exceptions.unsupportedOperation("Current database do not support key:{}@{}", type, c, table.getSchemaAndTable());
			}
			
		}
	}

	public void serializePartitionBy(PartitionBy partitionBy, boolean check) {
		SQLTemplatesEx template = configuration.getTemplates();
		PartitionMethod method = partitionBy.getMethod();
		if (template.supports(AlterTablePartitionOps.ADD_PARTITIONING) && template.supports(method)) {
			check = check && configuration.has(SpecialFeature.PARTITION_KEY_MUST_IN_PRIMARY);
			boolean singleConstraints = configuration.has(SpecialFeature.NO_KEYS_ON_PARTITION_TABLE);
			DDLMetadata meta = new DDLMetadata(true, true);
			meta.setAction("ALTER TABLE ");
			result.add(meta);
			meta.addExpression(getPartitionByExpression((RelationalPathEx<?>)table, partitionBy,check,singleConstraints));	
		}
	}
	
	
	private Expression<?> getPartitionByExpression(RelationalPathEx<?> table, PartitionBy partitionBy, boolean check, boolean singleConstraints) {
		if (check) {
			checkPartitionFieldsInPrimaryKey(partitionBy);
		}
		Expression<?> result= DDLExpressions.simple(PartitionDefineOps.PARTITION_BY, partitionBy.define(configuration));
		if(configuration.has(SpecialFeature.INDEPENDENT_PARTITION_CREATION)) {
			for(Partition p:partitionBy.partitions()) {
				DDLMetadata meta = new DDLMetadata(true, true);
				PartitionAssigned st = (PartitionAssigned) partitionBy;
				meta.addExpression(st.defineOnePartition(p, configuration));
				this.result.add(meta);
				//Create index and constraints for this table/partition.
				if(singleConstraints) {
					addConstraintForPartition(p,table);
				}
			}
		}
		return result;
	}

	private void addConstraintForPartition(Partition p, RelationalPathEx<?> table) {
		TableRouting routing=TableRouting.suffix("_"+ p.name());
//		PrimaryKey<?> keys=table.getPrimaryKey(); 
//		{
//			if (keys != null && !keys.getLocalColumns().isEmpty()) {
//				Expression<?> columns = DDLExpressions.wrap(ExpressionUtils.list(Tuple.class, keys.getLocalColumns()));
//				tableDefExpressions.add(DDLExpressions.constraintDefinition(ConstraintType.PRIMARY_KEY, table,new SchemaAndTable(null, ""), columns));	
//			}
//		}
		// Add Constraint or index
		if (CollectionUtils.isNotEmpty(table.getConstraints())) {
			for (Constraint constraint : table.getConstraints()) {
				DDLMetadata meta=addIndependConstraintMeta(constraint);
				if(meta==null) {
					continue;
				}
				meta.setSubrouting(routing);
			}
		}
	}

	private void checkPartitionFieldsInPrimaryKey(PartitionBy pb) {
		if (table.getPrimaryKey() == null) {
			throw new IllegalArgumentException("Partition table must have a primary key.");
		}
		Set<Path<?>> missedPath = new HashSet<>(pb.exprPath());
		table.getPrimaryKey().getLocalColumns().forEach(missedPath::remove);
		if (!missedPath.isEmpty()) {
			throw new IllegalArgumentException(
					"A PRIMARY KEY must include all columns in the table's partitioning function. " + missedPath);
		}
	}

	private Expression<?> generateConstraintDefinition(Constraint constraint, RelationalPath<?> table) {
		SchemaAndTable name = generateConstraintName(constraint.getName(), table, true);
		Expression<?> defExp;
		if (constraint.getConstraintType().isColumnList()) {
			List<Expression<?>> exps = new ArrayList<>(constraint.getPaths());
			defExp = DDLExpressions.wrapList(exps);
		} else {
			defExp = constraint.getCheckClause();
		}
		return DDLExpressions.constraintDefinition(constraint.getConstraintType(), table, name, defExp);
	}

	/*
	 * 这个方法主要的目的是处理schema和constraintName的Overide(包括处理大小写)
	 */
	private SchemaAndTable generateConstraintName(String name, RelationalPath<?> tableEntity, boolean generate) {
		SchemaAndTable table = getSchemaAndTable(tableEntity);
		if (StringUtils.isEmpty(name)) {
			if (generate) {
				name = "idx_" + table.getTable() + "_" + com.github.xuse.querydsl.util.StringUtils.randomString();
			} else {
				name = "";
			}
		} else {
			name = name.replace("${table}", table.getTable());
		}
		SchemaAndTable constraintName = new SchemaAndTable(table.getSchema(), name);
		constraintName = configuration.getOverride(constraintName);
		return constraintName;
	}

	private SchemaAndTable getSchemaAndTable(RelationalPath<?> path) {
		return routing.getOverride(path.getSchemaAndTable(), configuration);
	}

	private Expression<?> generateColumnDefinition(Path<?> p, ColumnMetadataEx cx, boolean isPk) {
		SQLTemplatesEx template = configuration.getTemplates();
		Expression<?> dataType = generateDataTypeAndDefaultDefinition(cx, isPk);
		// append features
		List<Expression<?>> columnLevelConstraints = new ArrayList<>();
		if (cx != null && cx.getFeatures() != null) {
			for (ColumnFeature f : cx.getFeatures()) {
				Expression<?> value = f.get(template);
				if (value != null) {
					columnLevelConstraints.add(value);
				}
			}
		}
		Expression<?> columnSpec = DDLExpressions.columnSpec(p, dataType,
				DDLExpressions.defList(columnLevelConstraints));
		if (cx != null && StringUtils.isNotEmpty(cx.getComment())) {
			Expression<String> content = ConstantImpl.create(cx.getComment());
			if (configuration.has(SpecialFeature.INDEPENDENT_COMMENT_STATEMENT)) {
				// generate a independent statement of comment.
				createCommentStatement(content, p);
			} else {
				columnSpec = DDLExpressions.simple(DDLOps.COMMENT_ON_COLUMN, columnSpec, content);
			}
		}
		return columnSpec;
	}

	private Expression<?> generateDataTypeAndDefaultDefinition(ColumnMetadataEx cx, boolean isPk) {
		SQLTemplatesEx template = configuration.getTemplates();
		ColumnDef exp = template.getColumnDataType(cx.getJdbcType(), cx.getSize(), cx.getDigits());
		boolean unsigned = cx != null && cx.isUnsigned() && SQLTypeUtils.isNumeric(cx.getJdbcType());
		Expression<?> defaultValue = cx.getDefaultExpression();
		Expression<?> dataType = DDLExpressions.dataType(DDLOps.DATA_TYPE, exp.getDataType(), cx.isNullable() && !isPk, unsigned,
				defaultValue);
		return dataType;
	}

	@SuppressWarnings("unused")
	private static final Expression<?> CHANGE_LINE = Expressions.simpleTemplate(Object.class, "\n ");
	
	
	public void serializeAlterTable(CompareResult difference) {
		if (!configuration.has(SpecialFeature.MULTI_COLUMNS_IN_ALTER_TABLE)) {
			for (ColumnMapping cp : difference.getAddColumns()) {
				serializeAlterTable0(difference.ofAddSingleColumn(cp));
			}
			for (String cp : difference.getDropColumns()) {
				serializeAlterTable0(difference.ofDropSingleColumn(cp));
			}
			//每条SQL只能执行一个CHANGE
			for (ColumnModification cp : difference.getChangeColumns()) {
				//如果有列改名，就先改名
				ColumnModification isRenamed= cp.ofRenameOnly();
				if(isRenamed!=null) {
					serializeAlterTable0(difference.ofSingleChangeColumn(isRenamed));
				}
				if (cp.getChanges().size() == 1) {
					serializeAlterTable0(difference.ofSingleChangeColumn(cp));
				} else {
					for (ColumnChange cg : cp.getChanges()) {
						serializeAlterTable0(difference.ofSingleChangeColumn(cp.ofSingleChange(cg)));
					}
				}
			}
			for (Constraint c : difference.getDropConstraints()) {
				serializeAlterTable0(difference.ofDropSingleConstraint(c));
			}	
			for (Constraint c : difference.getAddConstraints()) {
				serializeAlterTable0(difference.ofAddSingleConstraint(c));
			}
			serializeAlterTable0(difference.ofOthers());
		}else {
			serializeAlterTable0(difference);
		}
	}
	
	private void serializeAlterTable0(CompareResult compareResults) {
		DDLMetadata meta = new DDLMetadata(true, true);
		meta.setAction("ALTER TABLE ");
		result.add(meta);
		List<Expression<?>> tableDefExpressions = new ArrayList<>();
		// drop columns
		for (String drop : compareResults.getDropColumns()) {
			tableDefExpressions.add(DDLExpressions.simple(AlterTableOps.DROP_COLUMN,
					Expressions.path(Object.class, table, drop), DDLExpressions.empty()));
		}
		// add columns
		PrimaryKey<?> keys = table.getPrimaryKey();
		for (ColumnMapping column : compareResults.getAddColumns()) {
			boolean isPk = keys == null ? false : keys.getLocalColumns().contains(column.getPath());
			Expression<?> columnSpec = generateColumnDefinition(column.getPath(), column, isPk);
			tableDefExpressions.add(DDLExpressions.simple(AlterTableOps.ADD_COLUMN, columnSpec));
		}
		// change columns
		for (ColumnModification change : compareResults.getChangeColumns()) {
			if (configuration.getTemplates().notSupports(AlterTableOps.CHANGE_COLUMN)) {
				if(change.hasRename()) {
					Expression<?> old=DDLExpressions.text(change.getOriginalName());
					if(configuration.getTemplates().supports(AlterTableOps.RENAME_COLUMN)) {
						tableDefExpressions.add(DDLExpressions.simple(AlterTableOps.RENAME_COLUMN, old,change.getPath()));
					}else if(configuration.getTemplates().supports(OtherStatement.RENAME_COLUMN)) {
						DDLMetadata renameStatement = new DDLMetadata(true, true);
						renameStatement.addExpression(DDLExpressions.simple(OtherStatement.RENAME_COLUMN, old, change.getPath(), change.getPath().getMetadata().getParent()));
						result.add(renameStatement);
					}else {
						throw new UnsupportedOperationException("Unable to rename the column due to the absence of an appropriate statement in SQLTemplates.");
					}
				}
				// 标准SQL，必须逐个修改列特性
				for (ColumnChange cg : change.getChanges()) {
					Operator op = cg.getType();
					if (cg.getType() == AlterColumnOps.SET_COMMENT) {
						if (configuration.has(SpecialFeature.INDEPENDENT_COMMENT_STATEMENT)) {
							createCommentStatement(cg.getTo(),  change.getPath());
							continue;
						} else {
							op = DDLOps.COMMENT_ON_COLUMN;
						}
					}else if(cg.getType()==AlterColumnOps.SET_DATATYPE) {
						//SET_DATATYPE表达式比较特殊，已经带有SET_DATATYPE操作了。
						Expression<?> alterClause = DDLExpressions.simple(AlterTableOps.ALTER_COLUMN, change.getPath(),cg.getTo());
						tableDefExpressions.add(alterClause);
						continue;
					}
					//构造列修改表达式
					Expression<?> alterClause = DDLExpressions.simple(AlterTableOps.ALTER_COLUMN, change.getPath(),
							DDLExpressions.simple(op, cg.getTo()));
					tableDefExpressions.add(alterClause);
				}
			} else {
				// MySQL或Oracle，可以重定义列的多项特性，此时对比的具体修改内容已不重要，只要重新定义新列规格即可。
				Expression<?> columnSpec = generateColumnDefinition(change.getPath(), change.getNewColumn(), false);
				Expression<?> oldPath = change.getPath();
				if(change.hasRename()) {
					oldPath = DDLExpressions.text(change.getOriginalName());
				}
				tableDefExpressions
						.add(DDLExpressions.simple(AlterTableOps.CHANGE_COLUMN, oldPath, columnSpec));
			}
		}

		boolean ignoreKeysOnPartitionedTable = false;
		if(table instanceof RelationalPathEx) {
			ignoreKeysOnPartitionedTable = ((RelationalPathEx<?>) table).getPartitionBy()!=null && configuration.has(SpecialFeature.NO_KEYS_ON_PARTITION_TABLE); 
		}
		//Drop constraints
		for (Constraint constraint : compareResults.getDropConstraints()) {
			ConstraintType type = constraint.getConstraintType();
			AlterTableConstraintOps ops = type.getDropOpsInAlterTable();
			if (ops == null || configuration.getTemplates().notSupports(ops)) {
				serialzeConstraintIndepentDrop(constraint);
			} else {
				SchemaAndTable constraintName = generateConstraintName(constraint.getName(), table, false);
				Expression<?> dropClause = DDLExpressions.simple(ops,
						Expressions.path(Object.class, constraintName.getTable()));
				tableDefExpressions.add(dropClause);
			}
		}
		//Add constraints
		if(!ignoreKeysOnPartitionedTable) {
			for (Constraint constraint : compareResults.getAddConstraints()) {
				ConstraintType type = constraint.getConstraintType();
				if (!configuration.getTemplates().supportCreateInTableDefinition(type)) {
					addIndependConstraintMeta(constraint);
				} else {
					Expression<?> expr = generateConstraintDefinition(constraint, table);
					Expression<?> alterClause = DDLExpressions.simple(AlterTableOps.ALTER_TABLE_ADD, expr);
					tableDefExpressions.add(alterClause);
				}
			}	
		}
		//Other table options
		for (Map.Entry<Operator, String> e : compareResults.getOtherChange().entrySet()) {
			Operator op = e.getKey();
			String value = e.getValue();
			if (op == DDLOps.COMMENT_ON_TABLE) {
				if (configuration.has(SpecialFeature.INDEPENDENT_COMMENT_STATEMENT)) {
					createCommentStatement(DDLExpressions.createConstant(value), null);
				} else {
					tableDefExpressions.add(DDLExpressions.simple(DDLOps.COMMENT_ON_COLUMN, DDLExpressions.empty(),
							ConstantImpl.create(value)));
				}
			} else if (op == DDLOps.COLLATE) {
				tableDefExpressions
						.add(DDLExpressions.simple(DDLOps.COLLATE, DDLExpressions.empty(), DDLExpressions.text(value)));
			}
		}
		if (tableDefExpressions.isEmpty()) {
			meta.clear();
		}else {
			meta.addExpression(DDLExpressions.tableDefinitionList(tableDefExpressions, false));
		}
	}

	private void createCommentStatement(Expression<?> content, Path<?> columnPath) {
		DDLMetadata comment = new DDLMetadata(true, false);
		if(columnPath==null) {
			comment.addExpression(DDLExpressions.simple(DDLOps.COMMENT_ON_TABLE, table, content));
		}else {
			comment.addExpression(DDLExpressions.simple(DDLOps.COMMENT_ON_COLUMN, columnPath, content));
		}
		result.add(comment);
	}

	public class DDLMetadata {
		private final boolean useLiterals;

		private final boolean skipParent;

		private String action;

		private final List<Expression<?>> expressions = new ArrayList<>();
		
		private TableRouting subrouting;

		public void clear() {
			action = null;
			expressions.clear();
		}

		public boolean isEmpty() {
			return action == null && expressions.isEmpty();
		}

		public DDLMetadata(boolean useLiterals, boolean skipParent) {
			this.useLiterals = useLiterals;
			this.skipParent = skipParent;
		}

		public DDLMetadata setAction(String action) {
			this.action = action;
			return this;
		}

		public void setSubrouting(TableRouting subrouting) {
			this.subrouting = subrouting;
		}

		public void addExpression(Expression<?> expression) {
			if (expression != null) {
				this.expressions.add(expression);
			}
		}

		public String toSQL() {
			if(isEmpty()) {
				return null;
			}
			SQLSerializerAlter serializer = new SQLSerializerAlter(DDLMetadataBuilder.this.configuration, true,
					useLiterals, skipParent);
			serializer.setRouting(WrapdRoutingStrategy.wrap(routing,subrouting));
			serializer.serializeAction(action, DDLMetadataBuilder.this.table, expressions);
			return serializer.toString();
		}

		@Override
		public String toString() {
			return isEmpty() ? "(empty)" : action + " - " + expressions;
		}
	}

	public String getSql() {
		if(this.result.size()>1) {
			throw Exceptions.illegalState("There's more sqls in this action. {}",result.size());
		}
		if(this.result.isEmpty()) {
			return null;
		}
		return this.result.get(0).toSQL();
	}
	
	public List<String> getSqls() {
		List<String> sqls = new ArrayList<>();
		for (DDLMetadata meta : this.result) {
			String sql = meta.toSQL();
			if (sql != null) {
				sqls.add(sql);
			}
		}
		return sqls;
	}
}
