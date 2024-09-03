package com.github.xuse.querydsl.util;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

import org.junit.Test;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.util.binary.BufferedEncoder;
import com.github.xuse.querydsl.util.binary.CodecContext;

public class BinaryUtilsTest {
	@Test
	public void testBinary() {
		Aaa a = new Aaa();
		a.setName(StringUtils.randomString());
		a.setGender(Gender.FEMALE);
		a.setTaskStatus(TaskStatus.INIT);
		a.setVersion(1);
		a.setDataDouble(2.4d);
		a.setDataInt(23);
		a.setDataFloat(0.2f);
		a.setDataShort((short) 1);
		a.setDataBigint(213L);
		a.setDataDecimal(new BigDecimal("1"));
		a.setDataBool(false);
		a.setDataDate(new Date());
		a.setDataTime(new Time(1000));
		a.setDateTimestamp(new Date());
		a.setDataBit(false);
		
		
		BufferedEncoder encode=new BufferedEncoder(1024);
		encode.putObject(a);
		
		byte[] buffer= encode.toByteArray();
		System.out.println(buffer.length);
		
		
		
	}
}
