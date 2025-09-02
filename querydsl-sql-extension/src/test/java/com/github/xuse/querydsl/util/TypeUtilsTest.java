package com.github.xuse.querydsl.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.expression.ClassLoaderAccessor;

public class TypeUtilsTest {

	
	@Test
	public void typeUtilsTest() {
		CharSequence cs=TypeUtils.tryCast("", CharSequence.class);
		assertNotNull(cs);
		
		cs=TypeUtils.tryCast(new Object(), CharSequence.class);
		assertNull(cs);
		
		cs=TypeUtils.tryCast(null, CharSequence.class);
		assertNull(cs);
		
		Constructor<?> cons= TypeUtils.getDeclaredConstructor(Foo.class);
		assertNotNull(cons);
		
		cons= TypeUtils.getDeclaredConstructor(SQLQueryFactory.class);
		assertNull(cons);
		
		List<Field> fields=TypeUtils.getAllDeclaredFields(Foo.class);
		assertTrue(fields.size()>0);
		
		assertFalse(TypeUtils.isRecord(SQLQueryFactory.class));
		
		Foo foo=TypeUtils.newInstance(Foo.class);
		assertNotNull(foo);
		
		Exception e = null;
		try {
			TypeUtils.newInstance(SQLQueryFactory.class);
		}catch(Exception ex) {
			e = ex;
		}
		assertEquals(NoSuchMethodException.class , e.getCause().getClass());
		
		try {
			TypeUtils.newInstance(Ex.class);
		}catch(Exception ex) {
			e = ex;
		}
		assertEquals(IllegalArgumentException.class , e.getClass());
	}
	
	static class Ex {
		public Ex(){
			throw new IllegalArgumentException();
		}
	}

	@Test
	public void testRecordAnaly() {
		Assumptions.assumeTrue(JDKEnvironment.JVM_VERSION>=16,"Only JDK > 16");
		URL clzUrl=this.getClass().getResource("/RecordFoo.class");
		assertNotNull(clzUrl);
		
		ClassLoaderAccessor cl=new ClassLoaderAccessor(this.getClass().getClassLoader());
		Class<?> recordClz = cl.defineClz("io.github.xuse.demo.RecordFoo", IOUtils.toByteArray(clzUrl));
		
		assertTrue(TypeUtils.isRecord(recordClz));
		String[] typeNames=
				TypeUtils.getRecordFieldNames(recordClz);
		assertEquals(5, typeNames.length);
	}
}
