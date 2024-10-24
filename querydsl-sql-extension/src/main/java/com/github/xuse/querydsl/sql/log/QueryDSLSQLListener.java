package com.github.xuse.querydsl.sql.log;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.DateFormats.TLDateFormat;
import com.github.xuse.querydsl.util.Primitives;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLDetailedListener;
import com.querydsl.sql.SQLListenerContext;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.dml.SQLMergeBatch;
import com.querydsl.sql.dml.SQLUpdateBatch;
import com.querydsl.sql.types.Null;

/**
 * 本类包含大量空方法，增加final修饰有利于虚拟机进行即时编译优化。
 *
 * 关于SQL语句换行问题，无需在本类中使用.replace('\n', ' ')进行转换，应该在创建SQLTemplate的时候就使用
 * com.querydsl.sql.SQLTemplates.Builder.newLineToSingleSpace()方法来实现。
 *
 * @author Joey
 */
public final class QueryDSLSQLListener implements SQLDetailedListener {

	private final Logger log = LoggerFactory.getLogger("SQL");

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

	/**
	 *  输出格式，全部初始化。根据配置等级输出，当error时输出等级自动+1
	 */
	private final Formatter errorFormatter;

	private final Formatter infoFormatter;

	/**
	 *  初级格式，仅输出SQL
	 *  @author Joey
	 */
	static class Formatter {

		protected final TLDateFormat DATE_FORMAT = DateFormats.DATE_TIME_CS;

		private final int maxBatchOutput;

		public Formatter(int maxBatchOutput) {
			this.maxBatchOutput = maxBatchOutput;
		}

		protected StringBuilder formatSQL(String sql) {
			if(sql.length()>2048) {
				//Print up to 2K size SQL, considering the balance between I/O performance and usage.
				StringBuilder sb=new StringBuilder(2560);
				return sb.append(sql, 0, 2048).append("...");
			}
			return new StringBuilder(sql);
		}

		String format(Collection<SQLBindings> bs) {
			if (bs.isEmpty()) {
				return "";
			}
			Iterator<SQLBindings> iter = bs.iterator();
			SQLBindings binding = iter.next();
			StringBuilder sb = formatSQL(binding.getSQL());
			List<Path<?>> constantPaths = CollectionUtils.nullElementsList();
			if (binding instanceof SQLBindingsAlter) {
				constantPaths = ((SQLBindingsAlter) binding).getPaths();
			}
			int total = bs.size();
			int row = 0;
			if (total > 1) {
				sb.append("\nBatch Params: (").append(++row).append('/').append(total).append(")");
			}
			appendParams(sb, binding.getNullFriendlyBindings(), constantPaths);
			int maxBatchOutput = this.maxBatchOutput;
			while (iter.hasNext()) {
				binding = iter.next();
				sb.append("\nBatch Params: (").append(++row).append('/').append(total).append(")");
				appendParams(sb, binding.getNullFriendlyBindings(), constantPaths);
				if (row >= maxBatchOutput) {
					sb.append(" ... Parameters after are ignored to reduce the size of log.");
					break;
				}
			}
			return sb.toString();
		}

		private void appendParams(StringBuilder sb, List<Object> params, List<Path<?>> constantPaths) {
			int size = Math.min(params.size(),100);
			if (size > 0) {
				paramsBegin(sb);
				for (int count = 0; count < size; count++) {
					if (count > 0) {
						newParamSep(sb);
					}
					Path<?> p = constantPaths.get(count);
					Object value = params.get(count);
					append0(sb, p, value == null ? Null.DEFAULT : value, count);
				}
				paramsEnd(sb, params.size() - size);
			}
		}

		protected void paramsEnd(StringBuilder sb, int extraParams) {
			if(extraParams>0) {
				sb.append("...(").append(extraParams).append(" params skipped");
			}
			sb.append(']');
		}

		protected void paramsBegin(StringBuilder sb) {
			sb.append('[');
		}

		protected void newParamSep(StringBuilder sb) {
			sb.append(", ");
		}

		protected void append0(StringBuilder sb, Path<?> p, Object value, int count) {
			if (value == null || value == Null.DEFAULT) {
				sb.append("null");
				return;
			}
			appendValue(sb, value);
		}

		protected void appendValue(StringBuilder sb, Object value) {
			Class<?> vClass = value.getClass();
			if (vClass == byte[].class) {
				sb.append(((byte[]) value).length).append(" bytes");
			} else if (value instanceof Date) {
				sb.append(DATE_FORMAT.format((Date) value));
			} else {
				String valStr = String.valueOf(value);
				appendString(sb, valStr);
			}
		}

		protected void appendString(StringBuilder sb, String valStr) {
			if (valStr.length() > 40) {
				// 如果日志太长是不行的
				sb.append("[").append(valStr, 0, 38).append("..]");
				sb.append(" chars=").append(valStr.length());
			} else {
				sb.append(valStr);
			}
		}
	}

	static class Formatter1 extends Formatter {

		protected Formatter1(int n) {
			super(n);
		}

		@Override
		protected void appendString(StringBuilder sb, String valStr) {
			sb.append(valStr);
		}

		protected void paramsBegin(StringBuilder sb) {
			sb.append('\n');
		}

		protected void paramsEnd(StringBuilder sb, int extra) {
		}
	}

	static class Formatter2 extends Formatter1 {

		protected Formatter2(int n) {
			super(n);
		}

		@Override
		protected void newParamSep(StringBuilder sb) {
			sb.append("\n");
		}

		@Override
		protected void append0(StringBuilder sb, Path<?> p, Object value, int count) {
			sb.append("  ").append(count + 1);
			if (p != null) {
				PathMetadata metadata=p.getMetadata();
				sb.append(") ");
				if(metadata.getParent()!=null) {
					sb.append(metadata.getParent().getMetadata().getName()).append('.');
				}
				sb.append(metadata.getName());
			} else {
				sb.append(") ?");
			}
			sb.append('-').append(value.getClass().getSimpleName()).append(": ");
			if (value == null || value == Null.DEFAULT) {
				sb.append("null");
			} else {
				appendValue(sb, value);
			}
		}
	}

	public static final int FORMAT_COMPACT = 0;

	public static final int FORMAT_FULL = 1;

	public static final int FORMAT_DEBUG = 2;

	/**
	 *  构造
	 */
	public QueryDSLSQLListener() {
		this(0, ContextKeyConstants.MAX_BATCH_LOG);
	}

	/**
	 * 构造
	 * @param format format
	 * <ul>
	 * <li>{@link #FORMAT_COMPACT} Suitable for large-scale production environments.</li>
	 * <li>{@link #FORMAT_FULL} Long string variable will not be truncated, and there is newline before SQL and binding parameters. </li>
	 * <li>{@link #FORMAT_DEBUG} for develop environment, especially suitable for console output.</li>
	 * </ul>
	 *  <ul>
	 * <li>{@link #FORMAT_COMPACT} 适合大型生产环境的紧凑格式。</li>
	 * <li>{@link #FORMAT_FULL} 长的字符串会完整输出，SQL和参数之间会换行。</li>
	 * <li>{@link #FORMAT_DEBUG} 最详细的信息输出，会有频繁换行，适合开发环境观察语句和输出。</li>
	 * </ul>
	 */
	public QueryDSLSQLListener(int format) {
		this(format, ContextKeyConstants.MAX_BATCH_LOG);
	}

	/**
	 * @param format format
	 * <ul>
	 * <li>{@link #FORMAT_COMPACT} Suitable for large-scale production environments.</li>
	 * <li>{@link #FORMAT_FULL} Long string variable will not be truncated, and there is newline before SQL and binding parameters. </li>
	 * <li>{@link #FORMAT_DEBUG} for develop environment, especially suitable for console output.</li>
	 * </ul>
	 *  <ul>
	 * <li>{@link #FORMAT_COMPACT} 适合大型生产环境的紧凑格式。</li>
	 * <li>{@link #FORMAT_FULL} 长的字符串会完整输出，SQL和参数之间会换行。</li>
	 * <li>{@link #FORMAT_DEBUG} 最详细的信息输出，会有频繁换行，适合开发环境观察语句和输出。</li>
	 * </ul>
	 * @param maxBatchCount 批量操作时，最大打印n组参数
	 */
	public QueryDSLSQLListener(int format, int maxBatchCount) {
		switch(format) {
			case FORMAT_COMPACT:
				this.infoFormatter = new Formatter(maxBatchCount);
				this.errorFormatter = new Formatter1(maxBatchCount);
				break;
			case FORMAT_FULL:
				this.infoFormatter = new Formatter1(maxBatchCount);
				this.errorFormatter = new Formatter2(maxBatchCount);
				break;
			default:
				this.infoFormatter = new Formatter2(maxBatchCount);
				this.errorFormatter = new Formatter2(maxBatchCount);
		}
	}

	@Override
	public final void prePrepare(SQLListenerContext context) {
		if (log.isInfoEnabled()) {
			log.info(infoFormatter.format(context.getAllSQLBindings()));
		}
	}
	
	@Override
	public final void preExecute(SQLListenerContext context) {
	}

	@Override
	public final void executed(SQLListenerContext context) {
		boolean slow = Boolean.TRUE.equals(context.getData(ContextKeyConstants.SLOW_SQL));
		int maxExceed = Primitives.unbox((Integer) context.getData(ContextKeyConstants.EXCEED), 0);
		if (slow || log.isInfoEnabled()) {
			String action = (String) context.getData(ContextKeyConstants.ACTION);
			if (action == null || action.length() == 0) {
				// 兼容官方版本
				return;
			}
			if (action.charAt(0) == '(') {
				// Internal actions, only print on debug level.
				if (!log.isDebugEnabled()) {
					return;
				}
			}
			Object time = context.getData(ContextKeyConstants.ELAPSED_TIME);
			String count = String.valueOf(context.getData(ContextKeyConstants.COUNT));
			StringBuilder sb = new StringBuilder(52);
			if (time == null) {
				time = -1L;
			}
			sb.append("Records ").append(action).append(':').append(count).append(", elapsed ").append(time).append("ms.");
			if (maxExceed > 0) {
				sb.append("NOTE: result set was truncated since it exceeds the MaxRows = ").append(maxExceed);
			}
			if (slow) {
				log.error("SlowSQL:[{}].\n{}", errorFormatter.format(context.getAllSQLBindings()), sb);
			} else {
				log.info(sb.toString());
			}
		}
	}

	@Override
	public final void prepared(SQLListenerContext context) {
	}

	@Override
	public final void exception(SQLListenerContext context) {
		log.error(errorFormatter.format(context.getAllSQLBindings()), context.getException());
	}

	@Override
	public final void end(SQLListenerContext context) {
	}
}
