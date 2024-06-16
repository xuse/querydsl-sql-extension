package com.querydsl.sql;

import com.querydsl.core.QueryFactory;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.dml.AbstractSQLDeleteClause;
import com.querydsl.sql.dml.AbstractSQLInsertClause;
import com.querydsl.sql.dml.AbstractSQLUpdateClause;
import com.querydsl.sql.dml.SQLMergeClause;

public interface SQLCommonQueryFactoryAlter<Q extends SQLCommonQuery<?>, // extends AbstractSQLQuery<?>
    D extends AbstractSQLDeleteClause<?>,
    U extends AbstractSQLUpdateClause<?>,
    I extends AbstractSQLInsertClause<?>,
    M extends SQLMergeClause> extends QueryFactory<Q> {

    /**
     * Create a new DELETE clause
     *
     * @param path table to delete from
     * @return delete clause
     */
    D delete(RelationalPath<?> path);

    /**
     * Create a new SELECT query
     *
     * @param from query source
     * @return query
     */
    Q from(Expression<?> from);

    /**
     * Create a new SELECT query
     *
     * @param from query sources
     * @return query
     */
    Q from(Expression<?>... from);

    /**
     * Create a new SELECT query
     *
     * @param subQuery query source
     * @param alias alias
     * @return query
     */
    Q from(SubQueryExpression<?> subQuery, Path<?> alias);

    /**
     * Create a new INSERT INTO clause
     *
     * @param path table to insert to
     * @return insert clause
     */
    I insert(RelationalPath<?> path);

    /**
     * Create a new MERGE clause
     *
     * @param path table to merge into
     * @return merge clause
     */
    M merge(RelationalPath<?> path);

    /**
     * Create a new UPDATE clause
     *
     * @param path table to update
     * @return update clause
     */
    U update(RelationalPath<?> path);

    /* (non-Javadoc)
     * @see com.querydsl.core.QueryFactory#query()
     */
    @Override
    Q query();

}