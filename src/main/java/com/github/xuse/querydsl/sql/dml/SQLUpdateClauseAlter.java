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
import com.github.xuse.querydsl.sql.IRelationPathEx;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.dml.AbstractSQLUpdateClause;
import com.querydsl.sql.dml.SQLUpdateClause;

/**
 * Defines an UPDATE clause. If you need to subtype this, use
 * {@link AbstractSQLUpdateClause} instead.
 */
public class SQLUpdateClauseAlter extends SQLUpdateClause {
	
	private final ConfigurationEx configEx;
	
	public SQLUpdateClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx=configuration;
	}

	public SQLUpdateClauseAlter(Supplier<Connection> connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx=configuration;
	}
	
	
	private static final AdvancedMapper FOR_UPDATE = new AdvancedMapper(AdvancedMapper.SCENARIO_UPDATE);
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public SQLUpdateClauseAlter populate(Object bean) {
		Collection<? extends Path<?>> primaryKeyColumns = entity.getPrimaryKey() != null
				? entity.getPrimaryKey().getLocalColumns()
				: Collections.<Path<?>>emptyList();
		Map<Path<?>, Object> values = FOR_UPDATE.createMap(entity, bean, configEx);
		for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
			if (!primaryKeyColumns.contains(entry.getKey())) {
				set((Path) entry.getKey(), entry.getValue());
			}
		}
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
				listeners.notifyUpdate(entity, metadata, updates);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				int rc = stmt.executeUpdate();
				postExecuted(context, System.currentTimeMillis() - start, "Updated", rc);
				return rc;
			} else {
				stmts = createStatements();
				listeners.notifyUpdates(entity, batches);

				listeners.preExecute(context);
				long start = System.currentTimeMillis();
				long rc = executeBatch(stmts);
				postExecuted(context, System.currentTimeMillis() - start, "BatchUpdated", rc);
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

//	private List<Pair<Path<?>, Expression<?>>> optionalUpdates = new ArrayList<>();
	
	// @WithBridgeMethods(value = SQLUpdateClause.class, castRequired = true)
	public <T> SQLUpdateClauseAlter setIf(boolean doSet, Path<T> path, T value) {
		if (doSet && path != null) {
			if (value instanceof Expression<?>) {
				updates.put(path, (Expression<?>) value);
			} else if (value != null) {
				updates.put(path, ConstantImpl.create(value));
			} else {
				setNull(path);
			}
		}
		return this;
	}

	// @WithBridgeMethods(value = SQLUpdateClause.class, castRequired = true)
	public <T> SQLUpdateClauseAlter setIf(boolean doSet, Path<T> path, Expression<? extends T> expression) {
		if (doSet && path != null) {
			if (expression != null) {
				updates.put(path, expression);
			} else {
				setNull(path);
			}
		}
		return this;
	}
	
	private Integer queryTimeout;	
	/**
	 * 设置查询超时（秒）
	 * @param queryTimeout
	 */
	public SQLUpdateClauseAlter setQueryTimeout(int queryTimeout) {
		this.queryTimeout=queryTimeout;
		return this;
	}
	
//	
//	public <T> SQLUpdateClauseAlter setOptional(Path<T> path, T value) {
//		optionalUpdates.add(Pair.of(path, value));
//		return this;
//	}
//	
//	
//	public <T> SQLUpdateClause setOptional(Path<T> path, Expression<? extends T> expression) {
//		optionalUpdates.add(Pair.of(path, value));
//		return this;
//	}
	
    protected PreparedStatement createStatement() throws SQLException {
        listeners.preRender(context);
        SQLSerializer serializer = createSerializer();
        serializer.serializeUpdate(metadata, entity, updates);
        SQLBindings bindings = createBindings(metadata, serializer);
        context.addSQL(bindings);
        queryString = bindings.getSQL();
        constants = serializer.getConstants();
       // logQuery(logger, queryString, constants);
        //原代码此处有笔误
        listeners.rendered(context); 

        listeners.prePrepare(context);
        PreparedStatement stmt = connection().prepareStatement(queryString);
        if(queryTimeout!=null) {
        	stmt.setQueryTimeout(queryTimeout);
        }else if(configEx.getDefaultQueryTimeout()>0){
			stmt.setQueryTimeout(configEx.getDefaultQueryTimeout());
        }
        setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
        context.addPreparedStatement(stmt);
        listeners.prepared(context);

        return stmt;
    }

    /**
     * 自动拼入需要自动生成的更新字段，如update_time等字段。
     *  @param auto 当auto=true时，仅当有需要更新的字段时才会拼入上述列。
     */
    public void populateAutoGeneratedColumns(boolean auto) {
    	//如果没有需要更新的字段，auto=true时不会填充自动生成字段。
    	if(auto && this.updates.isEmpty()) {
    		return;
    	}
    	IRelationPathEx<?> entity=(IRelationPathEx<?>)this.entity;
    	List<Path<?>> paths = entity.getColumns();
    	for(Path<?> p:paths) {
    		ColumnMapping metadata=entity.getColumnMetadata(p);
    		//用户没有显式设置过值时才会填充字段
    		if(metadata.getGenerated()==null || this.updates.get(p)!=null) {
    			continue;
    		}
    		Object value=AdvancedMapper.asAutoValue(metadata.getGenerated(), metadata, null, AdvancedMapper.SCENARIO_UPDATE,configEx);
    		if (value instanceof Expression<?>) {
    			updates.put(p, (Expression<?>)value);
    	    }else if(!metadata.isUnsavedValue(value)) {
    	    	updates.put(p, ConstantImpl.create(value));
			}
    	}
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
		if(this.configEx.getSlowSqlWarnMillis()<=cost) {
			context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
		}
		listeners.executed(context);
	}
}
