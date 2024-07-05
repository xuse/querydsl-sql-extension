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
import java.util.Objects;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.TupleMapper;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RoutingStrategy;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.dml.AbstractSQLUpdateClause;
import com.querydsl.sql.dml.Mapper;

/**
 * Defines an UPDATE clause. If you need to subtype this, use
 * {@link AbstractSQLUpdateClause} instead.
 */
public class SQLUpdateClauseAlter extends AbstractSQLUpdateClause<SQLUpdateClauseAlter> {
	
	private final ConfigurationEx configEx;
	private RoutingStrategy routing;
	
	public SQLUpdateClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx=configuration;
	}

	public SQLUpdateClauseAlter(Supplier<Connection> connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx=configuration;
	}
	
	private final static AdvancedMapper FOR_UPDATE = new AdvancedMapper(AdvancedMapper.SCENARIO_UPDATE);
	
	private final static TupleMapper FOR_TUPLE_UPDATE = new TupleMapper(AdvancedMapper.SCENARIO_UPDATE);
	
    /**
     * 根据传入对象来指定要更新的字段
     */
	public SQLUpdateClauseAlter populate(Object bean) {
		boolean tuple=(bean instanceof Tuple);
		populate0(bean,getUpdateBindingMapper(tuple), false);
		return this;
    }
    
	@SuppressWarnings("rawtypes")
	static Mapper getUpdateBindingMapper(boolean tuple) {
		return tuple ? FOR_TUPLE_UPDATE : FOR_UPDATE;
	}

	/**
	 * 根据传入的对象来指定要更新的字段
	 * @param bean
	 * @param pkAsWhere 在指定set子句的同时，主键作为where条件，即按主键更新。
	 * @return SQLUpdateClauseAlter
	 * @implSpec 如果设置主键作为更新条件，复合主键的所有值都必须设置。
	 */
    public SQLUpdateClauseAlter populate(Object bean, boolean pkAsWhere) {
    	boolean tuple=(bean instanceof Tuple);
    	populate0(bean, getUpdateBindingMapper(tuple), pkAsWhere);
    	return this;
	}
	
	/**
	 * 当向数据库更新一个数值时，查出旧数值。新旧对象进行对比，仅当新对象有数值和旧对象不同时才会写入更新字段。
	 * 本方法在对比时，会跳过自动生成字段。当对比完成后如果确认需要更新，那么才会写入自动更新字段的值。
	 * <p>
	 * 这个方法一般和execute连用。
	 * <p>
	 * 在使用本方法时，标记为notupdate的字段如果对比结果不一致也会更新.
	 * @param bean 新对象
	 * @param old 旧对象
	 * @return 更新句柄
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public SQLUpdateClauseAlter populateWithCompare(Object bean, Object old) {
    	Collection<? extends Path<?>> primaryKeyColumns = entity.getPrimaryKey() != null
				? entity.getPrimaryKey().getLocalColumns()
				: Collections.<Path<?>>emptyList();
				
    	AdvancedMapper mapper = AdvancedMapper.DEFAULT;
		Map<Path<?>, Object> values = mapper.createMap(entity, bean);
		Map<Path<?>, Object> oldvalues = old==null? Collections.emptyMap():mapper.createMap(entity, old);
		
		
		RelationalPathEx<?> entityEx=null;
		if(entity instanceof RelationalPathEx) {
			entityEx=(RelationalPathEx<?>)entity;
		}
		
		for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
			Path<?> p=entry.getKey();
			ColumnMapping column=entity==null? null:entityEx.getColumnMetadata(p);
			
			//是自动生成的列，对于自动生成的列，跳过不处理
			if(column!=null && column.getGenerated()!=null) {
				continue;
			}
			//主键，跳过不处理
			if (primaryKeyColumns.contains(p)) {
				//Do not fill PK fields
				continue;
			}
			//旧值比较
			Object oldValue=oldvalues.get(p);
			if(!Objects.equals(oldValue, entry.getValue())) {
				//不等，考虑更新
				set((Path)p , entry.getValue());
			}
		}
		//更新自动生成的列
		this.populateAutoGeneratedColumns(true);
		return this;
    }
    
    /*
     * 这个方法通过AdvancedMapper提供了更具体的字段映射行为，一般来说外部无需使用。
     * @param bean 数据对象
     * @param mapper AdvancedMapper
     * @param pkAsWhere 在指定set字句的同时，就将where条件设置为主键条件。这种情况下复合主键的所有值都必须设置。
     * @return SQLUpdateClauseAlter
     */
    public <T> SQLUpdateClauseAlter populate(T bean, Mapper<T> mapper, boolean pkAsWhere) {
    	populate0(bean,mapper,pkAsWhere);
    	return this;
	}
	
    @SuppressWarnings({"rawtypes","unchecked"})
	private <T> void populate0(Object bean, Mapper mapper, boolean pkAsWhere) {
		Collection<? extends Path<?>> primaryKeyColumns = entity.getPrimaryKey() != null
				? entity.getPrimaryKey().getLocalColumns()
				: Collections.<Path<?>>emptyList();
		Map<Path<?>, Object> values = mapper.createMap(entity, bean);
		int pkConditionFilled=0;
		for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
			if (primaryKeyColumns.contains(entry.getKey())) {
				//fill PK as where conditions.
				if(pkAsWhere) {
					SimpleExpression<?> column=(SimpleExpression<?>)entry.getKey();
					where(column.eq(ConstantImpl.create(entry.getValue())));
					pkConditionFilled++;
				}
			}else {
				set((Path) entry.getKey(), entry.getValue());
			}
		}
		if(pkAsWhere) {
			int totalPK = primaryKeyColumns.size();
			//如果主键条件不完整，可能导致update范围扩大甚至全表更新。为避免出现这种危险，检查主键条件的完整性。
			if (pkConditionFilled < totalPK) {
				throw Exceptions.illegalArgument("There is null value on some primary key columns. ({}/{}) entity:{},where:{}",
						pkConditionFilled,totalPK,entity.getClass().getName(),metadata.getWhere());
			}
		}
	}

	@Override
	public long execute() {
		if(isEmpty()) {
			return 0;
		}
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
	 * @return this
	 */
	public SQLUpdateClauseAlter setQueryTimeout(int queryTimeout) {
		this.queryTimeout=queryTimeout;
		return this;
	}

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
        SQLSerializerAlter serializer = new SQLSerializerAlter(configEx, true);
        serializer.setUseLiterals(useLiterals);
		serializer.setRouting(routing);
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
     * @return this
     */
    public SQLUpdateClauseAlter populateAutoGeneratedColumns(boolean auto) {
    	//如果没有需要更新的字段，auto=true时不会填充自动生成字段。
    	if(auto && this.updates.isEmpty()) {
    		return this;
    	}
    	RelationalPathEx<?> entity=(RelationalPathEx<?>)this.entity;
    	List<Path<?>> paths = entity.getColumns();
    	for(Path<?> p:paths) {
    		ColumnMapping metadata=entity.getColumnMetadata(p);
    		//用户没有显式设置过值时才会填充字段
    		if(metadata.getGenerated()==null || this.updates.get(p)!=null) {
    			continue;
    		}
    		Object value=AdvancedMapper.asAutoValue(metadata.getGenerated(), metadata, AdvancedMapper.SCENARIO_UPDATE);
    		if (value instanceof Expression<?>) {
    			updates.put(p, (Expression<?>)value);
    	    }else if(!metadata.isUnsavedValue(value)) {
    	    	updates.put(p, ConstantImpl.create(value));
			}
    	}
    	return this;
    }
    
    /**
     * 制定需要更新的字段，数据从对象中读取
     * @param obj 存储数据的对象
     * @param paths 指定需要更新的字段，未指定的字段将被忽略。
     * @return this
     */
    public SQLUpdateClauseAlter populateFields(Object obj,Path<?>... paths) {
    	RelationalPathEx<?> entity=(RelationalPathEx<?>)this.entity;
    	for(Path<?> p:paths) {
    		ColumnMapping metadata=entity.getColumnMetadata(p);
    		Object value=AdvancedMapper.asAutoValue(metadata.getGenerated(), metadata, AdvancedMapper.SCENARIO_UPDATE);
    		if (value instanceof Expression<?>) {
    			updates.put(p, (Expression<?>)value);
    	    }else if(!metadata.isUnsavedValue(value)) {
    	    	updates.put(p, ConstantImpl.create(value));
			}
    	}
    	populateAutoGeneratedColumns(false);
    	return this;
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
	
	/**
	 * 检查某个指定的路径有没有设置过值
	 * @param path
	 * @return true if the path was set.
	 */
	public boolean containsSetPath(Path<?> path) {
		return updates.containsKey(path);
	}
	
	/**
	 * 手动删除之前被设置进去的Set子句。一般用于防止更新一些无需更新的字段。
	 * @param path
	 * @return SQLUpdateClauseAlter
	 */
	public SQLUpdateClauseAlter removeSetPath(Path<?>... path) {
		//int count=0;
		for(Path<?> p:path) {
			updates.remove(p);
		}
		return this;
	}
	
	public SQLUpdateClauseAlter withRouting(RoutingStrategy routing){
		this.routing = routing;
		return this;
	}
}
