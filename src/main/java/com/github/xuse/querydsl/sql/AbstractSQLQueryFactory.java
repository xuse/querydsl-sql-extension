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
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLMergeClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.AbstractSQLQuery;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLCommonQuery;
import com.querydsl.sql.SQLCommonQueryFactory;
import com.querydsl.sql.SQLCommonQueryFactoryAlter;



/**
 * {@code AbstractSQLQueryFactory} is the base class for {@link SQLCommonQueryFactory} implementations
 *
 * @param <Q> query type
 *
 * @author tiwe
 */
public abstract class AbstractSQLQueryFactory<Q extends SQLCommonQuery<?>> implements SQLCommonQueryFactoryAlter<Q,
    SQLDeleteClauseAlter, SQLUpdateClauseAlter, SQLInsertClauseAlter, SQLMergeClauseAlter>,SQLMetadataQueryFactory,ISQLFactoryEx {

    protected final ConfigurationEx configuration;

    protected final Supplier<Connection> connection;

    public AbstractSQLQueryFactory(ConfigurationEx configuration, Supplier<Connection> connProvider) {
        this.configuration = configuration;
        this.connection = connProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Q from(Expression<?> from) {
        return (Q) query().from(from);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Q from(Expression<?>... args) {
        return (Q) query().from(args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Q from(SubQueryExpression<?> subQuery, Path<?> alias) {
        return (Q) query().from(subQuery, alias);
    }

    public final ConfigurationEx getConfiguration() {
        return configuration;
    }

    public final Connection getConnection() {
        return connection.get();
    }

    /**
     * Create a new SQL query with the given projection
     *
     * @param expr projection
     * @param <T> type of the projection
     * @return select(expr)
     */
    public abstract <T> AbstractSQLQuery<T, ?> select(Expression<T> expr);

    /**
     * Create a new SQL query with the given projection
     *
     * @param exprs projection
     * @return select(exprs)
     */
    public abstract AbstractSQLQuery<Tuple, ?> select(Expression<?>... exprs);

    /**
     * Create a new SQL query with the given projection
     *
     * @param expr distinct projection
     * @param <T> type of the projection
     * @return select(distinct expr)
     */
    public abstract <T> AbstractSQLQuery<T, ?> selectDistinct(Expression<T> expr);

    /**
     * Create a new SQL query with the given projection
     *
     * @param exprs distinct projection
     * @return select(distinct exprs)
     */
    public abstract AbstractSQLQuery<Tuple, ?> selectDistinct(Expression<?>... exprs);

    /**
     * Create a new SQL query with zero as the projection
     *
     * @return select(0)
     */
    public abstract AbstractSQLQuery<Integer, ?> selectZero();

    /**
     * Create a new SQL query with one as the projection
     *
     * @return select(1)
     */
    public abstract AbstractSQLQuery<Integer, ?> selectOne();

    /**
     * Create a new SQL query with the given projection and source
     *
     * @param expr query source and projection
     * @param <T> type of the projection
     * @return select(expr).from(expr)
     */
    public abstract <T> AbstractSQLQuery<T, ?> selectFrom(RelationalPath<T> expr);

}
