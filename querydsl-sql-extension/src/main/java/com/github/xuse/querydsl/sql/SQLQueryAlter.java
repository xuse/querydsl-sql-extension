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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.expression.Projection;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.util.Holder;
import com.mysema.commons.lang.CloseableIterator;
import com.mysema.commons.lang.Pair;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.QueryModifiers;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
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
import com.querydsl.sql.StatementOptions;

import lombok.extern.slf4j.Slf4j;

/**
 * {@code SQLQuery} is a JDBC based implementation of the {@link SQLCommonQuery}
 * interface
 *
 * @param <T> type of result.
 * @author tiwe
 */
@Slf4j
public class SQLQueryAlter<T> extends AbstractSQLQuery<T, SQLQueryAlter<T>> {

	private static final long serialVersionUID = -3451422354253107107L;

	private static final QueryFlag rowCountFlag = new QueryFlag(QueryFlag.Position.AFTER_PROJECTION,
			", count(*) over() ");

	private final ConfigurationEx configEx;

	private Supplier<Connection> connProvider;

	private Connection conn;

	// /////////// 覆盖检查字段结束/////////////
	private boolean exceedSizeLog;

	private RoutingStrategy routing;

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
		SQLListenerContext parentContext = startContext(connection(), queryMixin.getMetadata());
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
				long total = fetchCount();
				if (total > 0) {
					return new QueryResults<T>(fetch(), originalModifiers, total);
				} else {
					return QueryResults.emptyResults();
				}
			}
		} finally {
			endContext(parentContext);
		}
	}

	/**
	 * @deprecated use {@link #fetchResults()} instead.
	 * @return Pair of Integer,List&lt;T&gt;
	 */
	public Pair<Integer, List<T>> fetchAndCount() {
		int count = (int) fetchCount();
		if (count == 0) {
			return Pair.of(count, Collections.emptyList());
		}
		return Pair.of(count, fetch());
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
		SQLSerializerAlter serializer = new SQLSerializerAlter(configEx, false);
		serializer.setUseLiterals(useLiterals);
		serializer.setRouting(routing);
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

	/*
	 * 原版是通过三个代码分支在fetch方法中直接走三段不同的代码逻辑的，本框架修改时将其提取为JdbcProjection对象。
	 */
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
			int argSize = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				if (getLastCell) {
					lastCell = rs.getObject(argSize);
					getLastCell = false;
				}
				result.add(fetch(rs));
			}
			return result;
		}

		protected abstract RT fetch(ResultSet rs) throws SQLException;
	}

	/*
	 * 标准返回值Projection，根据FactoryExpression的转换规则返回
	 */
	final class FactoryExpressionResult<RT> extends AbstractProjection<RT> {
		private final FactoryExpression<RT> expr;
		private final int argSize;
		private final Path<?>[] argPath;
		private final Class<?>[] argTypes;

		FactoryExpressionResult(FactoryExpression<RT> factoryExpr) {
			List<Expression<?>> args = factoryExpr.getArgs();
			int argSize = args.size();
			this.expr = factoryExpr;
			this.argSize = argSize;
			this.argPath = new Path<?>[argSize];
			this.argTypes = new Class<?>[argSize];
			for (int i = 0; i < argSize; i++) {
				Expression<?> expr = args.get(i);
				argPath[i] = (expr instanceof Path ? (Path<?>) expr : null);
				argTypes[i] = expr.getType();
			}
		}

		protected final RT fetch(ResultSet rs) throws SQLException {
			int argSize = this.argSize;
			Configuration configuration = SQLQueryAlter.this.configuration;
			Object[] args = new Object[argSize];
			for (int i = 0; i < argSize; i++) {
				try {
					args[i] = configuration.get(rs, argPath[i], i + 1, argTypes[i]);
				} catch (SQLException ex) {
					throw ex;
				} catch (Exception ex) {
					throw new SQLException("get field:" + argPath[i] + "error", ex);
				}
			}
			return expr.newInstance(args);
		}
	}

	/*
	 * 以Object[]形式返回所有列
	 */
	final class WildcardAllResult<RT> extends AbstractProjection<RT> {
		private final int columnSize;

		public WildcardAllResult(int columnSize) {
			super();
			this.columnSize = columnSize;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected final RT fetch(ResultSet rs) throws SQLException {
			int size = this.columnSize;
			Object[] row = new Object[size];
			for (int i = 0; i < size; i++) {
				row[i] = rs.getObject(i + 1);
			}
			return (RT) row;
		}
	}

	/*
	 * 仅获取结果集第一列的Path数据类型
	 */
	final class SingleValueResult<RT> extends AbstractProjection<RT> {
		private final Expression<RT> expr;
		private final Path<?> path;

		public SingleValueResult(Expression<RT> expr) {
			this.expr = expr;
			this.path = expr instanceof Path ? (Path<?>) expr : null;
		}

		@Override
		protected final RT fetch(ResultSet rs) throws SQLException {
			return configuration.get(rs, path, 1, expr.getType());
		}
	}

	/*
	 * 仅获取结果集第一列的原始数据类型
	 */
	final class DefaultValueResult<RT> extends AbstractProjection<RT> {
		@SuppressWarnings("unchecked")
		@Override
		protected final RT fetch(ResultSet rs) throws SQLException {
			return (RT) rs.getObject(1);
		}
	}

	private StatementOptions statementOptions = StatementOptions.DEFAULT;

	/**
	 * 设置本次查询载入的最大行数
	 *
	 * @param maxRows maxRows
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
	 * @param maxRows maxRows
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
	 * @param fetchSize fetchSize
	 * @return this
	 */
	public SQLQueryAlter<T> setFetchSize(int fetchSize) {
		StatementOptions options = this.statementOptions;
		setStatementOptions(new StatementOptions(options.getMaxFieldSize(), options.getMaxRows(),
				options.getQueryTimeout(), fetchSize));
		return this;
	}

	/**
	 * 设置查询超时（秒）
	 *
	 * @param queryTimeout queryTimeout
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

	public SQLBindings getSQL(boolean forCount) {
		return getSQL(serialize(forCount));
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
		// This is the new type
		@SuppressWarnings("unchecked")
		SQLQueryAlter<U> newType = (SQLQueryAlter<U>) this;
		return newType;
	}

	@Override
	public SQLQueryAlter<Tuple> select(Expression<?>... exprs) {
		queryMixin.setProjection(exprs);
		// This is the new type
		@SuppressWarnings("unchecked")
		SQLQueryAlter<Tuple> newType = (SQLQueryAlter<Tuple>) this;
		return newType;
	}

	/**
	 * 设置Limit，如果传入值为null或零或负数，则设置无效
	 *
	 * @param limit limit
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
	 * @param offset offset
	 * @return this
	 */
	public final SQLQueryAlter<T> offsetIf(Integer offset) {
		if (offset == null || offset < 0) {
			return queryMixin.getSelf();
		}
		return queryMixin.offset(offset);
	}

	/**
	 * 设置查询路由策略并返回当前实例，支持链式调用。
	 * <p>
	 * Sets the query routing strategy and returns the current instance to support
	 * method chaining.
	 * 
	 * @param routing 要应用的路由策略。
	 *                <p>
	 *                The routing strategy to apply.
	 * @return 当前SQL查询修改器实例。
	 *         <p>
	 *         The current SQL query alter instance.
	 */
	public SQLQueryAlter<T> withRouting(RoutingStrategy routing) {
		this.routing = routing;
		return this;
	}
}
