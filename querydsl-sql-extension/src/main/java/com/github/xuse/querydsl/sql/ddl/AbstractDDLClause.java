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
import com.github.xuse.querydsl.sql.log.ExceptionLogDetail;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.support.DistributedLock;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLListeners;

import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for all DDL (Data Definition Language) operations.
 * <p>
 * Implements the Template Method pattern: subclasses override {@link #generateSQL()} or
 * {@link #generateSQLs()} to produce SQL statements, while this class handles connection
 * management, listener lifecycle, distributed locking, and statement execution.
 *
 * <h3>Thread Safety</h3>
 * <p>This class is <b>NOT thread-safe</b>. Each instance maintains mutable state
 * ({@code context}, {@code routing}, {@code useDDLLock}) that is modified during
 * {@link #execute()}. Instances are designed to be created per-operation and must not
 * be shared across threads. Typical usage follows the builder pattern:
 * <pre>{@code
 * factory.createTable(path).ifExists().execute();
 * }</pre>
 *
 * <h3>Distributed Locking</h3>
 * <p>For scenarios where multiple application instances may perform DDL concurrently,
 * call {@link #useDDLLock()} before {@link #execute()} to enable distributed lock
 * acquisition. When the lock cannot be acquired, the operation is skipped with a
 * warning log and returns 0.
 *
 * <h3>Connection Lifecycle</h3>
 * <p>This class obtains a {@link java.sql.Connection} from the provided
 * {@link MetadataQuerySupport} but does <b>not</b> close it. The caller or connection
 * pool is responsible for connection lifecycle management.
 *
 * @param <C> the concrete DDL clause type (for fluent API support)
 * @author Joey
 */
@Slf4j
public abstract class AbstractDDLClause<C extends DDLClause<C>> implements DDLClause<C> {
	
	protected MetadataQuerySupport connection;

	protected ConfigurationEx configuration;

	protected RelationalPathEx<?> table;
	
	protected final SQLListeners listeners;
	  
    protected SQLListenerContextImpl context;
    
    protected RoutingStrategy routing;
	
	protected boolean useDDLLock = false;
	
	protected ExceptionLogDetail exceptionLogDetail;
	
	private static final String DDL_DISTRIBUTED_LOCK_NAME = "lock#table_ddl";
	
	public AbstractDDLClause(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPathEx<?> path) {
		this.connection = connection;
		this.configuration = configuration;
		this.table = path;
		this.listeners = new SQLListeners(configuration.get().getListeners());
		for(Operator op:checkSupports()) {
			if(!configuration.supports(op)) {
				throw Exceptions.unsupportedOperation("Current database do not support this operation. op={}", op);
			}
		}
	}
	
	protected List<Operator> checkSupports() {
		return Collections.emptyList();
	};

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
		if (this.exceptionLogDetail != null) {
			context.setData(ContextKeyConstants.EXCEPTION_LOG_DETAIL, this.exceptionLogDetail);
		}
        context.setException(e);
        listeners.exception(context);
    }
    
	private void postExecuted(SQLListenerContextImpl context, long cost, int count) {
		context.setData(ContextKeyConstants.ELAPSED_TIME, cost);
		context.setData(ContextKeyConstants.COUNT, count);
		context.setData(ContextKeyConstants.ACTION, "DDL");
		int value = ContextKeyConstants.DDL;
		if (this.configuration.getSlowSqlWarnMillis() <= cost) {
			value = value | ContextKeyConstants.SLOW;
		}
		context.setData(ContextKeyConstants.IMPORTANT, value);
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
	
	/**
	 * Set how much detail is logged if this statement fails.
	 * <p>设置本语句执行失败时的异常日志详细度。适用于预期内的异常（如对象已存在），
	 * 避免完整堆栈淹没业务日志，同时不影响其它语句的错误日志。
	 *
	 * @param detail {@link ExceptionLogDetail#FULL_STACK} 完整堆栈（默认）、
	 *        {@link ExceptionLogDetail#BRIEF} 仅SQL和异常摘要、
	 *        {@link ExceptionLogDetail#NONE} 不打印
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public C exceptionLog(ExceptionLogDetail detail) {
		this.exceptionLogDetail = detail;
		return (C) this;
	}

	@SuppressWarnings("unchecked")
	public C useDDLLock() {
		this.useDDLLock = true;
		return (C)this;
	}

	public List<String> toSQLs() {
		return generateSQLs();
	}
}
