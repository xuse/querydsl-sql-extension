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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.github.xuse.querydsl.sql.ContextKeyConstants;
import com.google.common.collect.Maps;
import com.querydsl.core.types.Path;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLSerializer;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;


public class SQLDeleteClauseAlter extends SQLDeleteClause {
    public SQLDeleteClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity) {
        super(connection, new Configuration(templates), entity);
    }

    public SQLDeleteClauseAlter(Connection connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }

    public SQLDeleteClauseAlter(Provider<Connection> connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }
    
    @Override
    public long execute() {
        context = startContext(connection(), metadata, entity);
        PreparedStatement stmt = null;
        Collection<PreparedStatement> stmts = null;
        try {
            if (batches.isEmpty()) {
                stmt = createStatement();
                listeners.notifyDelete(entity, metadata);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                int rc = stmt.executeUpdate();
                postExecuted(context,System.currentTimeMillis()-start, "Deleted",rc);
                return rc;
            } else {
                stmts = createStatements();
                listeners.notifyDeletes(entity, batches);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                long rc = executeBatch(stmts);
                postExecuted(context,System.currentTimeMillis()-start, "BatchDeleted",rc);
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
    
    protected PreparedStatement createStatement() throws SQLException {
        listeners.preRender(context);
        SQLSerializer serializer = createSerializer();
        serializer.serializeDelete(metadata, entity);
        queryString = serializer.toString();
        constants = serializer.getConstants();
        logQuery(logger, queryString, constants);
        context.addSQL(createBindings(metadata, serializer));
        listeners.rendered(context);

        listeners.prePrepare(context);
        PreparedStatement stmt = connection().prepareStatement(queryString);
        setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());

        context.addPreparedStatement(stmt);
        prepared(context,serializer.getConstants(), serializer.getConstantPaths(),-1);
        return stmt;
    }

    protected Collection<PreparedStatement> createStatements() throws SQLException {
        boolean addBatches = !configuration.getUseLiterals();
        listeners.preRender(context);
        SQLSerializer serializer = createSerializer();
        serializer.serializeDelete(batches.get(0), entity);
        queryString = serializer.toString();
        constants = serializer.getConstants();
        logQuery(logger, queryString, constants);
        context.addSQL(createBindings(metadata, serializer));
        listeners.rendered(context);

        Map<String, PreparedStatement> stmts = Maps.newHashMap();

        // add first batch
        listeners.prePrepare(context);
        PreparedStatement stmt = connection().prepareStatement(queryString);
        setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
        if (addBatches) {
            stmt.addBatch();
        }
        stmts.put(queryString, stmt);
        context.addPreparedStatement(stmt);
        prepared(context,serializer.getConstants(), serializer.getConstantPaths(),0);


        // add other batches
        for (int i = 1; i < batches.size(); i++) {
            listeners.preRender(context);
            serializer = createSerializer();
            serializer.serializeDelete(batches.get(i), entity);
            context.addSQL(createBindings(metadata, serializer));
            listeners.rendered(context);

            stmt = stmts.get(serializer.toString());
            if (stmt == null) {
                listeners.prePrepare(context);
                stmt = connection().prepareStatement(serializer.toString());
                stmts.put(serializer.toString(), stmt);
                context.addPreparedStatement(stmt);
                prepared(context,serializer.getConstants(), serializer.getConstantPaths(),i);
            }
            setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());
            if (addBatches) {
                stmt.addBatch();
            }
        }
        preparedFinish(context, batches.size());
        return stmts.values();
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
