package com.github.xuse.querydsl.util;

import org.junit.Test;

import com.github.xuse.querydsl.datatype.util.binary.BufferedEncoder;

public class BinaryUtilsTest {
	@Test
	public void testBinary() {
		Aaa a = new Aaa();
		a.setName(StringUtils.randomString());
//		a.setGender(Gender.FEMALE);
//		a.setTaskStatus(TaskStatus.INIT);
		a.setVersion(1);
		
		BufferedEncoder encode=new BufferedEncoder(1024);
		encode.putObject(a);
		
		byte[] buffer= encode.toByteArray();
		System.out.println(buffer.length);
		
		
		
	}
}
