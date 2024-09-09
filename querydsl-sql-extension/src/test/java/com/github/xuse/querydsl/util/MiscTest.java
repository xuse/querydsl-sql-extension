package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

import com.github.xuse.querydsl.enums.Gender;

public class MiscTest {
	@Test
	public void testHolder() {
		Holder<Boolean> holder=new Holder<>(Boolean.FALSE);
		assertEquals(Boolean.FALSE,holder.get());
		
		holder=new Holder<>();
		assertEquals(null,holder.get());
		
		holder.value=Boolean.TRUE;
		assertEquals(Boolean.TRUE,holder.get());
	}
	
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
	
	@Test(expected = IllegalArgumentException.class)
	public void testEnumsException() {
		Gender g = Enums.valueOf(Gender.class, 3);
		assertEquals(Gender.MALE, g);
	}
	
	
	@Test
	public void testReader() throws IOException {
		try(StringReader reader=new StringReader(hello)){
			try(InputStream in=new ReaderInputStream(reader,StandardCharsets.UTF_8)){
				byte[] data=IOUtils.toByteArray(in);
				assertArrayEquals(hello.getBytes(StandardCharsets.UTF_8),data);
			}
		};
	}

}
