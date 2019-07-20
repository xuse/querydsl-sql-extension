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

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Provider;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.sql.ddl.AlterTableQuery;
import com.github.xuse.querydsl.sql.ddl.CreateConstraintQuery;
import com.github.xuse.querydsl.sql.ddl.CreateIndexQuery;
import com.github.xuse.querydsl.sql.ddl.CreateTableQuery;
import com.github.xuse.querydsl.sql.ddl.DropConstraintQuery;
import com.github.xuse.querydsl.sql.ddl.DropIndexQuery;
import com.github.xuse.querydsl.sql.ddl.DropTableQuery;
import com.github.xuse.querydsl.sql.spring.SpringProvider;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLCloseListener;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.UnmanagedConnectionCloseListener;
import com.querydsl.sql.mssql.SQLServerQueryFactory;
import com.querydsl.sql.mysql.MySQLQueryFactory;
import com.querydsl.sql.oracle.OracleQueryFactory;
import com.querydsl.sql.postgresql.PostgreSQLQueryFactory;

/**
 * Factory class for query and DML clause creation
 *
 * @author Joey
 *
 */
public class SQLQueryFactory extends AbstractSQLQueryFactory<SQLQueryAlter<?>> {
	Logger log = LoggerFactory.getLogger(SQLQueryFactory.class);

	public SQLQueryFactory(SQLTemplates templates, Provider<Connection> connection) {
		this(new Configuration(templates), connection);
	}

	public SQLQueryFactory(Configuration configuration, Provider<Connection> connProvider) {
		super(configuration, connProvider);
		log.info("Init QueryDSL Factory(extension) with {}.", configuration.getTemplates().getClass().getName());
	}

	public SQLQueryFactory(Configuration configuration, DataSource dataSource) {
		this(configuration, dataSource, true);
	}

	public SQLQueryFactory(Configuration configuration, DataSource dataSource, boolean release) {
		super(configuration, new DataSourceProvider(dataSource));
		if (release) {
			configuration.addListener(SQLCloseListener.DEFAULT);
		}
		log.info("Init QueryDSL Factory(extension) with {}.", configuration.getTemplates().getClass().getName());
	}

	public static SQLQueryFactory createSpringQueryFactory(DataSource datasource, Configuration configuration) {
		// 用于关闭连接非事务状态下创建的连接。
		configuration.addListener(UnmanagedConnectionCloseListener.DEFAULT);
		return new SQLQueryFactory(configuration, new SpringProvider(datasource));
	}

	static class DataSourceProvider implements Provider<Connection> {
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
		return new com.querydsl.sql.SQLQueryFactory(configuration, connection);
	}

	public MySQLQueryFactory asMySQL() {
		return new com.querydsl.sql.mysql.MySQLQueryFactory(configuration, connection);
	}

	
	public SQLServerQueryFactory asSQLServer() {
		return new SQLServerQueryFactory(configuration, connection);
	}

	public OracleQueryFactory asOracle() {
		return new OracleQueryFactory(configuration, connection);
	}
	
	public PostgreSQLQueryFactory asPostgreSQL() {
		return new PostgreSQLQueryFactory(configuration, connection);
	}

	@Override
	public SQLQueryAlter<?> query() {
		return new SQLQueryAlter<Void>(connection, configuration);
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
	public <T> CreateTableQuery createTable(RelationalPathBase<T> path) {
		return new CreateTableQuery(connection, configuration, path);
	}

	@Override
	public <T> DropTableQuery dropTable(RelationalPathBase<T> path) {
		return new DropTableQuery(connection, configuration, path);
	}

	@Override
	public <T> AlterTableQuery alterTable(RelationalPathBase<T> path) {
		return new AlterTableQuery(connection, configuration, path);
	}

	@Override
	public <T> CreateIndexQuery createIndex(RelationalPathBase<T> path) {
		return new CreateIndexQuery(connection, configuration, path);
	}

	@Override
	public <T> DropIndexQuery dropIndex(RelationalPathBase<T> path) {
		return new DropIndexQuery(connection, configuration, path);
	}

	@Override
	public <T> CreateConstraintQuery createContraint(RelationalPathBase<T> path) {
		return new CreateConstraintQuery(connection, configuration, path);
	}

	@Override
	public <T> DropConstraintQuery dropConstraint(RelationalPathBase<T> path) {
		return new DropConstraintQuery(connection, configuration, path);
	}
}
