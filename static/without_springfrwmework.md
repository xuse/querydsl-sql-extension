## 创建SQLQueryFactory对象

### 方法1 传入DataSource

如果传入带有连接池的DataSource，, 每次操作完成后可以自动归还连接。

```java
//Create your datasource
DataSource ds = createYourDataSource();

ConfigurationEx configuration = new ConfigurationEx(SQLQueryFactory.calcSQLTemplate(Constants.jdbcUrl));
configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_COMPACT));

com.github.xuse.querydsl.sql.SQLQueryFactory factory = new SQLQueryFactory(configuration, ds, true);
```

### 方法2 自行管理连接

```java
DataSource ds = createYourDataSource();

ConfigurationEx configuration = new ConfigurationEx(SQLQueryFactory.calcSQLTemplate(Constants.jdbcUrl));
configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_COMPACT));


try(Connection conn=ds.getConnection){
	com.github.xuse.querydsl.sql.SQLQueryFactory factory = new SQLQueryFactory(configuration, ds, true);    
    .... //Your business code.
}
```

### 方法3 自行编写一个手动事务管理器

用法示例

```java
public static com.github.xuse.querydsl.sql.SQLQueryFactory factory;
public static TransactionProvider tx;
    
static{
	DataSource ds = createYourDataSource();
	ConfigurationEx configuration = new ConfigurationEx(SQLQueryFactory.calcSQLTemplate(Constants.jdbcUrl));
	configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_COMPACT));
	factory = new SQLQueryFactory(configuration, tx = new TransactionProvider(ds));    
}

//使用
public void someBusiness(){
    tx.beginTransaction();
    
	try{
	    //Do business logic.        
        //commit and close transaction.
	    tx.commitTransaction(true);
    } catch(RuntimeException e){
		tx.rollbackTransaction(true);
    }


}

```

实现仅供参考。

```java
public class TransactionProvider implements Supplier<Connection> {
    private final DataSource dataSource;
    
    private final ThreadLocal<ConnectionHolder> threadLocal=new ThreadLocal<>();
    
    private final Connection globalConnection;
    
    public TransactionProvider(DataSource dataSource) {
    	this(dataSource,true);
    }

    public TransactionProvider(DataSource dataSource,boolean keepGlobalConn) {
        this.dataSource = dataSource;
        if(keepGlobalConn) {
        	try {
				this.globalConnection=dataSource.getConnection();
				globalConnection.setAutoCommit(true);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
        }else {
        	globalConnection=null;
        }
    }

    @Override
    public Connection get() {
    	ConnectionHolder conn=threadLocal.get();
    	if(conn!=null) {
    		return conn.conn;
    	}
    	if(globalConnection!=null) {
    		return globalConnection;
    	}
    	throw new IllegalStateException("There's no transaction");
    }
    
    public void beginTransaction() {
    	ConnectionHolder holder=threadLocal.get();
    	Assert.isNull(holder);
    	holder = new ConnectionHolder(dataSource);
    	threadLocal.set(holder);
    }
    
    public void endTransaction() {
    	ConnectionHolder holder=threadLocal.get();
    	Assert.notNull(holder);
    	close(holder);
    }
    
    public void commitTransaction(boolean close) {
    	ConnectionHolder holder=threadLocal.get();
    	Assert.notNull(holder);
    	try {
    		holder.commit();	
    	}finally {
    		if(close) {
        		close(holder);
        	}	
    	}
    }
    
	public void rollbackTransaction(boolean close) {
    	ConnectionHolder holder=threadLocal.get();
    	Assert.notNull(holder);
    	try {
    		holder.rollback();
    	}finally {
        	if(close) {
        		close(holder);
        	}
    	}
    }
	
	public void close() {
		ConnectionHolder holder=threadLocal.get();
		if(holder!=null) {
			close(holder);
		}
	}
	    
    private void close(ConnectionHolder holder) {
    	threadLocal.remove();
		holder.close();
	}
	
	static class ConnectionHolder{
		private final Connection conn;
		
		@SneakyThrows
		public ConnectionHolder(DataSource dataSource) {
			Connection conn = this.conn= dataSource.getConnection();
			conn.setAutoCommit(false);
		}
		@SneakyThrows
		public void commit() {
			conn.commit();
		}
		@SneakyThrows
		public void rollback(){
			conn.rollback();
		}
		@SneakyThrows
		public void close(){
			conn.close();
		}
	}

	public void shutdown() {
		if(globalConnection!=null) {
			try {
				globalConnection.close();
			} catch (SQLException e) {
				//Do nothing.
			}
		}
	}
}
```

