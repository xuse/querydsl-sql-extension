package com.github.xuse.querydsl.r2dbc.clause;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryFlag;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.dml.InsertClause;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.DefaultMapper;
import com.querydsl.sql.dml.Mapper;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.types.Null;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractR2InsertClause<C extends AbstractR2InsertClause<C>> extends R2ClauseBase<C> implements InsertClause<C> {
    public AbstractR2InsertClause(ConnectionFactory connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}

    protected final QueryMetadata metadata = new DefaultQueryMetadata();

    @Nullable
    protected SubQueryExpression<?> subQuery;

    @Nullable
    protected SQLQuery<?> subQueryBuilder;

    protected final List<SQLInsertBatch> batches = new ArrayList<SQLInsertBatch>();

    protected final List<Path<?>> columns = new ArrayList<Path<?>>();

    protected final List<Expression<?>> values = new ArrayList<Expression<?>>();

    protected transient String queryString;

    protected transient List<Object> constants;

    protected transient boolean batchToBulk;

//    public AbstractSQLInsertClause(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity, SQLQuery<?> subQuery) {
//        this(connection, configuration, entity);
//        this.subQueryBuilder = subQuery;
//    }

//    public AbstractSQLInsertClause(Connection connection, ConfigurationEx configuration, RelationalPath<?> entity) {
//        super(configuration, connection);
//        this.entity = entity;
//        metadata.addJoin(JoinType.DEFAULT, entity);
//    }

    /**
     * Add the given String literal at the given position as a query flag
     *
     * @param position position
     * @param flag query flag
     * @return the current object
     */
    public C addFlag(Position position, String flag) {
        metadata.addFlag(new QueryFlag(position, flag));
        return (C) this;
    }

    /**
     * Add the given Expression at the given position as a query flag
     *
     * @param position position
     * @param flag query flag
     * @return the current object
     */
    public C addFlag(Position position, Expression<?> flag) {
        metadata.addFlag(new QueryFlag(position, flag));
        return (C) this;
    }

    /**
     * Add the current state of bindings as a batch item
     *
     * @return the current object
     */
    public C addBatch() {
        if (subQueryBuilder != null) {
            subQuery = subQueryBuilder.select(values.toArray(new Expression[0])).clone();
            values.clear();
        }
        batches.add(new SQLInsertBatch(columns, values, subQuery));
        columns.clear();
        values.clear();
        subQuery = null;
        return (C) this;
    }

    /**
     * Set whether batches should be optimized into a single bulk operation.
     * Will revert to batches, if bulk is not supported
     */
    public void setBatchToBulk(boolean b) {
        this.batchToBulk = b && configuration.getTemplates().isBatchToBulkSupported();
    }

    public void clear() {
        batches.clear();
        columns.clear();
        values.clear();
        subQuery = null;
    }

    @Override
    public C columns(Path<?>... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return (C) this;
    }

    /**
     * Execute the clause and return the generated key with the type of the
     * given path. If no rows were created, null is returned, otherwise the key
     * of the first row is returned.
     *
     * @param <T>
     * @param path path for key
     * @return generated key
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T executeWithKey(Path<T> path) {
        return executeWithKey((Class<T>) path.getType(), path);
    }

    /**
     * Execute the clause and return the generated key cast to the given type.
     * If no rows were created, null is returned, otherwise the key of the first
     * row is returned.
     *
     * @param <T>
     * @param type type of key
     * @return generated key
     */
    public <T> T executeWithKey(Class<T> type) {
        return executeWithKey(type, null);
    }

    protected <T> T executeWithKey(Class<T> type, @Nullable Path<T> path) {
    	return null;
//        ResultSet rs = null;
//        try {
//            rs = executeWithKeys();
//            if (rs.next()) {
//                return qqConfiguration.get(rs, path, 1, type);
//            } else {
//                return null;
//            }
//        } catch (SQLException e) {
//            throw qConfiguration.translate(e);
//        } finally {
//            if (rs != null) {
//                close(rs);
//            }
//            reset();
//        }
    }

    /**
     * Execute the clause and return the generated key with the type of the
     * given path. If no rows were created, or the referenced column is not a
     * generated key, null is returned. Otherwise, the key of the first row is
     * returned.
     *
     * @param <T>
     * @param path path for key
     * @return generated keys
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> executeWithKeys(Path<T> path) {
    	return null;
    }

    public <T> List<T> executeWithKeys(Class<T> type) {
    	return null;
    }

    /**
     * Execute the clause and return the generated keys as a ResultSet
     *
     * @return result set with generated keys
     */
    public ResultSet executeWithKeys() {
    	return null;
//        context = startContext(connection(), metadata, entity);
//        try {
//            PreparedStatement stmt = null;
//            if (batches.isEmpty()) {
//                stmt = createStatement(true);
//                listeners.notifyInsert(entity, metadata, columns, values, subQuery);
//                listeners.preExecute(context);
//                stmt.executeUpdate();
//                listeners.executed(context);
//            } else if (batchToBulk) {
//                stmt = createStatement(true);
//                listeners.notifyInserts(entity, metadata, batches);

//                listeners.preExecute(context);
//                stmt.executeUpdate();
//                listeners.executed(context);
//            } else {
//                Collection<PreparedStatement> stmts = createStatements(true);
//                if (stmts != null && stmts.size() > 1) {
//                    throw new IllegalStateException("executeWithKeys called with batch statement and multiple SQL strings");
//                }
//                stmt = stmts.iterator().next();
//                listeners.notifyInserts(entity, metadata, batches);

//                listeners.preExecute(context);
//                stmt.executeBatch();
//                listeners.executed(context);
//            }
//
//            final Statement stmt2 = stmt;
//            ResultSet rs = stmt.getGeneratedKeys();
//            return new ResultSetAdapter(rs) {
//                @Override
//                public void close() throws SQLException {
//                    try {
//                        super.close();
//                    } finally {
//                        stmt2.close();
//                        reset();
//                        endContext(context);
//                    }
//                }
//            };
//        } catch (SQLException e) {
//            onException(context, e);
//            reset();
//            endContext(context);
//            throw qConfiguration.translate(queryString, constants, e);
//        }
    }

    @Override
    public long execute() {
    	return 0L;
//        context = startContext(connection(), metadata,entity);
//        PreparedStatement stmt = null;
//        Collection<PreparedStatement> stmts = null;
//        try {
//            if (batches.isEmpty()) {
//                stmt = createStatement(false);
//                listeners.notifyInsert(entity, metadata, columns, values, subQuery);
//
//                listeners.preExecute(context);
//                int rc = stmt.executeUpdate();
//                listeners.executed(context);
//                return rc;
//            } else if (batchToBulk) {
//                stmt = createStatement(false);
//                listeners.notifyInserts(entity, metadata, batches);
//
//                listeners.preExecute(context);
//                int rc = stmt.executeUpdate();
//                listeners.executed(context);
//                return rc;
//            } else {
//                stmts = createStatements(false);
//                listeners.notifyInserts(entity, metadata, batches);
//
//                listeners.preExecute(context);
//                long rc = executeBatch(stmts);
//                listeners.executed(context);
//                return rc;
//            }
//        } catch (SQLException e) {
//            onException(context,e);
//            throw qConfiguration.translate(queryString, constants, e);
//        } finally {
//            if (stmt != null) {
//                close(stmt);
//            }
//            if (stmts != null) {
//                close(stmts);
//            }
//            reset();
//            endContext(context);
//        }
    }

    public List<SQLBindings> getSQL() {
    	return null;
//        if (batches.isEmpty()) {
//            SQLSerializer serializer = createSerializer();
//            serializer.serializeInsert(metadata, entity, columns, values, subQuery);
//            return Collections.singletonList(createBindings(metadata, serializer));
//        } else if (batchToBulk) {
//            SQLSerializer serializer = createSerializer();
//            serializer.serializeInsert(metadata, entity, batches);
//            return Collections.singletonList(createBindings(metadata, serializer));
//        } else {
//            List<SQLBindings> builder = new ArrayList<>();
//            for (SQLInsertBatch batch : batches) {
//                SQLSerializer serializer = createSerializer();
//                serializer.serializeInsert(metadata, entity, batch.getColumns(), batch.getValues(), batch.getSubQuery());
//                builder.add(createBindings(metadata, serializer));
//            }
//            return CollectionUtils.unmodifiableList(builder);
//        }
    }

    @Override
    public C select(SubQueryExpression<?> sq) {
        subQuery = sq;
        for (Map.Entry<ParamExpression<?>, Object> entry : sq.getMetadata().getParams().entrySet()) {
            metadata.setParam((ParamExpression) entry.getKey(), entry.getValue());
        }
        return (C) this;
    }

    @Override
    public <T> C set(Path<T> path, T value) {
        columns.add(path);
        if (value instanceof Expression<?>) {
            values.add((Expression<?>) value);
        } else if (value != null) {
            values.add(ConstantImpl.create(value));
        } else {
            values.add(Null.CONSTANT);
        }
        return (C) this;
    }

    @Override
    public <T> C set(Path<T> path, Expression<? extends T> expression) {
        columns.add(path);
        values.add(expression);
        return (C) this;
    }

    @Override
    public <T> C setNull(Path<T> path) {
        columns.add(path);
        values.add(Null.CONSTANT);
        return (C) this;
    }

    @Override
    public C values(Object... v) {
        for (Object value : v) {
            if (value instanceof Expression<?>) {
                values.add((Expression<?>) value);
            } else if (value != null) {
                values.add(ConstantImpl.create(value));
            } else {
                values.add(Null.CONSTANT);
            }
        }
        return (C) this;
    }

    @Override
    public String toString() {
    	return null;
//        SQLSerializer serializer = createSerializer();
//        if (!batches.isEmpty() && batchToBulk) {
//            serializer.serializeInsert(metadata, entity, batches);
//        } else {
//            serializer.serializeInsert(metadata, entity, columns, values, subQuery);
//        }
//        return serializer.toString();
    }

    /**
     * Populate the INSERT clause with the properties of the given bean. The
     * properties need to match the fields of the clause's entity instance.
     *
     * @param bean bean to use for population
     * @return the current object
     */
    public C populate(Object bean) {
        return populate(bean, DefaultMapper.DEFAULT);
    }

    /**
     * Populate the INSERT clause with the properties of the given bean using
     * the given Mapper.
     *
     * @param obj object to use for population
     * @param mapper mapper to use
     * @return the current object
     */
    @SuppressWarnings("rawtypes")
    public <T> C populate(T obj, Mapper<T> mapper) {
        Map<Path<?>, Object> values = mapper.createMap(entity, obj);
        for (Map.Entry<Path<?>, Object> entry : values.entrySet()) {
            set((Path) entry.getKey(), entry.getValue());
        }
        return (C) this;
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty() && batches.isEmpty();
    }

    public int getBatchCount() {
//        return batches.size();
    	return 0;
    }

}