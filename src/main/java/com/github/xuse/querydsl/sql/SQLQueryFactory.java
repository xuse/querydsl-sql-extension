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
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dialect.MySQLWithJSONTemplates;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLMergeClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.spring.SpringProvider;
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
	private final ConfigurationEx configEx;

	Logger log = LoggerFactory.getLogger(SQLQueryFactory.class);

	public SQLQueryFactory(SQLTemplates templates, Supplier<Connection> connection) {
		this(new ConfigurationEx(templates), connection);
		
	}

	public SQLQueryFactory(ConfigurationEx configuration, Supplier<Connection> connProvider) {
		super(configuration, connProvider);
		this.configEx=configuration;
		log.info("Init QueryDSL Factory(extension) with {}.", configuration.getTemplates().getClass().getName());
	}

	public SQLQueryFactory(ConfigurationEx configuration, DataSource dataSource) {
		this(configuration, dataSource, true);
	}

	public SQLQueryFactory(ConfigurationEx configuration, DataSource dataSource, boolean release) {
		super(configuration, new DataSourceProvider(dataSource));
		this.configEx=configuration;
		if (release) {
			configuration.addListener(SQLCloseListener.DEFAULT);
		}
		log.info("Init QueryDSL Factory(extension) with {}.", configuration.getTemplates().getClass().getName());
	}

	/**
	 * 根据URL计算使用的SQL模板
	 * @param url
	 * @return SQLTemplates
	 */
	public static SQLTemplates calcSQLTemplate(String url) {
		if(url.startsWith("jdbc:mysql:")) {
			return new MySQLWithJSONTemplates() ;
		}else if(url.startsWith("jdbc:derby:")) {
			return DerbyTemplates.builder().build();
		}else if(url.startsWith("jdbc:postgresql:")) {
			return new PostgreSQLTemplates();
		}else if(url.startsWith("jdbc:sqlserver")) {
			return new SQLServer2012Templates();
		}else if(url.startsWith("jdbc:oracle:")) {
			return new OracleTemplates();
		}
		throw Exceptions.illegalArgument(url);
	}

	public static SQLQueryFactory createSpringQueryFactory(DataSource datasource, ConfigurationEx configuration) {
		// 用于关闭连接非事务状态下创建的连接。
		configuration.addListener(UnmanagedConnectionCloseListener.DEFAULT);
		return new SQLQueryFactory(configuration, new SpringProvider(datasource));
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
	
	protected ConfigurationEx getConfigurationEx() {
		return configEx;
	}

	public com.querydsl.sql.SQLQueryFactory asRaw() {
		return new com.querydsl.sql.SQLQueryFactory(configuration.get(), connection);
	}

	public MySQLQueryFactory asMySQL() {
		return new com.querydsl.sql.mysql.MySQLQueryFactory(configuration.get(), connection);
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
    
    public boolean isInSpringTransaction() {
    	if(connection instanceof  SpringProvider) {
    		return ((SpringProvider) connection).isTx();
    	}
    	throw new UnsupportedOperationException();
    }

	@Override
	public SQLMetadataQueryFactory getMetadataFactory() {
		return new SQLMetadataFactoryImpl(connection,configEx);
	}
}
