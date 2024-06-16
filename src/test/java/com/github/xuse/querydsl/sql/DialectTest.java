package com.github.xuse.querydsl.sql;

import java.sql.Types;

import org.junit.Test;

import com.github.xuse.querydsl.sql.dialect.MySQLWithJSONTemplates;
import com.querydsl.core.types.SQLTemplatesEx;

public class DialectTest {
	private SQLTemplatesEx template=new MySQLWithJSONTemplates();
	
	@Test
	public void testDataType() {
		System.out.println(template.getColumnDataType(Types.TIMESTAMP, 0, 0));
		System.out.println(template.getColumnDataType(Types.TIMESTAMP, 2, 0));
		System.out.println(template.getColumnDataType(Types.TIMESTAMP, 6, 0));
		System.out.println(template.getColumnDataType(Types.TIMESTAMP, 9, 0));
		System.out.println(template.getColumnDataType(Types.TIMESTAMP, 29, 0));
	}

}
