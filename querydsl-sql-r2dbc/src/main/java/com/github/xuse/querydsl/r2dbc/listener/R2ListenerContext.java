package com.github.xuse.querydsl.r2dbc.listener;

import java.util.Collection;

import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLDetailedListener;
import com.querydsl.sql.SQLListenerContext;

/**
 * A context object that is progressively filled out during query execution and is passed to each {@link
 * SQLDetailedListener} callback method
 */
public interface R2ListenerContext {


    /**
     * The context getData is a general purpose place that listeners can place objects.  It allows listeners to pass
     * context between themselves during callbacks.
     * <p>
     *
     * @param dataKey the key to look up
     * @return the context object under that key
     */
    Object getData(String dataKey);

    /**
     * The context setData is a general purpose place that listeners can place objects.  It allows listeners to pass
     * context between themselves during callbacks.
     * <p>
     * A good time to place objects into the context is during {@link com.querydsl.sql.SQLDetailedListener#start(SQLListenerContext)}
     * and then access if after that.
     *
     * @param dataKey the key to use
     * @param value   the value to place under that key
     */
    void setData(String dataKey, Object value);

    /**
     * Return the underlying query metadata
     *
     * @return the underlying query metadata
     */
    QueryMetadata getMetadata();

    /**
     * Return the underlying sql or first in a batch query
     *
     * <p>NOTE : This can be null depending on the stage of the query execution</p>
     *
     * @return the underlying sql or first in a batch query
     */
    String getSQL();

    /**
     * Return the underlying sql including bindings or first in a batch query
     *
     * <p>NOTE : This can be null depending on the stage of the query execution</p>
     *
     * @return the underlying sql including bindings or first in a batch query
     */
    SQLBindings getSQLBindings();

    /**
     * Return the underlying sql collection if the query is a batch query
     *
     * <p>NOTE : This can be empty depending on the stage of the query execution</p>
     *
     * @return the underlying sql collection if the query is a batch query
     */
    Collection<String> getSQLStatements();

    /**
     * Return the underlying sql collection including bindings if the query is a batch query
     *
     * <p>NOTE : This can be empty depending on the stage of the query execution</p>
     *
     * @return the underlying sql collection including bindings if the query is a batch query
     */
    Collection<SQLBindings> getAllSQLBindings();

    /**
     * Return the underlying entity affected
     *
     * <p>NOTE : This can be null depending on the stage of the query execution</p>
     *
     * @return the underlying entity affected
     */
    RelationalPath<?> getEntity();

    /**
     * Return the underlying exception that has happened during query execution
     *
     * <p>NOTE : This can be null depending on whether an exception occurred</p>
     *
     * @return the underlying exception that has happened during query execution
     */
    Exception getException();
}
