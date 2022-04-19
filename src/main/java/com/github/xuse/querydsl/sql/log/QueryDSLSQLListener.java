package com.github.xuse.querydsl.sql.log;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
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
import com.querydsl.sql.types.Null;

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
	
	/**
	 * 输出格式，全部初始化。根据配置等级输出，当error时输出等级自动+1
	 */
	private final Formatter errorFormatter;
	
	private final Formatter infoFormatter;
	
	/**
	 * 初级格式，仅输出SQL
	 * @author jiyi
	 *
	 */
	static class Formatter{
		protected final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
		
		protected String formatSQL(String sql) {
			return sql.replace('\n', ' ');
		}
		
		String format(Collection<SQLBindings> bs) {
			if (bs.isEmpty()) {
				return "";
			}
			Iterator<SQLBindings> iter = bs.iterator();
			SQLBindings bingding = iter.next();
			StringBuilder sb = new StringBuilder(formatSQL(bingding.getSQL()));
			List<Path<?>> constantPaths = ALL_NULL_LIST;
			if (bingding instanceof SQLBindingsAlter) {
				constantPaths = ((SQLBindingsAlter) bingding).getPaths();
			}
			int total = bs.size();
			int row = 0;
			if (total > 1) {
				sb.append("\nBatch Params: (").append(++row).append('/').append(total).append(")");
			}
			appendParams(sb, bingding.getNullFriendlyBindings(), constantPaths);
			while (iter.hasNext()) {
				bingding = iter.next();
				sb.append("\nBatch Params: (").append(++row).append('/').append(total).append(")");
				appendParams(sb, bingding.getNullFriendlyBindings(), constantPaths);
				if (row > ContextKeyConstants.MAX_BATCH_LOG) {
					sb.append("Parameters after are ignored to reduce the size of log.");
					break;
				}
			}
			return sb.toString();
		}

		private void appendParams(StringBuilder sb,List<Object> params,List<Path<?>> constantPaths) {
			paramsBegin(sb);
			for (int count = 0; count < params.size(); count++) {
				if (count > 0) {
					newParamSep(sb);
				}
				Path<?> p = constantPaths.get(count);
				Object value = params.get(count);
				append0(sb, p, value, count);
			}
			paramsEnd(sb);
		}
		
		protected void paramsEnd(StringBuilder sb) {
			sb.append(']');
		}

		protected void paramsBegin(StringBuilder sb) {
			sb.append('[');
		}

		protected void newParamSep(StringBuilder sb) {
			sb.append(", ");
		}

		protected void append0(StringBuilder sb, Path<?> p, Object value, int count) {
			if (value == null || value==Null.DEFAULT) {
				sb.append("null");
				return;
			}
			appendValue(sb,value);
		}

		protected void appendValue(StringBuilder sb, Object value) {
			Class<?> vClass = value.getClass();
			if (vClass == byte[].class) {
				sb.append(((byte[]) value).length).append(" bytes");
			}else if(value instanceof Date){
				sb.append(DATE_FORMAT.format((Date)value));
			}else {
				String valStr = String.valueOf(value);
				appendString(sb,valStr);
			}
		}

		protected void appendString(StringBuilder sb,String valStr) {
			if (valStr.length() > 40) {// 如果日志太长是不行的
				sb.append("[").append(valStr.substring(0, 38)).append("..]");
				sb.append(" chars=").append(valStr.length());
			} else {
				sb.append(valStr);
			}
		}
	}
	
	static class Formatter1 extends Formatter{
		@Override
		protected void appendString(StringBuilder sb, String valStr) {
			sb.append(valStr);
		}
		
		protected void paramsBegin(StringBuilder sb) {
			sb.append('\n');
		}
		protected void paramsEnd(StringBuilder sb) {
		}
	}
	
	static class Formatter2 extends Formatter1{
		@Override
		protected void newParamSep(StringBuilder sb) {
			sb.append("\n");
		}

		@Override
		protected void append0(StringBuilder sb, Path<?> p, Object value, int count) {
			sb.append("  ").append(count);
			if(p!=null) {
				sb.append(')').append(p);
			}
			sb.append('-').append(value.getClass().getSimpleName()).append(':');
			if (value == null || value==Null.DEFAULT) {
				sb.append("null");
			}else {
				appendValue(sb,value);
			}
		}
	}
		
	
	public QueryDSLSQLListener() {
		this(0);
	}
	
	public QueryDSLSQLListener(int format) {
		switch(format) {
		case 0:
			this.infoFormatter=new Formatter();
			this.errorFormatter=new Formatter1();
			break;
		case 1:
			this.infoFormatter=new Formatter1();
			this.errorFormatter=new Formatter2();
			break;
		default:
			this.infoFormatter=new Formatter2();
			this.errorFormatter=new Formatter2();
		}
	}
	
	@Override
	public final void preExecute(SQLListenerContext context) {
		if (!log.isInfoEnabled()) {
			return;
		}
		log.info(infoFormatter.format(context.getAllSQLBindings()));
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
		boolean slow=Boolean.TRUE.equals(context.getData(ContextKeyConstants.SLOW_SQL));
		if (slow || log.isInfoEnabled()) {
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
			if(slow) {
				log.error("SlowSQL:[{}].\n{}",context.getSQL(),sb);
			}else {
				log.info(sb.toString());
			}
		}
	}

	@Override
	public final void prepared(SQLListenerContext context) {
	}


	@Override
	public final void exception(SQLListenerContext context) {
		log.error(errorFormatter.format(context.getAllSQLBindings()),context.getException());
	}

	@Override
	public final void end(SQLListenerContext context) {
	}
}
