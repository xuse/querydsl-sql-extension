package com.github.xuse.querydsl.r2dbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.jdbcwrapper.R2ResultWrapper;
import com.github.xuse.querydsl.r2dbc.jdbcwrapper.R2StatementWrapper;
import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
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
import com.querydsl.sql.dml.AbstractSQLClause;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class R2dbFactory {
	protected final ConfigurationEx configEx;

	protected final ConnectionFactory connection;

	public R2dbFactory(ConnectionFactory connection, ConfigurationEx configEx) {
		super();
		this.configEx = configEx;
		this.connection = connection;
	}

	public R2Executeable<SQLInsertClauseAlter> insert(RelationalPath<?> t) {
		SQLInsertClauseAlter clause = new SQLInsertClauseAlter(null, configEx, t);
		return new R2Executeable<>(clause);
	}

	public <T> R2Fetchable<T> select(Expression<T> expr) {
		SQLQueryAlter<T> query = new SQLQueryAlter<T>((Supplier<Connection>) null, configEx);
		return new R2Fetchable<>(query.select(expr));
	}

	public R2Fetchable<Tuple> select(Expression<?>... exprs) {
		SQLQueryAlter<Tuple> query = new SQLQueryAlter<Tuple>((Supplier<Connection>) null, configEx);
		return new R2Fetchable<>(query.select(exprs));
	}
	
	public <T> R2Fetchable<T> selectFrom(RelationalPath<T> expr) {
		SQLQueryAlter<T> query = new SQLQueryAlter<T>((Supplier<Connection>) null, configEx);
		return new R2Fetchable<>(query.select(expr).from(expr));
	}
	


	///////////////////////////////////////////////////////////////////////////////
	public class R2Fetchable<R> {
		final SQLQueryAlter<R> query;
		private SQLBindings sqls;
		
		R2Fetchable(SQLQueryAlter<R> clause) {
			this.query = clause;
		}

		public R2Fetchable<R> prepare(Consumer<SQLQueryAlter<R>> consumer){
			if(consumer!=null)
				consumer.accept(query);
			return this;
		}
		
		public Flux<R> fetch() {
			return Mono.just(query.getSQL()).flatMapMany(this::fetch0);
		}

		public Mono<R> fetchFirst() {
			QueryMetadata metadata=query.getMetadata();
			if (metadata.getModifiers().getLimit() == null
					&& !metadata.getProjection().toString().contains("count(")) {
				metadata.setLimit(2L);
			}
			return Mono.just(query.getSQL()).flatMapMany(this::fetch0).next();
		}

		public Mono<Long> fetchCount() {
			return Mono.just(query.getSQL(true)).flatMap(this::fetchCnt);	
		}

		private Flux<R> fetch0(SQLBindings sqls) {
			this.sqls = sqls;
			return Mono.from(connection.create())
					.flatMapMany(conn -> binds(conn.createStatement(sqls.getSQL()), sqls).execute())
					.flatMap(this::transform);
		}
		
		private Mono<Long> fetchCnt(SQLBindings sqls) {
			this.sqls = sqls;
			return Mono.from(connection.create())
					.flatMapMany(conn -> binds(conn.createStatement(sqls.getSQL()), sqls).execute())
					.flatMap(this::transformLong).next();
		}
		
		private Flux<Long> transformLong(Result result){
			return Flux.from(result.map((r, m) -> r.get(0, Long.class)));
		}

		private Flux<R> transform(Result result) {
			return Flux.from(result.map(getProjection(result, false)));
		}

		private AbstractProjection<R> getProjection(Result rs, boolean getLastCell){
			@SuppressWarnings("unchecked")
			Expression<R> expr = (Expression<R>) query.getMetadata().getProjection();
			AbstractProjection<R> fe;
			if (expr instanceof FactoryExpression) {
				FactoryExpressionResult<R> r = new FactoryExpressionResult<R>((FactoryExpression<R>) expr);
				r.getLastCell = getLastCell;
				fe = r;
			} else if (expr == null) {
				DefaultValueResult<R> r = new DefaultValueResult<>();
				r.getLastCell = getLastCell;
				fe = r;
			} else if (expr.equals(Wildcard.all)) {
				WildcardAllResult<R> r = new WildcardAllResult<>();
				r.getLastCell = getLastCell;
				fe = r;
			} else {
				SingleValueResult<R> r = new SingleValueResult<>(expr);
				r.getLastCell = getLastCell;
				fe = r;
			}
			return fe;
		}

		abstract class AbstractProjection<RT> implements BiFunction<Row,RowMetadata,RT> {
			boolean getLastCell;
			Object lastCell;
			@Override
			public RT apply(Row r,RowMetadata meta) {
//				int argSize = getArgSize();
//				if (getLastCell) {
//					lastCell = rs.getObject(argSize + 1);
//					getLastCell = false;
//				}
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

			protected abstract int getArgSize();
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
				Configuration configuration = R2dbFactory.this.configEx.get();
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

			@Override
			protected int getArgSize() {
				return argSize;
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
			@Override
			protected int getArgSize() {
				return columnSize;
			}
			@SuppressWarnings("unchecked")
			@Override
			protected RT convert(Row rs, RowMetadata meta) throws SQLException {
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
			private R2ResultWrapper wrapper=new R2ResultWrapper();

			public SingleValueResult(Expression<RT> expr) {
				this.expr = expr;
				this.path = expr instanceof Path ? (Path<?>) expr : null;
			}
			@Override
			protected RT convert(Row rs, RowMetadata meta) throws SQLException {
				Configuration configuration = R2dbFactory.this.configEx.get();
				wrapper.prepare(rs, meta);
				return configuration.get(wrapper, path, 1, expr.getType());
			}
			@Override
			protected int getArgSize() {
				return 1;
			}
		}
		/*
		 * 仅获取结果集第一列的原始数据类型
		 */
		final class DefaultValueResult<RT> extends AbstractProjection<RT> {
			@Override
			protected int getArgSize() {
				return 1;
			}
			@SuppressWarnings("unchecked")
			@Override
			protected RT convert(Row rs, RowMetadata meta){
				return (RT) rs.get(0);
			}
		}
	}

	public class R2Executeable<T extends AbstractSQLClause<T>> {
		T clause;

		public R2Executeable(T clause) {
			this.clause = clause;
		}

		public R2Executeable<T> prepare(Consumer<T> consumer) {
			if(consumer!=null)
				consumer.accept(clause);
			return this;
		}

		public Mono<Long> execute() {
			List<SQLBindings> sqls = clause.getSQL();
			return Flux.fromIterable(sqls).flatMap(this::execute).reduce((a, b) -> a + b);
		}

		private Mono<Long> execute(SQLBindings sqls) {
			return Mono.from(connection.create())
					.flatMapMany(conn -> binds(conn.createStatement(sqls.getSQL()), sqls).execute())
					.flatMap(Result::getRowsUpdated).reduce((a, b) -> a + b);
		}
	}

	private Statement binds(Statement stmt, SQLBindings binding) {
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
		return stmt;
	}
}
