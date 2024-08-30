package com.querydsl.sql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.util.Assert;
import com.querydsl.core.JoinExpression;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.types.Null;

/**
 * 扩展了官方的SQLSerializer的一个行为，为了访问包私有属性无法移动到其他包。
 * <hr>
 * 官方SQLSerializer针对多值的常量Collection，会转换为 (?,?,?)形式。 但是在实现一些数据库的变长函数时，如下所示——
 * 
 * MYSQL的
 * <ul>
 * <li>JSON_CONTAINS_PATH(json_doc, one_or_all, path[, path] ...)</li>
 * <li>Oracle的TRANSLATE(text,char1, char2 ....)</li>
 * </ul>
 * 此时，两侧强制加上的括号导致上述函数表达式无法正常生成。
 * 为此，扩展SQL序列化实现，当包装的常量为Object[]时，展开为不带括号的逗号分隔表达式形式。
 *
 * @author Joey
 */
public class SQLSerializerAlter extends SQLSerializer {

	private final ConfigurationEx configurationEx;

	private RoutingStrategy routing = RoutingStrategy.DEFAULT;

	public SQLSerializerAlter(ConfigurationEx conf, boolean dml) {
		super(conf.get(), dml);
		this.configurationEx = conf;
		this.configurationEx.getTemplates();
	}

	public SQLSerializerAlter(ConfigurationEx conf, boolean dml, boolean useLiterial, boolean skipParent) {
		super(conf.get(), dml);
		this.configurationEx = conf;
		this.configurationEx.getTemplates();
		this.useLiterals = useLiterial;
		this.skipParent = skipParent;
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
			Path<?> lastPath = constantPaths.peekLast();
			int size = ((Collection) constant).size() - 1;
			for (int i = 0; i < size; i++) {
				constantPaths.add(lastPath);
			}
		} else {
			if (stage == Stage.SELECT && constant != Null.DEFAULT
					&& configurationEx.getTemplates().isWrapSelectParameters()) {
				String typeName = configuration.getTypeNameForCast(constant.getClass());
				Expression type = Expressions.constant(typeName);
				super.visitOperation(constant.getClass(), SQLOps.CAST, Arrays.<Expression<?>>asList(Q, type));
			} else {
				serializeConstant(constants.size() + 1, null);
			}
			constants.add(constant);
			// 容错功能有点问题
			if (constantPaths.size() < constants.size()) {
				constantPaths.add(null);
			}
		}
	}

	// 解决schema是否输出的问题。目前的schema输出控制不够灵活，取决于sql templates中的配置，缺少表维度的控制手段
	@Override
	public Void visit(Path<?> path, Void context) {
		if (dml) {
			if (path instanceof RelationalPath<?>) {
				RelationalPath<?> entity = (RelationalPath<?>) path;
				SchemaAndTable schemaAndTable = getSchemaAndTable(entity);
				boolean precededByDot;
				String schema = schemaAndTable.getSchema();
				// 下一句和原框架不同，其他均相同.
				if (isPrintSchema(entity, schema)) {//dmlWithSchema &&
					appendSchemaName(schema);
					append(".");
					precededByDot = true;
				} else {
					precededByDot = false;
				}
				appendTableName(schemaAndTable.getTable(), precededByDot);
				return null;
			} else if (skipParent) {
				/*
				 * 2024-07-25 remove logic 'entity.equals(path.getMetadata().getParent()) &&'.
				 * the check from original code may be not necessary.
				 */
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

	private boolean isPrintSchema(RelationalPath<?> path, String schema) {
		return (templates.isPrintSchema() || configurationEx.isPrintSchema(path)) && schema != null
				&& schema.length() > 0;
	}

	protected SchemaAndTable getSchemaAndTable(RelationalPath<?> path) {
		return routing.getOverride(path.getSchemaAndTable(), configurationEx);
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


	public void serializeForInsert(QueryMetadata metadata, RelationalPath<?> entity, List<Path<?>> columns,
			List<Expression<?>> values, SubQueryExpression<?> subQuery) {
		super.serializeForInsert(metadata, entity, columns, values, subQuery);
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
		for (Object s : spec) {
			if (s instanceof String) {
				append((String) s);
			} else if (s instanceof Expression<?>) {
				((Expression<?>) s).accept(this, null);
			}
		}
	}
	
	public final SQLSerializerAlter handleValueList(List<? extends Expression<?>> expressions, 
			List<Path<?>> columns, List<Path<?>> expectedColumns) {
		String sep=COMMA;
		if(columns.equals(expectedColumns)) {
			
		}
		if (!expressions.isEmpty()) {
			handle(expressions.get(0));
			for (int i = 1; i < expressions.size(); i++) {
				append(sep);
				handle(expressions.get(i));
			}
		}
		return this;
		

//		public void handleBulk(List<Path<?>> columns, List<Object> prepareValueExpr) {
//			Assert.equals(columns.size(), prepareValueExpr.size());
//			int len = columns.size();
//			Path<?> path = columns.get(0);
//			Object value = prepareValueExpr.get(0);
//			if (value instanceof Expression<?>) {
//				handle(value);
//			} else if (value != null) {
//				append("?");
//				constants.add(value);
//				this.constantPaths.add(path);
//			} else {
//				append("?");
//				constants.add(Null.DEFAULT);
//				this.constantPaths.add(path);
//			}
//			for (int i = 1; i < len; i++) {
//				append(COMMA);
//				path = columns.get(i);
//				value = prepareValueExpr.get(i);
//				if (value instanceof Expression<?>) {
//					handle(value);
//				} else if (value != null) {
//					append("?");
//					constants.add(value);
//					this.constantPaths.add(path);
//				} else {
//					append("?");
//					constants.add(Null.DEFAULT);
//					this.constantPaths.add(path);
//				}
//			}
//		}
	}

	public void serializeAction(String action, RelationalPath<?> path, List<Expression<?>> exprs) {
		this.entity = path;
		if (action != null) {
			append(action);
			this.visit(path, null);
			append(" ");
		}
		Iterator<Expression<?>> iter = exprs.iterator();
		if (iter.hasNext()) {
			iter.next().accept(this, null);
		}
		while (iter.hasNext()) {
			append(" ");
			iter.next().accept(this, null);
		}
	}

	protected void serializeForInsert(QueryMetadata metadata, RelationalPath<?> entity, List<SQLInsertBatch> batches) {
		SQLInsertBatch batch=batches.get(0);
		List<Path<?>> sqlColumns = batch.getColumns();
		serializeForInsert(metadata, entity, sqlColumns, batch.getValues(), null);
		for (int i = 1; i < batches.size(); i++) {
			batch = batches.get(i);
			append(COMMA);
			append("(");
			handleValueList(batch.getValues(), batch.getColumns(),sqlColumns);
			append(")");
		}
	}


	public RoutingStrategy getRouting() {
		return routing;
	}

	public void setRouting(RoutingStrategy routing) {
		if (routing != null) {
			this.routing = routing;
		}
	}
}
