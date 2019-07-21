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

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
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
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.StatementOptions;

/**
 * {@code SQLQuery} is a JDBC based implementation of the {@link SQLCommonQuery}
 * interface
 *
 * @param <T>
 * @author tiwe
 */
public class SQLQueryAlter<T> extends AbstractSQLQuery<T, SQLQueryAlter<T>> {
	private static final long serialVersionUID = -3451422354253107107L;
	private static final Logger logger = LoggerFactory.getLogger(SQLQueryAlter.class);
	private static final QueryFlag rowCountFlag = new QueryFlag(QueryFlag.Position.AFTER_PROJECTION, ", count(*) over() ");

	////////////// 覆盖检查字段开始 <p>////////////
	/*
	 * 以下是来自父类AbstractSQLQuery的私有字段，这里相当于克隆了一份，因此是不安全的。 确保安全要求——
	 * 检查所有引用该字段的位置，其位置数量应当和父类AbstractSQLQuery完全一致。
	 * 相当于完全拦截了父类中字段的使用，使得父类的同名字段作废。使用子类的字段。 这一点也不优雅！ 2018-10-09 v4.2.1版本检查通过
	 */
	private boolean getLastCell;

	private Object lastCell;

	private SQLListenerContext parentContext;

	@Nullable
	private Provider<Connection> connProvider;

	@Nullable
	private Connection conn;
	///////////// 覆盖检查字段结束/////////////

	public SQLQueryAlter() {
		super((Connection) null, new Configuration(SQLTemplates.DEFAULT), new DefaultQueryMetadata());
	}

	public SQLQueryAlter(Connection conn, Configuration configuration, QueryMetadata metadata) {
		super(conn, configuration, metadata);
		this.conn = conn;
	}

	public SQLQueryAlter(Connection conn, Configuration configuration) {
		super(conn, configuration);
		this.conn = conn;
	}

	public SQLQueryAlter(Connection conn, SQLTemplates templates, QueryMetadata metadata) {
		super(conn, new Configuration(templates), metadata);
		this.conn = conn;
	}

	public SQLQueryAlter(Connection conn, SQLTemplates templates) {
		super(conn, new Configuration(templates));
		this.conn = conn;
	}

	public SQLQueryAlter(Provider<Connection> connProvider, Configuration configuration, QueryMetadata metadata) {
		super(connProvider, configuration, metadata);
		this.connProvider = connProvider;
	}

	public SQLQueryAlter(Provider<Connection> connProvider, Configuration configuration) {
		super(connProvider, configuration, new DefaultQueryMetadata());
		this.connProvider = connProvider;
	}

	@SuppressWarnings("unchecked")
	@Override
	public QueryResults<T> fetchResults() {
		parentContext = startContext(connection(), queryMixin.getMetadata());
		Expression<T> expr = (Expression<T>) queryMixin.getMetadata().getProjection();
		QueryModifiers originalModifiers = queryMixin.getMetadata().getModifiers();
		try {
			if (configuration.getTemplates().isCountViaAnalytics() && queryMixin.getMetadata().getGroupBy().isEmpty()) {
				List<T> results;
				try {
					queryMixin.addFlag(rowCountFlag);
					getLastCell = true;
					results = fetch();
				} finally {
					queryMixin.removeFlag(rowCountFlag);
				}
				long total;
				if (!results.isEmpty()) {
					if (lastCell instanceof Number) {
						total = ((Number) lastCell).longValue();
					} else {
						throw new IllegalStateException("Unsupported lastCell instance " + lastCell);
					}
				} else {
					total = fetchCount();
				}
				return new QueryResults<T>(results, originalModifiers, total);

			} else {
				queryMixin.setProjection(expr);
				long total = fetchCount();
				if (total > 0) {
					return new QueryResults<T>(fetch(), originalModifiers, total);
				} else {
					return QueryResults.emptyResults();
				}
			}
		} finally {
			endContext(parentContext);
			reset();
			getLastCell = false;
			parentContext = null;
		}
	}

	@Override
	public long fetchCount() {
		try {
			return unsafeCount();
		} catch (SQLException e) {
			String error = "Caught " + e.getClass().getName();
			logger.error(error, e);
			throw configuration.translate(e);
		}
	}

	private long unsafeCount() throws SQLException {
		SQLListenerContextImpl context = startContext(connection(), getMetadata());
		List<Object> constants = ImmutableList.of();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String queryString = null;
		try {
			listeners.preRender(context);
			SQLSerializer serializer = serialize(true);
			SQLBindings sql = getSQL(serializer);
			queryString = sql.getSQL();
			logQuery(queryString, serializer.getConstants());
			context.addSQL(sql);
			listeners.rendered(context);

			constants = serializer.getConstants();
			listeners.prePrepare(context);

			stmt = getPreparedStatement(queryString);
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
			cleanupMDC();
		}
	}

	private Connection connection() {
		if (conn == null) {
			if (connProvider != null) {
				conn = connProvider.get();
			} else {
				throw new IllegalStateException("No connection provided");
			}
		}
		return conn;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> fetch() {
		Expression<T> expr = (Expression<T>) queryMixin.getMetadata().getProjection();
		SQLListenerContextImpl context = startContext(connection(), queryMixin.getMetadata());
		String queryString = null;
		List<Object> constants = ImmutableList.of();

		try {
			listeners.preRender(context);
			SQLSerializer serializer = serialize(false);
			SQLBindings sql = getSQL(serializer);
			queryString = sql.getSQL();
			logQuery(queryString, serializer.getConstants());
			context.addSQL(sql);
			listeners.rendered(context);

			listeners.notifyQuery(queryMixin.getMetadata());
			constants = serializer.getConstants();

			listeners.prePrepare(context);
			final PreparedStatement stmt = getPreparedStatement(queryString);
			try {
				setParameters(stmt, constants, serializer.getConstantPaths(), queryMixin.getMetadata().getParams());
				context.addPreparedStatement(stmt);
				listeners.prepared(context);

				listeners.preExecute(context);
				long timeesp = System.currentTimeMillis();
				final ResultSet rs = stmt.executeQuery();
				timeesp = System.currentTimeMillis() - timeesp;
				try {
					lastCell = null;
					final List<T> result = new ArrayList<T>();
					if (expr instanceof FactoryExpression) {
						FactoryExpression<T> fe = (FactoryExpression<T>) expr;
						while (rs.next()) {
							if (getLastCell) {
								lastCell = rs.getObject(fe.getArgs().size() + 1);
								getLastCell = false;
							}
							result.add(newInstance(fe, rs, 0));
						}
					} else if (expr.equals(Wildcard.all)) {
						int columnSize = rs.getMetaData().getColumnCount();
						while (rs.next()) {
							Object[] row = new Object[columnSize];
							if (getLastCell) {
								lastCell = rs.getObject(row.length);
								getLastCell = false;
							}
							for (int i = 0; i < row.length; i++) {
								row[i] = rs.getObject(i + 1);
							}
							result.add((T) row);
						}
					} else {
						while (rs.next()) {
							if (getLastCell) {
								lastCell = rs.getObject(2);
								getLastCell = false;
							}
							result.add(get(rs, expr, 1, expr.getType()));
						}
					}
					postExecuted(context, timeesp, "Fetch", result.size());
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
				} finally {
					rs.close();
				}
			} finally {
				stmt.close();
			}
		} catch (SQLException e) {
			onException(context, e);
			throw configuration.translate(queryString, constants, e);
		} finally {
			endContext(context);
			reset();
		}
	}

	private <RT> RT newInstance(FactoryExpression<RT> c, ResultSet rs, int offset)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, SQLException {
		Object[] args = new Object[c.getArgs().size()];
		for (int i = 0; i < args.length; i++) {
			args[i] = get(rs, c.getArgs().get(i), offset + i + 1, c.getArgs().get(i).getType());
		}
		return c.newInstance(args);
	}

	@Nullable
	private <U> U get(ResultSet rs, Expression<?> expr, int i, Class<U> type) throws SQLException {
		return configuration.get(rs, expr instanceof Path ? (Path<?>) expr : null, i, type);
	}

	private void reset() {
		cleanupMDC();
	}

	private StatementOptions statementOptions = StatementOptions.DEFAULT;

	public void setStatementOptions(StatementOptions statementOptions) {
		super.setStatementOptions(statementOptions);
		this.statementOptions = statementOptions;
	}

	private PreparedStatement getPreparedStatement(String queryString) throws SQLException {
		PreparedStatement statement = connection().prepareStatement(queryString);
		if (statementOptions.getFetchSize() != null) {
			statement.setFetchSize(statementOptions.getFetchSize());
		}
		if (statementOptions.getMaxFieldSize() != null) {
			statement.setMaxFieldSize(statementOptions.getMaxFieldSize());
		}
		if (statementOptions.getQueryTimeout() != null) {
			statement.setQueryTimeout(statementOptions.getQueryTimeout());
		}
		if (statementOptions.getMaxRows() != null) {
			statement.setMaxRows(statementOptions.getMaxRows());
		}
		return statement;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CloseableIterator<T> iterate() {
		Expression<T> expr = (Expression<T>) queryMixin.getMetadata().getProjection();
		return iterateSingle(queryMixin.getMetadata(), expr);
	}

	@SuppressWarnings("unchecked")
	private CloseableIterator<T> iterateSingle(QueryMetadata metadata, @Nullable final Expression<T> expr) {
		SQLListenerContextImpl context = startContext(connection(), queryMixin.getMetadata());
		String queryString = null;
		List<Object> constants = ImmutableList.of();

		try {
			listeners.preRender(context);
			SQLSerializer serializer = serialize(false);
			SQLBindings sql = getSQL(serializer);
			queryString = sql.getSQL();
			logQuery(queryString, serializer.getConstants());
			context.addSQL(sql);
			listeners.rendered(context);

			listeners.notifyQuery(queryMixin.getMetadata());
			constants = serializer.getConstants();

			listeners.prePrepare(context);
			final PreparedStatement stmt = getPreparedStatement(queryString);
			setParameters(stmt, constants, serializer.getConstantPaths(), metadata.getParams());
			context.addPreparedStatement(stmt);
			listeners.prepared(context);

			listeners.preExecute(context);
			long start = System.currentTimeMillis();
			final ResultSet rs = stmt.executeQuery();
			postExecuted(context, System.currentTimeMillis() - start, "Iterated", 1);
			if (expr == null) {
				return new SQLResultIterator<T>(configuration, stmt, rs, listeners, context) {
					@Override
					public T produceNext(ResultSet rs) throws Exception {
						return (T) rs.getObject(1);
					}
				};
			} else if (expr instanceof FactoryExpression) {
				return new SQLResultIterator<T>(configuration, stmt, rs, listeners, context) {
					@Override
					public T produceNext(ResultSet rs) throws Exception {
						return newInstance((FactoryExpression<T>) expr, rs, 0);
					}
				};
			} else if (expr.equals(Wildcard.all)) {
				return new SQLResultIterator<T>(configuration, stmt, rs, listeners, context) {
					@Override
					public T produceNext(ResultSet rs) throws Exception {
						Object[] rv = new Object[rs.getMetaData().getColumnCount()];
						for (int i = 0; i < rv.length; i++) {
							rv[i] = rs.getObject(i + 1);
						}
						return (T) rv;
					}
				};
			} else {
				return new SQLResultIterator<T>(configuration, stmt, rs, listeners, context) {
					@Override
					public T produceNext(ResultSet rs) throws Exception {
						return get(rs, expr, 1, expr.getType());
					}
				};
			}

		} catch (SQLException e) {
			onException(context, e);
			endContext(context);
			throw configuration.translate(queryString, constants, e);
		} catch (RuntimeException e) {
			logger.error("Caught " + e.getClass().getName() + " for " + queryString);
			throw e;
		} finally {
			reset();
		}
	}

	/**
	 * Get the results as a JDBC ResultSet
	 *
	 * @return results as ResultSet
	 */
	public ResultSet getResults() {
		final SQLListenerContextImpl context = startContext(connection(), queryMixin.getMetadata());
		String queryString = null;
		List<Object> constants = ImmutableList.of();

		try {
			listeners.preRender(context);
			SQLSerializer serializer = serialize(false);
			SQLBindings sql = getSQL(serializer);
			queryString = sql.getSQL();
			logQuery(queryString, serializer.getConstants());
			context.addSQL(sql);
			listeners.rendered(context);

			listeners.notifyQuery(queryMixin.getMetadata());

			constants = serializer.getConstants();

			listeners.prePrepare(context);
			final PreparedStatement stmt = getPreparedStatement(queryString);
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
						reset();
						endContext(context);
					}
				}
			};
		} catch (SQLException e) {
			onException(context, e);
			reset();
			endContext(context);
			throw configuration.translate(queryString, constants, e);
		}
	}

	protected SQLBindings getSQL(SQLSerializer serializer) {
		List<Object> args = newArrayList();
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
		SQLQueryAlter<T> q = new SQLQueryAlter<T>(conn, getConfiguration(), getMetadata().clone());
		q.clone(this);
		return q;
	}

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, int count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
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
}
