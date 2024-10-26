package com.github.xuse.querydsl.r2dbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.r2dbc.core.dml.Delete;
import com.github.xuse.querydsl.r2dbc.core.dml.Dummy;
import com.github.xuse.querydsl.r2dbc.core.dml.Insert;
import com.github.xuse.querydsl.r2dbc.core.dml.R2Clause;
import com.github.xuse.querydsl.r2dbc.core.dml.SQLContainer;
import com.github.xuse.querydsl.r2dbc.core.dml.SQLQueryR2;
import com.github.xuse.querydsl.r2dbc.core.dml.Update;
import com.github.xuse.querydsl.r2dbc.jdbcwrapper.R2ResultWrapper;
import com.github.xuse.querydsl.r2dbc.jdbcwrapper.R2StatementWrapper;
import com.github.xuse.querydsl.r2dbc.listener.R2BaseListener;
import com.github.xuse.querydsl.r2dbc.listener.R2ListenerContextImpl;
import com.github.xuse.querydsl.r2dbc.listener.R2Listeners;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * R2dbc query factory
 * 
 * @author Joey
 *
 */
public class R2dbcFactory {
	protected final ConfigurationEx configEx;

	protected final ConnectionFactory connection;
	
	private final R2BaseListener listeners;
	
	private final Function<Connection, Mono<Void>> closeHandler;

	public R2dbcFactory(ConnectionFactory connection, ConfigurationEx configEx) {
		this(connection,configEx,c->(Mono<Void>)c.close());	
	}
	
	public R2dbcFactory(ConnectionFactory connection, ConfigurationEx configEx,Function<Connection, Mono<Void>> closeHandler) {
		super();
		this.closeHandler =closeHandler;
		this.configEx = configEx;
		this.connection = connection;
		listeners=R2Listeners.wrap(configEx.get().getListeners());
	}

	public <T> R2Fetchable<T> select(Expression<T> expr) {
		SQLQueryR2<T> query = new SQLQueryR2<T>(configEx);
		query.select(expr);
		return new R2Fetchable<>(query);
	}

	public R2Fetchable<Tuple> select(Expression<?>... exprs) {
		SQLQueryR2<Tuple> query = new SQLQueryR2<Tuple>(configEx);
		query.select(exprs);
		return new R2Fetchable<>(query);
	}

	public <T> R2Fetchable<T> selectFrom(RelationalPath<T> expr) {
		SQLQueryR2<T> query = new SQLQueryR2<T>(configEx);
		query.select(expr).from(expr);
		return new R2Fetchable<>(query);
	}
	public <T> R2Fetchable<T> selectFrom(LambdaTable<T> expr) {
		SQLQueryR2<T> query = new SQLQueryR2<T>(configEx);
		query.select(expr).from(expr);
		return new R2Fetchable<>(query);
	}
	
	public <E> R2Executable<Insert<E>,E> insert(RelationalPath<E> path) {
		return new R2Executable<>(new Insert<>(configEx, path),path);
	}
	public final <E> R2Executable<Update<E>,E> update(RelationalPath<E> path) {
		return new R2Executable<>(new Update<>(configEx, path),path);
	}
	public final <E> R2Executable<Delete<E>,E> delete(RelationalPath<E> path) {
		return new R2Executable<>(new Delete<>(configEx, path),path);
	}
	public <E> R2Executable<Insert<E>,E> insert(LambdaTable<E> path) {
		return new R2Executable<>(new Insert<>(configEx, path),path);
	}
	public final <E> R2Executable<Update<E>,E> update(LambdaTable<E> path) {
		return new R2Executable<>(new Update<>(configEx, path),path);
	}
	public final <E> R2Executable<Delete<E>,E> delete(LambdaTable<E> path) {
		return new R2Executable<>(new Delete<>( configEx, path),path);
	}
	///////////////////////////////////////////////////////////////////////////////
	public class R2Fetchable<R>{
		final SQLQueryR2<R> query;
		private SQLBindings sqls;
		private R2ListenerContextImpl context;
		
		R2Fetchable(SQLQueryR2<R> clause) {
			this.query = clause;
		}

		public R2Fetchable<R> prepare(Consumer<SQLQueryR2<R>> consumer){
			if(consumer!=null)
				consumer.accept(query);
			return this;
		}
		
		public @NotNull Flux<R> fetch() {
			context = startContext(query.getMetadata(),null);
			listeners.preRender(context);
			return Mono.just(query.getSQL(false)).flatMapMany(this::fetch0);
		}
		
		public @NotNull Mono<R> fetchFirst() {
			context =  startContext(query.getMetadata(),null);
			QueryMetadata metadata=query.getMetadata();
			if (metadata.getModifiers().getLimit() == null
					&& !String.valueOf(metadata.getProjection()).contains("count(")) {
				metadata.setLimit(2L);
			}
			listeners.preRender(context);
			return Mono.just(query.getSQL(false)).flatMapMany(this::fetch0).next();
		}

		public @NotNull Mono<Long> fetchCount() {
			context = startContext(query.getMetadata(),null);
			listeners.preRender(context);
			return Mono.just(query.getSQL(true)).flatMap(this::fetchCnt);	
		}
		//////////////////// private methods //////////////////
		
		//使用{#usingWhen}方法来关闭连接。
		private @NotNull Flux<R> fetch0(SQLBindings sql) {
			this.sqls = sql;
			context.addSQL(sql);
			listeners.rendered(context);
			query.notifyAction(listeners, context);
			context.setData(ContextKeyConstants.ACTION, "Fetch");
			return Flux.usingWhen(connection.create(),
				conn-> Flux.from(createStatement(conn, sql, context).execute()).flatMap(this::transform).doOnNext(this::onFetchNext).doOnComplete(this::postFetch),
				this::close);
		}
		
		//使用{#usingWhen}方法来关闭连接。
		private Mono<Long> fetchCnt(SQLBindings sql) {
			this.sqls = sql;
			context.addSQL(sql);
			listeners.rendered(context);
			query.notifyAction(listeners, context);
			context.setData(ContextKeyConstants.ACTION, "Count");
			return Mono.usingWhen(connection.create(), conn-> 
				Flux.from(createStatement(conn, sql, context).execute()).flatMap(this::transformLong).next().doOnSuccess(this::postCount),
				this::close);
		}
		
		private void postFetch() {
			postCount(context.fetchCount);
		}
		
		private void postCount(long count) {
			R2ListenerContextImpl context = this.context;
			long cost=context.markEnd();
			context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
			context.setData(ContextKeyConstants.COUNT, count);
			if (configEx.getSlowSqlWarnMillis() <= cost) {
				context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
			}
			listeners.executed(context);
			endContext(context);
		}
		
		private void onFetchNext(R r) {
			context.resultIncrement();
		}
		
		private Publisher<Long> transformLong(Result result){
			return result.map((r, m) -> r.get(0, Long.class));
		}

		private Publisher<R> transform(Result result) {
			return result.map(getProjection());
		}

		private AbstractProjection<R> getProjection(){
			@SuppressWarnings("unchecked")
			Expression<R> expr = (Expression<R>) query.getMetadata().getProjection();
			AbstractProjection<R> fe;
			if (expr instanceof FactoryExpression) {
				fe = new FactoryExpressionResult<R>((FactoryExpression<R>) expr);
			} else if (expr == null) {
				fe = new DefaultValueResult<>();
			} else if (expr.equals(Wildcard.all)) {
				fe = new WildcardAllResult<>();
			} else {
				fe = new SingleValueResult<>(expr);
			}
			return fe;
		}

		abstract class AbstractProjection<RT> implements BiFunction<Row,RowMetadata,RT> {
			@Override
			public RT apply(Row r,RowMetadata meta) {
				try {
					return convert(r,meta);
				}catch(SQLException e) {
					if(sqls!=null) {
						throw configEx.translate(sqls.getSQL(), sqls.getNullFriendlyBindings(), e);	
					}else {
						throw configEx.translate("", Collections.emptyList(), e);
					}
				}
			}

			protected abstract RT convert(Row rs,RowMetadata meta)throws SQLException;
		}

		/*
		 * 标准返回值Projection，根据FactoryExpression的转换规则返回
		 */
		final class FactoryExpressionResult<RT> extends AbstractProjection<RT> {
			private final FactoryExpression<RT> expr;
			private final int argSize;
			private final Path<?>[] argPath;
			private final Class<?>[] argTypes;
			
			private final R2ResultWrapper wrapper=new R2ResultWrapper();
			
			FactoryExpressionResult(FactoryExpression<RT> factoryExpr) {
				List<Expression<?>> args = factoryExpr.getArgs();
				int argSize = args.size();
				this.expr = factoryExpr;
				this.argSize = argSize;
				this.argPath = new Path<?>[argSize];
				this.argTypes = new Class<?>[argSize];
				for (int i = 0; i < argSize; i++) {
					Expression<?> expr = args.get(i);
					argPath[i] = (expr instanceof Path ? (Path<?>) expr : null);
					argTypes[i] = expr.getType();
				}
			}

			protected final RT convert(Row row,RowMetadata meta) throws SQLException {
				wrapper.prepare(row,meta);
				int argSize= this.argSize;
				Configuration configuration = R2dbcFactory.this.configEx.get();
				Object[] args = new Object[argSize];
				for (int i = 0; i < argSize; i++) {
					try {
						args[i] = configuration.get(wrapper, argPath[i], i + 1, argTypes[i]);
					} catch (SQLException ex) {
						throw ex;
					} catch (Exception ex) {
						throw new SQLException("get field:" + argPath[i] + "error", ex);
					}
				}
				return expr.newInstance(args);
			}
		}

		/*
		 * 以Object[]形式返回所有列
		 */
		final class WildcardAllResult<RT> extends AbstractProjection<RT> {
			private int columnSize = -1;
			public WildcardAllResult() {
				super();
			}
			@SuppressWarnings("unchecked")
			@Override
			protected RT convert(Row rs, RowMetadata meta) {
				int size;
				if(columnSize>-1) {
					size = columnSize;
				}else {
					size = columnSize = meta.getColumnMetadatas().size();
				}
				Object[] row = new Object[size];
				for (int i = 0; i < size; i++) {
					row[i] = rs.get(i);
				}
				return (RT) row;
			}
		}
		/*
		 * 仅获取结果集第一列的Path数据类型
		 */
		final class SingleValueResult<RT> extends AbstractProjection<RT> {
			private final Expression<RT> expr;
			private final Path<?> path;
			private final R2ResultWrapper wrapper=new R2ResultWrapper();

			public SingleValueResult(Expression<RT> expr) {
				this.expr = expr;
				this.path = expr instanceof Path ? (Path<?>) expr : null;
			}
			@Override
			protected RT convert(Row rs, RowMetadata meta) throws SQLException {
				Configuration configuration = R2dbcFactory.this.configEx.get();
				wrapper.prepare(rs, meta);
				return configuration.get(wrapper, path, 1, expr.getType());
			}
		}
		/*
		 * 仅获取结果集第一列的原始数据类型
		 */
		final class DefaultValueResult<RT> extends AbstractProjection<RT> {
			@SuppressWarnings("unchecked")
			@Override
			protected RT convert(Row rs, RowMetadata meta){
				return (RT) rs.get(0);
			}
		}
		
		private Mono<Void> close(Connection connection) {
	    	if (connection != null && context.getData(R2ListenerContextImpl.PARENT_CONTEXT) == null) {
	    		return closeHandler.apply(connection);
	    	}
	    	return Mono.empty();
	    }
	}

	public class R2Executable<T extends SQLContainer,E>{
		final T clause;
		private R2ListenerContextImpl context;
		private final RelationalPath<E> entity;
		protected final R2Clause r2Clause;

		public R2Executable(T clause, RelationalPath<E> entity) {
			this.clause = clause;
			this.entity = entity;
			this.r2Clause = (clause instanceof R2Clause) ? (R2Clause) clause : Dummy.R2Clause;
		}

		public R2Executable<T,E> prepare(Consumer<T> consumer) {
			if(consumer!=null)
				consumer.accept(clause);
			return this;
		}

		public Mono<Long> execute() {
			context = startContext(r2Clause.getMetadata(), entity);
			List<SQLBindings> sqls = clause.getSQL();
			
			context.addSQLs(sqls);	
			listeners.rendered(context);
			String action = r2Clause.notifyAction(listeners, context);
			context.setData(ContextKeyConstants.ACTION, action);
			return Flux.fromIterable(sqls).flatMap(this::execute).reduce(Long::sum);
		}

		private Mono<Long> execute(SQLBindings sqls) {
			return Mono.usingWhen(connection.create(),
					conn-> Flux.from(createStatement(conn, sqls, context).execute()).flatMap(Result::getRowsUpdated).reduce(Long::sum).doOnSuccess(this::postExecute),
					this::close);
		}
		
		private void postExecute(long count) {
			R2ListenerContextImpl context = this.context;
			long cost=context.markEnd();
			context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
			context.setData(ContextKeyConstants.COUNT, count);
			if (configEx.getSlowSqlWarnMillis() <= cost) {
				context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
			}
			listeners.executed(context);
		}
		
		private Mono<Void> close(Connection connection) {
	    	if (connection != null && context.getData(R2ListenerContextImpl.PARENT_CONTEXT) == null) {
	    		return closeHandler.apply(connection);
	    	}
	    	return Mono.empty();
	    }
	}

    protected R2ListenerContextImpl startContext(QueryMetadata metadata,RelationalPath<?> entity) {
        R2ListenerContextImpl context = new R2ListenerContextImpl(metadata, entity);
        listeners.start(context);
        return context;
    }
    
	public void endContext(R2ListenerContextImpl context) {
		listeners.end(context);
		context.clear();
	}

	private Statement createStatement(io.r2dbc.spi.Connection conn, SQLBindings binding,R2ListenerContextImpl context) {
		listeners.prePrepare(context);
		Statement stmt=conn.createStatement(binding.getSQL());
		listeners.prepared(context);
		Configuration config = configEx.get();
		List<Path<?>> paths = CollectionUtils.nullElementsList();
		if (binding instanceof SQLBindingsAlter) {
			paths = ((SQLBindingsAlter) binding).getPaths();
		}
		List<Object> objects = binding.getNullFriendlyBindings();
		PreparedStatement wrapped = new R2StatementWrapper(stmt);
		try {
			for (int i = 0; i < objects.size(); ) {
				Path<?> path = paths.get(i);
				Object value = objects.get(i);
				config.set(wrapped, path, ++i, value);
			}
		} catch (SQLException ex) {
			throw Exceptions.toRuntime(ex);
		}
		listeners.preExecute(context);
		context.markStartTime();
		return stmt;
	}
}
