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

import static com.google.common.collect.Lists.newArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.core.util.ResultSetAdapter;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.AbstractSQLInsertClause;
import com.querydsl.sql.dml.SQLInsertClause;

/**
 * SQLInsertClause defines an INSERT INTO clause If you need to subtype this,
 * use {@link AbstractSQLInsertClause} instead.
 *
 * @author tiwe
 *
 */
public class SQLInsertClauseAlter extends SQLInsertClause {
	
	private final ConfigurationEx configEx;
	
	public SQLInsertClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity) {
		this(connection, new ConfigurationEx(templates), entity);
	}

	public SQLInsertClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity, SQLQuery<?> subQuery) {
		this(connection, new ConfigurationEx(templates), entity, subQuery);
	}

	public SQLInsertClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity, SQLQuery<?> subQuery) {
		super(connection, configuration.get(), entity, subQuery);
		this.configEx=configuration;
	}

	public SQLInsertClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx=configuration;
	}

	public SQLInsertClauseAlter(Provider<Connection> connection, ConfigurationEx configuration, RelationalPath<?> entity, SQLQuery<?> subQuery) {
		super(connection, configuration.get(), entity, subQuery);
		this.configEx=configuration;
	}

	public SQLInsertClauseAlter(Provider<Connection> connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx=configuration;
	}

	/**
	 * Execute the clause and return the generated keys as a ResultSet
	 *
	 * @return result set with generated keys
	 */
	public ResultSet executeWithKeys() {
		context = startContext(connection(), metadata, entity);
		try {
			PreparedStatement stmt = null;
			if (batches.isEmpty()) {
				stmt = createStatement(true);
				listeners.notifyInsert(entity, metadata, columns, values, subQuery);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				int rc = stmt.executeUpdate();
				postExecuted(context, System.currentTimeMillis() - start, "Insert", rc);
			} else if (batchToBulk) {
				stmt = createStatement(true);
				listeners.notifyInserts(entity, metadata, batches);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				int rc = stmt.executeUpdate();
				postExecuted(context, System.currentTimeMillis() - start, "Insert", rc);
			} else {
				Collection<PreparedStatement> stmts = createStatements(true);
				if (stmts != null && stmts.size() > 1) {
					throw new IllegalStateException("executeWithKeys called with batch statement and multiple SQL strings");
				}
				stmt = stmts.iterator().next();
				listeners.notifyInserts(entity, metadata, batches);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				int[] rc = stmt.executeBatch();
				long count = 0;
				for (int r : rc) {
					count = count + r;
				}
				postExecuted(context, System.currentTimeMillis() - start, "BatchInsert", count);
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
		} catch (SQLException e) {
			onException(context, e);
			reset();
			endContext(context);
			throw configuration.translate(queryString, constants, e);
		}
	}

	@Override
	public long execute() {
		context = startContext(connection(), metadata, entity);
		PreparedStatement stmt = null;
		Collection<PreparedStatement> stmts = null;
		try {
			if (batches.isEmpty()) {
				stmt = createStatement(false);
				listeners.notifyInsert(entity, metadata, columns, values, subQuery);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				int rc = stmt.executeUpdate();
				postExecuted(context, System.currentTimeMillis() - start, "Insert", rc);
				return rc;
			} else if (batchToBulk) {
				stmt = createStatement(false);
				listeners.notifyInserts(entity, metadata, batches);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				int rc = stmt.executeUpdate();
				postExecuted(context, System.currentTimeMillis() - start, "Insert", rc);
				return rc;
			} else {
				stmts = createStatements(false);
				listeners.notifyInserts(entity, metadata, batches);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				long rc = executeBatch(stmts);
				postExecuted(context, System.currentTimeMillis() - start, "BatchInsert", rc);
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

	/**
	 * 覆盖父类实现，使用默认的AdvancedMapper，支持primtive类型字段，并可支持注解@UnsavedValue
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SQLInsertClauseAlter populate(Object bean) {
		Map<Path<?>, Object> values = AdvancedMapper.INSTANCE.createMap(entity, bean);
		for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
			set((Path) entry.getKey(), entry.getValue());
		}
		return this;
	}

	protected PreparedStatement createStatement(boolean withKeys) throws SQLException {
		listeners.preRender(context);
		SQLSerializer serializer = createSerializer();
		if (subQueryBuilder != null) {
			subQuery = subQueryBuilder.select(values.toArray(new Expression[values.size()])).clone();
			values.clear();
		}

		if (!batches.isEmpty() && batchToBulk) {
			serializer.serializeInsert(metadata, entity, batches);
		} else {
			serializer.serializeInsert(metadata, entity, columns, values, subQuery);
		}
		SQLBindingsAlter bindings = createBindings(metadata, serializer);
		context.addSQL(bindings);
		listeners.rendered(context);
		return prepareStatementAndSetParameters(serializer, withKeys);
	}

	protected PreparedStatement prepareStatementAndSetParameters(SQLBindingsAlter bindings, List<Object> constants, boolean withKeys) throws SQLException {
		listeners.prePrepare(context);

		this.queryString = bindings.getSQL();
		this.constants = constants;
//        logQuery(logger, queryString, constants);
		PreparedStatement stmt;
		if (withKeys) {
			if (entity.getPrimaryKey() != null) {
				String[] target = new String[entity.getPrimaryKey().getLocalColumns().size()];
				for (int i = 0; i < target.length; i++) {
					Path<?> path = entity.getPrimaryKey().getLocalColumns().get(i);
					String column = ColumnMetadata.getName(path);
					target[i] = column;
				}
				stmt = connection().prepareStatement(queryString, target);
			} else {
				stmt = connection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
			}
		} else {
			stmt = connection().prepareStatement(queryString);
		}
		setParameters(stmt, constants, bindings.getPaths(), metadata.getParams());

		context.addPreparedStatement(stmt);
		listeners.prepared(context);
		return stmt;
	}

	@Override
	protected SQLBindingsAlter createBindings(QueryMetadata metadata, SQLSerializer serializer) {
		String queryString = serializer.toString();
		List<Object> args = newArrayList();
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
