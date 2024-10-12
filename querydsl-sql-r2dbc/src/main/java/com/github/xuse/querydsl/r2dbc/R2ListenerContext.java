package com.github.xuse.querydsl.r2dbc;

import java.util.Collection;

import com.querydsl.sql.SQLDetailedListener;
import com.querydsl.sql.SQLListenerContext;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;

/**
 * A context object that is progressively filled out during query execution and is passed to each {@link
 * SQLDetailedListener} callback method
 */
public interface R2ListenerContext extends SQLListenerContext{
    /**
     * Return the underlying connection if there is one
     *
     * <p>NOTE : This can be null depending on the stage of the query execution</p>
     *
     * @return the underlying connection if there is one
     */
    Connection getR2Connection();


    /**
     * Return the underlying statement or the first if its batch query
     *
     * <p>NOTE : This can be null depending on the stage of the query execution</p>
     *
     * @return the underlying statement or the first if its batch query
     */
    Statement getR2Statement();
    
    
    default java.sql.Connection getConnection(){
    	throw new UnsupportedOperationException();
    }
    
    default java.sql.PreparedStatement getPreparedStatement(){
    	throw new UnsupportedOperationException();
    }
    
    default Collection<java.sql.PreparedStatement> getPreparedStatements(){
    	throw new UnsupportedOperationException();
    }

}
