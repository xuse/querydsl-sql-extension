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
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.Mappers;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.expression.AbstractMapperSupport;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
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
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.dml.AbstractSQLUpdateClause;
import com.querydsl.sql.dml.Mapper;
import com.querydsl.sql.types.Null;

/**
 * Defines an UPDATE clause. If you need to subtype this, use
 * {@link AbstractSQLUpdateClause} instead.
 */
public class SQLUpdateClauseAlter extends AbstractSQLUpdateClause<SQLUpdateClauseAlter> {

	private final ConfigurationEx configEx;

	private RoutingStrategy routing;

	private boolean updateNulls = false;

	private boolean updateAutoColumns = true;

	SQLUpdateClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx = configuration;
	}

	public SQLUpdateClauseAlter(Supplier<Connection> connection, ConfigurationEx configuration,
			RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configEx = configuration;
	}

	/**
	 * <h2>English</h2>
	 * To update specified fields based on the provided object.
	 *
	 * <h2>中文</h2>
	 * 根据传入对象来指定要更新的字段
	 * 
	 * @param bean object input.
	 * @return SQLUpdateClauseAlter
	 */
	public SQLUpdateClauseAlter populate(Object bean) {
		boolean tuple = (bean instanceof Tuple);
		populate0(bean, Mappers.getUpdate(tuple, updateNulls), false);
		return this;
	}

	/**
	 * <h2>English</h2>
	 * Specify fields to update based on the provided object.
	 *
	 * <h2>中文</h2>
	 * 根据传入的对象来指定要更新的字段
	 * 
	 * @param bean      bean
	 * @param pkAsWhere 在指定set子句的同时，主键作为where条件，即按主键更新。
	 * @return SQLUpdateClauseAlter
	 * @implSpec 如果设置主键作为更新条件，复合主键的所有值都必须设置。
	 */
	public SQLUpdateClauseAlter populate(Object bean, boolean pkAsWhere) {
		boolean tuple = (bean instanceof Tuple);
		populate0(bean, Mappers.getUpdate(tuple, updateNulls), pkAsWhere);
		return this;
	}

	/**
	 * <h2>English:</h2>
	 * Comparison update. In a comparison update, only the changed
	 * fields are set. /If there are no differences between the two objects, the
	 * database will not be written to. Only changed columns will be set.
	 * 
	 * <h2>中文:</h2> 对比更新下，仅有变化的字段被SET，如果两个对象无区别，将不会写数据库。
	 * <p>
	 * 在使用本方法时，标记为not update的字段如果对比结果不一致也会更新.
	 * 
	 * @param bean 新对象
	 * @param old  旧对象
	 * @return SQLUpdateClauseAlter the current object. / 当前对象
	 */
	public SQLUpdateClauseAlter populateWithCompare(Object bean, Object old) {
		Mapper<?> mapper = Mappers.getNormal(false, updateNulls);
		return populateWithCompare(bean, old, mapper, false);
	}

	/**
	 * <h2>English:</h2>
	 * Comparison update. In a comparison update, only the changed
	 * fields are set. /If there are no differences between the two objects, the
	 * database will not be written to. Only changed columns will be set.
	 * 
	 * <h2>中文:</h2> 对比更新下，仅有变化的字段被SET，如果两个对象无区别，将不会写数据库。
	 * <p>
	 * 在使用本方法时，标记为not update的字段如果对比结果不一致也会更新.
	 *
	 * @param bean      The new object. / 新对象
	 * @param old       The old object. /旧对象
	 * @param pkAsWhere true to set where condition with primary key values. false:
	 *                  do nothing with where conditions.
	 * @return SQLUpdateClauseAlter / 当前对象
	 */
	public SQLUpdateClauseAlter populateWithCompare(Object bean, Object old, boolean pkAsWhere) {
		Mapper<?> mapper = Mappers.getNormal(false, updateNulls);
		return populateWithCompare(bean, old, mapper, pkAsWhere);
	}

	/**
	 * <h2>English:</h2>
	 * Comparison update. In a comparison update, only the changed
	 * fields are set. /If there are no differences between the two objects, the
	 * database will not be written to. Only changed columns will be set.
	 * 
	 * <h2>中文:</h2> 对比更新下，仅有变化的字段被SET，如果两个对象无区别，将不会写数据库。
	 * 
	 * @param bean          新对象
	 * @param old           旧对象
	 * @param mapper        mapper
	 * @param fillPkAsWhere true to set where condition with primary key values.
	 *                      false:
	 *                      do nothing with where conditions.
	 * @return SQLUpdateClauseAlter
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SQLUpdateClauseAlter populateWithCompare(Object bean, Object old, Mapper mapper, boolean fillPkAsWhere) {
		Collection<? extends Path<?>> primaryKeyColumns = entity.getPrimaryKey() != null
				? entity.getPrimaryKey().getLocalColumns()
				: Collections.emptyList();
		Map<Path<?>, Object> values = mapper.createMap(entity, bean);
		Map<Path<?>, Object> oldValues = old == null ? Collections.emptyMap() : mapper.createMap(entity, old);
		RelationalPathEx<?> entityEx = null;
		if (entity instanceof RelationalPathEx) {
			entityEx = (RelationalPathEx<?>) entity;
		}
		int pkConditions = 0;
		for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
			Path<?> p = entry.getKey();
			ColumnMapping column = entityEx == null ? null : entityEx.getColumnMetadata(p);
			// 是自动生成的列，对于自动生成的列，跳过不处理
			if (column != null && column.getGenerated() != null) {
				continue;
			}
			Object oldValue = oldValues.remove(p);
			if (primaryKeyColumns.contains(p)) {
				// 主键，跳过不处理
				if (fillPkAsWhere && oldValue != Null.DEFAULT) {
					where(((SimpleExpression<?>) p).eq(ConstantImpl.create(oldValue)));
					pkConditions++;
				}
				continue;
			}
			// 新旧值比较
			BiPredicate<Object, Object> predicate = configEx.getComparePredicate(p);
			if (!predicate.test(oldValue, entry.getValue())) {
				set((Path) p, entry.getValue());
			}
		}
		if (fillPkAsWhere) {
			for (Map.Entry<Path<?>, Object> entry : oldValues.entrySet()) {
				Path<?> p = entry.getKey();
				Object oldValue = entry.getValue();
				if (primaryKeyColumns.contains(p) && oldValue != Null.DEFAULT) {
					// Mapper对于null值处理有约定，如果主键字段为null，那么即便是支持空值绑定，也是不返回的，因为null值对主键是必然无效的。
					// 但考虑到三方Mapper实现，此处仍有必要对Null值进行判断处理
					where(((SimpleExpression<?>) p).eq(ConstantImpl.create(entry.getValue())));
					pkConditions++;
				}
			}
			// 如果主键条件不完整，可能导致update范围扩大甚至全表更新。为避免出现这种危险，检查主键条件的完整性。
			int totalPK = primaryKeyColumns.size();
			if (pkConditions < totalPK) {
				throw Exceptions.illegalArgument(
						"Only a portion of primary key fields have assigned values ({}/{}). To update with primary key, please ensure all primary key fields are assigned. entity:{},where:{}",
						pkConditions, totalPK, entity.getClass().getName(), metadata.getWhere());

			}

		}
		return this;
	}

	/**
	 * <h2>English</h2>
	 * This method provides more specific field mapping behavior through the
	 * AdvancedMapper and generally does not need to be used externally.
	 * <h2>中文</h2>
	 * 这个方法通过AdvancedMapper提供了更具体的字段映射行为，一般来说外部无需使用。
	 * 
	 * @param bean      The data object.
	 * @param mapper    AdvancedMapper
	 * @param pkAsWhere When specifying the SET clause, the WHERE condition is set
	 *                  to the primary key condition. In this case, all values of
	 *                  the composite primary key must be specified.
	 *                  <p>
	 *                  在指定set字句的同时，就将where条件设置为主键条件。这种情况下复合主键的所有值都必须设置。
	 * @return SQLUpdateClauseAlter
	 * @param <T> The type of target object.
	 */
	public <T> SQLUpdateClauseAlter populate(T bean, Mapper<T> mapper, boolean pkAsWhere) {
		populate0(bean, mapper, pkAsWhere);
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void populate0(Object bean, Mapper mapper, boolean pkAsWhere) {
		Collection<? extends Path<?>> primaryKeyColumns = entity.getPrimaryKey() != null
				? entity.getPrimaryKey().getLocalColumns()
				: Collections.emptyList();
		Map<Path<?>, Object> values = mapper.createMap(entity, bean);
		int pkConditionFilled = 0;
		for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
			if (primaryKeyColumns.contains(entry.getKey())) {
				// fill PK as where conditions.
				if (pkAsWhere) {
					SimpleExpression<?> column = (SimpleExpression<?>) entry.getKey();
					where(column.eq(ConstantImpl.create(entry.getValue())));
					pkConditionFilled++;
				}
			} else {
				set((Path) entry.getKey(), entry.getValue());
			}
		}
		if (pkAsWhere) {
			int totalPK = primaryKeyColumns.size();
			// 如果主键条件不完整，可能导致update范围扩大甚至全表更新。为避免出现这种危险，检查主键条件的完整性。
			if (pkConditionFilled < totalPK) {
				throw Exceptions.illegalArgument(
						"Only a portion of primary key fields have assigned values ({}/{}). To update with primary key, please ensure all primary key fields are assigned. entity:{},where:{}",
						pkConditionFilled, totalPK, entity.getClass().getName(), metadata.getWhere());
			}
		}
	}

	@Override
	public long execute() {
		if (isEmpty()) {
			return 0;
		}
		if (updateAutoColumns) {
			populateAutoGeneratedColumns0();
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

	/**
	 * Set the value of the given path.
	 * <h2>中文</h2>
	 * <p>
	 * 设置字段值，如果doSet为false，则不会设置该字段的值。
	 * 
	 * @param <T>   The type of target object.
	 * @param doSet weather to set the value of the given path.
	 * @param path  The path of target object.
	 * @param value The value of target object.
	 * @return SQLUpdateClauseAlter
	 */
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

	/**
	 * Set the value of the given path.
	 * <h2>中文</h2>
	 * 设置字段值，如果doSet为false，则不会设置该字段的值。
	 * 
	 * @param <T>        The type of target object.
	 * @param doSet      weather to set the value of the given path.
	 * @param path       The path of target object.
	 * @param expression The expression of target object.
	 * @return SQLUpdateClauseAlter
	 */
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
	 * <h2>English</h2>
	 * Set query timeout in seconds.
	 * <h2>中文</h2>
	 * 设置查询超时（秒）
	 * 
	 * @param queryTimeout queryTimeout
	 * @return this
	 */
	public SQLUpdateClauseAlter setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
		return this;
	}

	protected PreparedStatement createStatement() throws SQLException {
		listeners.preRender(context);
		SQLSerializerAlter serializer = createSerializer();
		serializer.serializeUpdate(metadata, entity, updates);
		SQLBindings bindings = createBindings(metadata, serializer);
		context.addSQL(bindings);
		queryString = bindings.getSQL();
		constants = serializer.getConstants();
		// logQuery(logger, queryString, constants);
		// 原代码此处有笔误
		listeners.rendered(context);
		listeners.prePrepare(context);
		PreparedStatement stmt = connection().prepareStatement(queryString);
		if (queryTimeout != null) {
			stmt.setQueryTimeout(queryTimeout);
		} else if (configEx.getDefaultQueryTimeout() > 0) {
			stmt.setQueryTimeout(configEx.getDefaultQueryTimeout());
		}
		setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
		context.addPreparedStatement(stmt);
		listeners.prepared(context);
		return stmt;
	}

	public SQLUpdateClauseAlter updateAutoColumns(boolean flag) {
		this.updateAutoColumns = flag;
		return this;
	}

	/**
	 * Specify the fields to be updated; unspecified fields will be ignored. Fields
	 * like `update_time` will be automatically generated and included in the
	 * update.
	 *
	 * <h2>中文</h2> 拼入需要自动生成的更新字段，如update_time等字段。
	 *
	 * @param auto 当auto=true时，仅当整个update语句有需要更新的字段时才会拼入上述列。
	 * @return this
	 * @deprecated use {@link #updateAutoColumns(true)} instead of this method.
	 */
	/**
	 * Specify the fields to be updated; unspecified fields will be ignored. Fields
	 * like `update_time` will be automatically generated and included in the
	 * update.
	 *
	 * <h2>中文</h2> 拼入需要自动生成的更新字段，如update_time等字段。
	 *
	 * @param auto Only if auto=true, the above columns will be included in the
	 *             update only if the update statement has fields to be updated.
	 *             <p>
	 *             当auto=true时，仅当整个update语句有需要更新的字段时才会拼入上述列。
	 * @deprecated For now, auto generated columns will be added into SQL when
	 *             {@link #execute()} is called. Calling this method is no longer
	 *             needed. You can use {@link #updateAutoColumns(boolean)} to
	 *             disable auto generated columns.
	 *             <p>
	 *             现在当执行{@link #execute()}时，默认会添加自动列的数值/表达式生成，不再需要显式调用此方法。您可以
	 *             用{@link #updateAutoColumns(boolean)} 关闭自动列的数值/表达式生成.
	 * @return this
	 */
	public SQLUpdateClauseAlter populateAutoGeneratedColumns(boolean auto) {
		if (auto && this.updates.isEmpty()) {
			return this;
		}
		// 如果没有需要更新的字段，auto=true时不会填充自动生成字段。
		populateAutoGeneratedColumns0();
		return this;
	}

	private void populateAutoGeneratedColumns0() {
		RelationalPathEx<?> entity = (RelationalPathEx<?>) this.entity;
		for (ColumnMapping metadata : entity.getAutoColumns()) {
			Path<?> p = metadata.getPath();
			// 用户没有显式设置过值时才会填充字段
			if (metadata.getGenerated() == null || this.updates.get(p) != null) {
				continue;
			}
			Object value = AdvancedMapper.asAutoValue(metadata.getGenerated(), metadata,
					AbstractMapperSupport.SCENARIO_UPDATE);
			if (value instanceof Expression<?>) {
				updates.put(p, (Expression<?>) value);
			} else if (!metadata.isUnsavedValue(value)) {
				assert value != null;
				updates.put(p, ConstantImpl.create(value));
			}
		}
	}

	/**
	 * <h2>English</h2>
	 * Specify the fields to be updated, and read the data from the passed-in
	 * object.
	 * <h2>中文</h2>
	 * 指定需要更新的字段，数据从传入对象中读取
	 * 
	 * @param obj   the data object. 数据对象
	 * @param paths Specify the fields to be updated; unspecified fields will be
	 *              ignored. 指定需要更新的字段，未指定的字段将被忽略。
	 * @return this
	 */
	public SQLUpdateClauseAlter populateFields(Object obj, Path<?>... paths) {
		RelationalPathEx<?> entity = (RelationalPathEx<?>) this.entity;
		for (Path<?> p : paths) {
			ColumnMapping metadata = entity.getColumnMetadata(p);
			Object value = AdvancedMapper.asAutoValue(metadata.getGenerated(), metadata,
					AbstractMapperSupport.SCENARIO_UPDATE);
			if (value instanceof Expression<?>) {
				updates.put(p, (Expression<?>) value);
			} else if (!metadata.isUnsavedValue(value)) {
				assert value != null;
				updates.put(p, ConstantImpl.create(value));
			}
		}
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

	/**
	 * By default, null values in the bean will not update to the database. After
	 * call this method, the population method such as {@link #populate(Object)}
	 * and {@link #populateWithCompare(Object, Object)} will allow the system to
	 * attempt to update null values to the database.
	 *
	 * <h2>中文</h2>
	 * 默认不会更新传入Bean中null数值。开启后会尝试更新null值到数据库中。
	 * 
	 * @param flag true to enable the update null values feature.
	 * @return SQLUpdateClauseAlter
	 */
	public SQLUpdateClauseAlter updateNulls(boolean flag) {
		if (!updates.isEmpty()) {
			throw Exceptions.illegalState("This method should be called before method 'populate'.");
		}
		this.updateNulls = flag;
		return this;
	}

	/**
	 * To check if a specific path has been set with a value.
	 *
	 * <h2>中文</h2>
	 * 检查某个指定的路径有没有设置过值
	 * 
	 * @param path path
	 * @return true if the path was set.
	 */
	public boolean containsSetPath(Path<?> path) {
		return updates.containsKey(path);
	}

	/**
	 * Manually remove the previously set Set clause. This is generally used to
	 * prevent updating fields that do not need to be updated.
	 *
	 * <h2>中文</h2>
	 * 手动删除之前被设置进去的Set子句。一般用于防止更新一些无需更新的字段。
	 *
	 * @param path path
	 * @return SQLUpdateClauseAlter
	 */
	public SQLUpdateClauseAlter removeSetPath(Path<?>... path) {
		// int count=0;
		for (Path<?> p : path) {
			updates.remove(p);
		}
		return this;
	}

	public SQLUpdateClauseAlter withRouting(RoutingStrategy routing) {
		this.routing = routing;
		return this;
	}
}
