package com.github.xuse.querydsl.sql.ddl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dialect.SpecialFeature;
import com.querydsl.core.types.ConstraintType;
import com.querydsl.core.types.DDLOps.AlterTableConstraintOps;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.DDLExpressions;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

public class DropConstraintQuery extends AbstractDDLClause<DropConstraintQuery> {

	public DropConstraintQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		super(connection, configuration, path);
	}

	private final LinkedHashSet<Constraint> toDrop = new LinkedHashSet<>();

	private List<Constraint> current;

	private List<Constraint> getCurrent() {
		if (current != null) {
			return current;
		}
		SchemaAndTable acutalTable = connection.asInCurrentSchema(table.getSchemaAndTable());
		if (routing != null) {
			acutalTable = routing.getOverride(acutalTable, configuration);
		}
		List<Constraint> list = connection.getIndexes(acutalTable, MetadataQuerySupport.INDEX_POILCY_MERGE_CONSTRAINTS);
		if (list == null) {
			list = Collections.emptyList();
		}
		return current = list;
	}

	public DropConstraintQuery drop(String name) {
		for (Constraint c : getCurrent()) {
			if (StringUtils.equalsIgnoreCase(c.getName(), name)) {
				toDrop.add(c);
			}
		}
		return this;
	}

	/**
	 * 指定删除表中除了主键以外的所有约束。
	 * 
	 * @implSpec 不包含主键
	 * 
	 * @return this
	 */
	public DropConstraintQuery dropAllConstraints() {
		for (Constraint c : getCurrent()) {
			ConstraintType type = c.getConstraintType();
			if (type == ConstraintType.PRIMARY_KEY) {
				continue;
			}
			if (!type.isIndex() && !type.isIgnored()) {
				toDrop.add(c);
			}
		}
		return this;
	}
	
	/**
	 * 指定删除表中的索引。
	 * @implSpec 不包含主键
	 * @return this
	 */
	public DropConstraintQuery dropAllIndecies() {
		for (Constraint c : getCurrent()) {
			ConstraintType type = c.getConstraintType();
			if (type.isIndex()) {
				toDrop.add(c);
			}
		}
		return this;
	}
	
	/**
	 * 删除主键。不会修改主键上的自增特性。对于某些将自增与主键绑定的RDBMS将会删除失败。需要同时修改自增特性并删除主键。
	 * @return this
	 */
	public DropConstraintQuery dropParimarKey() {
		for (Constraint c : getCurrent()) {
			ConstraintType type = c.getConstraintType();
			if (type == ConstraintType.PRIMARY_KEY) {
				toDrop.add(c);
			}
		}
		return this;
	}
	
	/**
	 * 删除所有索引和约束（不含主键）
	 * @return
	 */
	public DropConstraintQuery dropAll() {
		for (Constraint c : getCurrent()) {
			ConstraintType type = c.getConstraintType();
			if (type == ConstraintType.PRIMARY_KEY) {
				continue;
			}
			if (!type.isIgnored()) {
				toDrop.add(c);
			}
		}
		return this;
	}

	@Override
	protected List<String> generateSQLs() {
		List<String> sqls = new ArrayList<>();
		List<Constraint> indendentOps = new ArrayList<>();
		tryAlterTable(sqls, indendentOps);
		if (indendentOps != null) {
			for (Constraint c : indendentOps) {
				sqls.add(dropStatement(c));
			}
		}
		return sqls;
	}

	private String dropStatement(Constraint c) {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.setRouting(routing);
		serializer.serialzeConstraintIndepentDrop(this.table, c);
		return serializer.toString();
	}

	private void tryAlterTable(List<String> sqls, List<Constraint> indendentOps) {
		// Process Alter table process.
		List<Expression<?>> exp = new ArrayList<>();
		for (Constraint constraint : toDrop) {
			AlterTableConstraintOps ops = constraint.getConstraintType().getDropOpsInAlterTable();
			if (configuration.getTemplates().supports(ops)) {
				exp.add(DDLExpressions.simple(ops, DDLExpressions.text(constraint.getName())));
			} else {
				indendentOps.add(constraint);
			}
		}
		if (exp.isEmpty()) {
			return;
		}
		
		if (configuration.has(SpecialFeature.ONE_COLUMN_IN_SINGLE_DDL)) {
			for(Expression<?> one:exp) {
				SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
				serializer.setRouting(routing);
				serializer.serializeAction("ALTER TABLE ", table, " ",one);
				sqls.add(serializer.toString());	
			}
		}else {
			SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
			serializer.setRouting(routing);
			serializer.serializeAction("ALTER TABLE ", table, " ",
					Expressions.list(exp.toArray(new Expression<?>[exp.size()])));
			sqls.add(serializer.toString());	
		}
	}

	@Override
	protected String generateSQL() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean preExecute(MetadataQuerySupport metadata) {
		return true;
	}

	@Override
	protected int finished(List<String> sqls) {
		return this.toDrop.size();
	}

}
