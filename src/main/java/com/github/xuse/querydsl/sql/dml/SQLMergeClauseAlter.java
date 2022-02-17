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
package com.github.xuse.querydsl.sql.dml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.querydsl.core.FilteredClause;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.util.ResultSetAdapter;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLListener;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLNoCloseListener;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.dml.EmptyResultSet;
import com.querydsl.sql.dml.SQLMergeClause;

/**
 * {@code SQLMergeClause} defines an MERGE INTO clause
 *
 * @author tiwe
 *
 */
public class SQLMergeClauseAlter extends SQLMergeClause {
	private final ConfigurationEx configEx;
	
	public SQLMergeClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx=configuration;
	}

	public SQLMergeClauseAlter(Supplier<Connection> connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx=configuration;
	}

	private Integer queryTimeout;

	/**
	 * 设置查询超时（秒）
	 * 
	 * @param queryTimeout
	 */
	public SQLMergeClauseAlter setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
		return this;
	}

	/**
	 * Execute the clause and return the generated keys as a ResultSet
	 *
	 * @return result set with generated keys
	 */
	public ResultSet executeWithKeys() {
		context = startContext(connection(), metadata, entity);
		try {
			if (configuration.getTemplates().isNativeMerge()) {
				PreparedStatement stmt = null;
				if (batches.isEmpty()) {
					stmt = createStatement(true);
					if (queryTimeout != null) {
						stmt.setQueryTimeout(queryTimeout);
					}else if(configEx.getDefaultQueryTimeout()>0){
						stmt.setQueryTimeout(configEx.getDefaultQueryTimeout());
					}
					listeners.notifyMerge(entity, metadata, keys, columns, values, subQuery);

					listeners.preExecute(context);
					long start = System.currentTimeMillis();
					int rc = stmt.executeUpdate();
					postExecuted(context, System.currentTimeMillis() - start, "Merged", rc);
				} else {
					Collection<PreparedStatement> stmts = createStatements(true);
					if (stmts != null && stmts.size() > 1) {
						throw new IllegalStateException(
								"executeWithKeys called with batch statement and multiple SQL strings");
					}
					stmt = stmts.iterator().next();
					if (queryTimeout != null) {
						stmt.setQueryTimeout(queryTimeout);
					}else if(configEx.getDefaultQueryTimeout()>0){
						stmt.setQueryTimeout(configEx.getDefaultQueryTimeout());
					}
					listeners.notifyMerges(entity, metadata, batches);

					listeners.preExecute(context);
					long start = System.currentTimeMillis();
					long rc = executeBatch(stmt);
					postExecuted(context, System.currentTimeMillis() - start, "BatchMerge", rc);
				}

				final Statement stmt2 = stmt;
				ResultSet rs = stmt.getGeneratedKeys();
				return new ResultSetAdapter(rs) {
					@Override
					public void close() throws SQLException {
						try {
							super.close();
						} finally {
							stmt2.close();
							reset();
							endContext(context);
						}
					}
				};
			} else {
				if (hasRow()) {
					// update
					SQLUpdateClauseAlter update = new SQLUpdateClauseAlter(connection(), configEx, entity);
					if (queryTimeout != null) {
						update.setQueryTimeout(queryTimeout);
					}else if(configEx.getDefaultQueryTimeout()>0){
						update.setQueryTimeout(configEx.getDefaultQueryTimeout());
					}
					update.addListener(listeners);
					//必须在populate之前执行
					addKeyConditions(update, true);
					populate(update);
					// 源代码中没有update.execute()，属于BUG，此处进行了修复。
					update.execute();
					reset();
					endContext(context);
					return EmptyResultSet.DEFAULT;
				} else {
					// insert
					SQLInsertClauseAlter insert = new SQLInsertClauseAlter(connection(), configEx, entity);
					insert.addListener(listeners);
					populate(insert);
					return insert.executeWithKeys();
				}
			}
		} catch (SQLException e) {
			onException(context, e);
			reset();
			endContext(context);
			throw configuration.translate(queryString, constants, e);
		}
	}
	
    protected SQLSerializer createSerializer() {
        SQLSerializer serializer = new SQLSerializerAlter(configuration, true);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

	private long executeBatch(PreparedStatement stmt) throws SQLException {
		int[] rcs = stmt.executeBatch();
		long count = 0;
		for (int r : rcs) {
			count = count + r;
		}
		return count;
	}

	protected long executeNativeMerge() {
		context = startContext(connection(), metadata, entity);
		PreparedStatement stmt = null;
		Collection<PreparedStatement> stmts = null;
		try {
			if (batches.isEmpty()) {
				stmt = createStatement(false);
				listeners.notifyMerge(entity, metadata, keys, columns, values, subQuery);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				int rc = stmt.executeUpdate();
				postExecuted(context, System.currentTimeMillis() - start, "Merged", rc);
				return rc;
			} else {
				stmts = createStatements(false);
				listeners.notifyMerges(entity, metadata, batches);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				long rc = executeBatch(stmts);
				postExecuted(context, System.currentTimeMillis() - start, "Merge", rc);
				return rc;
			}
		} catch (SQLException e) {
			onException(context, e);
			throw configuration.translate(queryString, constants, e);
		} finally {
			if (stmt != null) {
				close(stmt);
			}
			if (stmts != null) {
				close(stmts);
			}
			reset();
			endContext(context);
		}
	}

	protected boolean hasRow() {
		SQLQueryAlter<?> query = new SQLQueryAlter<Void>(connection(), configEx).from(entity);
		for (SQLListener listener : listeners.getListeners()) {
			query.addListener(listener);
		}
		query.addListener(SQLNoCloseListener.DEFAULT);
		addKeyConditions(query, false);
		return query.select(Expressions.ONE).fetchFirst() != null;
	}

	protected long executeCompositeMerge() {
		if (hasRow()) {
			// update
			SQLUpdateClauseAlter update = new SQLUpdateClauseAlter(connection(), configEx, entity);
			//必须在populator之前执行
			addKeyConditions(update, true);
			populate(update);
			addListeners(update);
			return update.execute();
		} else {
			// insert
			SQLInsertClauseAlter insert = new SQLInsertClauseAlter(connection(), configEx, entity);
			addListeners(insert);
			populate(insert);
			return insert.execute();

		}
	}

	@Override
	protected SQLBindingsAlter createBindings(QueryMetadata metadata, SQLSerializer serializer) {
		String queryString = serializer.toString();
		List<Object> args = new ArrayList<>();
		Map<ParamExpression<?>, Object> params = metadata.getParams();
		for (Object o : serializer.getConstants()) {
			if (o instanceof ParamExpression) {
				if (!params.containsKey(o)) {
					throw new ParamNotSetException((ParamExpression<?>) o);
				}
				o = metadata.getParams().get(o);
			}
			args.add(o);
		}
		return new SQLBindingsAlter(queryString, args, serializer.getConstantPaths());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void addKeyConditions(FilteredClause<?> query, boolean removeFromUpdate) {
		List<? extends Path<?>> keys = getKeys();

		Iterator<Path<?>> columnIterator = columns.iterator();
		Iterator<Expression<?>> valueIterator = values.iterator();
		int count = 0;
		while (columnIterator.hasNext()) {
			Path<?> column = columnIterator.next();
			Expression<?> value = valueIterator.next();
			if (keys.contains(column)) {
				count++;
				if (value instanceof NullExpression) {
					query.where(ExpressionUtils.isNull(column));
				} else {
					query.where(ExpressionUtils.eq(column, (Expression) value));
				}
				if (removeFromUpdate) {
					columnIterator.remove();
					valueIterator.remove();
				}
			}
		}
		if (count < keys.size()) {
			throw new IllegalStateException("Missed value for keys " + keys);
		}
	}

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, long count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
		if(this.configEx.getSlowSqlWarnMillis()<=cost) {
			context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
		}
		listeners.executed(context);
	}
}
