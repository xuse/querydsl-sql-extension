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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.annotation.Condition;
import com.github.xuse.querydsl.annotation.ConditionBean;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.expression.ProjectionsAlter;
import com.github.xuse.querydsl.sql.expression.QBeanEx;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.sql.result.Projection;
import com.github.xuse.querydsl.util.Exceptions;
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
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
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

	private SQLListenerContext parentContext;

	private final ConfigurationEx configEx;

	@Nullable
	private Provider<Connection> connProvider;

	@Nullable
	private Connection conn;
	///////////// 覆盖检查字段结束/////////////

	public SQLQueryAlter() {
		super((Connection) null, new Configuration(SQLTemplates.DEFAULT), new DefaultQueryMetadata());
		this.configEx = new ConfigurationEx(super.getConfiguration());
	}

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

	public SQLQueryAlter(Connection conn, SQLTemplates templates, QueryMetadata metadata) {
		super(conn, new Configuration(templates), metadata);
		this.conn = conn;
		this.configEx = new ConfigurationEx(super.getConfiguration());
	}

	public SQLQueryAlter(Connection conn, SQLTemplates templates) {
		super(conn, new Configuration(templates));
		this.conn = conn;
		this.configEx = new ConfigurationEx(super.getConfiguration());
	}

	public SQLQueryAlter(Provider<Connection> connProvider, ConfigurationEx configuration, QueryMetadata metadata) {
		super(connProvider, configuration.get(), metadata);
		this.connProvider = connProvider;
		this.configEx = configuration;
	}

	public SQLQueryAlter(Provider<Connection> connProvider, ConfigurationEx configuration) {
		super(connProvider, configuration.get(), new DefaultQueryMetadata());
		this.connProvider = connProvider;
		this.configEx = configuration;
	}

	@Override
	public QueryResults<T> fetchResults() {
		parentContext = startContext(connection(), queryMixin.getMetadata());
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
				// 我怎么感觉这两句话是没什么用处的。
//				Expression<T> expr = (Expression<T>) queryMixin.getMetadata().getProjection();
//				queryMixin.setProjection(expr);
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
			// logQuery(queryString, serializer.getConstants());
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

	@Override
	public List<T> fetch() {
		return fetch(null);
	}

	private List<T> fetch(Holder<Object> getLastCell) {
		SQLListenerContextImpl context = startContext(connection(), queryMixin.getMetadata());
		String queryString = null;
		List<Object> constants = ImmutableList.of();

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
			final PreparedStatement stmt = getPreparedStatement(queryString);
			try {
				setParameters(stmt, constants, serializer.getConstantPaths(), queryMixin.getMetadata().getParams());
				context.addPreparedStatement(stmt);
				listeners.prepared(context);
				listeners.preExecute(context);
				long timeesp = System.currentTimeMillis();
				ResultSet rs = stmt.executeQuery();
				timeesp = System.currentTimeMillis() - timeesp;
				try {
					Projection<T> fe = getProjection(rs, getLastCell != null);
					final List<T> result = fe.convert(rs);
					if (getLastCell != null && fe instanceof AbstractProjection) {
						getLastCell.value = ((AbstractProjection<?>) fe).lastCell;
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
		} catch (RuntimeException e) {
			onException(context, e);
			throw e;
		} finally {
			endContext(context);
			reset();
		}
	}

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
			int argSize = getArgSize();
			while (rs.next()) {
				if (getLastCell) {
					lastCell = rs.getObject(argSize + 1);
					getLastCell = false;
				}
				result.add(fetch(rs));
			}
			return result;
		}

		protected abstract RT fetch(ResultSet rs) throws SQLException;

		protected abstract int getArgSize();

	}

	final class FactoryExpressionResult<RT> extends AbstractProjection<RT> {
		private final FactoryExpression<RT> c;
		private final int argSize;
		private final List<Path<?>> argPath;
		private final List<Class<?>> argTypes;

		FactoryExpressionResult(FactoryExpression<RT> c) {
			List<Expression<?>> args = c.getArgs();
			int argSize = args.size();
			this.c = c;
			this.argSize = argSize;
			this.argPath = new ArrayList<>(argSize);
			this.argTypes = new ArrayList<>(argSize);
			for (int i = 0; i < argSize; i++) {
				Expression<?> expr = args.get(i);
				argPath.add(expr instanceof Path ? (Path<?>) expr : null);
				argTypes.add(expr.getType());
			}
		}

		protected RT fetch(ResultSet rs) throws SQLException {
			int offset = 0;
			Object[] args = new Object[argSize];
			for (int i = 0; i < args.length; i++) {
				args[i] = configuration.get(rs, argPath.get(i), offset + i + 1, argTypes.get(i));
			}
			return c.newInstance(args);
		}

		@Override
		protected int getArgSize() {
			return argSize;
		}
	}

	final class WildcardAllResult<RT> extends AbstractProjection<RT> {
		private final int columnSize;

		public WildcardAllResult(int columnSize) {
			super();
			this.columnSize = columnSize;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected RT fetch(ResultSet rs) throws SQLException {
			Object[] row = new Object[columnSize];
			for (int i = 0; i < row.length; i++) {
				row[i] = rs.getObject(i + 1);
			}
			return (RT) row;
		}

		@Override
		protected int getArgSize() {
			return columnSize;
		}
	}

	final class SingleValueResult<RT> extends AbstractProjection<RT> {
		private final Expression<RT> expr;
		private final Path<?> path;

		public SingleValueResult(Expression<RT> expr) {
			this.expr = expr;
			this.path = expr instanceof Path ? (Path<?>) expr : null;
		}

		@Override
		protected RT fetch(ResultSet rs) throws SQLException {
			return configuration.get(rs, path, 1, expr.getType());
		}

		@Override
		protected int getArgSize() {
			return 1;
		}
	}

	final class DefaultValueResult<RT> extends AbstractProjection<RT> {
		@SuppressWarnings("unchecked")
		@Override
		protected RT fetch(ResultSet rs) throws SQLException {
			return (RT) rs.getObject(1);
		}

		@Override
		protected int getArgSize() {
			return 1;
		}
	}

	private void reset() {
		cleanupMDC();
	}

	private StatementOptions statementOptions = StatementOptions.DEFAULT;

	/**
	 * 设置本次查询载入的最大行数
	 * 
	 * @param maxRows
	 */
	public SQLQueryAlter<T> setMaxRows(int maxRows) {
		StatementOptions options = this.statementOptions;
		setStatementOptions(new StatementOptions(options.getMaxFieldSize(), maxRows, options.getQueryTimeout(), options.getFetchSize()));
		return this;
	}

	/**
	 * 设置本次查询每批获取大小
	 * 
	 * @param fetchSize
	 */
	public SQLQueryAlter<T> setFetchSisze(int fetchSize) {
		StatementOptions options = this.statementOptions;
		setStatementOptions(new StatementOptions(options.getMaxFieldSize(), options.getMaxRows(), options.getQueryTimeout(), fetchSize));
		return this;
	}

	/**
	 * 设置查询超时（秒）
	 * 
	 * @param queryTimeout
	 */
	public SQLQueryAlter<T> setQueryTimeout(int queryTimeout) {
		StatementOptions options = this.statementOptions;
		setStatementOptions(new StatementOptions(options.getMaxFieldSize(), options.getMaxRows(), queryTimeout, options.getFetchSize()));
		return this;
	}

	public void setStatementOptions(StatementOptions statementOptions) {
		super.setStatementOptions(statementOptions);
		this.statementOptions = statementOptions;
	}

	private PreparedStatement getPreparedStatement(String queryString) throws SQLException {
		PreparedStatement statement = connection().prepareStatement(queryString);
		StatementOptions statementOptions = this.statementOptions;
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

	@Override
	public CloseableIterator<T> iterate() {
		return iterateSingle(queryMixin.getMetadata());
	}

	private CloseableIterator<T> iterateSingle(QueryMetadata metadata) {
		SQLListenerContextImpl context = startContext(connection(), queryMixin.getMetadata());
		String queryString = null;
		List<Object> constants = ImmutableList.of();

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
			final PreparedStatement stmt = getPreparedStatement(queryString);
			setParameters(stmt, constants, serializer.getConstantPaths(), metadata.getParams());
			context.addPreparedStatement(stmt);
			listeners.prepared(context);

			listeners.preExecute(context);
			long start = System.currentTimeMillis();
			final ResultSet rs = stmt.executeQuery();
			postExecuted(context, System.currentTimeMillis() - start, "Iterated", 1);
			Projection<T> expr = getProjection(rs, false);
			return expr.iterator(stmt, rs, context);
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
			// logQuery(queryString, serializer.getConstants());
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
		} catch (RuntimeException e) {
			onException(context, e);
			reset();
			endContext(context);
			throw e;
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
	
	/**
	 * 设置Limit，如果传入值为null或零或负数，则设置无效
	 * @param limit
	 * @return
	 */
	public final SQLQueryAlter<T> limitIf(Integer limit) {
		if(limit==null  || limit<=0) {
			return queryMixin.getSelf();
		}
		return queryMixin.limit(limit);
	}
	
	/**
	 * 设置Offset，如果传入值为null或负数，则设置无效
	 */
	public final SQLQueryAlter<T> offsetIf(Integer offset) {
		if(offset==null || offset<0) {
			return queryMixin.getSelf();
		}
		return queryMixin.offset(offset);
	}

	/**
	 * 支持使用一个参数Bean自动拼装查询条件。
	 * @param <X>
	 * @param conditionBean
	 * @param beanPath
	 * @return
	 */
	public final <X> SQLQueryAlter<T> where(X conditionBean, IRelationPathEx<T> beanPath) {
		List<Path<?>> pathes = beanPath.getColumns();
		@SuppressWarnings({"unchecked" })
		Class<X> clz=(Class<X>) conditionBean.getClass();
		QBeanEx<X> qb = ProjectionsAlter.bean(clz, beanPath);
		ConditionBean cb=conditionBean.getClass().getAnnotation(ConditionBean.class);
		if(cb==null) {
			throw new IllegalArgumentException("Condition bean must annotated with @ConditionBean");
		}
		
		List<Expression<?>> exps = qb.getArgs();
		Object[] values = qb.getBeanCodec().values(conditionBean);
		int len = pathes.size();
		Field[] fields = qb.getBeanCodec().getFields();
		for (int i = 0; i < len; i++) {
			Field field = fields[i];
			if (field == null) {
				continue;
			}
			Object value = values[i];
			Expression<?> exp1=exps.get(i);
			ColumnMapping cm = beanPath.getColumnMetadata(pathes.get(i));
			Condition annotation = field.getAnnotation(Condition.class);
			if(annotation!=null && annotation.name().length()>0 && !cm.get().getName().equals(annotation.name())) {
				throw Exceptions.illegalArgument("Field {}.{} has matched a property, but name of annotation pointer to another field.{} ", clz.getName(),field.getName(),annotation.name());
			}
			appendCondition(field,value,exp1,cm, annotation==null? Ops.EQ :annotation.value());
		}
		for(String key:cb.additional()) {
			Field field=getField(clz,key);
			if(field==null) {
				logger.error("@ConditionBean on{} field {} not found.",clz,key);
				continue;
			}
			field.setAccessible(true);
			Condition annotation = field.getAnnotation(Condition.class);
			if(annotation==null || annotation.name().isEmpty()) {
				throw Exceptions.illegalArgument("Miss real field name in annotation, please check @Condition annotation. field is {}", field);
			}
			Path<?> path=beanPath.getColumn(annotation.name());
			if(path==null) {
				throw Exceptions.illegalArgument("can't find path {} in {}, which annotated in field {}",annotation.name(),beanPath.getType(),field);
			}
			ColumnMapping cm = beanPath.getColumnMetadata(path);
			try {
				Object value = field.get(conditionBean);
				appendCondition(field,value,(Expression<?>)path,cm, annotation==null? Ops.EQ :annotation.value());
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		if(!cb.limitField().isEmpty()) {
			Number limit=getNumber(cb.limitField(),conditionBean);
			if(limit!=null) {
				limitIf(limit.intValue());
			}
		}
		if(!cb.offsetField().isEmpty()) {
			Number offset=getNumber(cb.offsetField(), conditionBean);
			if(offset!=null) {
				offsetIf(offset.intValue());
			}
		}
		return this;
	}

	private Number getNumber(String limitField, Object conditionBean) {
		Field field=getField(conditionBean.getClass(),limitField);
		if(field==null) {
			throw Exceptions.illegalArgument("Can not find limit field {}", limitField);
		}
		field.setAccessible(true);
		try {
			Object value = field.get(conditionBean);
			if(value instanceof Number) {
				return (Number)value;
			}
			return null;
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private Field getField(Class<?> clz, String key) {
		Field field = null;
		try {
			field=clz.getDeclaredField(key);
		}catch(NoSuchFieldException e) {
		}
		if(field!=null || clz.getSuperclass()==Object.class) {
			return field;
		}
		return getField(clz.getSuperclass(),key);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void appendCondition(Field field, Object value,Expression<?> exp1,ColumnMapping cm,Ops ops) {
		
		if(ops==Ops.IN || ops==Ops.BETWEEN) {
			if(!hasElements(value)) {
				return;
			}
		}else {
			if (cm.isUnsavedValue(value)) {
				return;
			}
		}
		switch (ops) {
		case IN:{
			SimpleExpression exp = (SimpleExpression) exp1;
			if(value instanceof Collection<?>) {
				queryMixin.where(exp.in((Collection<?>)value));	
			}else if(value instanceof Object[]) {
				queryMixin.where(exp.in((Object[])value));
			}
			break;
		}
		case EQ: {
			SimpleExpression exp = (SimpleExpression) exp1;
			queryMixin.where(exp.eq(value));
			break;
		}
		case LT: {
			if(exp1 instanceof NumberExpression) {
				NumberExpression exp=(NumberExpression)exp1;
				queryMixin.where(exp.lt((Number) value));	
			}else {
				ComparableExpression exp = (ComparableExpression) exp1;
				queryMixin.where(exp.lt((Comparable) value));
			}
			break;
		}
		case LOE: {
			if(exp1 instanceof NumberExpression) {
				NumberExpression exp=(NumberExpression)exp1;
				queryMixin.where(exp.loe((Number) value));	
			}else {
				ComparableExpression exp = (ComparableExpression) exp1;
				queryMixin.where(exp.loe((Comparable) value));
			}
			break;
		}
		case GT:{
			if(exp1 instanceof NumberExpression) {
				NumberExpression exp=(NumberExpression)exp1;
				queryMixin.where(exp.gt((Number) value));	
			}else {
				ComparableExpression exp = (ComparableExpression) exp1;
				queryMixin.where(exp.gt((Comparable) value));
			}
			break;
		}
		case GOE:{
			if(exp1 instanceof NumberExpression) {
				NumberExpression exp=(NumberExpression)exp1;
				queryMixin.where(exp.goe((Number) value));	
			}else {
				ComparableExpression exp = (ComparableExpression) exp1;
				queryMixin.where(exp.goe((Comparable) value));
			}
			break;
		}
		case BETWEEN:{
			ComparableExpression exp = (ComparableExpression) exp1;
			if(value instanceof Collection<?>) {
				List<? extends Comparable> list=toList((Collection<Comparable>)value);
				if(list.size()<2) {
					throw new IllegalArgumentException("Invalid param, the value for between condition must be 2 elements.");
				}
				queryMixin.where(exp.between(list.get(0),list.get(1)));
			}else if(value instanceof Object[]) {
				Object[] bvalue=(Object[])value;
				if(bvalue.length<2) {
					throw new IllegalArgumentException("Invalid param, the value for between condition must be 2 elements.");
				}
				queryMixin.where(exp.in((Comparable)bvalue[0],(Comparable)bvalue[1]));
			}
			break;
		}
		case STARTS_WITH:{
			StringExpression exp=(StringExpression)exp1;
			queryMixin.where(exp.startsWith(String.valueOf(value)));
			break;
		}
		case STARTS_WITH_IC:{
			StringExpression exp=(StringExpression)exp1;
			queryMixin.where(exp.startsWithIgnoreCase(String.valueOf(value)));
			break;
		}
		case ENDS_WITH:{
			StringExpression exp=(StringExpression)exp1;
			queryMixin.where(exp.endsWith(String.valueOf(value)));
			break;
		}
		case ENDS_WITH_IC:{
			StringExpression exp=(StringExpression)exp1;
			queryMixin.where(exp.endsWithIgnoreCase(String.valueOf(value)));
			break;
		}
		case STRING_CONTAINS:{
			StringExpression exp=(StringExpression)exp1;
			queryMixin.where(exp.contains(String.valueOf(value)));
			break;
		}
		case LIKE:{
			StringExpression exp=(StringExpression)exp1;
			queryMixin.where(exp.like(String.valueOf(value)));
			break;
		}
		case LIKE_IC:{
			StringExpression exp=(StringExpression)exp1;
			queryMixin.where(exp.likeIgnoreCase(String.valueOf(value)));
			break;
		}
		case IS_NULL:	{
			SimpleExpression exp = (SimpleExpression) exp1;
			queryMixin.where(exp.isNull());
			break;
		}
		case IS_NOT_NULL:{
			SimpleExpression exp = (SimpleExpression) exp1;
			queryMixin.where(exp.isNotNull());
			break;
		}
		default:
			throw new UnsupportedOperationException("Ops." + ops + " is not supported on field " + field.getDeclaringClass().getName() + "." + field.getName());
		}
	}

	@SuppressWarnings({ "rawtypes"})
	private boolean hasElements(Object value) {
		if(value==null) {
			return false;
		}
		if(value instanceof Collection) {
			return ((Collection) value).isEmpty();
		}
		if(value instanceof Object[]) {
			return ((Object[])value).length==0;
		}
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<? extends Comparable> toList(Collection<? extends Comparable> value) {
		if(value instanceof List) {
			return (List<Comparable>)value;
		}else {
			return new ArrayList<>(value);
		}
		
	}

}
