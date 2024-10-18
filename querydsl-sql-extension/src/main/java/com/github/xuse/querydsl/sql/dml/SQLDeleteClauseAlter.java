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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.Mappers;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.dml.AbstractSQLDeleteClause;
import com.querydsl.sql.dml.Mapper;

public class SQLDeleteClauseAlter extends AbstractSQLDeleteClause<SQLDeleteClauseAlter> {

	private final ConfigurationEx configEx;

	private RoutingStrategy routing;

	SQLDeleteClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx = configuration;
	}

	public SQLDeleteClauseAlter(Supplier<Connection> connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx = configuration;
	}

	private Integer queryTimeout;

	/**
	 * 设置查询超时（秒）
	 *
	 * @param queryTimeout queryTimeout
	 * @return SQLDeleteClauseAlter
	 */
	public SQLDeleteClauseAlter setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
		return this;
	}

	@Override
	public long execute() {
		context = startContext(connection(), metadata, entity);
		PreparedStatement stmt = null;
		Collection<PreparedStatement> stmts = null;
		try {
			if (batches.isEmpty()) {
				stmt = createStatement();
				if (queryTimeout != null) {
					stmt.setQueryTimeout(queryTimeout);
				} else if (configEx.getDefaultQueryTimeout() > 0) {
					stmt.setQueryTimeout(configEx.getDefaultQueryTimeout());
				}
				listeners.notifyDelete(entity, metadata);
				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				int rc = stmt.executeUpdate();
				postExecuted(context, System.currentTimeMillis() - start, "Deleted", rc);
				return rc;
			} else {
				stmts = createStatements();
				if (queryTimeout != null) {
					for (PreparedStatement st : stmts) {
						st.setQueryTimeout(queryTimeout);
					}
				} else if (configEx.getDefaultQueryTimeout() > 0) {
					for (PreparedStatement st : stmts) {
						st.setQueryTimeout(configEx.getDefaultQueryTimeout());
					}
				}
				listeners.notifyDeletes(entity, batches);
				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				long rc = executeBatch(stmts);
				postExecuted(context, System.currentTimeMillis() - start, "BatchDeleted", rc);
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

	protected PreparedStatement createStatement() throws SQLException {
		listeners.preRender(context);
		SQLSerializerAlter serializer = createSerializer();
		serializer.serializeDelete(metadata, entity);
		serializer.setRouting(routing);
		SQLBindings bindings = createBindings(metadata, serializer);
		context.addSQL(bindings);
		queryString = bindings.getSQL();
		constants = serializer.getConstants();
		// logQuery(logger, queryString, constants);
		listeners.rendered(context);
		listeners.prePrepare(context);
		PreparedStatement stmt = connection().prepareStatement(queryString);
		setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
		context.addPreparedStatement(stmt);
		listeners.prepared(context);
		return stmt;
	}

	@Override
	protected SQLBindings createBindings(QueryMetadata metadata, SQLSerializer serializer) {
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

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, long count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
		if (this.configEx.getSlowSqlWarnMillis() <= cost) {
			context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
		}
		listeners.executed(context);
	}

	public SQLDeleteClauseAlter withRouting(RoutingStrategy routing) {
		this.routing = routing;
		return this;
	}

	public SQLDeleteClauseAlter populatePrimaryKey(Object bean) {
		boolean tuple = (bean instanceof Tuple);
		populatePrimaryKey0(bean, Mappers.getNormal(tuple));
		return this;
	}

	/*
	 * 父类方法不支持routing参数。
	 */
	@Override
	protected SQLSerializerAlter createSerializer() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configEx, true);
		serializer.setUseLiterals(useLiterals);
		serializer.setRouting(routing);
		return serializer;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populatePrimaryKey0(Object bean, Mapper mapper) {
		Collection<? extends Path<?>> primaryKeyColumns = entity.getPrimaryKey() != null ? entity.getPrimaryKey().getLocalColumns() : Collections.emptyList();
		if (primaryKeyColumns.isEmpty()) {
			return;
		}
		Map<Path<?>, Object> values = mapper.createMap(entity, bean);
		int pkConditionFilled = 0;
		for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
			if (primaryKeyColumns.contains(entry.getKey())) {
				// fill PK as where conditions.
				SimpleExpression<?> column = (SimpleExpression<?>) entry.getKey();
				where(column.eq(ConstantImpl.create(entry.getValue())));
				pkConditionFilled++;
			}
		}
		int totalPK = primaryKeyColumns.size();
		// 如果主键条件不完整，可能导致update范围扩大甚至全表更新。为避免出现这种危险，检查主键条件的完整性。
		if (pkConditionFilled < totalPK) {
			throw Exceptions.illegalArgument("There is null value on some primary key columns. ({}/{}) entity:{},where:{}", pkConditionFilled, totalPK, entity.getClass().getName(), metadata.getWhere());
		}
	}
}
