package com.querydsl.core.types.dsl;

import java.util.List;

import com.github.xuse.querydsl.util.Assert;
import com.querydsl.core.types.ConstraintType;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Visitor;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

/**
 * 约束/索引创建
 * 
 * Operator= 索引类型
 * Args{0} 上级Path
 * Args{1} 约束/索引Path(用来提供名称)
 * Args{2} 内部字段或CHECK表达式。如果是外键就是引用关系（待扩展）
 */
public class ConstraintOperation implements Operation<Object>{
	private static final long serialVersionUID = 1L;
	private Operation<Object> op;

	public ConstraintOperation(SchemaAndTable name, Operator type,RelationalPath<?> table,Expression<?> definition) {
		Assert.notNull(name);
		SimplePath<?> constraintPath=Expressions.path(Object.class,null, name.getTable());
		op=ExpressionUtils.operation(Void.class, type, table, constraintPath,definition);
	}

	@Override
	public <R, C> R accept(Visitor<R, C> v, C context) {
		return op.accept(v, context);
	}

	@Override
	public Class<? extends Object> getType() {
		return Object.class;
	}

	@Override
	public Expression<?> getArg(int index) {
		return op.getArg(index);
	}

	@Override
	public List<Expression<?>> getArgs() {
		return op.getArgs();
	}

	@Override
	public ConstraintType getOperator() {
		return (ConstraintType)op.getOperator();
	}
}
