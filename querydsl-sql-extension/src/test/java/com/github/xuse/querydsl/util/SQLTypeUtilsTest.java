package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.ArrayPath;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.core.types.dsl.TimePath;

public class SQLTypeUtilsTest {
	@Test
	public void createPathTest() {
		String name="p";
		PathBuilder<Foo> parent=new PathBuilder<>(Foo.class, PathMetadataFactory.forVariable("a"));
		Path<?> p=SQLTypeUtils.createPathByType(String.class, name, parent);
		assertTrue(p instanceof StringPath);
		
		p = SQLTypeUtils.createPathByType(Long.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Integer.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Short.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Double.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Float.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Long.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Integer.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Short.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Double.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Float.TYPE, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(BigInteger.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(BigDecimal.class, name, parent);
		assertTrue(p instanceof NumberPath);
		
		p = SQLTypeUtils.createPathByType(Date.class, name, parent);
		assertTrue(p instanceof DateTimePath);
		
		/////////////////////////////
		p = SQLTypeUtils.createPathByType(java.sql.Date.class, name, parent);
		assertTrue(p instanceof DatePath);
		
		p = SQLTypeUtils.createPathByType(java.sql.Time.class, name, parent);
		assertTrue(p instanceof TimePath);
		
		p = SQLTypeUtils.createPathByType(java.sql.Timestamp.class, name, parent);
		assertTrue(p instanceof DateTimePath);
		
		p = SQLTypeUtils.createPathByType(Instant.class, name, parent);
		assertTrue(p instanceof DateTimePath);
		
		p = SQLTypeUtils.createPathByType(LocalDate.class, name, parent);
		assertTrue(p instanceof DatePath);
		
		p = SQLTypeUtils.createPathByType(LocalTime.class, name, parent);
		assertTrue(p instanceof TimePath);
		
		p = SQLTypeUtils.createPathByType(LocalDateTime.class, name, parent);
		assertTrue(p instanceof DateTimePath);
		
		//////////////////////
		p = SQLTypeUtils.createPathByType(byte[].class, name, parent);
		assertTrue(p instanceof ArrayPath);
		
		p = SQLTypeUtils.createPathByType(Gender.class, name, parent);
		assertTrue(p instanceof EnumPath);
		
		p = SQLTypeUtils.createPathByType(Boolean.class, name, parent);
		assertTrue(p instanceof BooleanPath);
		
		p = SQLTypeUtils.createPathByType(Boolean.TYPE, name, parent);
		assertTrue(p instanceof BooleanPath);
	}
}
