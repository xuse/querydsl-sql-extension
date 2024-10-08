package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.lambda.LambdaColumnBase;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.QBeans;
import com.querydsl.sql.RelationalPath;

/**
 * 提供一个 字段不重复（可被多个Bean重复使用），字段可自由定义，并且表字段顺序不连续情况下的多Bean拼装器。
 * 
 * @see QBeans 官方版本基于查询表所有字段，并且字段在SQL中连续的场景。
 * @author Joey
 */
public class QAliasBeansDiscontinuous extends DiscontinuousFieldBeans<AliasMapBeans> {
	public QAliasBeansDiscontinuous(List<Expression<?>> expressions,
			Map<RelationalPath<?>, QValuesBuildBean> beans) {
		super(AliasMapBeans.class, expressions, beans);
	}

	/**
	 * generate a QBeans2 builder.
	 * 
	 * @return Builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private final Map<Expression<?>, Integer> map = new HashMap<>();

		private final List<Expression<?>> exprs = new ArrayList<>();

		private final Map<RelationalPath<?>, QValuesBuildBean> qBeans = new HashMap<>();

		public void addBean(RelationalPath<?> type, List<? extends Expression<?>> expressions) {
			addBean(type, expressions.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
		}

		public void addBean(RelationalPath<?> type, Expression<?>... expressions) {
			int len = expressions.length;
			int[] indexes = new int[len];
			QBeanEx<?> qBean = ProjectionsAlter.bean(type, expressions);
			for (int i = 0; i < len; i++) {
				Expression<?> expr = expressions[i];
				if (expr instanceof LambdaColumnBase) {
					expr = PathCache.getPath((LambdaColumnBase<?, ?>) expr);
				}
				indexes[i] = map.computeIfAbsent(expr, this::addExpr);
			}
			qBeans.put(type, new QValuesBuildBean(indexes, qBean));
		}

		private int addExpr(Expression<?> expr) {
			int size = exprs.size();
			exprs.add(expr);
			return size;
		}

		public QAliasBeansDiscontinuous build() {
			return new QAliasBeansDiscontinuous(exprs, qBeans);
		}
	}


	@Override
	public AliasMapBeans newInstance(Object... args) {
		Map<String, Object> beans = new HashMap<String, Object>();
		for (Map.Entry<RelationalPath<?>, QValuesBuildBean> entry : qBeans.entrySet()) {
			RelationalPath<?> path = entry.getKey();
			QValuesBuildBean qBean = entry.getValue();
			beans.put(path.getMetadata().getName(), qBean.newInstance(args));
		}
		return new AliasMapBeans(beans);
	}
}