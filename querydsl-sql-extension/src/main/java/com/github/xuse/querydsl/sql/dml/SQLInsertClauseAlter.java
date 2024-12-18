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

import static com.github.xuse.querydsl.sql.expression.AbstractMapperSupport.SCENARIO_INSERT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.lambda.LambdaColumn;
import com.github.xuse.querydsl.sql.Mappers;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dialect.SpecialFeature;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.BeanCodecManager;
import com.github.xuse.querydsl.sql.expression.BindingProvider.ListPathBindings;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.core.util.CollectionUtils;
import com.querydsl.core.util.ResultSetAdapter;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.dml.AbstractSQLInsertClause;
import com.querydsl.sql.dml.Mapper;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.types.Null;

import lombok.extern.slf4j.Slf4j;

/**
 * SQLInsertClause defines an INSERT INTO clause If you need to subtype this,
 * use {@link AbstractSQLInsertClause} instead.
 *
 * @author tiwe. revised by Joey
 */
@Slf4j
public class SQLInsertClauseAlter extends AbstractSQLInsertClause<SQLInsertClauseAlter> {

	private final ConfigurationEx configuration;

	private RoutingStrategy routing;

	private Boolean writeNulls;
	
	/**
	 * In a Batch scenario, parameter normalization (with unspecified columns filled
	 * with NULL) is performed starting from the second row to ensure consistency
	 * with the parameters of the first row. Ultimately, ensure that the Batch
	 * operation consists of a single statement with multiple sets of parameters.
	 * 
	 * 在Batch情况下，对第二条开始的数据进行参数归一化（未指定列补NULL），确保与第一条记录的参数一致。最终确保Batch操作仅有一条语句多组参数。
	 */
	private boolean normalizeBatchValues = true;

	private transient Collection<Object> populatedBatch = Collections.emptyList();

	@SuppressWarnings("rawtypes")
	private transient Mapper batchMapper;

	SQLInsertClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity,
			SQLQuery<?> subQuery) {
		super(connection, configuration.get(), entity, subQuery);
		this.configuration = configuration;
		this.batchToBulk = configuration.getTemplates().isBatchToBulkInDefault();
	}

	SQLInsertClauseAlter(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configuration = configuration;
		this.batchToBulk = configuration.getTemplates().isBatchToBulkInDefault();
	}

	public SQLInsertClauseAlter(Supplier<Connection> connection, ConfigurationEx configuration,
			RelationalPath<?> entity, SQLQuery<?> subQuery) {
		super(connection, configuration.get(), entity, subQuery);
		this.configuration = configuration;
		this.batchToBulk = configuration.getTemplates().isBatchToBulkInDefault();
	}

	public SQLInsertClauseAlter(Supplier<Connection> connection, ConfigurationEx configuration,
			RelationalPath<?> entity) {
		super(connection, configuration.get(), entity);
		this.configuration = configuration;
		this.batchToBulk = configuration.getTemplates().isBatchToBulkInDefault();
	}

	/**
	 * Add fields with automatically generated values
	 * <p>
	 * 添加自动生成值的字段
	 * 
	 * @return SQLInsertClauseAlter
	 */
	public SQLInsertClauseAlter populateAutoGeneratedColumns() {
		RelationalPathEx<?> entity = (RelationalPathEx<?>) this.entity;
		List<Path<?>> paths = entity.getColumns();
		for (Path<?> p : paths) {
			ColumnMapping metadata = entity.getColumnMetadata(p);
			// 用户没有显式设置过值时才会填充字段
			if (metadata.getGenerated() == null) {
				continue;
			}
			if (columns.contains(p)) {
				continue;
			}
			Object value = AdvancedMapper.asAutoValue(metadata.getGenerated(), metadata, SCENARIO_INSERT);
			if (value instanceof Expression<?>) {
				columns.add(p);
				values.add((Expression<?>) value);
			} else if (!metadata.isUnsavedValue(value)) {
				columns.add(p);
				assert value != null;
				values.add(ConstantImpl.create(value));
			}
		}
		return this;
	}

	/**
	 * Execute the clause and return the generated keys as a ResultSet
	 * <p>
	 * 执行该子句并将生成的键作为 ResultSet 返回。
	 * 
	 * @return result set with generated keys
	 */
	public ResultSet executeWithKeys() {
		context = startContext(connection(), metadata, entity);
		try {
			lazyFillBatches();
			PreparedStatement stmt;
			if (!populatedBatch.isEmpty()) {
				// 新模式PopulatedBatch
				if (batchToBulk) {
					stmt = newStatementBulk(populatedBatch, true);
					executeStatementInternal(stmt,false);
				} else {
					stmt = newStatementBatch(populatedBatch, true);
					executeBatchInternal(Collections.singletonList(stmt), false);
				}
			}else if (batches.isEmpty()) {
				stmt = createStatement(true);
				listeners.notifyInsert(entity, metadata, columns, values, subQuery);
				executeStatementInternal(stmt,false);
			} else if (batchToBulk) {
				stmt = createStatement(true);
				listeners.notifyInserts(entity, metadata, batches);
				listeners.preExecute(context);
				executeStatementInternal(stmt,false);
			} else {
				Collection<PreparedStatement> stmts = createStatements(true);
				if (stmts.size() > 1) {
					throw new IllegalStateException(
							"executeWithKeys called with batch statement and multiple SQL strings");
				}
				stmt = stmts.iterator().next();
				listeners.notifyInserts(entity, metadata, batches);
				executeBatchInternal(Collections.singletonList(stmt), false);
			}
			final Statement stmt2 = stmt;
			ResultSet rs = stmt.getGeneratedKeys();
			return new ResultSetAdapter(rs) {
				@Override
				public void close(){
					SQLTypeUtils.close(rs);
					SQLTypeUtils.close(stmt2);
					reset();
					endContext(context);
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
		try {
			lazyFillBatches();
			if (!populatedBatch.isEmpty()) {
				// 新模式PopulatedBatch
				if (batchToBulk) {
					PreparedStatement stmt = newStatementBulk(populatedBatch, false);
					return executeStatementInternal(stmt,true);
				} else {
					PreparedStatement stmt = newStatementBatch(populatedBatch, false);
					return executeBatchInternal(Collections.singleton(stmt),true);
				}
			} else if (batches.isEmpty()) {
				// 单条模式
				if (columns.isEmpty()) {
					return 0;// 没有任何数据需要插入
				}
				PreparedStatement stmt = createStatement(false);
				listeners.notifyInsert(entity, metadata, columns, values, subQuery);
				return executeStatementInternal(stmt,true);
			} else if (batchToBulk) {
				// 批量语句拼接模式
				PreparedStatement stmt = createStatement(false);
				listeners.notifyInserts(entity, metadata, batches);
				return executeStatementInternal(stmt,true);
			} else {
				// JDBC批操作模式
				Collection<PreparedStatement> stmts = createStatements(false);
				listeners.notifyInserts(entity, metadata, batches);
				return executeBatchInternal(stmts,true);
			}
		} catch (SQLException e) {
			onException(context, e);
			throw configuration.translate(queryString, constants, e);
		} finally {
			reset();
			endContext(context);
		}
	}

	private long executeStatementInternal(PreparedStatement stmt, boolean close) {
		try {
			listeners.preExecute(context);
			long start = System.currentTimeMillis();
			int rc = stmt.executeUpdate();
			postExecuted(context, System.currentTimeMillis() - start, "Insert", rc);
			return rc;
		} catch (SQLException e) {
			onException(context, e);
			throw configuration.translate(queryString, constants, e);
		}finally {
			if(close) {
				SQLTypeUtils.close(stmt);
			}
		}
	}

	private long executeBatchInternal(Collection<PreparedStatement> stmts, boolean close) {
		listeners.preExecute(context);
		long start = System.currentTimeMillis();
		try {
			int rc = 0;
			for (PreparedStatement stmt : stmts) {
				for (int i : stmt.executeBatch()) {
					rc += i;	
				}
			}
			postExecuted(context, System.currentTimeMillis() - start, "BatchInsert", rc);
			return rc;
		} catch (SQLException e) {
			onException(context, e);
			throw configuration.translate(queryString, constants, e);
		} finally {
			if(close) {
				stmts.forEach(SQLTypeUtils::close);
			}
		}
	}
	
    public SQLInsertClauseAlter addBatch() {
        if (subQueryBuilder != null) {
            subQuery = subQueryBuilder.select(values.toArray(new Expression[0])).clone();
            values.clear();
        }
        if(batches.isEmpty()) {
        	batches.add(new SQLInsertBatch(columns, values, subQuery));
		} else if (normalizeBatchValues) {
        	List<Path<?>> columns = this.columns;
			List<Path<?>> template = batches.get(0).getColumns();
        	List<Expression<?>> batchValues=this.values;
        	if(!fastEquals(template,columns)) {
				batchValues = normalizeValues(template, columns, batchValues);
    		}
        	batches.add(new SQLInsertBatch(template, batchValues, subQuery));
        }else {
        	batches.add(new SQLInsertBatch(columns, values, subQuery));
        }
        this.columns.clear();
        this.values.clear();
        subQuery = null;
        return this;
    }

	private boolean fastEquals(List<Path<?>> template, List<Path<?>> columns) {
		int len = template.size();
		if(len!=columns.size()) {
			return false;
		}
		for(int i=0;i<len;i++) {
			//use !=, not !equals()
			if(template.get(i)!=columns.get(i)) {
				return false;
			}
		}
		return true;
	}

	private List<Expression<?>> normalizeValues(List<Path<?>> template, List<Path<?>> columns, List<Expression<?>> values) {
		Map<Path<?>, Expression<?>> valuesMap = new HashMap<>();
		for (int i = 0; i < columns.size(); i++) {
			valuesMap.put(columns.get(i), values.get(i));
		}
		int len = template.size();
		List<Expression<?>> result = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			Path<?> p = template.get(i);
			Expression<?> value = valuesMap.get(p);
			if (value == null) {
				value = configuration.getTemplates().getDefaultExpr();
			}
			if (value == null) {
				value = Null.CONSTANT;
			}
			result.add(value);
		}
		return result;
	}

	private PreparedStatement newStatementBulk(Collection<Object> beans, boolean withKeys) throws SQLException {
		Iterator<Object> iter = beans.iterator();
		Object bean = iter.next();
		BatchProcessor batch = createBatch(bean);
		batch.prepareBulk(beans.size() - 1, withKeys);
		Configuration configuration = this.configuration.get();
		while (iter.hasNext()) {
			bean = iter.next();
			batch.setBulkParameter(bean,configuration);
		}
		return batch.stmt;
	}

	private PreparedStatement newStatementBatch(Collection<Object> beans, boolean withKeys) throws SQLException {
		Iterator<Object> iter = beans.iterator();
		Object bean = iter.next();
		BatchProcessor batch = createBatch(bean);
		batch.prepareBatch(withKeys);
		Configuration configuration = this.configuration.get();
		while (iter.hasNext()) {
			bean = iter.next();
			batch.setBatchStatement(bean, configuration);
		}
		return batch.stmt;
	}
	
	/*
	 * 父类方法不支持routing参数。
	 */
	@Override
	protected SQLSerializerAlter createSerializer() {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.setUseLiterals(useLiterals);
		serializer.setRouting(routing);
		return serializer;
	}

	/**
	 * Set the data for bulk insertion
	 * <p>
	 * 设置要批量插入的数据
	 *
	 * @implSpec 使用带NULLBinding的方式设置变量，批量装配时，无论是否为null值，都会在SQL中显示声明，因此数据库的默认值将无效
	 * @param beans data to insert
	 * @return this SQLInsertClauseAlter
	 */
	public SQLInsertClauseAlter populateBatch(Collection<?> beans) {
		int type = Mappers.TYPE_BEAN;
		if(writeNulls==null || writeNulls) {
			type = type | Mappers.NULLS_BIND;
		}
		populateBatch0(beans,Mappers.get(SCENARIO_INSERT,type));
		return this;
	}

	/**
	 * Set the data for bulk insertion
	 * <p>
	 * 设置要批量插入的数据
	 *
	 * @param beans  data to insert
	 * @param mapper the mapper object.
	 * @return this SQLInsertClauseAlter
	 * @see AdvancedMapper
	 * @param <T> The type of target object.
	 */
	public <T> SQLInsertClauseAlter populateBatch(Collection<T> beans, Mapper<?> mapper) {
		if (mapper == null) {
			return populateBatch(beans);
		}
		populateBatch0(beans, mapper);
		return this;
	}

	/**
	 * Set the data for bulk insertion (Tuple objects)
	 * <p>
	 * 设置要批量插入的动态数据（用Tuple类型表示）
	 * 
	 * @param beans 数据
	 * @return this
	 */
	public SQLInsertClauseAlter populateTuples(Collection<Tuple> beans) {
		populateBatch0(beans, Mappers.get(SCENARIO_INSERT,Mappers.NULLS_BIND | Mappers.TYPE_TUPLE));
		return this;
	}

	private void populateBatch0(Collection<?> beans, @SuppressWarnings("rawtypes") Mapper mapper) {
		if (beans.isEmpty()) {
			return;
		}
		Collection<Object> batch;
		if ((batch = populatedBatch).isEmpty()) {
			batch = populatedBatch = new ArrayList<>();
		}
		batch.addAll(beans);
		this.batchMapper = mapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public SQLInsertClauseAlter populate(Object bean) {
		int type = Mappers.TYPE_BEAN;
		if (writeNulls!=null && writeNulls) {
			type = type | Mappers.NULLS_BIND;
		}
		return populate(bean, Mappers.get(SCENARIO_INSERT, type));
	}

	/**
	 * Populate the INSERT clause with the properties of the given bean.
	 * The properties need to match the fields of the clause's entity instance.
	 * <p>
	 * 使用定 bean 的属性填充 INSERT 子句。这些属性需要与子句的实体实例的字段匹配。
	 * 
	 * @param bean bean to use for population
	 * @return the current object
	 */
	@SuppressWarnings("unchecked")
	public SQLInsertClauseAlter populate(Tuple bean) {
		int type = Mappers.TYPE_TUPLE;
		if (writeNulls !=null && writeNulls) {
			type = type | Mappers.NULLS_BIND;
		}
		return populate(bean, Mappers.get(SCENARIO_INSERT,type));
	}
	
	
    /**
     * Set whether batches should be optimized into a single bulk operation.
     * Will revert to batches, if bulk is not supported.
     * 
     * 
     */
    public SQLInsertClauseAlter batchToBulk(boolean b) {
        this.batchToBulk = b && configuration.getTemplates().isBatchToBulkSupported();
        return this;
    }

	protected Collection<PreparedStatement> createStatements(boolean withKeys) throws SQLException {
		boolean addBatches = !super.configuration.getUseLiterals();
		listeners.preRender(context);
		if (subQueryBuilder != null) {
			subQuery = subQueryBuilder.select(values.toArray(new Expression[0])).clone();
			values.clear();
		}
		Map<String, PreparedStatement> stmts = new HashMap<>();
		// add first batch
		{
			SQLSerializerAlter serializer = createSerializer();
			SQLInsertBatch batch = batches.get(0);
			serializer.serializeInsert(metadata, entity, batch.getColumns(), batch.getValues(), batch.getSubQuery());
			String sql = serializer.toString();
			PreparedStatement stmt = prepareStatementAndSetParameters(
					new SQLBindingsAlter(sql, serializer.getConstants(), serializer.getConstantPaths()), withKeys);
			if (addBatches) {
				stmt.addBatch();
			}
			stmts.put(sql, stmt);
			context.addSQL(createBindings(metadata, serializer));
			listeners.rendered(context);
		}
		// add other batches
		int maxLoginBatch = configuration.getMaxRecordsLogInBatch();
		for (int i = 1; i < batches.size(); i++) {
			SQLInsertBatch batch = batches.get(i);
			listeners.preRender(context);
			SQLSerializerAlter serializer = createSerializer(); 
			serializer.serializeInsert(metadata, entity, batch.getColumns(), batch.getValues(), batch.getSubQuery());
			String sql = serializer.toString();
			if (i <= maxLoginBatch) {
				context.addSQL(createBindings(metadata, serializer));
				listeners.rendered(context);
			}
			PreparedStatement stmt = stmts.get(serializer.toString());
			if (stmt == null) {
				stmt = prepareStatementAndSetParameters(
						new SQLBindingsAlter(sql, serializer.getConstants(), serializer.getConstantPaths()), withKeys);
				stmts.put(serializer.toString(), stmt);
			} else {
				setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
			}
			if (addBatches) {
				stmt.addBatch();
			}
		}
		if (stmts.size() > 1) {
			log.warn("There are multi sqls in this batch.{}", stmts.keySet());
		}
		return stmts.values();
	}
	
	private SQLBindingsAlter getSQLForSingle() {
		SQLSerializerAlter serializer = createSerializer(); 
		if (subQueryBuilder != null) {
			subQuery = subQueryBuilder.select(values.toArray(new Expression[values.size()])).clone();
			values.clear();
		}
		if (!batches.isEmpty() && batchToBulk) {
			//这个实现是有问题的,所有Batch都必须有完全相同的Column，必须先按Column分组
			serializer.serializeInsert(metadata, entity, batches);
		} else {
			serializer.serializeInsert(metadata, entity, columns, values, subQuery);
		}
		return createBindings(metadata, serializer);
	}
	
	@Override
	public List<SQLBindings> getSQL() {
		if (batches.isEmpty()) {
			return Collections.singletonList(getSQLForSingle());
        } else if (batchToBulk) {
            SQLSerializer serializer = createSerializer();
            serializer.serializeInsert(metadata, entity, batches);
            return Collections.singletonList(createBindings(metadata, serializer));
        } else {
            List<SQLBindings> builder = new ArrayList<>();
            for (SQLInsertBatch batch : batches) {
                SQLSerializer serializer = createSerializer();
                serializer.serializeInsert(metadata, entity, batch.getColumns(), batch.getValues(), batch.getSubQuery());
                builder.add(createBindings(metadata, serializer));
            }
            return CollectionUtils.unmodifiableList(builder);
        }
	}

	public <B,T extends Comparable<T>> SQLInsertClauseAlter set(LambdaColumn<B,T> path, T value) {
		return super.set(path, value);
	}
	
	public <B,T extends Comparable<T>> SQLInsertClauseAlter set(LambdaColumn<B,T> path, Expression<T> value) {
		return super.set(path, value);
	}
	
	protected PreparedStatement createStatement(boolean withKeys) throws SQLException {
		listeners.preRender(context);
		SQLBindingsAlter bindings=getSQLForSingle();
		context.addSQL(bindings);
		listeners.rendered(context);
		return prepareStatementAndSetParameters(bindings, withKeys);
	}

	protected PreparedStatement prepareStatementAndSetParameters(SQLBindingsAlter bindings, boolean withKeys)
			throws SQLException {
		listeners.prePrepare(context);
		this.queryString = bindings.getSQL();
		this.constants = bindings.getNullFriendlyBindings();
		PreparedStatement stmt;
		if (withKeys) {
			if (entity.getPrimaryKey() != null && !configuration.has(SpecialFeature.PERFER_AUTOGENERATED_KEYS)) {
				String[] target = new String[entity.getPrimaryKey().getLocalColumns().size()];
				for (int i = 0; i < target.length; i++) {
					Path<?> path = entity.getPrimaryKey().getLocalColumns().get(i);
					String column = ColumnMetadata.getName(path);
					column = configuration.getColumnOverride(entity.getSchemaAndTable(), column);
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

	protected void setParameters(PreparedStatement stmt, List<?> objects, List<Path<?>> constantPaths,
			Map<ParamExpression<?>, ?> params) {
		if (objects.size() != constantPaths.size()) {
			throw new IllegalArgumentException(
					"Expected " + objects.size() + " paths, " + "but got " + constantPaths.size());
		}
		for (int i = 0; i < objects.size(); i++) {
			Object o = objects.get(i);
			try {
				if (o instanceof ParamExpression) {
					if (!params.containsKey(o)) {
						throw new ParamNotSetException((ParamExpression<?>) o);
					}
					o = params.get(o);
				}
				super.configuration.set(stmt, constantPaths.get(i), i + 1, o);
			} catch (SQLException e) {
				Path<?> p = constantPaths == null ? null : constantPaths.get(i);
				log.error(queryString + "\nField " + i + " path=" + p + " set error. " + e.getMessage());
				throw super.configuration.translate(e);
			}
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

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, long count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
		if (this.configuration.getSlowSqlWarnMillis() <= cost) {
			context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
		}
		listeners.executed(context);
	}

	public SQLInsertClauseAlter withRouting(RoutingStrategy routing) {
		this.routing = routing;
		return this;
	}

	/**
	 * By default, null values in the bean will not write to the database. After
	 * call this method, the population method such as {@link #populate(Object)}
	 * will allow the system to attempt to update null values to the database.
	 *
	 * <h2>中文</h2> 默认不会写入Bean中null数值。开启后会尝试更新null值到数据库中。
	 * 
	 * @param flag true to enable the update null values feature.
	 * @return SQLUpdateClauseAlter
	 */
	public SQLInsertClauseAlter writeNulls(boolean flag) {
		if (!columns.isEmpty()) {
			throw Exceptions.illegalState("This method should be called before method 'populate'.");
		}
		this.writeNulls = flag;
		return this;
	}

	// 将缓存的批量对象，按旧模式写入到batches中。
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void lazyFillBatches() {
		if (batches.isEmpty() || populatedBatch.isEmpty()) {
			return;
		}
		Mapper mapper = batchMapper;
		for (Object obj : populatedBatch) {
			Map<Path<?>, Object> values = mapper.createMap(entity, obj);
			for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
				set((Path) entry.getKey(), entry.getValue());
			}
			addBatch();
		}
		populatedBatch = null;
	}

	/**
	 * 批量写入的优化实现。针对单一对象大批量写入，在MySQL进行性能测试。发现
	 * -- JDBC驱动的Batch性能很差，远不如MyBatis直接多组values(?),(?)拼接的速度 (194,262ms VS 10,984ms)
	 * -- 改用setBatchToBulk(true)进行测试，发现queryDSL为每个对象生成SQL再分组，也不快(16,999ms vs 7538ms)。
	 * -- 用下列方法实现后， 达到(2876ms vs 7538ms)
	 */
	class BatchProcessor {
		private final SQLBindingsAlter principle;
		
		private final BeanCodec beanCodec;

		private final List<Path<?>> constantPath;

		private PreparedStatement stmt;
		
		private final int maxLoginBatch = configuration.getMaxRecordsLogInBatch();
		
		private int count = 0;

		BatchProcessor(SQLInsertBatch batch, Class<?> beanClass) {
			useLiterals=false;
			SQLSerializerAlter serializer = createSerializer(); 
			listeners.preRender(context);
			serializer.serializeForInsert(metadata, entity, batch.getColumns(), batch.getValues(), null);
			principle = new SQLBindingsAlter(serializer.toString(), serializer.getConstants(),
					serializer.getConstantPaths());
			this.constantPath = serializer.getConstantPaths();
			beanCodec = BeanCodecManager.getInstance().getCodec(beanClass, new ListPathBindings(constantPath));
			context.addSQL(principle);
			listeners.rendered(context);
		}
		public void prepareBulk(int repeatTime, boolean withKeys) throws SQLException {
			String sql = principle.getSQL();
			int index = sql.indexOf("values (");
			String repeat = "," + sql.substring(index + 7);
			StringBuilder sb = new StringBuilder(sql.length() + repeatTime * repeat.length()).append(sql);
			for (int i = 0; i < repeatTime; i++) {
				sb.append(repeat);
			}
			this.stmt = prepareStatement(sb.toString(), withKeys);
			SQLBindingsAlter first = this.principle;
			setParameterBulk(stmt, first.getNullFriendlyBindings().toArray(), first.getPaths(), 0, configuration.get());
		}

		public void prepareBatch(boolean withKeys) throws SQLException {
			SQLBindingsAlter first = this.principle;
			this.stmt = prepareStatement(first.getSQL(), withKeys);
			setParameters(stmt, first.getNullFriendlyBindings(), first.getPaths(), null);
			stmt.addBatch();
		}

		public void setBulkParameter(Object bean,Configuration config) {
			Object[] values = this.beanCodec.values(bean);
			if (count++ < maxLoginBatch) {
				context.addSQL(new SQLBindingsAlter(null, Arrays.asList(values), constantPath));
			}
			setParameterBulk(this.stmt, values, constantPath, count * values.length, config);
		}
		
		public void setBatchStatement( Object bean,Configuration config) throws SQLException {
			List<Path<?>> paths = this.constantPath;
			PreparedStatement stmt = this.stmt;
			if (beanCodec.getType().isInstance(bean)) {
				Object[] values = beanCodec.values(bean);
				if (count++ < maxLoginBatch) {
					context.addSQL(new SQLBindingsAlter(null, Arrays.asList(values), paths));
				}
				int len = values.length;
				for (int i = 0; i < len;) {
					Path<?> path = paths.get(i);
					Object o = values[i];
					try {
						config.set(stmt, path, ++i, o);
					} catch (SQLException e) {
						log.error(principle.getSQL() + "\nField " + (i - 1) + " path=" + path + " set error. " + e.getMessage());
						throw e;
					}
				}
				stmt.addBatch();
			} else {
				throw Exceptions.illegalArgument("Object in batch must be in consistent type {}. encounter a {}",
						beanCodec.getType(), bean.getClass());
			}
		}
		private void setParameterBulk(PreparedStatement stmt, Object[] objects, List<Path<?>> paths, int offset,
				Configuration config) {
			int len = objects.length;
			for (int i = 0; i < len; ) {
				Object o = objects[i];
				Path<?> path = paths.get(i);
				try {
					config.set(stmt, path, ++i + offset, o);
				} catch (SQLException e) {
					log.error(principle.getSQL() + "\nField " + i + " path=" + path + " set error. " + e.getMessage());
					throw config.translate(e);
				}
			}
		}
		private PreparedStatement prepareStatement(String sql, boolean withKeys) throws SQLException {
			listeners.prePrepare(context);
			PreparedStatement stmt;
			if (withKeys) {
				if (entity.getPrimaryKey() != null && !configuration.has(SpecialFeature.PERFER_AUTOGENERATED_KEYS)) {
					String[] target = new String[entity.getPrimaryKey().getLocalColumns().size()];
					for (int i = 0; i < target.length; i++) {
						Path<?> path = entity.getPrimaryKey().getLocalColumns().get(i);
						String column = ColumnMetadata.getName(path);
						column = configuration.getColumnOverride(entity.getSchemaAndTable(), column);
						target[i] = column;
					}
					stmt = connection().prepareStatement(sql, target);
				} else {
					stmt = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				}
			} else {
				stmt = connection().prepareStatement(sql);
			}
			context.addPreparedStatement(stmt);
			listeners.prepared(context);
			return stmt;
		}
	}
	// 在Batch操作中，有三个PathList的概念，需要区分清楚
	// Full Path: Entity的全部列。RelationalPath.getColumns()得到这个列表
	// SQL Path: 参与到SQL中的列，在SQL Batch中的是是这个列表
	// Constant Path: 参与绑定变量，即每条数据需要传入的列,在SQLSerializer.getConstantPath()得到这个列表
	private BatchProcessor createBatch(Object obj) {
		List<Path<?>> columns = new ArrayList<>();
		List<Expression<?>> values = new ArrayList<>();
		@SuppressWarnings("unchecked")
		Map<Path<?>, Object> map = batchMapper.createMap(entity, obj);
		for (Map.Entry<Path<?>, Object> entry : map.entrySet()) {
			Path<?> p = entry.getKey();
			columns.add(p);
			Object value = entry.getValue();
			if (value instanceof Expression<?>) {
				values.add((Expression<?>) value);
			} else if (value != null) {
				values.add(ConstantImpl.create(value));
			} else {
				values.add(Null.CONSTANT);
			}
		}
		return new BatchProcessor(new SQLInsertBatch(columns, values, null), obj.getClass());
	}
	
	public SQLInsertClauseAlter normalizeBatch(boolean flag) {
		this.normalizeBatchValues = flag;
		return this;
	}
}
