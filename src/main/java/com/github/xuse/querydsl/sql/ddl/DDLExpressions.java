package com.github.xuse.querydsl.sql.ddl;

import java.util.List;

import com.github.xuse.querydsl.sql.dbmeta.Collate;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

/**
 * Expression helpers for DDL generation.
 */
public class DDLExpressions {

	private static final Expression<?> NOT_NULL = Expressions.simpleTemplate(Object.class, "NOT NULL");

	private static final Expression<?> CASCADE = Expressions.simpleTemplate(Object.class, "CASCADE");

	private static final Expression<?> EMPTY = Expressions.simpleTemplate(Object.class, "");

	@SuppressWarnings("rawtypes")
	public static final Expression[] ZERO_LENGTH_EXPRESION = new Expression[0];

	public static <T> Expression<T> wrap(Expression<T> exp) {
		return Expressions.simpleOperation(exp.getType(), Ops.WRAPPED, exp);
	}

	public static Expression<?> empty() {
		return EMPTY;
	}

	/**
	 * Generate expression AST like {@code  (exp1, exp2, exp3, ...)}
	 * @param paths paths
	 * @return expression;
	 */
	public static Expression<?> wrapList(Expression<?>... paths) {
		return wrap(Expressions.list(paths));
	}

	/**
	 * Generate expression AST like {@code  (exp1, exp2, exp3, ...)}
	 * @param paths paths
	 * @return expression;
	 */
	public static Expression<?> wrapList(List<? extends Expression<?>> paths) {
		int size = paths == null ? 0 : paths.size();
		if (size == 0) {
			return EMPTY;
		}
		return wrap(Expressions.list(paths.toArray(new Expression[size])));
	}

	public static Expression<?> nullAblity(boolean isNullable) {
		return isNullable ? simple(DDLOps.COLUMN_ALLOW_NULL) : NOT_NULL;
	}

	public static Expression<?> withCascade(Expression<?> path) {
		return simple(DDLOps.DEF_LIST, path, CASCADE);
	}

	public static Expression<?> withUnsigned(Expression<?> path) {
		return simple(DDLOps.UNSIGNED, path);
	}

	public static Expression<?> columnSpec(Path<?> column, Expression<?> dataType, Expression<?> columnConstraints) {
		return simple(DDLOps.COLUMN_SPEC, column, dataType, columnConstraints);
	}

	public static Expression<?> dataType(String dataType, boolean isNullable, boolean unsigned, Expression<?> defaultValue) {
		Expression<?> datatype = Expressions.template(Object.class, dataType);
		if (unsigned) {
			datatype = withUnsigned(datatype);
		}
		Expression<?> nullablity = nullAblity(isNullable);
		Expression<?> defaultExp = defaultValue == null ? EMPTY : simple(DDLOps.DEFAULT, defaultValue);
		return simple(DDLOps.DATA_TYPE, datatype, nullablity, defaultExp);
	}

	public static Expression<?> defList(List<Expression<?>> exprs) {
		if (exprs == null || exprs.isEmpty()) {
			return EMPTY;
		}
		Expression<?> rv = exprs.get(0);
		for (int i = 1; i < exprs.size(); i++) {
			rv = simple(DDLOps.DEF_LIST, rv, exprs.get(i));
		}
		return rv;
	}

	public static Expression<?> tableDefinitionList(List<Expression<?>> exprs, boolean wrap) {
		Expression<?> rv = exprs.get(0);
		for (int i = 1; i < exprs.size(); i++) {
			rv = simple(DDLOps.TABLE_DEFINITIONS, rv, exprs.get(i));
		}
		if (wrap) {
			rv = wrap(rv);
		}
		return rv;
	}

	/**
	 * create a AST for constraint/
	 * @param type {@link ConstraintType}
	 * @param table table
	 * @param constraintName constraintName
	 * @param definition definition
	 * @return expression
	 */
	public static Expression<?> constraintDefinition(ConstraintType type, RelationalPath<?> table, SchemaAndTable constraintName, Expression<?> definition) {
		String name = constraintName == null ? null : constraintName.getTable();
		SimplePath<?> thisPath = Expressions.path(Object.class, table, name);
		return Expressions.simpleOperation(Object.class, type, table, thisPath, definition);
	}

	public static Expression<?> charsetAndCollate(Expression<?> t, Collate collate) {
		if (collate == null) {
			return t;
		}
		t = simple(DDLOps.CHARSET, t, text(collate.charset));
		t = simple(DDLOps.COLLATE, t, text(collate.name()));
		return t;
	}

//	public static Expression<?> comment(Expression<?> t, String comment, boolean isColumn) {
//		DDLOps ops = isColumn ? DDLOps.COMMENT_ON_COLUMN : DDLOps.COMMENT_ON_TABLE;
//		if (StringUtils.isNotEmpty(comment)) {
//			return simple(ops, t, ConstantImpl.create(comment));
//		}
//		return t;
//	}

	public static Expression<?> simple(Operator op, Expression<?>... expressions) {
		return Expressions.simpleOperation(Void.class, op, expressions);
	}

	public static Expression<?> text(String str) {
		return Expressions.template(Object.class, str);
	}

	public static Expression<Boolean> wrapCheckExpression(String str) {
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		return Expressions.template(Boolean.class, str);
	}
}
