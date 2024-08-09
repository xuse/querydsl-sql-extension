package com.github.xuse.querydsl.sql.partitions;

import java.util.ArrayList;
import java.util.List;

import com.github.xuse.querydsl.annotation.partition.HashType;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HashPartitionBy implements PartitionBy{
	private final RelationalPathEx<?> table;
	
	private final String[] columns;
	
	private final HashType type;
	
	private final String exprText;
	
	/**
	 * finally the partition expression.
	 */
	private Expression<?> expr;
	
	/**
	 * The partition count.
	 */
	private int count;
	
	public HashPartitionBy(RelationalPathEx<?> table,String[] columns, String exprText,HashType type,int count) {
		this.table=table;
		this.columns=columns;
		this.exprText=exprText;
		
		this.type=type;
		this.count=count;
	}
	
	public HashPartitionBy(HashType type,Expression<?> expr,int count) {
		this.table = null;
		this.columns = StringUtils.EMPTY_STRING_ARRAY;
		this.exprText = "";
		
		this.type = type;
		this.count = count;
		this.expr = expr;
	}

	public Expression<?> getExpr() {
		if (expr == null) {
			expr = generateExpr();
		}
		return expr;
	}

	private Expression<?>  generateExpr() {
		boolean hasExpr = StringUtils.isNotBlank(exprText);
		boolean isColumns = columns != null && columns.length > 0;
		if (isColumns == hasExpr) {
			throw Exceptions.illegalArgument("A partition config must hava a expression or a column list. table=[{}]",
					table);
		}
		if(isColumns) {
			Assert.notNull(table);
			List<Path<?>> paths = new ArrayList<>();
			for (String name : columns) {
				Path<?> path = table.getColumn(name);
				if (path == null) {
					throw Exceptions.illegalArgument(
							"The partition config has error. field name = {} not found in class=[{}]", name,
							table.getType().getName());
				}
				paths.add(path);
			}
			return Expressions.list(paths.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
		}else {
			return DDLExpressions.text(exprText);
		}
	}

	public int count() {
		return count;
	}

	public Expression<?> define(ConfigurationEx configurationEx) {
		return DDLExpressions.simple(type.getMethod(), getExpr(), DDLExpressions.text(String.valueOf(count)));
	}

	public HashType type() {
		return type;
	}
}
