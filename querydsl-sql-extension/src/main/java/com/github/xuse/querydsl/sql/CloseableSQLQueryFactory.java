package com.github.xuse.querydsl.sql;

import java.sql.Connection;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.core.DefaultQueryMetadata;
import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLListeners;

/**
 * This SQLQueryFactory has one connection, and call {@link #close()} is required on finish.
 *
 */
public class CloseableSQLQueryFactory extends SQLQueryFactory implements AutoCloseable {
	private static final QueryMetadata metadata = new DefaultQueryMetadata();

	private final SQLListeners listeners;
	
	private final SQLListenerContextImpl context;
	
	static final class PooledConnection extends ConnectionAdapter{
		private PooledConnection(Connection conn) {
			super(conn);
		}
		@Override
		public void close() {
		}
	}

	public CloseableSQLQueryFactory(ConfigurationEx configuration, Connection connection) {
		super(configuration, () -> new PooledConnection(connection));
		listeners = new SQLListeners(getConfiguration().get().getListeners());
		context = new SQLListenerContextImpl(metadata, connection, null);
		listeners.start(context);
	}

	@Override
	public void close(){
		listeners.end(context);
	}

	@Override
	protected void tryInitTask(ConfigurationEx configuration) {
	}
}
