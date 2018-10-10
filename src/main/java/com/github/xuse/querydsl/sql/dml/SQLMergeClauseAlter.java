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
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.github.xuse.querydsl.sql.ContextKeyConstants;
import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.google.common.collect.Maps;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.util.ResultSetAdapter;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLListener;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLNoCloseListener;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.EmptyResultSet;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLMergeBatch;
import com.querydsl.sql.dml.SQLMergeClause;
import com.querydsl.sql.dml.SQLUpdateClause;

/**
 * {@code SQLMergeClause} defines an MERGE INTO clause
 *
 * @author tiwe
 *
 */
public class SQLMergeClauseAlter extends SQLMergeClause {
	public SQLMergeClauseAlter(Connection connection, Configuration configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}

	public SQLMergeClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity) {
		super(connection, templates, entity);
	}

	public SQLMergeClauseAlter(Provider<Connection> connection, Configuration configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
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
					listeners.notifyMerge(entity, metadata, keys, columns, values, subQuery);

					listeners.preExecute(context);
					long start = System.currentTimeMillis();
					int rc = stmt.executeUpdate();
					postExecuted(context, System.currentTimeMillis() - start, "Merge", rc);
				} else {
					Collection<PreparedStatement> stmts = createStatements(true);
					if (stmts != null && stmts.size() > 1) {
						throw new IllegalStateException(
								"executeWithKeys called with batch statement and multiple SQL strings");
					}
					stmt = stmts.iterator().next();
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
					SQLUpdateClause update = new SQLUpdateClause(connection(), configuration, entity);
					update.addListener(listeners);
					populate(update);
					addKeyConditions(update);
					reset();
					endContext(context);
					return EmptyResultSet.DEFAULT;
				} else {
					// insert
					SQLInsertClause insert = new SQLInsertClause(connection(), configuration, entity);
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

	protected PreparedStatement createStatement(boolean withKeys) throws SQLException {
        boolean addBatches = !configuration.getUseLiterals();
        listeners.preRender(context);
        SQLSerializer serializer = createSerializer();
        PreparedStatement stmt = null;
        if (batches.isEmpty()) {
            serializer.serializeMerge(metadata, entity, keys, columns, values, subQuery);
            context.addSQL(createBindings(metadata, serializer));
            listeners.rendered(context);

            listeners.prePrepare(context);
            stmt = prepareStatementAndSetParameter(serializer, withKeys, -1);
            context.addPreparedStatement(stmt);
            listeners.prepared(context);
        } else {
            serializer.serializeMerge(metadata, entity,
                    batches.get(0).getKeys(), batches.get(0).getColumns(),
                    batches.get(0).getValues(), batches.get(0).getSubQuery());
            context.addSQL(createBindings(metadata, serializer));
            listeners.rendered(context);

            stmt = prepareStatementAndSetParameter(serializer, withKeys, 0);

            // add first batch
            if (addBatches) {
                stmt.addBatch();
            }

            // add other batches
            for (int i = 1; i < batches.size(); i++) {
                SQLMergeBatch batch = batches.get(i);
                listeners.preRender(context);
                serializer = createSerializer();
                serializer.serializeMerge(metadata, entity, batch.getKeys(), batch.getColumns(), batch.getValues(), batch.getSubQuery());
                context.addSQL(createBindings(metadata, serializer));
                listeners.rendered(context);

                setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
                prepared(context, serializer.getConstants(), serializer.getConstantPaths(), i);
                if (addBatches) {
                    stmt.addBatch();
                }
            }
            preparedFinish(context, batches.size());
        }
        return stmt;
    }

	protected Collection<PreparedStatement> createStatements(boolean withKeys) throws SQLException {
		boolean addBatches = !configuration.getUseLiterals();
		Map<String, PreparedStatement> stmts = Maps.newHashMap();

		// add first batch
		listeners.preRender(context);
		SQLSerializer serializer = createSerializer();
		serializer.serializeMerge(metadata, entity, batches.get(0).getKeys(), batches.get(0).getColumns(),
				batches.get(0).getValues(), batches.get(0).getSubQuery());
		context.addSQL(createBindings(metadata, serializer));
		listeners.rendered(context);

		PreparedStatement stmt = prepareStatementAndSetParameters(serializer, withKeys);
		stmts.put(serializer.toString(), stmt);
		if (addBatches) {
			stmt.addBatch();
		}

		// add other batches
		for (int i = 1; i < batches.size(); i++) {
			SQLMergeBatch batch = batches.get(i);
			serializer = createSerializer();
			serializer.serializeMerge(metadata, entity, batch.getKeys(), batch.getColumns(), batch.getValues(),
					batch.getSubQuery());
			stmt = stmts.get(serializer.toString());
			if (stmt == null) {
				stmt = prepareStatementAndSetParameters(serializer, withKeys);
				stmts.put(serializer.toString(), stmt);
			} else {
				setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
			}
			if (addBatches) {
				stmt.addBatch();
			}
		}

		return stmts.values();
	}

	protected PreparedStatement prepareStatementAndSetParameter(SQLSerializer serializer, boolean withKeys, int index)
			throws SQLException {
		listeners.prePrepare(context);

		queryString = serializer.toString();
		constants = serializer.getConstants();
		logQuery(logger, queryString, constants);
		PreparedStatement stmt;
		if (withKeys) {
			String[] target = new String[keys.size()];
			for (int i = 0; i < target.length; i++) {
				target[i] = ColumnMetadata.getName(getKeys().get(i));
			}
			stmt = connection().prepareStatement(queryString, target);
		} else {
			stmt = connection().prepareStatement(queryString);
		}
		setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
		context.addPreparedStatement(stmt);
		prepared(context, serializer.getConstants(), serializer.getConstantPaths(), index);
		return stmt;
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
				postExecuted(context, System.currentTimeMillis() - start, "Merge", rc);
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
		SQLQuery<?> query = new SQLQueryAlter<Void>(connection(), configuration).from(entity);
		for (SQLListener listener : listeners.getListeners()) {
			query.addListener(listener);
		}
		query.addListener(SQLNoCloseListener.DEFAULT);
		addKeyConditions(query);
		return query.select(Expressions.ONE).fetchFirst() != null;
	}

	protected long executeCompositeMerge() {
		if (hasRow()) {
			// update
			SQLUpdateClause update = new SQLUpdateClause(connection(), configuration, entity);
			populate(update);
			addListeners(update);
			addKeyConditions(update);
			return update.execute();
		} else {
			// insert
			SQLInsertClause insert = new SQLInsertClause(connection(), configuration, entity);
			addListeners(insert);
			populate(insert);
			return insert.execute();

		}
	}

	/**
	 * first -1唯一 0初始 n后续
	 * 
	 * @param context
	 * @param objects
	 * @param constantPaths
	 * @param first
	 */
	private void prepared(SQLListenerContextImpl context, List<?> objects, List<Path<?>> constantPaths, int first) {
		switch (first) {
		case -1:
			context.setData(ContextKeyConstants.SIGLE_PARAMS, objects);
			context.setData(ContextKeyConstants.PARAMS_PATH, constantPaths);
			listeners.prepared(context);
			return;
		case 0: {
			context.setData(ContextKeyConstants.PARAMS_PATH, constantPaths);
			List<List<?>> mlist = new ArrayList<>();
			mlist.add(objects);
			context.setData(ContextKeyConstants.BATCH_PARAMS, mlist);
			return;
		}
		default: 
			if (first > ContextKeyConstants.MAX_BATCH_LOG) {
				return;
			}
			@SuppressWarnings("unchecked")
			List<List<?>> mlist = (List<List<?>>) context.getData(ContextKeyConstants.BATCH_PARAMS);
			mlist.add(objects);
		}
	}

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, long count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
		listeners.executed(context);
	}

	private void preparedFinish(SQLListenerContextImpl context, int maxSize) {
		context.setData(ContextKeyConstants.BATCH_SIZE, maxSize);
		listeners.prepared(context);
	}
}
