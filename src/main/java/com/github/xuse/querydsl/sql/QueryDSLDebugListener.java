package com.github.xuse.querydsl.sql;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLDetailedListener;
import com.querydsl.sql.SQLListenerContext;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.dml.SQLMergeBatch;
import com.querydsl.sql.dml.SQLUpdateBatch;

/**
 * 本类包含大量空方法，增加final修饰有利于虚拟机进行即时编译优化。
 * 
 * @author jiyi
 *
 */
public final class QueryDSLDebugListener implements SQLDetailedListener {

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
	public final void notifyMerge(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> keys, List<Path<?>> columns,
			List<Expression<?>> values, SubQueryExpression<?> subQuery) {
	}

	@Override
	public final void notifyMerges(RelationalPath<?> entity, QueryMetadata md, List<SQLMergeBatch> batches) {
	}

	@Override
	public final void notifyInsert(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> columns,
			List<Expression<?>> values, SubQueryExpression<?> subQuery) {
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
	}

	@Override
	public final void executed(SQLListenerContext context) {
		if(!log.isInfoEnabled()) {
			return;
		}
		Long time = (Long) context.getData(ContextKeyConstants.ELAPSED_TIME);
		String count = String.valueOf(context.getData(ContextKeyConstants.COUNT));
		String action = (String) context.getData(ContextKeyConstants.ACTION);
		if (action == null) {
			// 兼容官方版本
			log.info(context.getSQL());
			return;
		}
		StringBuilder sb = new StringBuilder();
		if (time == null) {
			time = -1L;
		}
		sb.append(action).append(' ').append(count).append(" records, elapsed ").append(time).append("ms.");
		log.info(sb.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void prepared(SQLListenerContext context) {
		StringBuilder sb = new StringBuilder(getSQL(context)).append("\n");
		List<Path<?>> constantPaths = (List<Path<?>>) context.getData(ContextKeyConstants.PARAMS_PATH);
		if (constantPaths == null || !log.isInfoEnabled()) {
			// 兼容官方版本
			return;
		}

		Integer batchSize = (Integer) context.getData(ContextKeyConstants.BATCH_SIZE);
		if (batchSize == null) {
			List<?> params = (List<?>) context.getData(ContextKeyConstants.SIGLE_PARAMS);
			for (int count = 0; count < constantPaths.size(); count++) {
				Path<?> p = constantPaths.get(count);
				Object value = params.get(count);
				append(sb, p, value, count);
			}
		} else {
			List<List<?>> params = (List<List<?>>) context.getData(ContextKeyConstants.BATCH_PARAMS);
			for (int row = 0; row < params.size(); row++) {
				sb.append("Batch Params: (").append(row + 1).append('/').append(batchSize + 1).append(")\n");
				for (int count = 0; count < constantPaths.size(); count++) {
					Path<?> p = constantPaths.get(count);
					Object value = params.get(count);
					append(sb, p, value, count);
				}
			}
			if (params.size() < batchSize) {
				sb.append("Parameters after are ignored to reduce the size of log.");
			}
		}
		log.info(sb.toString());
	}

	private void append(StringBuilder sb, Path<?> p, Object value, int count) {
		if(count>0) {
			sb.append("\n");
		}
		sb.append('(').append(count + 1).append(')');
		sb.append(p.toString()).append(':');
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
		log.error(getSQL(context), context.getException());
	}

	private String getSQL(SQLListenerContext context) {
		return context.getSQL().replace('\n', ' ');
	}

	@Override
	public final void end(SQLListenerContext context) {
	}
}
