package com.github.xuse.querydsl.sql.log;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.util.DateFormats;
import com.github.xuse.querydsl.util.DateFormats.TLDateFormat;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.github.xuse.querydsl.util.lang.Primitives;
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
 * <h2>English:</h2>
 * This class contains many empty methods marked as {@code final} to facilitate JIT compilation optimization.
 *
 * Regarding SQL statement line breaks, there is no need to use {@code .replace('\n', ' ')} in this class.
 * Instead, use {@code com.querydsl.sql.SQLTemplates.Builder.newLineToSingleSpace()} when creating SQLTemplates.
 * <h2>Chinese:</h2>
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
	 * 异常日志级别。默认 ERROR。
	 * 业务方可设置为 WARN，适用于预期内可恢复异常（如唯一键冲突后降级处理）不希望触发告警的场景。
	 */
	private int exceptionLogLevel = LOG_LEVEL_ERROR;

	/**
	 * 全局默认的异常日志详细度。语句级设置优先于此项。
	 */
	private ExceptionLogDetail exceptionLogDetail = ExceptionLogDetail.FULL_STACK;

	/**
	 * 按 SQLState 配置的异常日志详细度。key 为 SQLState，命中时覆盖全局默认值，
	 * 但仍低于语句级设置的优先级。
	 */
	private Map<String, ExceptionLogDetail> sqlStateDetails = Collections.emptyMap();

	/**
	 * 异常日志级别：ERROR（默认）
	 */
	public static final int LOG_LEVEL_ERROR = 0;

	/**
	 * 异常日志级别：WARN
	 */
	public static final int LOG_LEVEL_WARN = 1;
	
	static final TLDateFormat DATE_FORMAT = DateFormats.DATE_TIME_CS;
	static final Map<Class<?>,Function<Object,String>> VALUE_APPENDERS=new HashMap<>();
	static {
		VALUE_APPENDERS.put(java.util.Date.class, (value) -> DATE_FORMAT.format((java.util.Date) value));
		VALUE_APPENDERS.put(String[].class, (value) -> Arrays.toString((Object[])value));
		VALUE_APPENDERS.put(byte[].class, (value) -> ((byte[]) value).length +" bytes");
		VALUE_APPENDERS.put(int[].class, (value) -> Arrays.toString((int[])value));
		VALUE_APPENDERS.put(long[].class, (value) -> Arrays.toString((long[])value));
		VALUE_APPENDERS.put(double[].class, (value) -> Arrays.toString((double[])value));
		VALUE_APPENDERS.put(float[].class, (value) -> Arrays.toString((float[])value));
	}
	/**
	 *  初级格式，仅输出SQL
	 *  @author Joey
	 */
	static class Formatter {
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
			Function<Object, String> appender = VALUE_APPENDERS.get(value.getClass());
			if (appender == null) {
				String valStr = String.valueOf(value);
				appendString(sb, valStr);
			} else {
				sb.append(appender.apply(value));
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
				Function<Object, String> appender = VALUE_APPENDERS.get(value.getClass());
				if (appender == null) {
					String valStr = String.valueOf(value);
					appendString(sb, valStr);
				} else {
					sb.append(appender.apply(value));
				}
			}
		}
	}

	/**
	 * 适合大型生产环境的紧凑格式。
	 */
	public static final int FORMAT_COMPACT = 0;
	/**
	 * 长的字符串会完整输出，SQL和参数之间会换行。
	 */
	public static final int FORMAT_FULL = 1;
	/**
	 * 最详细的信息输出，会有频繁换行，适合开发环境观察语句和输出。
	 */
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

	/**
	 * 设置异常日志级别。
	 * @param level {@link #LOG_LEVEL_ERROR} 或 {@link #LOG_LEVEL_WARN}
	 * @return this
	 */
	public QueryDSLSQLListener setExceptionLogLevel(int level) {
		this.exceptionLogLevel = level;
		return this;
	}

	/**
	 * 设置全局默认的异常日志详细度。语句级设置（见各 clause 的
	 * {@code exceptionLog(ExceptionLogDetail)} 方法）优先于此项。
	 *
	 * @param detail 详细度，null 时按 {@link ExceptionLogDetail#FULL_STACK} 处理
	 * @return this
	 */
	public QueryDSLSQLListener setExceptionLogDetail(ExceptionLogDetail detail) {
		this.exceptionLogDetail = detail == null ? ExceptionLogDetail.FULL_STACK : detail;
		return this;
	}

	/**
	 * 针对指定的 SQLState 配置异常日志详细度。适用于批量降噪已有代码，
	 * 无需逐个调用点声明。
	 * <p>
	 * 常用 SQLState：{@code 23000} 完整性约束违例（含主键/唯一键冲突），
	 * {@code 23505} Derby/H2 的唯一约束冲突，{@code 23503} 外键违例。
	 * <p>
	 * 注意：该配置为全局生效，如果某处的约束冲突其实是缺陷，也会一并降噪。
	 * 精确控制请使用语句级的 {@code exceptionLog(...)}。
	 *
	 * @param sqlState SQLState 值
	 * @param detail 命中时使用的详细度
	 * @return this
	 */
	public QueryDSLSQLListener setSQLStateDetail(String sqlState, ExceptionLogDetail detail) {
		if (sqlState == null || detail == null) {
			return this;
		}
		if (this.sqlStateDetails.isEmpty()) {
			this.sqlStateDetails = new HashMap<>(8);
		}
		this.sqlStateDetails.put(sqlState, detail);
		return this;
	}

	/**
	 * 将主键/唯一键冲突相关的 SQLState 统一设置为指定详细度。等价于对
	 * {@code 23000}、{@code 23505}、{@code 23001}、{@code 23514} 分别调用
	 * {@link #setSQLStateDetail(String, ExceptionLogDetail)}。
	 *
	 * @param detail 详细度
	 * @return this
	 */
	public QueryDSLSQLListener setConstraintViolationDetail(ExceptionLogDetail detail) {
		for (String state : CONSTRAINT_VIOLATION_STATES) {
			setSQLStateDetail(state, detail);
		}
		return this;
	}

	/**
	 * 常见的完整性约束违例 SQLState。
	 */
	private static final String[] CONSTRAINT_VIOLATION_STATES = { "23000", "23001", "23503", "23505", "23514" };

	/**
	 * 计算本次异常应使用的详细度。优先级：语句级 &gt; SQLState 匹配 &gt; 全局默认。
	 */
	private ExceptionLogDetail resolveDetail(SQLListenerContext context, Exception ex) {
		Object perStatement = context.getData(ContextKeyConstants.EXCEPTION_LOG_DETAIL);
		if (perStatement instanceof ExceptionLogDetail) {
			return (ExceptionLogDetail) perStatement;
		}
		if (!sqlStateDetails.isEmpty()) {
			String state = findSQLState(ex);
			if (state != null) {
				ExceptionLogDetail byState = sqlStateDetails.get(state);
				if (byState != null) {
					return byState;
				}
			}
		}
		return exceptionLogDetail;
	}

	/**
	 * 沿异常链查找第一个可用的 SQLState。框架会把 SQLException 转换为
	 * RuntimeException 抛出，所以此处需要向下钻取 cause。
	 */
	private static String findSQLState(Throwable ex) {
		int depth = 0;
		while (ex != null && depth++ < 16) {
			if (ex instanceof SQLException) {
				String state = ((SQLException) ex).getSQLState();
				if (state != null && !state.isEmpty()) {
					return state;
				}
			}
			Throwable cause = ex.getCause();
			if (cause == ex) {
				break;
			}
			ex = cause;
		}
		return null;
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
		boolean sqlLog= log.isInfoEnabled();
		int important = Primitives.unbox((Integer) context.getData(ContextKeyConstants.IMPORTANT), 0)
				& (sqlLog ? 1 : 3);
		if (important>0 || log.isInfoEnabled()) {
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
			int maxExceed = Primitives.unbox((Integer) context.getData(ContextKeyConstants.EXCEED), 0);
			if (maxExceed > 0) {
				sb.append("NOTE: result set was truncated since it exceeds the MaxRows = ").append(maxExceed);
			}
			if (important>0) {
				log.error("{}[{}].\n{}", ContextKeyConstants.IMPORTANT_HINT[important],
						errorFormatter.format(context.getAllSQLBindings()), sb);
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
		Exception ex = context.getException();
		ExceptionLogDetail detail = resolveDetail(context, ex);
		if (detail == ExceptionLogDetail.NONE) {
			return;
		}
		boolean warn = exceptionLogLevel == LOG_LEVEL_WARN;
		if (detail == ExceptionLogDetail.BRIEF) {
			// 不输出堆栈，仅追加异常类名与message
			StringBuilder sb = new StringBuilder(errorFormatter.format(context.getAllSQLBindings()));
			appendBrief(sb, ex);
			if (warn) {
				log.warn(sb.toString());
			} else {
				log.error(sb.toString());
			}
			return;
		}
		String message = errorFormatter.format(context.getAllSQLBindings());
		if (warn) {
			log.warn(message, ex);
		} else {
			log.error(message, ex);
		}
	}

	/**
	 * 追加异常摘要：类名 + message，并沿 cause 链找出根因，不输出堆栈。
	 */
	private static void appendBrief(StringBuilder sb, Throwable ex) {
		if (ex == null) {
			return;
		}
		sb.append("\n").append(ex.getClass().getName());
		String message = ex.getMessage();
		if (message != null) {
			sb.append(": ").append(message);
		}
		Throwable root = ex;
		int depth = 0;
		while (root.getCause() != null && root.getCause() != root && depth++ < 16) {
			root = root.getCause();
		}
		if (root != ex) {
			sb.append("\nCaused by: ").append(root.getClass().getName());
			String rootMessage = root.getMessage();
			if (rootMessage != null) {
				sb.append(": ").append(rootMessage);
			}
		}
	}

	@Override
	public final void end(SQLListenerContext context) {
	}
}
