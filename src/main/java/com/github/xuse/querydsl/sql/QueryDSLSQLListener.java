package com.github.xuse.querydsl.sql;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLDetailedListener;
import com.querydsl.sql.SQLListenerContext;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.dml.SQLMergeBatch;
import com.querydsl.sql.dml.SQLUpdateBatch;

/**
 * 本类包含大量空方法，增加final修饰有利于虚拟机进行即时编译优化。
 * 
 * 关于SQL语句换行问题，无需在本类中使用.replace('\n', ' ')进行转换，应该在创建SQLTemplate的时候就使用
 * com.querydsl.sql.SQLTemplates.Builder.newLineToSingleSpace()方法来实现。
 * 
 * @author jiyi
 *
 */
public final class QueryDSLSQLListener implements SQLDetailedListener {

	private Logger log = LoggerFactory.getLogger("SQL");

	@Override
	public final void notifyQuery(QueryMetadata md) {
	}

	@Override
	public final void notifyDelete(RelationalPath<?> entity, QueryMetadata md) {
	}

	@Override
	public final void notifyDeletes(RelationalPath<?> entity, List<QueryMetadata> batches) {
	}

	@Override
	public final void notifyMerge(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> keys, List<Path<?>> columns, List<Expression<?>> values, SubQueryExpression<?> subQuery) {
	}

	@Override
	public final void notifyMerges(RelationalPath<?> entity, QueryMetadata md, List<SQLMergeBatch> batches) {
	}

	@Override
	public final void notifyInsert(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> columns, List<Expression<?>> values, SubQueryExpression<?> subQuery) {
	}

	@Override
	public final void notifyInserts(RelationalPath<?> entity, QueryMetadata md, List<SQLInsertBatch> batches) {
	}

	@Override
	public final void notifyUpdate(RelationalPath<?> entity, QueryMetadata md, Map<Path<?>, Expression<?>> updates) {
	}

	@Override
	public final void notifyUpdates(RelationalPath<?> entity, List<SQLUpdateBatch> batches) {
	}

	@Override
	public final void start(SQLListenerContext context) {
	}

	@Override
	public final void preRender(SQLListenerContext context) {
	}

	@Override
	public final void rendered(SQLListenerContext context) {
	}

	@Override
	public final void prePrepare(SQLListenerContext context) {
	}

	@Override
	public final void preExecute(SQLListenerContext context) {
		if (!log.isInfoEnabled()) {
			return;
		}
		Collection<SQLBindings> bs = context.getAllSQLBindings();
		if (bs.isEmpty()) {
			return;
		}
		Iterator<SQLBindings> iter = bs.iterator();
		SQLBindings first = iter.next();

		StringBuilder sb = new StringBuilder(first.getSQL().replace('\n', ' ')).append('\n');
		List<Path<?>> constantPaths = ALL_NULL_LIST;
		if (first instanceof SQLBindingsAlter) {
			constantPaths = ((SQLBindingsAlter) first).getPaths();
		}
		if (bs.size() == 1) {
			List<?> params = first.getNullFriendlyBindings();
			for (int count = 0; count < params.size(); count++) {
				Path<?> p = constantPaths.get(count);
				Object value = params.get(count);
				append(sb, p, value, count);
			}
		} else {
			List<SQLBindings> list = convert(bs);
			int total = list.size();
			int loopMax = Math.min(total, ContextKeyConstants.MAX_BATCH_LOG);
			for (int row = 0; row < loopMax; row++) {
				if(row>0) {
					sb.append('\n');
				}
				SQLBindings bindings = list.get(row);
				sb.append("Batch Params: (").append(row + 1).append('/').append(total).append(")\n");
				List<Object> params = bindings.getNullFriendlyBindings();
				for (int count = 0; count < params.size(); count++) {
					Path<?> p = constantPaths.get(count);
					Object value = params.get(count);
					append(sb, p, value, count);
				}
			}
			if (ContextKeyConstants.MAX_BATCH_LOG < total) {
				sb.append("Parameters after are ignored to reduce the size of log.");
			}
		}
		log.info(sb.toString());
	}

	private List<SQLBindings> convert(Collection<SQLBindings> bs) {
		if (bs instanceof List) {
			return (List<SQLBindings>) bs;
		}
		return new ArrayList<SQLBindings>(bs);
	}

	private final static List<Path<?>> ALL_NULL_LIST = new AbstractList<Path<?>>() {
		@Override
		public Path<?> get(int index) {
			return null;
		}

		@Override
		public int size() {
			return 0;
		}
	};

	@Override
	public final void executed(SQLListenerContext context) {
		if (!log.isInfoEnabled()) {
			return;
		}
		String action = (String) context.getData(ContextKeyConstants.ACTION);
		if (action == null || action.length() == 0) {
			// 兼容官方版本
			return;
		}
		Object time = context.getData(ContextKeyConstants.ELAPSED_TIME);
		String count = String.valueOf(context.getData(ContextKeyConstants.COUNT));
		StringBuilder sb = new StringBuilder(52);
		if (time == null) {
			time = -1L;
		}
		sb.append("Records ").append(action).append(':').append(count).append(", elapsed ").append(time).append("ms.");
		log.info(sb.toString());
	}

	@Override
	public final void prepared(SQLListenerContext context) {
	}

	private void append(StringBuilder sb, Path<?> p, Object value, int count) {
		if (count > 0) {
			sb.append(", ");
		}
		sb.append('(').append(count + 1).append(')');
//		int pad = 0;
//		if (p == null) {
//			sb.append(":      ");
//		} else {
//			String s = p.toString();
//			int index = s.indexOf('.');
//			if (index > 0) {
//				s = s.substring(index + 1);
//			}
//			pad = 13 - s.length();
//			sb.append(s);
//		}
//		for (int i = 0; i < pad; i++) {
//			sb.append(' ');
//		}
		if (value == null) {
			sb.append("\tnull");
			return;
		}
		Class<?> vClass = value.getClass();
		if (vClass == byte[].class) {
			sb.append("\t").append(((byte[]) value).length).append(" bytes");
		} else {
			String valStr = String.valueOf(value);
			if (valStr.length() > 40) {// 如果日志太长是不行的
				sb.append("\t[").append(valStr.substring(0, 38)).append("..]");
				sb.append(" chars=").append(valStr.length());
			} else {
				sb.append("\t[").append(valStr).append(']');
			}
		}
	}

	@Override
	public final void exception(SQLListenerContext context) {
		log.error(context.getSQL().replace('\n', ' '), context.getException());
	}

	@Override
	public final void end(SQLListenerContext context) {
	}
}
