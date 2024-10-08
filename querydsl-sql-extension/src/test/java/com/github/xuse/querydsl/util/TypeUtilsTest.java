package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.expression.ClassLoaderAccessor;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.SimplePath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TimePath;

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
		
		List<Field> fields=TypeUtils.getFields(Foo.class);
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
		Assume.assumeTrue("Only for Database supports Partition",JDKEnvironment.JVM_VERSION>=16);
		URL clzUrl=this.getClass().getResource("/RecordFoo.class");
		assertNotNull(clzUrl);
		
		ClassLoaderAccessor cl=new ClassLoaderAccessor(this.getClass().getClassLoader());
		Class<?> recordClz = cl.defineClz("io.github.xuse.demo.RecordFoo", IOUtils.toByteArray(clzUrl));
		
		assertTrue(TypeUtils.isRecord(recordClz));
		String[] typeNames=
				TypeUtils.getRecordFieldNames(recordClz);
		assertEquals(5, typeNames.length);
	}
	
	@Test
	public void createPathTest() {
		String name="p";
		PathBuilder<Foo> parent=new PathBuilder<>(Foo.class, PathMetadataFactory.forVariable("a"));
		Path<?> p=TypeUtils.createPathByType(String.class, name, parent);
		assertTrue(p instanceof StringPath);
		
		p = TypeUtils.createPathByType(Long.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Integer.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Short.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Double.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Float.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Long.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Integer.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Short.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Double.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Float.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(BigInteger.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(BigDecimal.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = TypeUtils.createPathByType(Date.class, name, parent);
		assertTrue(p instanceof DateTimePath);
		
		/////////////////////////////
		p = TypeUtils.createPathByType(java.sql.Date.class, name, parent);
		assertTrue(p instanceof DatePath);
		
		p = TypeUtils.createPathByType(java.sql.Time.class, name, parent);
		assertTrue(p instanceof TimePath);
		
		p = TypeUtils.createPathByType(java.sql.Timestamp.class, name, parent);
		assertTrue(p instanceof DateTimePath);
		
		p = TypeUtils.createPathByType(Instant.class, name, parent);
		assertTrue(p instanceof DateTimePath);
		
		p = TypeUtils.createPathByType(LocalDate.class, name, parent);
		assertTrue(p instanceof DatePath);
		
		p = TypeUtils.createPathByType(LocalTime.class, name, parent);
		assertTrue(p instanceof TimePath);
		
		p = TypeUtils.createPathByType(LocalDateTime.class, name, parent);
		assertTrue(p instanceof DateTimePath);
		
		//////////////////////
		p = TypeUtils.createPathByType(byte[].class, name, parent);
		assertTrue(p instanceof SimplePath);
		
		p = TypeUtils.createPathByType(Gender.class, name, parent);
		assertTrue(p instanceof EnumPath);
		
		p = TypeUtils.createPathByType(Boolean.class, name, parent);
		assertTrue(p instanceof BooleanPath);
		
		p = TypeUtils.createPathByType(Boolean.TYPE, name, parent);
		assertTrue(p instanceof BooleanPath);
	}
}
