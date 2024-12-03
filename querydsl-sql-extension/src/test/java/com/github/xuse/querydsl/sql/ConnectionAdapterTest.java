package com.github.xuse.querydsl.sql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ConnectionAdapterTest {

    private static Connection mockConnection;
    private static ConnectionAdapter connectionAdapter;

    @BeforeAll
    public static void setUp() {
        mockConnection = mock(Connection.class);
        connectionAdapter = new ConnectionAdapter(mockConnection) {
			@Override
			public void close() throws SQLException {
			}};
    }

    @Test
    public void testPrepareStatement() throws SQLException {
        String sql = "SELECT * FROM table";
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(sql)).thenReturn(preparedStatement);

        PreparedStatement result = connectionAdapter.prepareStatement(sql);
        assertNotNull(result);
        assertEquals(preparedStatement, result);
    }

    @Test
    public void testCreateStatement() throws SQLException {
        Statement statement = mock(Statement.class);
        when(mockConnection.createStatement()).thenReturn(statement);

        Statement result = connectionAdapter.createStatement();
        assertNotNull(result);
        assertEquals(statement, result);
    }

    @Test
    public void testCommit() throws SQLException {
        connectionAdapter.commit();
        verify(mockConnection, times(1)).commit();
    }

    @Test
    public void testRollback() throws SQLException {
        connectionAdapter.rollback();
        verify(mockConnection, times(1)).rollback();
    }

    @Test
    public void testIsClosed() throws SQLException {
        when(mockConnection.isClosed()).thenReturn(true);

        boolean result = connectionAdapter.isClosed();
        assertTrue(result);
    }

    @Test
    public void testGetMetaData() throws SQLException {
        assertNull(connectionAdapter.getMetaData());
        verify(mockConnection, times(1)).getMetaData();
    }

    @Test
    public void testSetClientInfo() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("key", "value");
        
        connectionAdapter.setClientInfo(properties);
        verify(mockConnection, times(1)).setClientInfo(properties);
    }
    
    @Test
    public void testGetAutoCommit() throws SQLException {
        when(mockConnection.getAutoCommit()).thenReturn(true);
        
        assertTrue(connectionAdapter.getAutoCommit());
    }

    @Test
    public void testSetReadOnly() throws SQLException {
        connectionAdapter.setReadOnly(true);
        verify(mockConnection, times(1)).setReadOnly(true);
    }
    
    @Test
    public void testGetWarnings() throws SQLException {
        assertNull(connectionAdapter.getWarnings());
        verify(mockConnection, times(1)).getWarnings();
    }

    @Test
    public void testClearWarnings() throws SQLException {
        connectionAdapter.clearWarnings();
        verify(mockConnection, times(1)).clearWarnings();
    }

    @Test
    public void testSetTypeMap() throws SQLException {
        Map<String, Class<?>> map = mock(Map.class);
        connectionAdapter.setTypeMap(map);
        verify(mockConnection, times(1)).setTypeMap(map);
    }

    @Test
    public void testCreateBlob() throws SQLException {
        assertNull(connectionAdapter.createBlob());
        verify(mockConnection, times(1)).createBlob();
    }

    @Test
    public void testCreateClob() throws SQLException {
        assertNull(connectionAdapter.createClob());
        verify(mockConnection, times(1)).createClob();
    }
    
    @Test
    public void testStatements() throws SQLException {
        assertNull(connectionAdapter.prepareCall(""));
        assertNull(connectionAdapter.prepareCall("",0,0));
        assertNull(connectionAdapter.prepareCall("",0,0,0));
        
        assertNull(connectionAdapter.createStatement());
        assertNull(connectionAdapter.createStatement(1,1));
        assertNull(connectionAdapter.createStatement(0, 0, 0));        

        assertNull(connectionAdapter.prepareStatement(""));
        assertNull(connectionAdapter.prepareStatement("",1,1));
        assertNull(connectionAdapter.prepareStatement("",0,0,0));
        assertNull(connectionAdapter.prepareStatement("",1));
        assertNull(connectionAdapter.prepareStatement("",new int[] {1}));
        assertNull(connectionAdapter.prepareStatement("",new String[] {"id"}));
    }
    @Test
    public void testCreateType() throws SQLException {
    	assertNull(connectionAdapter.createNClob());
    	assertNull(connectionAdapter.createSQLXML());
    	assertNull(connectionAdapter.createArrayOf("",new Object[0]));
    	assertNull(connectionAdapter.createStruct("",new Object[0]));
    	connectionAdapter.setSchema("");
    	assertNull(connectionAdapter.createNClob());
    	assertNull(connectionAdapter.createNClob());
    }
    
    @Test
    public void testConection() throws SQLException {
        assertFalse(connectionAdapter.isWrapperFor(DataSource.class));
        assertFalse(connectionAdapter.isClosed());
        assertFalse(connectionAdapter.isValid(5));
        connectionAdapter.setTransactionIsolation(0);
        assertEquals(0,connectionAdapter.getTransactionIsolation());
        connectionAdapter.setReadOnly(false);
        assertFalse(connectionAdapter.isReadOnly());
        connectionAdapter.setCatalog("");
        assertEquals(null,connectionAdapter.getCatalog());
        
        connectionAdapter.setNetworkTimeout(null,0);
        assertEquals(0,connectionAdapter.getNetworkTimeout());
        
        connectionAdapter.setClientInfo("","");
        assertNull(connectionAdapter.getClientInfo());
        assertNull(connectionAdapter.getClientInfo(""));
        connectionAdapter.setHoldability(0);
        assertEquals(0,connectionAdapter.getHoldability());
        connectionAdapter.setAutoCommit(false);
        assertFalse(connectionAdapter.getAutoCommit());
        
        
        assertNull(connectionAdapter.nativeSQL(""));
        assertNull(connectionAdapter.unwrap(DataSource.class));
        assertTrue(connectionAdapter.getTypeMap().isEmpty());
       
        connectionAdapter.setSavepoint();
        connectionAdapter.setSavepoint("");
        connectionAdapter.rollback(null);
        connectionAdapter.releaseSavepoint(null);
        assertNull(connectionAdapter.getSchema());
    	connectionAdapter.abort(null);
    }
}