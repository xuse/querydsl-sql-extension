package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Instant;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.sql.expression.BeanCodec;
import com.github.xuse.querydsl.sql.expression.BeanCodecManager;

public class BeanCodecTest {
	@Test
	public void testCodecs() {
		Foo foo = new Foo();
		foo.setCode("ABC");
		foo.setContent("con");
		foo.setCreated(Instant.now());
		foo.setGender(Gender.FEMALE);
		foo.setId(1);
		foo.setVersion(100);

		BeanCodec codec = BeanCodecManager.getInstance().getCodec(Foo.class);

		Object[] values = codec.values(foo);
		System.out.println(Arrays.toString(values));
		{
			Foo foo2 = (Foo) codec.newInstance(values);
			assertEquals(foo, foo2);
		}
		{
			// Test Copy
			Foo foo2 = new Foo();
			codec.copy(foo, foo2);
			assertEquals(foo, foo2);
			// Test set
			Foo foo3=new Foo();
			codec.sets(values, foo3);
			assertEquals(foo2, foo3);
		}
		values[1] = 123456;
		{
			Foo foo2 = (Foo) codec.newInstance(values);
			System.out.println(foo2);
			assertNotEquals(foo, foo2);
			
			Foo foo3=new Foo();
			codec.sets(values, foo3);
			assertEquals(foo2, foo3);
		}
	}

//	public static record User(String username, String email) {}
}
