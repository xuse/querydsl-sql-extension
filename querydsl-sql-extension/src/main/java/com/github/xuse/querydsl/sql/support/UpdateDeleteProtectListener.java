package com.github.xuse.querydsl.sql.support;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Constant;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.TemplateExpression;
import com.querydsl.core.types.Visitor;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBaseListener;
import com.querydsl.sql.dml.SQLUpdateBatch;


/**
 * <h2>English:</h2>
 * A listener that prevents accidental full-table updates or deletes.
 * This is recommended for general business code development to avoid catastrophic data loss.
 * <p>The following cases will be rejected:</p>
 * <ul>
 *   <li>WHERE clause is missing (null)</li>
 *   <li>WHERE clause contains only self-referencing equality conditions
 *       (e.g. {@code ID.eq(ID)}), which are always-true and equivalent to no condition</li>
 * </ul>
 * <h2>Chinese:</h2>
 * 为了防止开发者不小心遗漏where条件，造成全表记录被误写或者全表被删除的严重后果，一般业务代码开发中可以添加这个监听器。
 * <p>以下情形将被拦截：</p>
 * <ul>
 *   <li>where条件为空</li>
 *   <li>where条件仅包含列自引用的恒真条件（如 {@code ID.eq(ID)}），等同于无条件</li>
 * </ul>
 * @author Joey
 */
public class UpdateDeleteProtectListener  extends SQLBaseListener {
	
	private static final SelfEqConditionChecker CHECKER=new SelfEqConditionChecker();

	/**
	 * Visitor that counts effective (non-tautological) conditions in a WHERE expression tree.
	 * <p>
	 * A self-referencing equality like {@code column.eq(column)} is detected by reference
	 * identity ({@code ==}) and treated as a tautology (always-true), contributing zero to
	 * the counter. Logical operators (AND, OR, NOT) are traversed recursively. Any other
	 * operation increments the counter, indicating a meaningful condition exists.
	 * </p>
	 * <p>
	 * Note: NOT(self-eq) is technically always-false and won't cause full-table operations,
	 * but is still rejected here because nested NOT expressions (e.g. NOT(NOT(self-eq)))
	 * would revert to always-true. Erring on the side of caution.
	 * </p>
	 */
	static class SelfEqConditionChecker implements Visitor<Void,int[]>{
		@Override
		public Void visit(Constant<?> expr, @Nullable int[] context) {
			return null;
		}

		@Override
		public Void visit(FactoryExpression<?> expr, @Nullable int[] context) {
			return null;
		}

		@Override
		public Void visit(Operation<?> expr, @Nullable int[] context) {
			if(expr.getOperator() instanceof Ops) {
				Ops op = (Ops)expr.getOperator();
				switch(op) {
				case EQ:
					List<Expression<?>> args= expr.getArgs();
					// Reference identity check: same Path object on both sides means self-referencing.
					// Accepts the risk that two distinct Path instances with same meaning won't be caught.
					if(args.size()==2 && args.get(0).equals(args.get(1)) ){
						return null;
					}
					context[0]++;
					break;
				case OR:
				case AND:
				case NOT:
					// Logical connectors: recurse into sub-expressions
					expr.getArgs().forEach(e->e.accept(this, context));
					break;
				default:
					// Any other operator (GT, LT, LIKE, IN, etc.) counts as an effective condition
					context[0]++;
					break;
				}
			}
			return null;
		}

		@Override
		public Void visit(ParamExpression<?> expr, @Nullable int[] context) {
			return null;
		}

		@Override
		public Void visit(Path<?> expr, @Nullable int[] context) {
			return null;
		}

		@Override
		public Void visit(SubQueryExpression<?> expr, @Nullable int[] context) {
			return null;
		}

		@Override
		public Void visit(TemplateExpression<?> expr, @Nullable int[] context) {
			return null;
		}
		
	}
	
	@Override
	public void notifyDelete(RelationalPath<?> entity, QueryMetadata md) {
		if(md.getWhere()!=null) {
			int[] counter=new int[] {0};
			md.getWhere().accept(CHECKER, counter);
			if(counter[0]>0) {
				return;
			}
		}
		throw new UnsupportedOperationException("The deletion was rejected to prevent operation on all records.");
	}

	@Override
	public void notifyDeletes(RelationalPath<?> entity, List<QueryMetadata> batches) {
		for(QueryMetadata md: batches) {
			if(md.getWhere()!=null) {
				int[] counter=new int[] {0};
				md.getWhere().accept(CHECKER, counter);
				if(counter[0]>0) {
					continue;
				}
			}
			throw new UnsupportedOperationException("The deletion was rejected to prevent operation on all records.");
		}
	}

	@Override
	public void notifyUpdate(RelationalPath<?> entity, QueryMetadata md, Map<Path<?>, Expression<?>> updates) {
		if(md.getWhere()!=null) {
			int[] counter=new int[] {0};
			md.getWhere().accept(CHECKER, counter);
			if(counter[0]>0) {
				return;
			}
		}
		throw new UnsupportedOperationException("The update was rejected to prevent operation on all records.");
	}

	@Override
	public void notifyUpdates(RelationalPath<?> entity, List<SQLUpdateBatch> batches) {
		for(SQLUpdateBatch md: batches) {
			if(md.getMetadata().getWhere()!=null) {
				int[] counter=new int[] {0};
				md.getMetadata().getWhere().accept(CHECKER, counter);
				if(counter[0]>0) {
					continue;
				}
			}
			throw new UnsupportedOperationException("The update was rejected to prevent operation on all records.");
		}
	}

}
