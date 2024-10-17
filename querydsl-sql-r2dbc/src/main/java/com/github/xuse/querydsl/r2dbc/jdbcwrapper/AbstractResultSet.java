package com.github.xuse.querydsl.r2dbc.jdbcwrapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.github.xuse.querydsl.util.collection.CollectionUtils;

import io.r2dbc.spi.ColumnMetadata;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;

abstract class AbstractResultSet extends UpdateUnsupportedResultSet {
	protected Row row;
	protected List<? extends ColumnMetadata> metadatas;
	protected Map<String,? extends ColumnMetadata> index;
	private boolean wasNull;
	
	protected AbstractResultSet() {
		this.metadatas = row.getMetadata().getColumnMetadatas();
	}

	public void prepare(Row row, RowMetadata meta) {
		this.row=row;
		if(metadatas == null) {
			metadatas = meta.getColumnMetadatas();
			initIndexMap(metadatas);
		}
	}

	public boolean getBoolean(int columnIndex) {
		if(isBool(columnIndex)) {
			return pickPrimitive(row.get(columnIndex-1, Boolean.class), false);
		}else if(isNumber(columnIndex)) {
			return pickPrimitive(row.get(columnIndex-1, Integer.class), false, (v) -> v.intValue() == 1);
		}else {
			return pickPrimitive(row.get(columnIndex-1, String.class), false, (v) -> v.equals("1"));
		}
	}

	public double getDouble(int columnIndex) {
		return pickPrimitive(row.get(columnIndex-1, Double.class), 0d);
	}

	public float getFloat(int columnIndex) {
		return pickPrimitive(row.get(columnIndex-1, Float.class), 0f);
	}

	public long getLong(int columnIndex) {
		return pickPrimitive(row.get(columnIndex-1, Long.class), 0L);
	}

	public int getInt(int columnIndex) {
		return pickPrimitive(row.get(columnIndex-1, Integer.class), 0);
	}

	public String getString(int columnIndex) {
		return row.get(columnIndex-1, String.class);
	}

	public java.sql.Date getDate(int columnIndex) {
		return row.get(columnIndex-1, java.sql.Date.class);
	}

	public Timestamp getTimestamp(int columnIndex) {
		return row.get(columnIndex-1, java.sql.Timestamp.class);
	}

	public Time getTime(int columnIndex) {
		return row.get(columnIndex-1,java.sql.Time.class);
	}

	public Clob getClob(int columnIndex) {
		io.r2dbc.spi.Clob clob = row.get(columnIndex-1,io.r2dbc.spi.Clob.class);
		return clob==null? null:new JdbcClobImpl(clob);
	}

	public Blob getBlob(int columnIndex) {
		io.r2dbc.spi.Blob blob= row.get(columnIndex-1, io.r2dbc.spi.Blob.class);
		return blob==null?null:new JdbcBlobImpl(blob);
	}

	public boolean getBoolean(String columnName) {
		if(isBool(columnName)) {
			return pickPrimitive(row.get(columnName, Boolean.class), false);
		}else if(isNumber(columnName)) {
			return pickPrimitive(row.get(columnName, Integer.class), false, (v) -> v.intValue() == 1);
		}else {
			return pickPrimitive(row.get(columnName, String.class), false, (v) -> v.equals("1"));
		}
	}

	public double getDouble(String columnName) {
		return pickPrimitive(row.get(columnName, Double.class), 0d);
	}

	public float getFloat(String columnName) {
		return pickPrimitive(row.get(columnName, Float.class), 0f);
	}

	public long getLong(String columnName) {
		return pickPrimitive(row.get(columnName, Long.class), 0L);
	}

	public int getInt(String columnName) {
		return pickPrimitive(row.get(columnName, Integer.class), 0);
	}

	public Clob getClob(String columnName) {
		io.r2dbc.spi.Clob clob = row.get(columnName,io.r2dbc.spi.Clob.class);
		return clob==null? null:new JdbcClobImpl(clob);
	}

	public Blob getBlob(String columnName) {
		io.r2dbc.spi.Blob blob= row.get(columnName, io.r2dbc.spi.Blob.class);
		return blob==null?null:new JdbcBlobImpl(blob);
	}

	public String getString(String columnName) {
		return row.get(columnName, String.class);
	}

	public Timestamp getTimestamp(String columnName) {
		return row.get(columnName, java.sql.Timestamp.class);
	}

	public Time getTime(String columnName) {
		return row.get(columnName, java.sql.Time.class);
	}

	public Date getDate(String columnName) {
		return row.get(columnName, java.sql.Date.class);
	}

	public byte[] getBytes(int columnIndex) {
		return row.get(columnIndex-1,byte[].class);
	}

	@Override
	public byte[] getBytes(String columnLabel) {
		return row.get(columnLabel,byte[].class);
	}

	public RowId getRowId(int columnIndex) {
		//PG和Oracle的rowid实现名称不一样。需要驱动原生支持
		throw new UnsupportedOperationException();
	}

	
	public Object getObject(String columnName) {
		return row.get(columnName);
	}
	
	public Object getObject(int columnIndex) {
		return row.get(columnIndex-1);
	}

	public InputStream getBinaryStream(int columnIndex) {
		byte[] data= row.get(columnIndex-1,byte[].class);
		return new ByteArrayInputStream(data);
	}

	public InputStream getUnicodeStream(int columnIndex) {
		byte[] data= row.get(columnIndex-1,byte[].class);
		return data == null? null: new ByteArrayInputStream(data);
	}
	
	@Override
	public InputStream getUnicodeStream(String columnLabel) {
		byte[] data= row.get(columnLabel,byte[].class);
		return data == null? null: new ByteArrayInputStream(data);
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) {
		byte[] data= row.get(columnLabel,byte[].class);
		return data==null? null: new ByteArrayInputStream(data);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) {
		byte[] data= row.get(columnLabel,byte[].class);
		return data==null? null: new ByteArrayInputStream(data);
	}
	
	public InputStream getAsciiStream(int columnIndex) {
		byte[] data= row.get(columnIndex-1,byte[].class);
		return data==null? null: new ByteArrayInputStream(data);
	}

	public short getShort(int columnIndex) {
		return pickPrimitive(row.get(columnIndex-1, Short.class), (short)0);
	}

	public byte getByte(int columnIndex) {
		return pickPrimitive(row.get(columnIndex-1, Byte.class), (byte)0);
	}

	@Override
	public byte getByte(String columnLabel) {
		return pickPrimitive(row.get(columnLabel, Byte.class), (byte)0);
	}

	@Override
	public short getShort(String columnLabel) {
		return pickPrimitive(row.get(columnLabel, Short.class), (short)0);
	}
	
	public SQLXML getSQLXML(int columnIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Reader getCharacterStream(int columnIndex) {
		String s=row.get(columnIndex-1,String.class);
		return s == null ? null : new StringReader(s);
	}

	@Override
	public Reader getCharacterStream(String columnLabel) {
		String s=row.get(columnLabel,String.class);
		return s == null ? null : new StringReader(s);
	}

	@Override
	public boolean wasNull() {
		return wasNull;
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) {
		return row.get(columnIndex-1,BigDecimal.class);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) {
		return row.get(columnLabel,BigDecimal.class);
	}


	@Override
	public BigDecimal getBigDecimal(int columnIndex) {
		return row.get(columnIndex-1,BigDecimal.class);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) {
		return row.get(columnLabel,BigDecimal.class);
	}

	@Override
	public int getRow() {
		throw new UnsupportedOperationException("getRow");
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) {
		throw new UnsupportedOperationException("getObject");
	}

	@Override
	public Ref getRef(int columnIndex) {
		throw new UnsupportedOperationException("getRef");
	}

	@Override
	public Array getArray(int columnIndex) {
		throw new UnsupportedOperationException("getArray");
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) {
		throw new UnsupportedOperationException("getObject");
	}

	@Override
	public Ref getRef(String columnLabel) {
		throw new UnsupportedOperationException("getRef");
	}

	@Override
	public Array getArray(String columnLabel) {
		throw new UnsupportedOperationException("getArray");
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) {
		return row.get(columnIndex-1,Date.class);
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) {
		return row.get(columnLabel,Date.class);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) {
		return row.get(columnIndex-1,Time.class);
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) {
		return row.get(columnLabel,Time.class);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) {
		return row.get(columnIndex-1,Timestamp.class);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) {
		return row.get(columnLabel,Timestamp.class);
	}

	@Override
	public RowId getRowId(String columnLabel) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NClob getNClob(int columnIndex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public NClob getNClob(String columnLabel) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNString(int columnIndex) {
		return row.get(columnIndex-1,String.class);
	}

	@Override
	public String getNString(String columnLabel) {
		return row.get(columnLabel,String.class);
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) {
		String s= row.get(columnIndex-1,String.class);
		return s==null?null:new StringReader(s);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) {
		String s= row.get(columnLabel,String.class);
		return s==null?null:new StringReader(s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) {
		return (T)this;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) {
		return iface==Row.class;
	}


	@Override
	public SQLWarning getWarnings() {
		throw new UnsupportedOperationException("getWarnings");
	}

	@Override
	public void clearWarnings() {
		throw new UnsupportedOperationException("clearWarnings");
	}

	@Override
	public String getCursorName() {
		throw new UnsupportedOperationException("getCursorName");
	}

	@Override
	public int findColumn(String columnLabel) {
		throw new UnsupportedOperationException("findColumn");
	}

	@Override
	public boolean absolute(int row) {
		throw new UnsupportedOperationException("absolute");
	}

	@Override
	public boolean relative(int rows) {
		throw new UnsupportedOperationException("relative");
	}

	@Override
	public void setFetchDirection(int direction) {
		throw new UnsupportedOperationException("setFetchDirection");
	}

	@Override
	public int getFetchDirection() {
		throw new UnsupportedOperationException("getFetchDirection");
	}

	@Override
	public void setFetchSize(int rows) {
		throw new UnsupportedOperationException("setFetchSize");
	}

	@Override
	public int getFetchSize() {
		throw new UnsupportedOperationException("getFetchSize");
	}

	@Override
	public int getType() {
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	@Override
	public int getConcurrency() {
		return ResultSet.CONCUR_READ_ONLY;
	}

	@Override
	public Statement getStatement() {
		throw new UnsupportedOperationException("getStatement");
	}

	@Override
	public URL getURL(int columnIndex) {
		return row.get(columnIndex-1,URL.class);
	}

	@Override
	public URL getURL(String columnLabel) {
		return row.get(columnLabel,URL.class);
	}

	@Override
	public int getHoldability() {
		return ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public void moveToCurrentRow() {
		throw new UnsupportedOperationException("moveToCurrentRow");
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) {
		return row.get(columnIndex-1,type);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) {
		return row.get(columnLabel,type);
	}
	
	//////////
	private  <T> T pickPrimitive(T value, T ifNull) {
		if (wasNull = value == null) {
			return ifNull;
		}
		return value;
	}
	
	private <V, T> T pickPrimitive(V value, T ifNull, Function<V, T> func) {
		if (wasNull = value == null) {
			return ifNull;
		}
		return func.apply(value);
	}
	
	private boolean isBool(String name) {
		ColumnMetadata metadata=getIndexMap().get(name);
		return metadata!=null && Boolean.class==metadata.getJavaType();    
	}
	
	private boolean isNumber(String name) {
		ColumnMetadata metadata=getIndexMap().get(name);
		return metadata!=null && Number.class.isAssignableFrom(metadata.getJavaType());    
	}
	
	private Map<String,? extends ColumnMetadata> getIndexMap() {
		return index;
	}
	
	private void initIndexMap(List<? extends ColumnMetadata> metadatas) {
		index = CollectionUtils.group(metadatas, ColumnMetadata::getName);
	}

	private boolean isNumber(int i) {
		return Number.class.isAssignableFrom(metadatas.get(i).getJavaType());    
	}
	
	private boolean isBool(int i) {
		return Boolean.class == metadatas.get(i).getJavaType();
	}
	
}