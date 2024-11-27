package com.github.xuse.querydsl.r2dbc.jdbcwrapper;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

public class LobTest {

	@Test
	public void charSequenceFluxReaderTest() throws IOException {
		List<String> strs = Arrays.asList("12345", "67890", "abcdef");
		try(CharSequenceFluxReader reader = new CharSequenceFluxReader(Flux.fromIterable(strs), 4, 15)){
			char[] buf=new char[4];
			System.out.println(reader.read(buf));
			assertArrayEquals(new char[]{'5','6','7','8'},buf);
			
			
			System.out.println(reader.read(buf));
			assertArrayEquals(new char[]{'9','0','a','b'},buf);
			
			System.out.println(reader.read(buf));
			assertArrayEquals(new char[]{'c','d','e','f'},buf);
			
			System.out.println(reader.read(buf));
			assertArrayEquals(new char[]{'c','d','e','f'},buf);	
		};
	}
}
