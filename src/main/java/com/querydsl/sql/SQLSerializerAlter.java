package com.querydsl.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.column.ColumnFeature;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.column.ColumnMetadataEx;
import com.github.xuse.querydsl.sql.column.ColumnMetadataExImpl;
import com.github.xuse.querydsl.sql.dbmeta.ColumnDef;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.ddl.ColumnChange;
import com.github.xuse.querydsl.sql.ddl.ColumnModification;
import com.github.xuse.querydsl.sql.ddl.CompareResult;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.querydsl.core.JoinExpression;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.ConstraintType;
import com.querydsl.core.types.DDLOps;
import com.querydsl.core.types.DDLOps.AlterTableConstraintOps;
import com.querydsl.core.types.DDLOps.AlterTableOps;
import com.querydsl.core.types.DDLOps.DropStatement;
import com.querydsl.core.types.DDLOps.PartitionDefineOps;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.core.types.dsl.ConstraintOperation;
import com.querydsl.core.types.dsl.DDLExpressions;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.types.Null;

/**
 * 扩展了官方的SQLSerializer的一个行为。
 * <hr>
 * 官方SQLSerializer针对多值的常量Collection，会转换为 (?,?,?)形式。 但是在实现一些数据库的变长函数时，如下所示——
 * <code>
 * MYSQL的
 * <li>JSON_CONTAINS_PATH(json_doc, one_or_all, path[, path] ...)</li>
 * <li>Oracle的TRANSLATE(text,char1, char2 ....)</li>
 * </code> 此时，两侧强制加上的括号导致上述函数表达式无法正常生成。
 * <p />
 * 为此，扩展SQL序列化实现，当包装的常量为Object[]时，展开为不带括号的逗号分隔表达式形式。
 * 
 * @author Joey
 *
 */
public class SQLSerializerAlter extends SQLSerializer {
	private ConfigurationEx configurationEx;
	
	private RoutingStrategy routing = RoutingStrategy.DEFAULT;

	public SQLSerializerAlter(ConfigurationEx conf, boolean dml) {
		super(conf.get(), dml);
		this.configurationEx = conf;
		this.configurationEx.getTemplates();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void visitConstant(Object constant) {
		// 新增支持的常量表达形式，如果是对象数组，就转成不带小括号的多值。
		String leftBucket = "(";
		String rightBucket = ")";
		if (constant instanceof Object[]) {
			constant = Arrays.asList((Object[]) constant);
			leftBucket = rightBucket = "";
		}
		if (useLiterals) {
			if (constant instanceof Collection) {
				append(leftBucket);
				Iterator iter = ((Collection) constant).iterator();
				if (iter.hasNext()) {
					append(configuration.asLiteral(iter.next()));
					while (iter.hasNext()) {
						append(COMMA);
						append(configuration.asLiteral(iter.next()));
					}
				}
				append(rightBucket);
			} else {
				append(configuration.asLiteral(constant));
			}
		} else if (constant instanceof Collection) {
			append(leftBucket);
			Iterator iter = ((Collection) constant).iterator();
			if (iter.hasNext()) {
				serializeConstant(constants.size() + 1, null);
				constants.add(iter.next());
				// 官方代码。这个好像是一个修补BUG的逻辑，不知道具体什么时候会发生。我怀疑这是没用的代码。
				if (constantPaths.size() < constants.size()) {
					constantPaths.add(null);
				}
				while (iter.hasNext()) {
					append(COMMA);
					serializeConstant(constants.size() + 1, null);
					constants.add(iter.next());
				}
			}
			append(rightBucket);
			int size = ((Collection) constant).size() - 1;
			Path<?> lastPath = constantPaths.peekLast();
			for (int i = 0; i < size; i++) {
				constantPaths.add(lastPath);
			}
		} else {
			if (stage == Stage.SELECT && !Null.class.isInstance(constant)
					&& configurationEx.getTemplates().isWrapSelectParameters()) {
				String typeName = configuration.getTypeNameForCast(constant.getClass());
				Expression type = Expressions.constant(typeName);
				super.visitOperation(constant.getClass(), SQLOps.CAST, Arrays.<Expression<?>>asList(Q, type));
			} else {
				serializeConstant(constants.size() + 1, null);
			}
			constants.add(constant);
			if (constantPaths.size() < constants.size()) {
				constantPaths.add(null);
			}
		}
	}

	// 解决schema是否输出的问题。目前的schema输出控制不够灵活，取决于sql templates中的配置，缺少表维度的控制手段
	@Override
	public Void visit(Path<?> path, Void context) {
		if (dml) {
			if (path.equals(entity) && path instanceof RelationalPath<?>) {
				SchemaAndTable schemaAndTable = getSchemaAndTable((RelationalPath<?>) path);
				boolean precededByDot;
				String schema = schemaAndTable.getSchema();
				// 下一句和原框架不同，其他均相同.
				if (dmlWithSchema && isPrintSchema(path, schema)) {
					appendSchemaName(schema);
					append(".");
					precededByDot = true;
				} else {
					precededByDot = false;
				}
				appendTableName(schemaAndTable.getTable(), precededByDot);
				return null;
			} else if (entity.equals(path.getMetadata().getParent()) && skipParent) {
				appendAsColumnName(path, false);
				return null;
			}
		}
		final PathMetadata metadata = path.getMetadata();
		boolean precededByDot;
		if (metadata.getParent() != null && (!skipParent || dml)) {
			visit(metadata.getParent(), context);
			append(".");
			precededByDot = true;
		} else {
			precededByDot = false;
		}
		appendAsColumnName(path, precededByDot);
		return null;
	}

	private boolean isPrintSchema(Path<?> path, String schema) {
		return (templates.isPrintSchema() || configurationEx.isPrintSchema(path)) && schema != null && schema.length() > 0;
	}
	
    protected SchemaAndTable getSchemaAndTable(RelationalPath<?> path) {
        return routing.getOverride(path.getSchemaAndTable(),configurationEx);
    }

	protected void handleJoinTarget(JoinExpression je) {
		if (je.getTarget() instanceof RelationalPath && templates.isSupportsAlias()) {
			final RelationalPath<?> pe = (RelationalPath<?>) je.getTarget();
			if (pe.getMetadata().getParent() == null) {
				if (withAliases.contains(pe)) {
					appendTableName(pe.getMetadata().getName(), false);
					append(templates.getTableAlias());
				} else {
					SchemaAndTable schemaAndTable = getSchemaAndTable(pe);
					boolean precededByDot;
					String schema = schemaAndTable.getSchema();
					// 下一句和原框架不同，其他均相同。覆盖以支持表级别的schema携带。
					if (isPrintSchema(pe, schema)) {
						appendSchemaName(schema);
						append(".");
						precededByDot = true;
					} else {
						precededByDot = false;
					}
					appendTableName(schemaAndTable.getTable(), precededByDot);
					append(templates.getTableAlias());
				}
			}
		}
		inJoin = true;
		handle(je.getTarget());
		inJoin = false;
	}

	public void serializeAction(RelationalPath<?> entity, String... action) {
		this.entity = entity;
		for (String s : action) {
			if (s != null) {
				append(s);
			}
		}
		this.visit(entity, null);
	}
	
	public void serializeAction(String action, RelationalPath<?> entity, Object... spec) {
		this.entity = entity;
		append(action);
		this.visit(entity, null);
		for(Object s:spec) {
			if(s instanceof String) {
				append((String)s);		
			}else if(s instanceof Expression<?>){
				((Expression<?>) s).accept(this, null);
			}
		}
	}
	
	public void serializePath(Path<?> entity, String... action) {
		for (String s : action) {
			if (s != null) {
				append(s);
			}
		}
		this.visit(entity, null);
	}

	public List<Constraint> serializeTableCreate(RelationalPath<?> table, boolean processPartition) {
		this.useLiterals = true;
		this.skipParent = true;
		this.entity = table;
		SQLTemplatesEx template = configurationEx.getTemplates();
		serializeAction(table, configurationEx.getTemplates().getCreateTable());

		final RelationalPathEx<?> tableEx = (table instanceof RelationalPathEx) ? (RelationalPathEx<?>) table : null;
		List<Expression<?>> tableDefExpressions = new ArrayList<>();
		// Add columns
		for (Path<?> p : table.getColumns()) {
			ColumnMetadata c = table.getMetadata(p);
			ColumnMetadataEx cx = tableEx == null ? new ColumnMetadataExImpl(c) : tableEx.getColumnMetadata(p);
			tableDefExpressions.add(generateColumnDefinition(p, cx));
		}
		// Add Primary key.
		{
			PrimaryKey<?> keys = table.getPrimaryKey();
			if (keys != null && !keys.getLocalColumns().isEmpty()) {
				Expression<?> columns = DDLExpressions.wrap(ExpressionUtils.list(Tuple.class, keys.getLocalColumns()));
				tableDefExpressions.add(DDLExpressions.constraintDefinition(ConstraintType.PRIMARY_KEY, table, new SchemaAndTable(null, ""), columns));
			}
		}
		List<Constraint> independentConstraints = new ArrayList<>();
		// Add Constraint or index
		if (tableEx != null && CollectionUtils.isNotEmpty(tableEx.getConstraints())) {
			for (Constraint constraint : tableEx.getConstraints()) {
				if (template.supportCreateInTableDefinition(constraint.getConstraintType())) {
					tableDefExpressions.add(generateConstraintDefinition(constraint, table));
				} else {
					independentConstraints.add(constraint);
				}
			}
		}

		Expression<?> tableCreateExpression = DDLExpressions.tableDefinitionList(tableDefExpressions,true);
		if (tableEx != null) {
			tableCreateExpression = DDLExpressions.charsetAndCollate(tableCreateExpression, tableEx.getCollate());
			tableCreateExpression = DDLExpressions.comment(tableCreateExpression, tableEx.getComment());
		}
		//Generate SQL from the AST
		tableCreateExpression.accept(this, null);
		
		//处理建表时的Partition定义
		if (processPartition && tableEx != null && tableEx.getPartitionBy()!=null) {
			if(configurationEx.getTemplates().supports(PartitionDefineOps.PARTITION_BY)){
				serializePartitionBy(tableEx.getPartitionBy(), tableEx, true);
			}
		}
		return independentConstraints;
	}

	public void serializePartitionBy(PartitionBy partitionBy, RelationalPath<?> tableEx, boolean check) {
		skipParent = true;
		if(check) {
			checkPartitionFields(partitionBy, tableEx);
		}
		Expression<?> partitionExpression = partitionBy.generateExpression(configurationEx);
		append(" ");
		partitionExpression.accept(this, null);
	}

	private void checkPartitionFields(PartitionBy pb, RelationalPath<?> table) {
		if(table.getPrimaryKey()==null) {
			throw new IllegalArgumentException("Partition table must have a primary key.");
		}
		Set<Path<?>> missedPath = new HashSet<>(pb.exprPath());
		missedPath.removeAll(table.getPrimaryKey().getLocalColumns());
		if (!missedPath.isEmpty()) {
			throw new IllegalArgumentException("A PRIMARY KEY must include all columns in the table's partitioning function. " + missedPath);
			//log.warn("The path {} not in primary key",missedPath);
		}
	}

	// 创建独立的创建语句
	public void serialzeConstraintIndepentCreate(RelationalPath<?> table, Constraint c) {
		this.entity = table;
		this.skipParent = true;
		ConstraintType type = c.getConstraintType();
		if (type == null) {
			type = ConstraintType.KEY;
		}
		Operator ops=type.getIndependentCreateOps();
		Expression<?> defExp;
		if (type.isColumnList()) {
			defExp = DDLExpressions.wrapList(c.getPaths());
		} else {
			defExp = c.getCheckClause();
		}
		ConstraintOperation op = new ConstraintOperation(generateConstraintName(c.getName(), table, true),(ops==null? type:ops), entity,
				defExp);
		append("CREATE ");
		op.accept(this, null);
	}

	public void serialzeConstraintIndepentDrop(RelationalPath<?> table, Constraint c) {
		this.entity = table;
		this.skipParent = true;
		Assert.isNotEmpty(c.getName());
		SchemaAndTable name=generateConstraintName(c.getName(), table, false);
		Expression<?> dropExpr = DDLExpressions.simple(DropStatement.DROP_INDEX, Expressions.path(Object.class, name.getTable()));			
		dropExpr.accept(this, null);
	}
	
	/**
	 * 
	 * @param table
	 * @param cr
	 * @param resultContainer 返回的CompareResult中记录那些无法在Alter table语句中操作的索引和约束，需要用独立的语句进行删除或创建
	 * @return 有效操作数
	 */
	public int serializeAlterTable(RelationalPath<?> table, CompareResult cr, CompareResult resultContainer) {
		this.useLiterals = true;
		this.skipParent = true;
		this.entity = table;
		serializeAction(table, "ALTER TABLE ");
		append("\n  ");
		List<Expression<?>> tableDefExpressions = new ArrayList<>();
		//drop columns
		for(String drop:cr.getDropColumns()) {
			tableDefExpressions.add(DDLExpressions.simple(AlterTableOps.DROP_COLUMN, Expressions.path(Object.class, table, drop), DDLExpressions.empty()));
		}
		//add columns
		for(ColumnMapping column:cr.getAddColumns()) {
			Expression<?> columnSpec=generateColumnDefinition(column.getPath(),column);
			tableDefExpressions.add(DDLExpressions.simple(AlterTableOps.ADD_COLUMN, columnSpec));
		}
		//change columns
		for(ColumnModification change:cr.getChangeColumns()) {
			if(configurationEx.getTemplates().notSupports(AlterTableOps.CHANGE_COLUMN)) {
				for(ColumnChange cg:change.getChanges()) {
					Expression<?> alterClause=DDLExpressions.simple(AlterTableOps.ALTER_COLUMN, change.getPath(),DDLExpressions.simple(cg.getType(),cg.getTo()));
					tableDefExpressions.add(alterClause);
				}
			}else {
				Expression<?> columnSpec=generateColumnDefinition(change.getPath(),change.getNewColumn());
				tableDefExpressions.add(DDLExpressions.simple(AlterTableOps.CHANGE_COLUMN, change.getPath(), columnSpec));	
			}
		}
		
		
		List<Constraint> unsupportedDrop=new ArrayList<>();
		List<Constraint> unsupportedCreate=new ArrayList<>();
		//drop constraints
		for(Constraint constraint:cr.getDropConstraints()) {
			ConstraintType type=constraint.getConstraintType();
			AlterTableConstraintOps ops=type.getDropOpsInAlterTable();
			if(ops==null || configurationEx.getTemplates().notSupports(ops)) {
				//无法支持在AlterTable语句中修改
				unsupportedDrop.add(constraint);
				continue;
			}
			
			SchemaAndTable constraintName=generateConstraintName(constraint.getName(),table,false);
			Expression<?> dropClause = DDLExpressions.simple(ops, Expressions.path(Object.class, constraintName.getTable()));
			tableDefExpressions.add(dropClause);
		}
		//add constraints
		for(Constraint constraint:cr.getAddConstraints()) {
			ConstraintType type=constraint.getConstraintType();
			if(!configurationEx.getTemplates().supportCreateInTableDefinition(type)) {
				unsupportedCreate.add(constraint);
				continue;
			}
			Expression<?> expr = generateConstraintDefinition(constraint, table);
			Expression<?> alterClause = DDLExpressions.simple(AlterTableOps.ALTER_TABLE_ADD, expr);
			tableDefExpressions.add(alterClause);
		}
		resultContainer.addCreateConstraints(unsupportedCreate);
		resultContainer.addDropConstraints(unsupportedDrop);
		//Other table definitions
		for(Map.Entry<Operator, String> e:cr.getOtherChange().entrySet()) {
			Operator op=e.getKey();
			String value=e.getValue();
			if(op==DDLOps.COMMENT) {
				tableDefExpressions.add(DDLExpressions.simple(DDLOps.COMMENT, DDLExpressions.empty(),ConstantImpl.create(value)));	
			}else if(op==DDLOps.COLLATE) {
				tableDefExpressions.add(DDLExpressions.simple(DDLOps.COLLATE, DDLExpressions.empty(),DDLExpressions.text(value)));
			}
		}
		int effectiveClause=tableDefExpressions.size();
		if(effectiveClause>0) {
			Expression<?> tableCreateExpression = DDLExpressions.tableDefinitionList(tableDefExpressions, false);
			//Generate SQL from the AST
			tableCreateExpression.accept(this, null);			
		}
		return effectiveClause;
	}
	
	private Expression<?> generateConstraintDefinition(Constraint constraint, RelationalPath<?> table) {
		SchemaAndTable name = generateConstraintName(constraint.getName(), table,true);
		Expression<?> defExp;
		if (constraint.getConstraintType().isColumnList()) {
			List<Expression<?>> exps = new ArrayList<>();
			for (Path<?> p : constraint.getPaths()) {
				exps.add(p);
			}
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
		
		if(StringUtils.isEmpty(name)) {
			if(generate) {
				name = "idx_" + table.getTable() + "_" + com.github.xuse.querydsl.util.StringUtils.randomString();	
			}else {
				name = "";
			}
		}else {
			name = name.replace("${table}", table.getTable());
		}
		SchemaAndTable constraintName=new SchemaAndTable(table.getSchema(), name);
		constraintName = configurationEx.getOverride(constraintName);
		return constraintName;
	}

	private Expression<?> generateColumnDefinition(Path<?> p, ColumnMetadataEx cx) {
		SQLTemplatesEx template = configurationEx.getTemplates();

		Expression<?> dataType = generateDataTypeAndDefaultDefinition(p, cx);
		// append features
		List<Expression<?>> columnLevelConstraints = new ArrayList<>();
		if (cx != null && cx.getFeatures() != null && cx.getFeatures().length > 0) {
			for (ColumnFeature f : cx.getFeatures()) {
				Expression<?> value = f.get(template);
				if (value != null) {
					columnLevelConstraints.add(value);
				}
			}
		}
		Expression<?> columnSpec = DDLExpressions.columnSpec(p, dataType, DDLExpressions.defList(columnLevelConstraints));
		// append comment
		if (cx != null) {
			columnSpec = DDLExpressions.comment(columnSpec, cx.getComment());
		}
		return columnSpec;
	}

	private Expression<?> generateDataTypeAndDefaultDefinition(Path<?> p,ColumnMetadataEx cx) {
		SQLTemplatesEx template = configurationEx.getTemplates();
		ColumnDef exp = template.getColumnDataType(cx.getJdbcType(), cx.getSize(), cx.getDigits());
		boolean unsigned = cx != null && cx.isUnsigned() && SQLTypeUtils.isNumeric(cx.getJdbcType());
		Expression<?> defaultValue = cx.getDefaultExpression();
		Expression<?> dataType = DDLExpressions.dataType(exp.getDataType(), cx.isNullable(), unsigned, defaultValue);
		return dataType;
	}

	public RoutingStrategy getRouting() {
		return routing;
	}

	public void setRouting(RoutingStrategy routing) {
		if(routing!=null) {
			this.routing = routing;
		}
	}
}
