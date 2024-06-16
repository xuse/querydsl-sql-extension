/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.xuse.querydsl.sql;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.annotation.query.Condition;
import com.github.xuse.querydsl.annotation.query.ConditionBean;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.BeanCodecManager;
import com.github.xuse.querydsl.sql.expression.FieldCollector;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.sql.result.Projection;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.Holder;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryException;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.core.util.ResultSetAdapter;
import com.querydsl.sql.AbstractSQLQuery;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLCommonQuery;
import com.querydsl.sql.SQLListenerContext;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLResultIterator;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.StatementOptions;

import lombok.extern.slf4j.Slf4j;

/**
 * {@code SQLQuery} is a JDBC based implementation of the {@link SQLCommonQuery}
 * interface
 *
 * @param <T>
 * @author tiwe
 */
@Slf4j
public class SQLQueryAlter<T> extends AbstractSQLQuery<T, SQLQueryAlter<T>> {
	private static final long serialVersionUID = -3451422354253107107L;

	private static final QueryFlag rowCountFlag = new QueryFlag(QueryFlag.Position.AFTER_PROJECTION,
			", count(*) over() ");

	////////////// 覆盖检查字段开始 <p>////////////
	/*
	 * 以下是来自父类AbstractSQLQuery的私有字段，这里相当于克隆了一份，因此是不安全的。 确保安全要求——
	 * 检查所有引用该字段的位置，其位置数量应当和父类AbstractSQLQuery完全一致。
	 * 相当于完全拦截了父类中字段的使用，使得父类的同名字段作废。使用子类的字段。 这一点也不优雅！ 2018-10-09 v4.2.1版本检查通过
	 */
	private SQLListenerContext parentContext;

	private final ConfigurationEx configEx;

	private Supplier<Connection> connProvider;

	private Connection conn;
	///////////// 覆盖检查字段结束/////////////
	private boolean exceedSizeLog;

	public SQLQueryAlter() {
		super((Connection) null, new Configuration(SQLTemplates.DEFAULT), new DefaultQueryMetadata());
		this.configEx = new ConfigurationEx(super.getConfiguration());
	}

	public SQLQueryAlter(Connection conn, ConfigurationEx configuration, QueryMetadata metadata) {
		super(conn, configuration.get(), metadata);
		this.conn = conn;
		this.configEx = configuration;
	}

	public SQLQueryAlter(Connection conn, ConfigurationEx configuration) {
		super(conn, configuration.get());
		this.conn = conn;
		this.configEx = configuration;
	}

	public SQLQueryAlter(Connection conn, SQLTemplates templates, QueryMetadata metadata) {
		super(conn, new Configuration(templates), metadata);
		this.conn = conn;
		this.configEx = new ConfigurationEx(super.getConfiguration());
	}

	public SQLQueryAlter(Connection conn, SQLTemplates templates) {
		super(conn, new Configuration(templates));
		this.conn = conn;
		this.configEx = new ConfigurationEx(super.getConfiguration());
	}

	public SQLQueryAlter(Supplier<Connection> connProvider, ConfigurationEx configuration, QueryMetadata metadata) {
		super(connProvider, configuration.get(), metadata);
		this.connProvider = connProvider;
		this.configEx = configuration;
	}

	public SQLQueryAlter(Supplier<Connection> connProvider, ConfigurationEx configuration) {
		super(connProvider, configuration.get(), new DefaultQueryMetadata());
		this.connProvider = connProvider;
		this.configEx = configuration;
	}

	@Override
	public QueryResults<T> fetchResults() {
		parentContext = startContext(connection(), queryMixin.getMetadata());
		QueryModifiers originalModifiers = queryMixin.getMetadata().getModifiers();
		try {
			if (configuration.getTemplates().isCountViaAnalytics() && queryMixin.getMetadata().getGroupBy().isEmpty()) {
				List<T> results;
				Holder<Object> holder = new Holder<>();
				try {
					queryMixin.addFlag(rowCountFlag);
					results = fetch(holder);

				} finally {
					queryMixin.removeFlag(rowCountFlag);
				}
				long total;
				if (!results.isEmpty()) {
					if (holder.value instanceof Number) {
						total = ((Number) holder.value).longValue();
					} else {
						throw new IllegalStateException("Unsupported lastCell instance " + holder.value);
					}
				} else {
					total = fetchCount();
				}
				return new QueryResults<T>(results, originalModifiers, total);

			} else {
				// 我怎么感觉这两句话是没什么用处的。
//				Expression<T> expr = (Expression<T>) queryMixin.getMetadata().getProjection();
//				queryMixin.setProjection(expr);
				long total = fetchCount();
				if (total > 0) {
					return new QueryResults<T>(fetch(), originalModifiers, total);
				} else {
					return QueryResults.emptyResults();
				}
			}
		} finally {
			endContext(parentContext);
			parentContext = null;
		}
	}

	@Override
	public long fetchCount() {
		try {
			return unsafeCount();
		} catch (SQLException e) {
			String error = "Caught " + e.getClass().getName();
			log.error(error, e);
			throw configuration.translate(e);
		}
	}

	@Override
	protected SQLSerializer createSerializer() {
		SQLSerializer serializer = new SQLSerializerAlter(configEx, false);
		serializer.setUseLiterals(useLiterals);
		return serializer;
	}

	private long unsafeCount() throws SQLException {
		Connection conn;
		SQLListenerContextImpl context = startContext(conn = connection(), getMetadata());
		List<Object> constants = Collections.emptyList();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String queryString = null;
		try {
			listeners.preRender(context);
			SQLSerializer serializer = serialize(true);
			SQLBindings sql = getSQL(serializer);
			queryString = sql.getSQL();
			// logQuery(queryString, serializer.getConstants());
			context.addSQL(sql);
			listeners.rendered(context);

			constants = serializer.getConstants();
			listeners.prePrepare(context);

			stmt = getPreparedStatement(conn, queryString);
			setParameters(stmt, constants, serializer.getConstantPaths(), getMetadata().getParams());

			context.addPreparedStatement(stmt);
			listeners.prepared(context);

			listeners.preExecute(context);
			long start = System.currentTimeMillis();
			rs = stmt.executeQuery();
			long count;
			if (rs.next()) {
				count = rs.getLong(1);
			} else {
				count = 0L;
			}
			postExecuted(context, System.currentTimeMillis() - start, "Count", (int) count);
			return count;
		} catch (SQLException e) {
			onException(context, e);
			throw configuration.translate(queryString, constants, e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}
			endContext(context);
		}
	}

	private Connection connection() {
		if (conn == null) {
			if (connProvider != null) {
				return connProvider.get();
			} else {
				throw new IllegalStateException("No connection provided");
			}
		}
		return conn;
	}

	@Override
	public List<T> fetch() {
		return fetch(null);
	}

	private List<T> fetch(Holder<Object> getLastCell) {
		Connection conn;
		SQLListenerContextImpl context = startContext(conn = connection(), queryMixin.getMetadata());
		String queryString = null;
		List<Object> constants = Collections.emptyList();

		try {
			listeners.preRender(context);
			SQLSerializer serializer = serialize(false);
			SQLBindings sql = getSQL(serializer);
			queryString = sql.getSQL();
			// logQuery(queryString, serializer.getConstants());
			context.addSQL(sql);
			listeners.rendered(context);

			listeners.notifyQuery(queryMixin.getMetadata());
			constants = serializer.getConstants();

			listeners.prePrepare(context);
			try (PreparedStatement stmt = getPreparedStatement(conn, queryString)) {
				setParameters(stmt, constants, serializer.getConstantPaths(), queryMixin.getMetadata().getParams());
				context.addPreparedStatement(stmt);
				listeners.prepared(context);
				listeners.preExecute(context);
				long timeElapsed = System.currentTimeMillis();
				try (ResultSet rs = stmt.executeQuery()) {
					timeElapsed = System.currentTimeMillis() - timeElapsed;
					Projection<T> fe = getProjection(rs, getLastCell != null);
					final List<T> result = fe.convert(rs);
					if (getLastCell != null && fe instanceof AbstractProjection) {
						getLastCell.value = ((AbstractProjection<?>) fe).lastCell;
					}
					postExecuted(context, timeElapsed, "Fetch", result.size());
					return result;
				} catch (IllegalAccessException e) {
					onException(context, e);
					throw new QueryException(e);
				} catch (InvocationTargetException e) {
					onException(context, e);
					throw new QueryException(e);
				} catch (InstantiationException e) {
					onException(context, e);
					throw new QueryException(e);
				} catch (SQLException e) {
					onException(context, e);
					throw configuration.translate(queryString, constants, e);
				}
			}
		} catch (SQLException e) {
			onException(context, e);
			throw configuration.translate(queryString, constants, e);
		} catch (RuntimeException e) {
			onException(context, e);
			throw e;
		} finally {
			endContext(context);
		}
	}

	@SuppressWarnings("unchecked")
	private Projection<T> getProjection(ResultSet rs, boolean getLastCell) throws SQLException {
		Expression<T> expr = (Expression<T>) queryMixin.getMetadata().getProjection();
		Projection<T> fe;
		if (expr instanceof FactoryExpression) {
			FactoryExpressionResult<T> r = new FactoryExpressionResult<T>((FactoryExpression<T>) expr);
			r.getLastCell = getLastCell;
			fe = r;
		} else if (expr == null) {
			DefaultValueResult<T> r = new DefaultValueResult<>();
			r.getLastCell = getLastCell;
			fe = r;
		} else if (expr.equals(Wildcard.all)) {
			WildcardAllResult<T> r = new WildcardAllResult<>(rs.getMetaData().getColumnCount());
			r.getLastCell = getLastCell;
			fe = r;
		} else {
			SingleValueResult<T> r = new SingleValueResult<>(expr);
			r.getLastCell = getLastCell;
			fe = r;
		}
		return fe;
	}

	abstract class AbstractProjection<RT> implements Projection<RT> {
		boolean getLastCell;
		Object lastCell;

		@Override
		public CloseableIterator<RT> iterator(PreparedStatement stmt, ResultSet rs, SQLListenerContext context) {
			return new SQLResultIterator<RT>(configuration, stmt, rs, listeners, context) {
				@Override
				public RT produceNext(ResultSet rs) throws Exception {
					return fetch(rs);
				}
			};
		}

		@Override
		public List<RT> convert(ResultSet rs) throws SQLException {
			List<RT> result = new ArrayList<>();
			int argSize = getArgSize();
			while (rs.next()) {
				if (getLastCell) {
					lastCell = rs.getObject(argSize + 1);
					getLastCell = false;
				}
				result.add(fetch(rs));
			}
			return result;
		}

		protected abstract RT fetch(ResultSet rs) throws SQLException;

		protected abstract int getArgSize();

	}

	final class FactoryExpressionResult<RT> extends AbstractProjection<RT> {
		private final FactoryExpression<RT> c;
		private final int argSize;
		private final List<Path<?>> argPath;
		private final List<Class<?>> argTypes;

		FactoryExpressionResult(FactoryExpression<RT> c) {
			List<Expression<?>> args = c.getArgs();
			int argSize = args.size();
			this.c = c;
			this.argSize = argSize;
			this.argPath = new ArrayList<>(argSize);
			this.argTypes = new ArrayList<>(argSize);
			for (int i = 0; i < argSize; i++) {
				Expression<?> expr = args.get(i);
				argPath.add(expr instanceof Path ? (Path<?>) expr : null);
				argTypes.add(expr.getType());
			}
		}

		protected RT fetch(ResultSet rs) throws SQLException {
			int offset = 0;
			Object[] args = new Object[argSize];
			for (int i = 0; i < args.length; i++) {
				try {
					args[i] = configuration.get(rs, argPath.get(i), offset + i + 1, argTypes.get(i));
				} catch (SQLException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new SQLException("get field:" + argPath.get(i) + "error", ex);
				}
			}
			return c.newInstance(args);
		}

		@Override
		protected int getArgSize() {
			return argSize;
		}
	}

	final class WildcardAllResult<RT> extends AbstractProjection<RT> {
		private final int columnSize;

		public WildcardAllResult(int columnSize) {
			super();
			this.columnSize = columnSize;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected RT fetch(ResultSet rs) throws SQLException {
			Object[] row = new Object[columnSize];
			for (int i = 0; i < row.length; i++) {
				row[i] = rs.getObject(i + 1);
			}
			return (RT) row;
		}

		@Override
		protected int getArgSize() {
			return columnSize;
		}
	}

	final class SingleValueResult<RT> extends AbstractProjection<RT> {
		private final Expression<RT> expr;
		private final Path<?> path;

		public SingleValueResult(Expression<RT> expr) {
			this.expr = expr;
			this.path = expr instanceof Path ? (Path<?>) expr : null;
		}

		@Override
		protected RT fetch(ResultSet rs) throws SQLException {
			return configuration.get(rs, path, 1, expr.getType());
		}

		@Override
		protected int getArgSize() {
			return 1;
		}
	}

	final class DefaultValueResult<RT> extends AbstractProjection<RT> {
		@SuppressWarnings("unchecked")
		@Override
		protected RT fetch(ResultSet rs) throws SQLException {
			return (RT) rs.getObject(1);
		}

		@Override
		protected int getArgSize() {
			return 1;
		}
	}

	private StatementOptions statementOptions = StatementOptions.DEFAULT;

	/**
	 * 设置本次查询载入的最大行数
	 * 
	 * @param maxRows
	 * @return this
	 */
	public SQLQueryAlter<T> setMaxRows(int maxRows) {
		StatementOptions options = this.statementOptions;
		setStatementOptions(new StatementOptions(options.getMaxFieldSize(), maxRows, options.getQueryTimeout(),
				options.getFetchSize()));
		return this;
	}

	/**
	 * 设置本次查询载入的最大行数，并且当达到最大行数时记录截断警告
	 * 
	 * @param maxRows
	 * @return this
	 */
	public SQLQueryAlter<T> setMaxRowsWithWarn(int maxRows) {
		setMaxRows(maxRows);
		this.exceedSizeLog = true;
		return this;
	}

	/**
	 * 设置本次查询每批获取大小
	 * 
	 * @param fetchSize
	 * @return this
	 */
	public SQLQueryAlter<T> setFetchSisze(int fetchSize) {
		StatementOptions options = this.statementOptions;
		setStatementOptions(new StatementOptions(options.getMaxFieldSize(), options.getMaxRows(),
				options.getQueryTimeout(), fetchSize));
		return this;
	}

	/**
	 * 设置查询超时（秒）
	 * 
	 * @param queryTimeout
	 * @return this
	 */
	public SQLQueryAlter<T> setQueryTimeout(int queryTimeout) {
		StatementOptions options = this.statementOptions;
		setStatementOptions(new StatementOptions(options.getMaxFieldSize(), options.getMaxRows(), queryTimeout,
				options.getFetchSize()));
		return this;
	}

	public void setStatementOptions(StatementOptions statementOptions) {
		super.setStatementOptions(statementOptions);
		this.statementOptions = statementOptions;
	}

	private PreparedStatement getPreparedStatement(Connection conn, String queryString) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(queryString);
		StatementOptions statementOptions = this.statementOptions;
		if (statementOptions.getFetchSize() != null) {
			statement.setFetchSize(statementOptions.getFetchSize());
		}
		if (statementOptions.getMaxFieldSize() != null) {
			statement.setMaxFieldSize(statementOptions.getMaxFieldSize());
		}
		if (statementOptions.getQueryTimeout() != null) {
			statement.setQueryTimeout(statementOptions.getQueryTimeout());
		} else if (configEx.getDefaultQueryTimeout() > 0) {
			statement.setQueryTimeout(configEx.getDefaultQueryTimeout());
		}
		if (statementOptions.getMaxRows() != null) {
			statement.setMaxRows(statementOptions.getMaxRows());
		}
		return statement;
	}

	@Override
	public CloseableIterator<T> iterate() {
		return iterateSingle(queryMixin.getMetadata());
	}

	private CloseableIterator<T> iterateSingle(QueryMetadata metadata) {
		Connection conn;
		SQLListenerContextImpl context = startContext(conn = connection(), queryMixin.getMetadata());
		String queryString = null;
		List<Object> constants = Collections.emptyList();

		try {
			listeners.preRender(context);
			SQLSerializer serializer = serialize(false);
			SQLBindings sql = getSQL(serializer);
			queryString = sql.getSQL();
			// logQuery(queryString, serializer.getConstants());
			context.addSQL(sql);
			listeners.rendered(context);

			listeners.notifyQuery(queryMixin.getMetadata());
			constants = serializer.getConstants();

			listeners.prePrepare(context);
			final PreparedStatement stmt = getPreparedStatement(conn, queryString);
			setParameters(stmt, constants, serializer.getConstantPaths(), metadata.getParams());
			context.addPreparedStatement(stmt);
			listeners.prepared(context);

			listeners.preExecute(context);
			long start = System.currentTimeMillis();
			final ResultSet rs = stmt.executeQuery();
			Projection<T> expr = getProjection(rs, false);
			CloseableIterator<T> i = expr.iterator(stmt, rs, context);
			postExecuted(context, System.currentTimeMillis() - start, "Iterated", i.hasNext() ? 1 : 0);
			return i;
		} catch (SQLException e) {
			onException(context, e);
			endContext(context);
			throw configuration.translate(queryString, constants, e);
		} catch (RuntimeException e) {
			log.error("Caught " + e.getClass().getName() + " for " + queryString);
			throw e;
		}
	}

	/**
	 * Get the results as a JDBC ResultSet
	 *
	 * @return results as ResultSet
	 */
	public ResultSet getResults() {
		Connection conn;
		final SQLListenerContextImpl context = startContext(conn = connection(), queryMixin.getMetadata());
		String queryString = null;
		List<Object> constants = Collections.emptyList();

		try {
			listeners.preRender(context);
			SQLSerializer serializer = serialize(false);
			SQLBindings sql = getSQL(serializer);
			queryString = sql.getSQL();
			// logQuery(queryString, serializer.getConstants());
			context.addSQL(sql);
			listeners.rendered(context);

			listeners.notifyQuery(queryMixin.getMetadata());

			constants = serializer.getConstants();

			listeners.prePrepare(context);
			final PreparedStatement stmt = getPreparedStatement(conn, queryString);
			setParameters(stmt, constants, serializer.getConstantPaths(), getMetadata().getParams());
			context.addPreparedStatement(stmt);
			listeners.prepared(context);

			listeners.preExecute(context);
			long start = System.currentTimeMillis();
			final ResultSet rs = stmt.executeQuery();
			postExecuted(context, System.currentTimeMillis() - start, "ResultSet", 0);

			return new ResultSetAdapter(rs) {
				@Override
				public void close() throws SQLException {
					try {
						super.close();
					} finally {
						stmt.close();
						endContext(context);
					}
				}
			};
		} catch (SQLException e) {
			onException(context, e);
			endContext(context);
			throw configuration.translate(queryString, constants, e);
		} catch (RuntimeException e) {
			onException(context, e);
			endContext(context);
			throw e;
		}
	}

	protected SQLBindings getSQL(SQLSerializer serializer) {
		List<Object> args = new ArrayList<>();
		Map<ParamExpression<?>, Object> params = getMetadata().getParams();
		for (Object o : serializer.getConstants()) {
			if (o instanceof ParamExpression) {
				if (!params.containsKey(o)) {
					throw new ParamNotSetException((ParamExpression<?>) o);
				}
				o = queryMixin.getMetadata().getParams().get(o);
			}
			args.add(o);
		}
		return new SQLBindingsAlter(serializer.toString(), args, serializer.getConstantPaths());
	}

	@Override
	public SQLQueryAlter<T> clone() {
		return this.clone(this.conn);
	}

	@Override
	public SQLQueryAlter<T> clone(Connection conn) {
		SQLQueryAlter<T> q = new SQLQueryAlter<T>(conn, configEx, getMetadata().clone());
		q.clone(this);
		return q;
	}

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, int count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
		if (this.configEx.getSlowSqlWarnMillis() <= cost) {
			context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
		}
		if (this.exceedSizeLog && "Fetch".equals(action)) {
			Integer maxSize = this.statementOptions.getMaxRows();
			if (maxSize != null) {
				if (count >= maxSize) {
					context.setData(ContextKeyConstants.EXCEED, maxSize);
				}
			}
		}
		listeners.executed(context);
	}

	@Override
	public <U> SQLQueryAlter<U> select(Expression<U> expr) {
		queryMixin.setProjection(expr);
		@SuppressWarnings("unchecked") // This is the new type
		SQLQueryAlter<U> newType = (SQLQueryAlter<U>) this;
		return newType;
	}

	@Override
	public SQLQueryAlter<Tuple> select(Expression<?>... exprs) {
		queryMixin.setProjection(exprs);
		@SuppressWarnings("unchecked") // This is the new type
		SQLQueryAlter<Tuple> newType = (SQLQueryAlter<Tuple>) this;
		return newType;
	}

	/**
	 * 设置Limit，如果传入值为null或零或负数，则设置无效
	 * 
	 * @param limit
	 * @return this
	 */
	public final SQLQueryAlter<T> limitIf(Integer limit) {
		if (limit == null || limit <= 0) {
			return queryMixin.getSelf();
		}
		return queryMixin.limit(limit);
	}

	/**
	 * 设置Offset，如果传入值为null或负数，则设置无效
	 * 
	 * @param offset
	 * @return this
	 */
	public final SQLQueryAlter<T> offsetIf(Integer offset) {
		if (offset == null || offset < 0) {
			return queryMixin.getSelf();
		}
		return queryMixin.offset(offset);
	}

	/**
	 * 支持使用一个参数Bean自动拼装查询条件。
	 * 
	 * @param <X>
	 * @param conditionBean
	 * @param beanPath
	 * @return SQLQueryAlter
	 */
	public final <X> SQLQueryAlter<T> where(X conditionBean, RelationalPathEx<T> beanPath) {
		@SuppressWarnings({ "unchecked" })
		Class<X> clz = (Class<X>) conditionBean.getClass();
		ConditionBean cb = clz.getAnnotation(ConditionBean.class);
		if (cb == null) {
			throw new IllegalArgumentException("Condition bean must annotated with @ConditionBean");
		}
		BeanCodec codec=BeanCodecManager.getInstance().getPopulator(clz, new FieldCollector());
		Field[] fields = codec.getFields();
		Object[] values = codec.values(conditionBean);
		
		Map<String, Path<?>> bindings = new HashMap<>();
		for(Path<?> p:beanPath.getColumns()) {
			bindings.put(p.getMetadata().getName(),p);
		}
		Number limit = null;
		Number offset = null;
		for (int i = 0; i < fields.length; i++) {
			Field field=fields[i];
			Object value = values[i];
			if(field.getName().equals(cb.limitField())) {
				limit = (Number)value;
				continue;
			}
			if(field.getName().equals(cb.offsetField())) {
				offset = (Number)value;
				continue;
			}
			Condition condition = field.getAnnotation(Condition.class);
			if(condition==null) {
				continue;
			}
			String pathName=condition.path();
			if(StringUtils.isEmpty(pathName)) {
				pathName=field.getName();
			}
			Path<?> path=bindings.get(pathName);
			if(path==null) {
				throw Exceptions.illegalArgument("Not found path {} in bean {}", pathName, beanPath);
			}
			
			ColumnMapping cm = beanPath.getColumnMetadata(path);
			if(condition.ignoreUnsavedValue() && isUnsavedValue(cm, value, condition.value())) {
				continue;
			}
			appendCondition(value, path, condition.value());
		}
		if (limit != null && limit.intValue()>=0) {
			limit(limit.intValue());	
		}
		if (offset != null && offset.intValue()>=0) {
			offsetIf(offset.intValue());
		}
		return this;
	}

	private boolean isUnsavedValue(ColumnMapping cm, Object value, Ops operator) {
		if(operator==Ops.IN || operator==Ops.BETWEEN) {
			if(value==null) {
				return true;
			}
			if (value instanceof Collection) {
				for(Object e:(Collection<?>)value) {
					if(cm.isUnsavedValue(e)) {
						return true;
					}
				}
				return false;
			}
			if (value instanceof Object[]) {
				for(Object e:(Object[])value) {
					if(cm.isUnsavedValue(e)) {
						return true;
					}
				}
			}
			return false;
		}else {
			return cm.isUnsavedValue(value);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void appendCondition(Object value, Path<?> path, Ops ops) {
		if (ops == Ops.IN) {
			if (!hasElements(value,1)) {
				return;
			}
		} else if(ops == Ops.BETWEEN) {
			if (!hasElements(value,2)) {
				return;
			}
		} 
		switch (ops) {
		case IN: {
			SimpleExpression exp = (SimpleExpression) path;
			if (value instanceof Collection<?>) {
				queryMixin.where(exp.in((Collection<?>) value));
			} else if (value instanceof Object[]) {
				queryMixin.where(exp.in((Object[]) value));
			}
			break;
		}
		case EQ: {
			SimpleExpression exp = (SimpleExpression) path;
			if(value==null) {
				queryMixin.where(exp.isNull());
			}else {
				queryMixin.where(exp.eq(value));	
			}
			break;
		}
		case LT: {
			if (path instanceof NumberExpression) {
				NumberExpression exp = (NumberExpression) path;
				queryMixin.where(exp.lt((Number) value));
			} else {
				ComparableExpression exp = (ComparableExpression) path;
				queryMixin.where(exp.lt((Comparable) value));
			}
			break;
		}
		case LOE: {
			if (path instanceof NumberExpression) {
				NumberExpression exp = (NumberExpression) path;
				queryMixin.where(exp.loe((Number) value));
			} else {
				ComparableExpression exp = (ComparableExpression) path;
				queryMixin.where(exp.loe((Comparable) value));
			}
			break;
		}
		case GT: {
			if (path instanceof NumberExpression) {
				NumberExpression exp = (NumberExpression) path;
				queryMixin.where(exp.gt((Number) value));
			} else {
				ComparableExpression exp = (ComparableExpression) path;
				queryMixin.where(exp.gt((Comparable) value));
			}
			break;
		}
		case GOE: {
			if (path instanceof NumberExpression) {
				NumberExpression exp = (NumberExpression) path;
				queryMixin.where(exp.goe((Number) value));
			} else {
				ComparableExpression exp = (ComparableExpression) path;
				queryMixin.where(exp.goe((Comparable) value));
			}
			break;
		}
		case BETWEEN: {
			ComparableExpression exp = (ComparableExpression) path;
			if (value instanceof Collection<?>) {
				List<? extends Comparable> list = toList((Collection<Comparable>) value);
				if (list.size() < 2) {
					throw new IllegalArgumentException(
							"Invalid param, the value for between condition must be 2 elements.");
				}
				queryMixin.where(exp.between(list.get(0), list.get(1)));
			} else if (value instanceof Object[]) {
				Object[] bvalue = (Object[]) value;
				if (bvalue.length < 2) {
					throw new IllegalArgumentException(
							"Invalid param, the value for between condition must be 2 elements.");
				}
				queryMixin.where(exp.between((Comparable) bvalue[0], (Comparable) bvalue[1]));
			}
			break;
		}
		case STARTS_WITH: {
			StringExpression exp = (StringExpression) path;
			queryMixin.where(exp.startsWith(String.valueOf(value)));
			break;
		}
		case STARTS_WITH_IC: {
			StringExpression exp = (StringExpression) path;
			queryMixin.where(exp.startsWithIgnoreCase(String.valueOf(value)));
			break;
		}
		case ENDS_WITH: {
			StringExpression exp = (StringExpression) path;
			queryMixin.where(exp.endsWith(String.valueOf(value)));
			break;
		}
		case ENDS_WITH_IC: {
			StringExpression exp = (StringExpression) path;
			queryMixin.where(exp.endsWithIgnoreCase(String.valueOf(value)));
			break;
		}
		case STRING_CONTAINS: {
			StringExpression exp = (StringExpression) path;
			queryMixin.where(exp.contains(String.valueOf(value)));
			break;
		}
		case LIKE: {
			StringExpression exp = (StringExpression) path;
			queryMixin.where(exp.like(String.valueOf(value)));
			break;
		}
		case LIKE_IC: {
			StringExpression exp = (StringExpression) path;
			queryMixin.where(exp.likeIgnoreCase(String.valueOf(value)));
			break;
		}
		case IS_NULL: {
			SimpleExpression exp = (SimpleExpression) path;
			queryMixin.where(exp.isNull());
			break;
		}
		case IS_NOT_NULL: {
			SimpleExpression exp = (SimpleExpression) path;
			queryMixin.where(exp.isNotNull());
			break;
		}
		default:
			throw new UnsupportedOperationException("Ops." + ops + " is not supported on field "
					+ path);
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private boolean hasElements(Object value, int minElement) {
		if (value == null) {
			return false;
		}
		if (value instanceof Collection) {
			return ((Collection) value).size()>=minElement;
		}
		if (value instanceof Object[]) {
			return ((Object[]) value).length >=minElement;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<? extends Comparable> toList(Collection<? extends Comparable> value) {
		if (value instanceof List) {
			return (List<Comparable>) value;
		} else {
			return new ArrayList<>(value);
		}

	}

}
