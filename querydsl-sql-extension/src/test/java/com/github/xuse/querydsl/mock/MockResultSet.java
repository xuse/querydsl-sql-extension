package com.github.xuse.querydsl.mock;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MockResultSet extends AbstractResultSet {
	private List<Object[]> data=Collections.emptyList();
	private ResultSetMetaData metadata;
	private int index = -1;
	private boolean closed;
	
	public static final MockResultSet EMPTY=new MockResultSet() {
		@Override
		protected int toIndex(int columnIndex) {
			return 0;
		}
	};
	
	public static MockResultSet of(List<Object[]> data, List<String> names) {
		MockResultSet rs=new MockResultSet();
		rs.data=data;
		rs.metadata = MockResultSetMetadata.of(names);
		return rs; 
	}
	

	@Override
	public boolean next() throws SQLException {
		return ++index < data.size();
	}

	@Override
	public void close() throws SQLException {
		closed = true;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return metadata;
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return index == -1;
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return index >= data.size();
	}

	@Override
	public boolean isFirst() throws SQLException {
		return index == 0;
	}

	@Override
	public boolean isLast() throws SQLException {
		return index == data.size() - 1;
	}

	@Override
	public void beforeFirst() throws SQLException {
		index = -1;
	}

	@Override
	public void afterLast() throws SQLException {
		index = data.size();
	}
	

	private int toIndex(String columnName) {
		try {
			for(int i=1;i<=metadata.getColumnCount();i++) {
				if(columnName.equalsIgnoreCase(metadata.getColumnName(i))) {
					return i;
				}
			}
		} catch (SQLException e) {
		}
		return 0;
	}
	
	protected int toIndex(int columnIndex) {
		return columnIndex - 1;
	}

	@Override
	public boolean first() throws SQLException {
		if (data.isEmpty()) {
			return false;
		}
		index = 0;
		return true;
	}

	@Override
	public boolean last() throws SQLException {
		if (data.isEmpty()) {
			return false;
		}
		index = data.size() - 1;
		return true;
	}

	@Override
	public boolean previous() throws SQLException {
		return --index >= 0;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}
	
	public Object getObject(String columnName) throws SQLException {
		return data.get(this.index)[toIndex(columnName)];
	}

	public Object getObject(int cIndex) throws SQLException {
		return data.get(this.index)[toIndex(cIndex)];
	}
	
	public boolean getBoolean(String columnName) throws SQLException {
		return getBoolean(toIndex(columnName));
	}
	
	public boolean getBoolean(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof Boolean) {
			return (Boolean)o;
		}
		if(o==null) {
			return false;
		}
		return !"0".equals(o.toString());
	}

	public double getDouble(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof Number) {
			return ((Number)o).doubleValue();
		}
		if(o==null) {
			return 0d;
		}
		throw new SQLException("invalid type:"+o.getClass().getName());
	}
	
	public double getDouble(String columnName) throws SQLException {
		return getDouble(toIndex(columnName));
	}

	public float getFloat(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof Number) {
			return ((Number)o).floatValue();
		}
		if(o==null) {
			return 0f;
		}
		throw new SQLException("invalid type:"+o.getClass().getName());
	}

	public float getFloat(String columnName) throws SQLException {
		return getFloat(toIndex(columnName));
	}
	
	public long getLong(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof Number) {
			return ((Number)o).longValue();
		}
		if(o==null) {
			return 0L;
		}
		throw new SQLException("invalid type:"+o.getClass().getName());
	}

	public long getLong(String columnName) throws SQLException {
		return getLong(toIndex(columnName));
	}
	
	public int getInt(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof Number) {
			return ((Number)o).intValue();
		}
		if(o==null) {
			return 0;
		}
		throw new SQLException("invalid type:"+o.getClass().getName());
	}
	
	public int getInt(String columnName) throws SQLException {
		return getInt(toIndex(columnName));
	}

	public String getString(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof CharSequence) {
			return String.valueOf(o);
		}
		if(o==null) {
			return null;
		}
		throw new SQLException("invalid type:"+o.getClass().getName());
	}
	
	public String getString(String columnName) throws SQLException {
		return getString(toIndex(columnName));
	}

	public java.sql.Date getDate(String columnName) throws SQLException {
		return getDate(toIndex(columnName));
	}
	
	public java.sql.Date getDate(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof java.sql.Date) {
			return (java.sql.Date)o;
		}else if(o instanceof java.util.Date) {
			long time=((Date)o).getTime();
			return new java.sql.Date(time);
		}
		if(o==null) {
			return null;
		}
		throw new SQLException("invalid type:"+o.getClass().getName());
	}
	
	public java.sql.Timestamp getTimestamp(String columnName) throws SQLException {
		return getTimestamp(toIndex(columnName));
	}

	public Timestamp getTimestamp(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof java.sql.Timestamp) {
			return (java.sql.Timestamp)o;
		}else if(o instanceof java.util.Date) {
			long time=((Date)o).getTime();
			return new java.sql.Timestamp(time);
		}
		if(o==null) {
			return null;
		}
		throw new SQLException("invalid type:"+o.getClass().getName());
	}

	public Time getTime(int i) throws SQLException {
		Object o=getObject(i);
		if(o instanceof java.sql.Time) {
			return (java.sql.Time)o;
		}else if(o instanceof java.util.Date) {
			long time=((Date)o).getTime();
			return new java.sql.Time(time);
		}
		if(o==null) {
			return null;
		}
		throw new SQLException("invalid type:"+o.getClass().getName());
	}

	public java.sql.Time getTime(String columnName) throws SQLException {
		return getTime(toIndex(columnName));
	}

	
	@Override
	protected ResultSet get() throws SQLException {
		throw new UnsupportedOperationException();
	}
}
