package com.github.xuse.querydsl.sql;

import java.sql.Connection;
import java.util.function.Supplier;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLMergeClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.mysql.MySQLQuery;
import com.querydsl.sql.mysql.MySQLReplaceClause;

public class MySQLQueryFactory2 extends AbstractSQLQueryFactory<MySQLQuery<?>> {
    public MySQLQueryFactory2(ConfigurationEx configuration, Supplier<Connection> connection) {
        super(configuration, connection);
    }

    /**
     * Create a INSERT IGNORE INTO clause
     *
     * @param entity table to insert to
     * @return insert clause
     */
    public SQLInsertClauseAlter insertIgnore(RelationalPath<?> entity) {
        SQLInsertClauseAlter insert = insert(entity);
        insert.addFlag(Position.START_OVERRIDE, "insert ignore into ");
        return insert;
    }

    /**
     * Create a INSERT ... ON DUPLICATE KEY UPDATE clause
     *
     * @param entity table to insert to
     * @param clause clause
     * @return insert clause
     */
    public SQLInsertClauseAlter insertOnDuplicateKeyUpdate(RelationalPath<?> entity, String clause) {
    	SQLInsertClauseAlter insert = insert(entity);
        insert.addFlag(Position.END, " on duplicate key update " + clause);
        return insert;
    }

    /**
     * Create a INSERT ... ON DUPLICATE KEY UPDATE clause
     *
     * @param entity table to insert to
     * @param clause clause
     * @return insert clause
     */
    public SQLInsertClauseAlter insertOnDuplicateKeyUpdate(RelationalPath<?> entity, Expression<?> clause) {
    	SQLInsertClauseAlter insert = insert(entity);
        insert.addFlag(Position.END, ExpressionUtils.template(String.class, " on duplicate key update {0}", clause));
        return insert;
    }

    /**
     * Create a INSERT ... ON DUPLICATE KEY UPDATE clause
     *
     * @param entity table to insert to
     * @param clauses clauses
     * @return insert clause
     */
    public SQLInsertClauseAlter insertOnDuplicateKeyUpdate(RelationalPath<?> entity, Expression<?>... clauses) {
    	SQLInsertClauseAlter insert = insert(entity);
        StringBuilder flag = new StringBuilder(" on duplicate key update ");
        for (int i = 0; i < clauses.length; i++) {
            flag.append(i > 0 ? ", " : "").append("{").append(i).append("}");
        }
        insert.addFlag(Position.END, ExpressionUtils.template(String.class, flag.toString(), (Object[])clauses));
        return insert;
    }

	@Override
	public final SQLInsertClauseAlter insert(RelationalPath<?> path) {
		return new SQLInsertClauseAlter(connection, configuration, path);
	}
	
    @Override
    public MySQLQuery<?> query() {
        return new MySQLQuery<Void>(connection, configuration.get());
    }

    public MySQLReplaceClause replace(RelationalPath<?> entity) {
        return new MySQLReplaceClause(connection.get(), configuration.get(), entity);
    }

    @Override
    public <T> MySQLQuery<T> select(Expression<T> expr) {
        return query().select(expr);
    }

    @Override
    public MySQLQuery<Tuple> select(Expression<?>... exprs) {
        return query().select(exprs);
    }

    @Override
    public <T> MySQLQuery<T> selectDistinct(Expression<T> expr) {
        return query().select(expr).distinct();
    }

    @Override
    public MySQLQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return query().select(exprs).distinct();
    }

	@Override
	public <T> MySQLQuery<T> selectFrom(RelationalPath<T> expr) {
		return select(expr).from(expr);
	}
	
    @Override
    public MySQLQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    @Override
    public MySQLQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

	@Override
	public SQLDeleteClauseAlter delete(RelationalPath<?> path) {
		return null;
	}

	@Override
	public final SQLMergeClauseAlter merge(RelationalPath<?> path) {
		return new SQLMergeClauseAlter(connection, configuration, path);
	}

	@Override
	public final SQLUpdateClauseAlter update(RelationalPath<?> path) {
		return new SQLUpdateClauseAlter(connection, configuration, path);
	}
}