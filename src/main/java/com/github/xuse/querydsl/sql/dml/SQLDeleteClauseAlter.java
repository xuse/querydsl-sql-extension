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

import static com.google.common.collect.Lists.newArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.github.xuse.querydsl.sql.SQLBindingsAlter;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
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
    

	private Integer queryTimeout;	
	/**
	 * 设置查询超时（秒）
	 * @param queryTimeout
	 */
	public SQLDeleteClauseAlter setQueryTimeout(int queryTimeout) {
		this.queryTimeout=queryTimeout;
		return this;
	}
    
    @Override
    public long execute() {
        context = startContext(connection(), metadata, entity);
        PreparedStatement stmt = null;
        Collection<PreparedStatement> stmts = null;
        try {
            if (batches.isEmpty()) {
                stmt = createStatement();
                if(queryTimeout!=null) {
                	stmt.setQueryTimeout(queryTimeout);
                }
                listeners.notifyDelete(entity, metadata);

                listeners.preExecute(context);
                long start=System.currentTimeMillis();
                int rc = stmt.executeUpdate();
                postExecuted(context,System.currentTimeMillis()-start, "Deleted",rc);
                return rc;
            } else {
                stmts = createStatements();
                if(queryTimeout!=null) {
                	for(PreparedStatement st:stmts) {
                    	st.setQueryTimeout(queryTimeout);
                    }	
                }
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
        
        SQLBindings bindings= createBindings(metadata, serializer);
        context.addSQL(bindings);
        queryString = bindings.getSQL();
        constants = serializer.getConstants();
        //logQuery(logger, queryString, constants);
        listeners.rendered(context);

        listeners.prePrepare(context);
        PreparedStatement stmt = connection().prepareStatement(queryString);
        setParameters(stmt, serializer.getConstants(), serializer.getConstantPaths(), metadata.getParams());

        context.addPreparedStatement(stmt);
        listeners.prepared(context);

        return stmt;
    }
    
	@Override
	protected SQLBindings createBindings(QueryMetadata metadata, SQLSerializer serializer) {
		String queryString = serializer.toString();
        List<Object> args = newArrayList();
        Map<ParamExpression<?>, Object> params = metadata.getParams();
        for (Object o : serializer.getConstants()) {
            if (o instanceof ParamExpression) {
                if (!params.containsKey(o)) {
                    throw new ParamNotSetException((ParamExpression<?>) o);
                }
                o = metadata.getParams().get(o);
            }
            args.add(o);
        }
        return new SQLBindingsAlter(queryString, args, serializer.getConstantPaths());
	}

	private void postExecuted(SQLListenerContextImpl context, long cost, String action, long count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
		listeners.executed(context);
	}
}
