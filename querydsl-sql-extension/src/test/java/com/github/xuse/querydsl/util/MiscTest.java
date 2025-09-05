package com.github.xuse.querydsl.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.annotation.partition.AutoTimePartitions;
import com.github.xuse.querydsl.annotation.partition.ColumnFormat;
import com.github.xuse.querydsl.annotation.partition.Period;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.spring.core.resource.Resource;
import com.github.xuse.querydsl.util.lang.Annotations;
import com.github.xuse.querydsl.util.lang.Enums;

public class MiscTest {
	private String hello="Hello";
	
	@Test
	public void testFnvHash() {
		assertEquals(-178507445,FnvHash.fnv1a32(hello));
		assertEquals(7201466553693376363L,FnvHash.fnv1a64(hello));
		assertEquals(7201466553693376363L, FnvHash.fnv1a64(hello, 0, 9));
		assertEquals(3830027638908563206L, FnvHash.fnv1a64(hello, 0, 4));
		assertEquals(7201466553693376363L, FnvHash.fnv1a64(hello.toCharArray()));
		assertEquals(7201466553693376363L, FnvHash.fnv1a64(hello.getBytes(), 0, 9));
		assertEquals(3830027638908563206L, FnvHash.fnv1a64(hello.getBytes(), 0, 4));
		
		assertEquals(1335831723,FnvHash.fnv1a32Lower(hello));
		assertEquals(-6615550055289275125L,FnvHash.fnv1a64Lower(hello));
		
		assertEquals(-1221929401,FnvHash.fnv1a32Lower(hello+"1234+- #&$)@*$&_!"));
		assertEquals(100197987387005415L,FnvHash.fnv1a64Lower(hello+"1234+- #&$)@*$&_!"));
		
		String hello=null;
		assertEquals(0,FnvHash.fnv1a32(hello));
		assertEquals(0,FnvHash.fnv1a64(hello));
		assertEquals(0, FnvHash.fnv1a64(hello, 0, 4));
		assertEquals(0, FnvHash.fnv1a64((char[])null));
		assertEquals(0, FnvHash.fnv1a64((byte[])null, 0, 4));
		assertEquals(0,FnvHash.fnv1a32Lower(hello));
		assertEquals(0,FnvHash.fnv1a64Lower(hello));
	}
	
	@Test
	public void testEnums() {
		Gender g = Enums.valueOf(Gender.class, "MALE", Gender.FEMALE);
		assertEquals(Gender.MALE, g);

		g = Enums.valueOf(Gender.class, "BI", Gender.FEMALE);
		assertEquals(Gender.FEMALE, g);
		
		g = Enums.valueOf(Gender.class, 0);
		assertEquals(Gender.MALE, g);
		
		g = Enums.valueOf(Gender.class, -1,Gender.FEMALE);
		assertEquals(Gender.FEMALE, g);
		
		Integer i=1;
		g = Enums.valueOf(Gender.class, i,Gender.MALE);
		assertEquals(Gender.FEMALE, g);
		
		i=null;
		g = Enums.valueOf(Gender.class, i,Gender.MALE);
		assertEquals(Gender.MALE, g);
		
		try {
			g = Enums.valueOf(Gender.class, "MALE", "Invalid {}", "MMM");	
			g = Enums.valueOf(Gender.class, "MMM", "Invalid {}", "MMM");	
		}catch(IllegalArgumentException e) {
			assertEquals("Invalid MMM", e.getMessage());
		}
		
		Map<String,Gender> values=Enums.valuesMap(Gender.class);
		assertEquals(2, values.size());
	}
	
	@Test
	public void testEnumsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
		Gender g = Enums.valueOf(Gender.class, 3);
		assertEquals(Gender.MALE, g);
		});
	}
	
	@Test
	public void testSnowFlake() throws NoSuchFieldException, SecurityException, IllegalAccessException {
		SnowflakeIdWorker w= new SnowflakeIdWorker(10);
		assertTrue(w.nextId()!=0L);
		
		w= new SnowflakeIdWorker(2,2);
		assertTrue(w.nextId()!=0L);
		
		w.nextId();
		w.nextId();
		w.nextId();
		
		Field field=SnowflakeIdWorker.class.getDeclaredField("lastTimestamp");
		field.setAccessible(true);
		field.set(w, System.currentTimeMillis()+1000000L);
		
		try {
			w.nextId();
		}catch(Exception e) {
			assertTrue(e.getMessage().startsWith("Clock moved backwards"));
		}
	}

	@Test
	public void snowFlake() {
		SnowflakeIdWorker worker=new SnowflakeIdWorker(7,0) ;
		System.out.println(worker.nextId());
	}
	
	public static final Date get(int year, int month, int date, int hour, int minute, int second) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, date, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	@Test
	public void testSnowFlakeError1() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
		new SnowflakeIdWorker(-1);
		});
	}
	
	@Test
	public void testSnowFlakeError2() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
		new SnowflakeIdWorker(260);
		});
	}
	
	@Test
	public void testSnowFlakeError3() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
		new SnowflakeIdWorker(10, 101);
		});
	}
	
	@Test
	public void testSnowFlakeError4() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {
		new SnowflakeIdWorker(10, -1);
		});
	}
	
	@Test
	public void testClassScanner() {
		ClassScanner c=new ClassScanner();
		c.excludeInnerClass(true);
		assertTrue(c.isExcludeInnerClass());
		c.filterWith(e->true);
		List<Resource> ress=c.scan(new String[] {"com.github.xuse.querydsl.entity.partition"});
		assertTrue(ress.size()>0);
		
		ress=c.scan(new String[0]);
		assertTrue(ress.size()>0);
		
		
		
		URL url=this.getClass().getResource("/");
		c.rootClasspath(url);
		c.excludeInnerClass(false);
		c.filterWith(e->false);
		ress=c.scan(new String[] {" ","com.github.xuse.querydsl.util"});
		assertTrue(ress.size()==0);
		
		
		c.filterWith(null);
		ress=c.scan(new String[] {" "});
		assertTrue(ress.size()==0);
		
		ress=c.scan(new String[] {"com.github.xuse.querydsl.util"});
		assertTrue(ress.size()>0);
	}
	
	@Test	
	public void testAnnotations() {
		AutoTimePartitions a = Annotations.builder(AutoTimePartitions.class)
				.set(AutoTimePartitions::unit, Period.DAY)
				.set(AutoTimePartitions::periodsBegin, 1)
				.set(AutoTimePartitions::periodsEnd, 5)
				.set(AutoTimePartitions::createForMaxValue, true)
				.set(AutoTimePartitions::columnFormat, ColumnFormat.NUMBER_YEAR)
				.build();
		assertEquals(a.columnFormat(),ColumnFormat.NUMBER_YEAR);
		assertEquals(a.createForMaxValue(),true);
		assertEquals(a.periodsBegin(),1);
		assertEquals(a.periodsEnd(),5);
		assertEquals(a.unit(),Period.DAY);
	}
}
