package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertEquals;

import java.sql.Types;

import org.junit.Test;

import com.github.xuse.querydsl.sql.dialect.MySQLWithJSONTemplates;
import com.querydsl.core.types.SQLTemplatesEx;

public class DialectTest {
	private SQLTemplatesEx template=new MySQLWithJSONTemplates();
	
	@Test
	public void testDataType() {
		assertEquals("datetime",template.getColumnDataType(Types.TIMESTAMP, 0, 0).getDataType());
		assertEquals("datetime(2)",template.getColumnDataType(Types.TIMESTAMP, 2, 0).getDataType());
		assertEquals("datetime(6)",template.getColumnDataType(Types.TIMESTAMP, 6, 0).getDataType());
		assertEquals("datetime(6)",template.getColumnDataType(Types.TIMESTAMP, 9, 0).getDataType());
		assertEquals("datetime(6)",template.getColumnDataType(Types.TIMESTAMP, 29, 0).getDataType());
	}

}
