package com.github.xuse.querydsl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.github.xuse.querydsl.sql.dialect.MySQLWithJSONTemplates;
import com.querydsl.sql.SQLTemplates;

public class DevUtils {
	static Field queryDslKeywords;
	static {
		try {
			queryDslKeywords = SQLTemplates.class.getDeclaredField("reservedWords");
			queryDslKeywords.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void compareKeywords() throws IllegalArgumentException, IllegalAccessException {
		MySQLWithJSONTemplates t=new MySQLWithJSONTemplates();
		List<String> keys=loadMyKeywords("mysql8_keywords");
		Set<String> queryDsl=(Set<String>) queryDslKeywords.get(t);
		for(String key:keys) {
			if(!queryDsl.contains(key.toUpperCase())){
				System.out.println(key);
			}
		}
	}

	private List<String> loadMyKeywords(String path) {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/keywords/" + path)));) {
            return bufferedReader.lines()
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
	}
}
