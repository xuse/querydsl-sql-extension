package com.github.xuse.querydsl.sql.partitions;

import java.util.ArrayList;
import java.util.List;

import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;

public interface PartitionBy {
	PartitionMethod getMethod();
	
	Expression<?> define(ConfigurationEx configurationEx);
	
	List<Partition> partitions();
	
	Expression<?> getExpr();
	
	default int count() {
		return partitions().size();
	};

	default List<Path<?>> exprPath() {
		List<Path<?>> result=new ArrayList<>();
		getExpr().accept(new Visitor<Void, Void>() {
			@Override
			public Void visit(Constant<?> expr,  Void context) {
				return null;
			}
			@Override
			public Void visit(FactoryExpression<?> expr, Void context) {
				return null;
			}
			@Override
			public Void visit(Operation<?> expr, Void context) {
				return null;
			}
			@Override
			public Void visit(ParamExpression<?> expr, Void context) {
				return null;
			}
			@Override
			public Void visit(Path<?> expr, Void context) {
				result.add(expr);
				return null;
			}
			@Override
			public Void visit(SubQueryExpression<?> expr, Void context) {
				return null;
			}
			@Override
			public Void visit(TemplateExpression<?> expr, Void context) {
				return null;
			}
		}, null);
		return result;
	}
	
}
