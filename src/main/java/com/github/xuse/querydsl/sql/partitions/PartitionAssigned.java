package com.github.xuse.querydsl.sql.partitions;

import java.util.ArrayList;
import java.util.List;
import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;

public abstract class PartitionAssigned implements PartitionBy {

	protected boolean isColumns;

	protected Expression<?> expr;

	// For generate expr only.
	private final RelationalPathEx<?> table;

	private final String[] columns;

	private final String exprText;

	/**
	 * By Annotation
	 * @param table table
	 * @param columns columns
	 * @param expr expr
	 */
	public PartitionAssigned(RelationalPathEx<?> table, String[] columns, String expr) {
		this.table = table;
		this.columns = columns;
		this.exprText = expr;
		check(expr, columns, table);
	}

	public PartitionAssigned(boolean isColumn, Expression<?> expr) {
		this.table = null;
		this.columns = StringUtils.EMPTY_STRING_ARRAY;
		this.exprText = "";
		this.expr = expr;
		this.isColumns = isColumn;
	}

	public abstract Expression<?> defineOnePartition(Partition p, ConfigurationEx configurationEx);

	protected void check(String exprText, String[] columns, RelationalPath<?> table) {
		boolean hasExpr = StringUtils.isNotBlank(exprText);
		boolean isColumns = columns != null && columns.length > 0;
		if (isColumns == hasExpr) {
			throw Exceptions.illegalArgument("A partition config must hava a expression or a column list. table=[{}]", table.getSchemaAndTable());
		}
		this.isColumns = isColumns;
	}

	public Expression<?> getExpr() {
		if (expr == null) {
			expr = generateExpr();
		}
		return expr;
	}

	private Expression<?> generateExpr() {
		if (isColumns) {
			List<Path<?>> paths = new ArrayList<>();
			for (String name : columns) {
				Path<?> path = table.getColumn(name);
				if (path == null) {
					throw Exceptions.illegalArgument("The partition config has error. field name = {} not found in class=[{}]", name, table.getType().getName());
				}
				paths.add(path);
			}
			return Expressions.list(paths.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
		} else {
			return DDLExpressions.text(exprText);
		}
	}
}
