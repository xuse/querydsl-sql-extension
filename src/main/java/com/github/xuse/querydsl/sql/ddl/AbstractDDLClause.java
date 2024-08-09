package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.log.ContextKeyConstants;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.support.DistributedLock;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLListeners;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDDLClause<C extends DDLClause<C>> implements DDLClause<C> {
	
	protected MetadataQuerySupport connection;

	protected ConfigurationEx configuration;

	protected RelationalPathEx<?> table;
	
	protected final SQLListeners listeners;
	  
    protected SQLListenerContextImpl context;
    
    protected RoutingStrategy routing;
	
	protected boolean useDDLLock = false;
	
	private static final String DDL_DISTRIBUTED_LOCK_NAME = "lock#table_ddl";
	
	public AbstractDDLClause(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPathEx<?> path) {
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
		DistributedLock locked=null;
		if(useDDLLock) {
    		locked=connection.getLock(DDL_DISTRIBUTED_LOCK_NAME);
    		if(!locked.tryLock()) {
    			Entry<String,Date> lockBy=locked.lockedBy();
				log.warn("{} is lockedBy {}, {} will not execute.", DDL_DISTRIBUTED_LOCK_NAME, lockBy.getKey(),
						this.getClass().getName());
        		return 0;	
    		}
    	}
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
				context.getAllSQLBindings().clear();
				context.addSQL(new SQLBindings(s, Collections.emptyList()));
		        listeners.prePrepare(context);
				try (PreparedStatement st = c.prepareStatement(s)) {
					listeners.prepared(context);
					listeners.preExecute(context);
					int result = st.executeUpdate();
					postExecuted(context, System.currentTimeMillis() - start, result);
				} catch (SQLException e) {
					onException(context, e);
					throw configuration.get().translate(s, Collections.emptyList(), e);
				}
			}
			return finished(sqls);
		}finally {
			if(locked!=null) {
				locked.unlock();
			}
			listeners.end(context);
		}
	}
	
	
    protected int finished(List<String> sqls) {
    	if(sqls.isEmpty()) {
    		return 0;
    	}
		int count = (int) sqls.stream().filter(Objects::nonNull).count();
		return count;
	}

	protected boolean preExecute(MetadataQuerySupport metadata) {
    	return true;
    }

	protected void onException(SQLListenerContextImpl context, Exception e) {
        context.setException(e);
        listeners.exception(context);
    }
    
	private void postExecuted(SQLListenerContextImpl context, long cost, int count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, "DDL");
		if(this.configuration.getSlowSqlWarnMillis()<=cost) {
			context.setData(ContextKeyConstants.SLOW_SQL, Boolean.TRUE);
		}
		listeners.executed(context);
	}

	protected List<String> generateSQLs(){
		listeners.preRender(context);
		String sql=generateSQL();
		listeners.rendered(context);
		return sql==null? Collections.emptyList(): Collections.singletonList(sql);
	}
	

    /**
     * Called to create and start a new SQL Listener context
     *
     * @param connection the database connection
     * @param metadata   the metadata for that context
     * @param entity     the entity for that context
     * @return the newly started context
     */
    protected SQLListenerContextImpl startContext(Connection connection, QueryMetadata metadata, RelationalPath<?> entity) {
        SQLListenerContextImpl context = new SQLListenerContextImpl(metadata, connection, entity);
        listeners.start(context);
        return context;
    }

	protected abstract String generateSQL();
	
	@SuppressWarnings("unchecked")
	public C withRouting(RoutingStrategy routing) {
		this.routing=routing;
		return (C)this;
	}
	
	@SuppressWarnings("unchecked")
	public C useDDLLock() {
		this.useDDLLock = true;
		return (C)this;
	}
}
