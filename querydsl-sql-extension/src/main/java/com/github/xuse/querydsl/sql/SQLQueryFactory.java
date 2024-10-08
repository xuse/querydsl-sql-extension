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

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.init.InitProcessor;
import com.github.xuse.querydsl.init.TableDataInitializer;
import com.github.xuse.querydsl.lambda.LambdaTable;
import com.github.xuse.querydsl.lambda.PathCache;
import com.github.xuse.querydsl.repository.AbstractCrudRepository;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dialect.MySQLWithJSONTemplates;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLMergeClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.extension.ExtensionQueryFactory;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLCloseListener;
import com.querydsl.sql.SQLServer2012Templates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.mssql.SQLServerQueryFactory;
import com.querydsl.sql.oracle.OracleQueryFactory;
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory;

/**
 * Factory class for query and DML clause creation
 *
 * @author Joey
 */
public class SQLQueryFactory extends AbstractSQLQueryFactory<SQLQueryAlter<?>> implements SQLFactoryExtension{

	private final ConfigurationEx configEx;
	
	private final Map<Class<?>, ExtensionQueryFactory> extensions = new ConcurrentHashMap<>();

	Logger log = LoggerFactory.getLogger(SQLQueryFactory.class);

	public SQLQueryFactory(SQLTemplates templates, Supplier<Connection> connection) {
		this(new ConfigurationEx(templates), connection);
	}

	public SQLQueryFactory(ConfigurationEx configuration, Supplier<Connection> connProvider) {
		super(configuration, connProvider);
		this.configEx = configuration;
		log.info("Init QueryDSL Factory(extension) with {}.", configuration.getTemplates().getClass().getName());
		tryInitTask(configuration);
	}

	public SQLQueryFactory(ConfigurationEx configuration, DataSource dataSource) {
		this(configuration, dataSource, true);
	}

	public SQLQueryFactory(ConfigurationEx configuration, DataSource dataSource, boolean release) {
		super(configuration, new DataSourceProvider(dataSource));
		this.configEx = configuration;
		if (release) {
			configuration.addListener(SQLCloseListener.DEFAULT);
		}
		log.info("Init QueryDSL Factory(extension) with {}.", configuration.getTemplates().getClass().getName());
		tryInitTask(configuration);
	}

	protected void tryInitTask(ConfigurationEx configuration) {
		InitProcessor task = new InitProcessor(this, configuration.getScanOptions());
		task.run();
	}

	/**
	 * 根据URL计算使用的SQL模板
	 * @param url url
	 * @return SQLTemplates
	 */
	public static SQLTemplates calcSQLTemplate(String url) {
		if (url.startsWith("jdbc:mysql:")) {
			return new MySQLWithJSONTemplates();
		} else if (url.startsWith("jdbc:derby:")) {
			return DerbyTemplates.builder().build();
		} else if (url.startsWith("jdbc:postgresql:")) {
			return new PostgreSQLTemplates();
		} else if (url.startsWith("jdbc:sqlserver")) {
			return new SQLServer2012Templates();
		} else if (url.startsWith("jdbc:oracle:")) {
			return new OracleTemplates();
		} 
		throw Exceptions.illegalArgument(url);
	}

	static class DataSourceProvider implements Supplier<Connection> {

		private final DataSource ds;

		public DataSourceProvider(DataSource ds) {
			this.ds = ds;
		}

		@Override
		public Connection get() {
			try {
				return ds.getConnection();
			} catch (SQLException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

	public com.querydsl.sql.SQLQueryFactory asRaw() {
		return new com.querydsl.sql.SQLQueryFactory(configuration.get(), connection);
	}

	public MySQLQueryFactory2 asMySQL() {
		return new MySQLQueryFactory2(configuration, connection);
	}

	public SQLServerQueryFactory asSQLServer() {
		return new SQLServerQueryFactory(configuration.get(), connection);
	}

	public OracleQueryFactory asOracle() {
		return new OracleQueryFactory(configuration.get(), connection);
	}

	public PostgreSQLQueryFactory asPostgreSQL() {
		return new PostgreSQLQueryFactory(configuration.get(), connection);
	}

	@Override
	public SQLQueryAlter<?> query() {
		return new SQLQueryAlter<Void>(connection, configEx);
	}

	@Override
	public <T> SQLQueryAlter<T> select(Expression<T> expr) {
		return query().select(expr);
	}

	@Override
	public SQLQueryAlter<Tuple> select(Expression<?>... exprs) {
		return query().select(exprs);
	}

	@Override
	public <T> SQLQueryAlter<T> selectDistinct(Expression<T> expr) {
		return query().select(expr).distinct();
	}

	@Override
	public SQLQueryAlter<Tuple> selectDistinct(Expression<?>... exprs) {
		return query().select(exprs).distinct();
	}

	@Override
	public SQLQueryAlter<Integer> selectZero() {
		return select(Expressions.ZERO);
	}

	@Override
	public SQLQueryAlter<Integer> selectOne() {
		return select(Expressions.ONE);
	}

	@Override
	public <T> SQLQueryAlter<T> selectFrom(RelationalPath<T> expr) {
		return select(expr).from(expr);
	}

	@Override
	public final SQLDeleteClauseAlter delete(RelationalPath<?> path) {
		return new SQLDeleteClauseAlter(connection, configEx, path);
	}
	
	@Override
	public final SQLInsertClauseAlter insert(RelationalPath<?> path) {
		return new SQLInsertClauseAlter(connection, configEx, path);
	}

	@Override
	public final SQLMergeClauseAlter merge(RelationalPath<?> path) {
		return new SQLMergeClauseAlter(connection, configEx, path);
	}

	@Override
	public final SQLUpdateClauseAlter update(RelationalPath<?> path) {
		return new SQLUpdateClauseAlter(connection, configEx, path);
	}
	
	//Support for Lambda tables.
	public final <T> SQLDeleteClauseAlter delete(LambdaTable<T> path) {
		return new SQLDeleteClauseAlter(connection, configEx, path);
	}
	
	public final <T> SQLInsertClauseAlter insert(LambdaTable<T> path) {
		return new SQLInsertClauseAlter(connection, configEx, path);
	}

	public final <T> SQLUpdateClauseAlter update(LambdaTable<T> path) {
		return new SQLUpdateClauseAlter(connection, configEx, path);
	}

	public final <T> SQLMergeClauseAlter merge(LambdaTable<T> path) {
		return new SQLMergeClauseAlter(connection, configEx, path);
	}
	
	public <T> SQLQueryAlter<T> selectFrom(LambdaTable<T> expr) {
		return select(expr).from(expr);
	}

//	public boolean isInSpringTransaction() {
//		if (connection instanceof SpringProvider) {
//			return ((SpringProvider) connection).isTx();
//		}
//		return false;
//	}

	@Override
	public SQLMetadataQueryFactory getMetadataFactory() {
		return new SQLMetadataFactoryImpl(this);
	}

	/**
	 *  @param level one of the following <code>Connection</code> constants:
	 *         <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>,
	 *         <code>Connection.TRANSACTION_READ_COMMITTED</code>,
	 *         <code>Connection.TRANSACTION_REPEATABLE_READ</code>, or
	 *         <code>Connection.TRANSACTION_SERIALIZABLE</code>.
	 *         (Note that <code>Connection.TRANSACTION_NONE</code> cannot be used
	 *         because it specifies that transactions are not supported.)
	 *  @return CloseableSQLQueryFactory. must call close method for resource release
	 */
	public CloseableSQLQueryFactory oneConnectionSession(int level) {
		try {
			Connection conn = connection.get();
			if (level > 0) {
//				if (isInSpringTransaction()) {
//					log.warn("Unable to set transaction isolation because current session is Spring managed transaction.");
//				} else {
					conn.setTransactionIsolation(level);
//				}
			}
			return new CloseableSQLQueryFactory(configEx, conn);
		} catch (SQLException e) {
			throw configuration.get().translate(e);
		}
	}

	@Override
	public TableDataInitializer initializeTable(RelationalPath<?> table) {
		return new TableDataInitializer(this, table);
	}
	
	/**
	 * create a {@link CRUDRepository} object. the CRUDRepository provides api in another style to access the database.
	 * @param <T> the mapping class type of table.
	 * @param <ID> the primary key type of the table.
	 * @param path relational path.
	 * @return CRUDRepository  the CRUDRepository
	 * @see CRUDRepository
	 */
	public <T, ID> CRUDRepository<T, ID> asRepository(RelationalPath<T> path){
		return new AbstractCrudRepository<T, ID>() {
			@Override
			protected SQLQueryFactory getFactory() {
				return SQLQueryFactory.this;
			}
			@Override
			protected RelationalPath<T> getPath() {
				return path;
			}
		};
	}
	
	public <T, ID> CRUDRepository<T, ID> asRepository(LambdaTable<T> clz){
		return asRepository(PathCache.get(clz.get(), null));
	}
	
	public <T, ID> CRUDRepository<T, ID> asRepository(RelationalPath<T> path,Class<ID> primaryKeyType){
		return asRepository(path);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ExtensionQueryFactory> T asExtension(Class<T> clz) {
		return (T) extensions.computeIfAbsent(clz, this::generateExtensionInstance);
	}
	
	ExtensionQueryFactory generateExtensionInstance(Class<?> clz) {
		try {
			Constructor<?> constructor=clz.getDeclaredConstructor(ConfigurationEx.class,Supplier.class);
			constructor.setAccessible(true);
			return (ExtensionQueryFactory) constructor.newInstance(configEx, connection);
		} catch (Exception e) {
			throw Exceptions.toRuntime(e);
		}
	}
	
}
