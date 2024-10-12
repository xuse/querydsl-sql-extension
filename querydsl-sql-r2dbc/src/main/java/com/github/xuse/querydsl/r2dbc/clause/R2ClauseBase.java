package com.github.xuse.querydsl.r2dbc.clause;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.r2dbc.R2ListenerContextImpl;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.ParamExpression;
import com.querydsl.core.types.ParamNotSetException;
import com.querydsl.core.types.Path;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLListener;
import com.querydsl.sql.SQLListenerContextImpl;
import com.querydsl.sql.SQLListeners;
import com.querydsl.sql.SQLSerializer;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;

public abstract class R2ClauseBase<T extends R2ClauseBase<T>> {
	protected final ConnectionFactory connection;
	protected final ConfigurationEx configuration;
	protected final Configuration   qConfiguration;
	protected final RelationalPath<?> entity;
	

    protected final SQLListeners listeners;

    protected boolean useLiterals;

    protected R2ListenerContextImpl context;

	public R2ClauseBase(ConnectionFactory connection, ConfigurationEx configuration, RelationalPath<?> entity) {
		this.configuration=configuration;
		this.qConfiguration=configuration.get();
		this.connection=connection;
		this.entity=entity;
        this.listeners = new SQLListeners(configuration.get().getListeners());
	}

    /**
     * Add a listener
     *
     * @param listener listener to add
     */
    public void addListener(SQLListener listener) {
        listeners.add(listener);
    }
    

    /**
     * Clear the internal state of the clause
     */
    public abstract void clear();
    
    
    /**
     * Called to create and start a new SQL Listener context
     *
     * @param connection the database connection
     * @param metadata   the meta data for that context
     * @param entity     the entity for that context
     * @return the newly started context
     */
    protected R2ListenerContextImpl startContext(Connection connection, QueryMetadata metadata, RelationalPath<?> entity) {
    	R2ListenerContextImpl context = new R2ListenerContextImpl(metadata, connection, entity);
        listeners.start(context);
        return context;
    }

    /**
     * Called to make the call back to listeners when an exception happens
     *
     * @param context the current context in play
     * @param e       the exception
     */
    protected void onException(SQLListenerContextImpl context, Exception e) {
        context.setException(e);
        listeners.exception(context);
    }

    /**
     * Called to end a SQL listener context
     *
     * @param context the listener context to end
     */
    protected void endContext(SQLListenerContextImpl context) {
        listeners.end(context);
        this.context = null;
    }


    protected SQLBindings createBindings(QueryMetadata metadata, SQLSerializer serializer) {
        String queryString = serializer.toString();
        List<Object> args = new ArrayList<>();
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
        return new SQLBindings(queryString, args);
    }

    protected SQLSerializer createSerializer() {
        SQLSerializer serializer = new SQLSerializer(configuration.get(), true);
        serializer.setUseLiterals(useLiterals);
        return serializer;
    }

    /**
     * Get the SQL string and bindings
     *
     * @return SQL and bindings
     */
    public abstract List<SQLBindings> getSQL();

    protected void setParameters(Statement stmt, List<?> objects,
            List<Path<?>> constantPaths, Map<ParamExpression<?>, ?> params) {
        if (objects.size() != constantPaths.size()) {
            throw new IllegalArgumentException("Expected " + objects.size() + " paths, " +
                    "but got " + constantPaths.size());
        }
        for (int i = 0; i < objects.size(); i++) {
            Object o = objects.get(i);
                if (o instanceof ParamExpression) {
                    if (!params.containsKey(o)) {
                        throw new ParamNotSetException((ParamExpression<?>) o);
                    }
                    o = params.get(o);
                }
              //  configuration.set(stmt, constantPaths.get(i), i + 1, o);
        }
    }

    private long executeBatch(PreparedStatement stmt) throws SQLException {
        if (configuration.get().getUseLiterals()) {
            return stmt.executeUpdate();
        } else if (configuration.get().getTemplates().isBatchCountViaGetUpdateCount()) {
            stmt.executeBatch();
            return stmt.getUpdateCount();
        } else {
            long rv = 0;
            for (int i : stmt.executeBatch()) {
                rv += i;
            }
            return rv;
        }
    }

    protected long executeBatch(Collection<PreparedStatement> stmts) throws SQLException {
        long rv = 0;
        for (PreparedStatement stmt : stmts) {
            rv += executeBatch(stmt);
        }
        return rv;
    }

//    protected void close(Statement stmt) {
//        try {
//            stmt.close();
//        } catch (SQLException e) {
//            throw configuration.get().translate(e);
//        }
//    }

    protected void close(Collection<? extends Statement> stmts) {
        for (Statement stmt : stmts) {
//            close(stmt);
        }
    }

    protected void close(ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
            throw configuration.get().translate(e);
        }
    }

    protected void logQuery(Logger logger, String queryString, Collection<Object> parameters) {
        if (logger.isLoggable(Level.FINE)) {
            String normalizedQuery = queryString.replace('\n', ' ');
            logger.fine(normalizedQuery);
        }
    }

    protected void reset() {
    }

    protected Connection connection() {
       return null;
    }

    public void setUseLiterals(boolean useLiterals) {
        this.useLiterals = useLiterals;
    }

    public abstract int getBatchCount();

	

    
    
    
}
