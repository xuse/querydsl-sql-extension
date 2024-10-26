package com.github.xuse.querydsl.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.Exceptions.WrapException;

public class ExceptionsTest {

	private static final String DEFAULT = "DEFAULT";

	private IOException io = new IOException();
	private IllegalArgumentException iga = new IllegalArgumentException();
	private InvocationTargetException ite=new InvocationTargetException(iga);
	private RuntimeException runtime=new RuntimeException();
	private IllegalStateException ise=new IllegalStateException();
	
	
	@Test
	public void testExceptions() {
		assertEquals(iga,Exceptions.toRuntime(iga));
		assertTrue(Exceptions.toRuntime(io).getClass()==WrapException.class);
		assertEquals(iga,Exceptions.toRuntime(ite));
		
		{
			IllegalArgumentException ex=Exceptions.illegalArgument("{},{}", "a","b",io);
			assertEquals(io, ex.getCause());
		
			ex=Exceptions.illegalArgument("{},{}", "a","b");
			assertNull(ex.getCause());
			
			ex=Exceptions.asIllegalArgument(ite);
			assertEquals(iga, ex);
			
			ex=Exceptions.asIllegalArgument(iga);
			assertEquals(iga, ex);
			
			ex=Exceptions.illegalArgument(runtime, false);
			assertEquals(runtime, ex.getCause());
			
			try {
				ex=Exceptions.asIllegalArgument(runtime);	
			}catch(RuntimeException r) {
				assertEquals(runtime, r);	
			}
		}
		{
			IllegalStateException ex=Exceptions.illegalState("{},{}", "a","b",io);
			assertEquals(io, ex.getCause());
		
			ex=Exceptions.illegalState("{},{}", "a","b");
			assertNull(ex.getCause());
			
			ex=Exceptions.illegalState(new InvocationTargetException(ise));
			assertEquals(ise,ex);
			
			ex=Exceptions.illegalState(ise);
			assertEquals(ise, ex);
			
			ex=Exceptions.illegalState(runtime, false);
			assertEquals(runtime, ex.getCause());
			
			try {
				 ex=Exceptions.illegalState(iga);
			}catch(RuntimeException r) {
				assertEquals(iga, r);	
			}
			try {
				 ex=Exceptions.illegalState(ite);
			}catch(RuntimeException r) {
				assertEquals(iga, r);	
			}
		}
		{
			NoSuchElementException  ex=Exceptions.noSuchElement("{},{}", "a","b");
			assertNull(ex.getCause());
		}
		
		{
			IndexOutOfBoundsException ex=Exceptions.indexOutOfBounds("{},{}", "a","b");
			assertNull(ex.getCause());
		}
		{
			UnsupportedOperationException  ex=Exceptions.unsupportedOperation("{},{}", "a","b",io);
			assertEquals(io, ex.getCause());
		
			ex=Exceptions.unsupportedOperation("{},{}", "a","b");
			assertNull(ex.getCause());
		}
		{
			String str=Exceptions.format("{}, {}", "Hello","World");
			assertEquals("Hello, World",str);
		}
	}

	@Test
	public void testFunctions() {
		assertEquals("", Exceptions.apply(() -> getString(false), DEFAULT));
		assertEquals(DEFAULT, Exceptions.apply(() -> getString(true), DEFAULT));

		assertEquals("", Exceptions.apply(this::getString, false, DEFAULT));
		assertEquals(DEFAULT, Exceptions.apply(this::getString, true, DEFAULT));

		assertEquals("", Exceptions.apply((a, b) -> getString(false), "a", "b", DEFAULT));
		assertEquals(DEFAULT, Exceptions.apply((a, b) -> getString(true), "a", "b", DEFAULT));

		assertEquals("", Exceptions.applyNotNull(() -> getString(false), DEFAULT));
		assertEquals(DEFAULT, Exceptions.applyNotNull(() -> getNull(false), DEFAULT));
		assertEquals(DEFAULT, Exceptions.applyNotNull(() -> getNull(true), DEFAULT));

		assertEquals("", Exceptions.applyNotNull(this::getString, false, DEFAULT));
		assertEquals(DEFAULT, Exceptions.applyNotNull(this::getNull, false, DEFAULT));
		assertEquals(DEFAULT, Exceptions.applyNotNull(this::getNull, true, DEFAULT));

		assertEquals("", Exceptions.applyNotNull((a, b) -> getString(false), "a", "b", DEFAULT));
		assertEquals(DEFAULT, Exceptions.applyNotNull((a, b) -> getNull(false), "a", "b", DEFAULT));
		assertEquals(DEFAULT, Exceptions.applyNotNull((a, b) -> getNull(true), "a", "b", DEFAULT));
	}
	
	@Test
	public void testRetry() {
		boolean result = Exceptions.retry(3, "a", e->{
			return false;
		});
		assertFalse(result);
		
		final AtomicInteger count=new AtomicInteger();
		result = Exceptions.retry(3, "a", e->{
			return count.incrementAndGet()>2;
		});
		assertTrue(result);
	}

	private String getString(boolean rise) {
		if (rise) {
			throw new IllegalArgumentException();
		}
		return "";
	}

	private String getNull(boolean rise) {
		if (rise) {
			throw new IllegalArgumentException();
		}
		return null;
	}

}
