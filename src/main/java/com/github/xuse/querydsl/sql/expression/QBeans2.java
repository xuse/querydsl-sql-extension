package com.github.xuse.querydsl.sql.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.xuse.querydsl.lambda.LambdaColumnBase;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.sql.ddl.DDLExpressions;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Visitor;
import com.querydsl.sql.Beans;
import com.querydsl.sql.QBeans;
import com.querydsl.sql.RelationalPath;

import lombok.AllArgsConstructor;

/**
 * 提供一个 字段不重复（可被多个Bean重复使用），字段可自由定义，并且表字段顺序不连续情况下的多Bean拼装器。
 * @see QBeans 官方版本基于查询表所有字段，并且字段在SQL中连续的场景。
 * @author Joey
 */
public class QBeans2 extends FactoryExpressionBase<Beans> {

    private static final long serialVersionUID = -4411839816134215923L;

    private final Map<RelationalPath<?>, QValuesBuildBean> qBeans;

    private final List<Expression<?>> expressions;

    @AllArgsConstructor
	static class QValuesBuildBean {
    	final int[] indexes;
    	final QBeanEx<?> qbean;
		public Object newInstance(Object... args) {
			int[] indexes = this.indexes;
			int length = indexes.length;
			Object[] values = new Object[length];
			for (int i = 0; i < length; i++) {
				values[i] = args[indexes[i]];
			}
			return qbean.newInstance(values);
		}
    }
    
	public QBeans2(List<Expression<?>> expressions, Map<RelationalPath<?>, QValuesBuildBean> beans) {
	      super(Beans.class);
		this.expressions = expressions;
		this.qBeans = beans;
	}
    
	/**
	 * generate a QBeans2 builder.
	 * @return Builder
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder{
		private Map<Expression<?>,Integer> map=new HashMap<>();
		
		private List<Expression<?>> exprs = new ArrayList<>();
		
		private Map<RelationalPath<?>, QValuesBuildBean> qBeans = new HashMap<>();
		
		public void addBean(RelationalPath<?> type, List<? extends Expression<?>> expressions) {
			addBean(type, expressions.toArray(DDLExpressions.ZERO_LENGTH_EXPRESION));
		}
		
		public void addBean(RelationalPath<?> type, Expression<?>...expressions) {
			int len=expressions.length;
			int[] indexes=new int[len];
			QBeanEx<?> qBean=ProjectionsAlter.bean(type, expressions);
			for(int i=0;i<len;i++) {
				Expression<?> expr=expressions[i];
				if(expr instanceof LambdaColumnBase) {
					expr = PathCache.getPath((LambdaColumnBase<?,?>)expr);
				}
				indexes[i] = map.computeIfAbsent(expr, this::addExpr);
			}
			qBeans.put(type, new QValuesBuildBean(indexes, qBean));
		}
		
		private int addExpr(Expression<?> expr) {
			int size=exprs.size();
			exprs.add(expr);
			return size;
		}
		
		public QBeans2 build() {
			return new QBeans2(exprs, qBeans);
		}
	}

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(this, context);
    }

    @Override
    public List<Expression<?>> getArgs() {
        return expressions;
    }

    @Override
    public Beans newInstance(Object... args) {
        Map<RelationalPath<?>, Object> beans = new HashMap<RelationalPath<?>, Object>();
        for (Map.Entry<RelationalPath<?>, QValuesBuildBean> entry : qBeans.entrySet()) {
            RelationalPath<?> path = entry.getKey();
            QValuesBuildBean qBean = entry.getValue();
            beans.put(path, qBean.newInstance(args));
        }
        return new Beans(beans);
    }
}