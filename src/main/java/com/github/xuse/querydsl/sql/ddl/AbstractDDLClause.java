package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RoutingStrategy;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLListeners;

public abstract class AbstractDDLClause<C extends DDLClause<C>> implements DDLClause<C> {
	
	protected MetadataQuerySupport connection;

	protected ConfigurationEx configuration;

	protected RelationalPath<?> table;
	
	protected final SQLListeners listeners;
	  
    protected SQLListenerContextImpl context;
    
    protected RoutingStrategy routing;
    
	public AbstractDDLClause(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> path) {
		this.connection = connection;
		this.configuration = configuration;
		this.table = path;
		this.listeners = new SQLListeners(configuration.get().getListeners());
	}
	
	protected SQLTemplatesEx getTemplates() {
		return configuration.getTemplates();
	}
	
	protected static QueryMetadata DEFAULT = new DefaultQueryMetadata();
	 
	@Override
	public int execute() {
		if(!preExecute(connection)) {
			return 0;
		}
		try{
			Connection c=connection.getConnection();
			context = startContext(c, DEFAULT, table);
			List<String> sqls=generateSQLs();
			long start = System.currentTimeMillis();
			for (String s : sqls) {
				if(StringUtils.isEmpty(s)) {
					continue;
				}
				context.getAllSQLBindings().clear();;
				context.addSQL(new SQLBindings(s, Collections.emptyList()));
		        listeners.prePrepare(context);
				try (PreparedStatement st = c.prepareStatement(s)) {
					listeners.prepared(context);
					listeners.preExecute(context);
					int result = st.executeUpdate();
					postExecuted(context, System.currentTimeMillis() - start, "DDL", result);
				} catch (SQLException e) {
					onException(context, e);
					throw configuration.get().translate(s, null, e);
				}
			}
			return finished(sqls);
		}finally {
			listeners.end(context);
		}
	}
	
    protected int finished(List<String> sqls) {
    	if(sqls.isEmpty()) {
    		return 0;
    	}
		int count = (int) sqls.stream().filter(e -> e != null).count();
		return count;
	}

	protected boolean preExecute(MetadataQuerySupport metadata) {
    	return true;
    }

	protected void onException(SQLListenerContextImpl context, Exception e) {
        context.setException(e);
        listeners.exception(context);
    }
    
	private void postExecuted(SQLListenerContextImpl context, long cost, String action, int count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, action);
		if(this.configuration.getSlowSqlWarnMillis()<=cost) {
			context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
		}
		listeners.executed(context);
	}

	protected List<String> generateSQLs(){
		listeners.preRender(context);
		String sql=generateSQL();
		listeners.rendered(context);
		return sql==null? Collections.emptyList():Arrays.asList(sql);
	}
	

    /**
     * Called to create and start a new SQL Listener context
     *
     * @param connection the database connection
     * @param metadata   the meta data for that context
     * @param entity     the entity for that context
     * @return the newly started context
     */
    protected SQLListenerContextImpl startContext(Connection connection, QueryMetadata metadata, RelationalPath<?> entity) {
        SQLListenerContextImpl context = new SQLListenerContextImpl(metadata, connection, entity);
        listeners.start(context);
        return context;
    }

	protected abstract String generateSQL();
	
	public DDLClause<C> withRouting(RoutingStrategy routing) {
		this.routing=routing;
		return this;
	}
	
}
