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
package com.github.xuse.querydsl.sql.dml;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.github.xuse.querydsl.sql.ContextKeyConstants;
import com.google.common.collect.Maps;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.util.ResultSetAdapter;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.AbstractSQLInsertClause;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.dml.SQLInsertClause;

/**
 * SQLInsertClause defines an INSERT INTO clause
 * If you need to subtype this, use {@link AbstractSQLInsertClause} instead.
 *
 * @author tiwe
 *
 */
public class SQLInsertClauseAlter extends SQLInsertClause {
    public SQLInsertClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity) {
        this(connection, new Configuration(templates), entity);
    }

    public SQLInsertClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity, SQLQuery<?> subQuery) {
        this(connection, new Configuration(templates), entity, subQuery);
    }

    public SQLInsertClauseAlter(Connection connection, Configuration configuration, RelationalPath<?> entity, SQLQuery<?> subQuery) {
        super(connection, configuration, entity, subQuery);
    }

    public SQLInsertClauseAlter(Connection connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }

    public SQLInsertClauseAlter(Provider<Connection> connection, Configuration configuration, RelationalPath<?> entity, SQLQuery<?> subQuery) {
        super(connection, configuration, entity, subQuery);
    }

    public SQLInsertClauseAlter(Provider<Connection> connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }
    
    /**
     * Execute the clause and return the generated keys as a ResultSet
     *
     * @return result set with generated keys
     */
    public ResultSet executeWithKeys() {
        context = startContext(connection(), metadata, entity);
        try {
            PreparedStatement stmt = null;
            if (batches.isEmpty()) {
                stmt = createStatement(true);
                listeners.notifyInsert(entity, metadata, columns, values, subQuery);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                int rc=stmt.executeUpdate();
                postExecuted(context, System.currentTimeMillis()-start, "Insert", rc);
            } else if (batchToBulk) {
                stmt = createStatement(true);
                listeners.notifyInserts(entity, metadata, batches);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                int rc=stmt.executeUpdate();
                postExecuted(context, System.currentTimeMillis()-start, "Insert", rc);
            } else {
                Collection<PreparedStatement> stmts = createStatements(true);
                if (stmts != null && stmts.size() > 1) {
                    throw new IllegalStateException("executeWithKeys called with batch statement and multiple SQL strings");
                }
                stmt = stmts.iterator().next();
                listeners.notifyInserts(entity, metadata, batches);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                int[] rc=stmt.executeBatch();
                long count=0;
                for(int r:rc) {
                	count=count+r;
                }
                postExecuted(context, System.currentTimeMillis()-start, "BatchInsert", count);
            }

            final Statement stmt2 = stmt;
            ResultSet rs = stmt.getGeneratedKeys();
            return new ResultSetAdapter(rs) {
                @Override
                public void close() throws SQLException {
                    try {
                        super.close();
                    } finally {
                        stmt2.close();
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
        }
    }

    @Override
    public long execute() {
        context = startContext(connection(), metadata,entity);
        PreparedStatement stmt = null;
        Collection<PreparedStatement> stmts = null;
        try {
            if (batches.isEmpty()) {
                stmt = createStatement(false);
                listeners.notifyInsert(entity, metadata, columns, values, subQuery);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                int rc = stmt.executeUpdate();
                postExecuted(context, System.currentTimeMillis()-start, "Insert", rc);
                return rc;
            } else if (batchToBulk) {
                stmt = createStatement(false);
                listeners.notifyInserts(entity, metadata, batches);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                int rc = stmt.executeUpdate();
                postExecuted(context, System.currentTimeMillis()-start, "Insert", rc);
                return rc;
            } else {
                stmts = createStatements(false);
                listeners.notifyInserts(entity, metadata, batches);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                long rc = executeBatch(stmts);
                postExecuted(context, System.currentTimeMillis()-start, "BatchInsert", rc);
                return rc;
            }
        } catch (SQLException e) {
            onException(context,e);
            throw configuration.translate(queryString, constants, e);
        } finally {
            if (stmt != null) {
                close(stmt);
            }
            if (stmts != null) {
                close(stmts);
            }
            reset();
            endContext(context);
        }
    }

    protected PreparedStatement createStatement(boolean withKeys) throws SQLException {
        listeners.preRender(context);
        SQLSerializer serializer = createSerializer();
        if (subQueryBuilder != null) {
            subQuery = subQueryBuilder.select(values.toArray(new Expression[values.size()])).clone();
            values.clear();
        }

        if (!batches.isEmpty() && batchToBulk) {
            serializer.serializeInsert(metadata, entity, batches);
        } else {
            serializer.serializeInsert(metadata, entity, columns, values, subQuery);
        }
        context.addSQL(createBindings(metadata, serializer));
        listeners.rendered(context);
        return prepareStatementAndSetParameter(serializer, withKeys,-1);
    }

    protected Collection<PreparedStatement> createStatements(boolean withKeys) throws SQLException {
        boolean addBatches = !configuration.getUseLiterals();
        listeners.preRender(context);

        if (subQueryBuilder != null) {
            subQuery = subQueryBuilder.select(values.toArray(new Expression[values.size()])).clone();
            values.clear();
        }

        Map<String, PreparedStatement> stmts = Maps.newHashMap();

        // add first batch
        SQLSerializer serializer = createSerializer();
        serializer.serializeInsert(metadata, entity, batches.get(0).getColumns(), batches
                .get(0).getValues(), batches.get(0).getSubQuery());
        PreparedStatement stmt = prepareStatementAndSetParameter(serializer, withKeys,0);
        if (addBatches) {
            stmt.addBatch();
        }
        stmts.put(serializer.toString(), stmt);
        context.addSQL(createBindings(metadata, serializer));
        listeners.rendered(context);

        // add other batches
        for (int i = 1; i < batches.size(); i++) {
            SQLInsertBatch batch = batches.get(i);

            listeners.preRender(context);
            serializer = createSerializer();
            serializer.serializeInsert(metadata, entity, batch.getColumns(),
                    batch.getValues(), batch.getSubQuery());
            context.addSQL(createBindings(metadata, serializer));
            listeners.rendered(context);

            stmt = stmts.get(serializer.toString());
            if (stmt == null) {
                stmt = prepareStatementAndSetParameter(serializer, withKeys,i);
                stmts.put(serializer.toString(), stmt);
            } else {
                setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(),
                        metadata.getParams());
            }
            if (addBatches) {
                stmt.addBatch();
            }
        }
        preparedFinish(context, batches.size());

        return stmts.values();
    }
    
    protected PreparedStatement prepareStatementAndSetParameter(SQLSerializer serializer,
            boolean withKeys, int index) throws SQLException {
        listeners.prePrepare(context);

        queryString = serializer.toString();
        constants = serializer.getConstants();
        logQuery(logger, queryString, constants);
        PreparedStatement stmt;
        if (withKeys) {
            if (entity.getPrimaryKey() != null) {
                String[] target = new String[entity.getPrimaryKey().getLocalColumns().size()];
                for (int i = 0; i < target.length; i++) {
                    Path<?> path = entity.getPrimaryKey().getLocalColumns().get(i);
                    String column = ColumnMetadata.getName(path);
                    target[i] = column;
                }
                stmt = connection().prepareStatement(queryString, target);
            } else {
                stmt = connection().prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
            }
        } else {
            stmt = connection().prepareStatement(queryString);
        }
        setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(),
                metadata.getParams());

        context.addPreparedStatement(stmt);
        prepared(context,serializer.getConstants(), serializer.getConstantPaths(),index);
        return stmt;
    }
    
    /**
	 * first -1唯一 0初始 n后续
	 * 
	 * @param context
	 * @param objects
	 * @param constantPaths
	 * @param first
	 */
	private void prepared(SQLListenerContextImpl context, List<?> objects, List<Path<?>> constantPaths, int first) {
		switch (first) {
		case -1:
			context.setData(ContextKeyConstants.SIGLE_PARAMS, objects);
			context.setData(ContextKeyConstants.PARAMS_PATH, constantPaths);
			listeners.prepared(context);
			return;
		case 0: {
			context.setData(ContextKeyConstants.PARAMS_PATH, constantPaths);
			List<List<?>> mlist = new ArrayList<>();
			mlist.add(objects);
			context.setData(ContextKeyConstants.BATCH_PARAMS, mlist);
			return;
		}
		default: 
			if (first > ContextKeyConstants.MAX_BATCH_LOG) {
				return;
			}
			@SuppressWarnings("unchecked")
			List<List<?>> mlist = (List<List<?>>) context.getData(ContextKeyConstants.BATCH_PARAMS);
			mlist.add(objects);
		}
	}

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, long count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
		listeners.executed(context);
	}

	private void preparedFinish(SQLListenerContextImpl context, int maxSize) {
		context.setData(ContextKeyConstants.BATCH_SIZE, maxSize);
		listeners.prepared(context);
	}
}
