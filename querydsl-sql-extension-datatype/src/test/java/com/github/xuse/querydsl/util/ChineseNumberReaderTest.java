package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.datatype.util.ChineseNumberReader;

public class ChineseNumberReaderTest {
	@Test
	public void testStandard() {
		assertEquals("负一", ChineseNumberReader.DEFAULT.forNumber(-1));
		assertEquals("负九十九万九千九百九十九", ChineseNumberReader.DEFAULT.forNumber(-999999));
		assertEquals("零", ChineseNumberReader.DEFAULT.forNumber(0));
		assertEquals("九", ChineseNumberReader.DEFAULT.forNumber(9));
		assertEquals("十", ChineseNumberReader.DEFAULT.forNumber(10));
		assertEquals("十一", ChineseNumberReader.DEFAULT.forNumber(11));
		assertEquals("十七", ChineseNumberReader.DEFAULT.forNumber(17));
		assertEquals("二十", ChineseNumberReader.DEFAULT.forNumber(20));
		assertEquals("九十九", ChineseNumberReader.DEFAULT.forNumber(99));
		assertEquals("一百", ChineseNumberReader.DEFAULT.forNumber(100));
		assertEquals("一百零三", ChineseNumberReader.DEFAULT.forNumber(103));
		assertEquals("一百三十", ChineseNumberReader.DEFAULT.forNumber(130));
		assertEquals("一百三十八", ChineseNumberReader.DEFAULT.forNumber(138));
		assertEquals("二百", ChineseNumberReader.DEFAULT.forNumber(200));
		assertEquals("九百九十九", ChineseNumberReader.DEFAULT.forNumber(999));
		assertEquals("一千", ChineseNumberReader.DEFAULT.forNumber(1000));
		assertEquals("一千零一", ChineseNumberReader.DEFAULT.forNumber(1001));
		assertEquals("一千零一十", ChineseNumberReader.DEFAULT.forNumber(1010));
		assertEquals("五千一百", ChineseNumberReader.DEFAULT.forNumber(5100));
		assertEquals("五千一百零一", ChineseNumberReader.DEFAULT.forNumber(5101));
		assertEquals("五千一百一十一", ChineseNumberReader.DEFAULT.forNumber(5111));
		assertEquals("一万", ChineseNumberReader.DEFAULT.forNumber(10000));
		assertEquals("一万零一", ChineseNumberReader.DEFAULT.forNumber(10001));
		assertEquals("一万零一百零一", ChineseNumberReader.DEFAULT.forNumber(10101));
		assertEquals("一万零一百一十一", ChineseNumberReader.DEFAULT.forNumber(10111));
		assertEquals("一万一千一百零一", ChineseNumberReader.DEFAULT.forNumber(11101));
		assertEquals("一万一千一百一十一", ChineseNumberReader.DEFAULT.forNumber(11111));
		assertEquals("十二万三千四百五十六", ChineseNumberReader.DEFAULT.forNumber(123456));
		assertEquals("一亿二千三百四十万", ChineseNumberReader.DEFAULT.forNumber(1_2340_0000)); 
		assertEquals("一亿二千三百四十五万", ChineseNumberReader.DEFAULT.forNumber(1_2345_0000));
		assertEquals("十亿", ChineseNumberReader.DEFAULT.forNumber(1000000000));
		assertEquals("十亿零一", ChineseNumberReader.DEFAULT.forNumber(1000000001));
		assertEquals("十亿零五千", ChineseNumberReader.DEFAULT.forNumber(1000005000));
		assertEquals("十亿零五千零一", ChineseNumberReader.DEFAULT.forNumber(1000005001));
		assertEquals("十亿零三千四百零六万七千八百九十二", ChineseNumberReader.DEFAULT.forNumber(1034067892));
		assertEquals("十二亿三千四百五十六万七千八百九十", ChineseNumberReader.DEFAULT.forNumber(1234567890));
		assertEquals("十万零三千三百四十亿零九万", ChineseNumberReader.DEFAULT.forNumber(10_3340_0009_0000L));
		assertEquals("一万亿", ChineseNumberReader.DEFAULT.forNumber(1_0000_0000_0000L));
		assertEquals("十万亿", ChineseNumberReader.DEFAULT.forNumber(10_0000_0000_0000L));
		assertEquals("零", ChineseNumberReader.DEFAULT.forDouble(0d));
		assertEquals("十点三八", ChineseNumberReader.DEFAULT.forDouble(10.38));
		assertEquals("一百点三八", ChineseNumberReader.DEFAULT.forDouble(100.380));
		assertEquals("一百点三八一", ChineseNumberReader.DEFAULT.forDouble(100.381));
		assertEquals("一百点三八一二三一", ChineseNumberReader.DEFAULT.forDouble(100.381231));
		assertEquals("十点三八", ChineseNumberReader.DEFAULT.forDouble("10.38"));
		assertEquals("一百点三八", ChineseNumberReader.DEFAULT.forDouble("100.380"));
		assertEquals("一百点三八一", ChineseNumberReader.DEFAULT.forDouble("100.381"));
		assertEquals("一百点三八一二三一", ChineseNumberReader.DEFAULT.forDouble("100.381231"));
	}
	
	@Test
	public void testFiance() {
		assertEquals("负壹", ChineseNumberReader.FINANCE.forNumber(-1));
		assertEquals("负玖拾玖万玖仟玖佰玖拾玖", ChineseNumberReader.FINANCE.forNumber(-999999));
		assertEquals("零", ChineseNumberReader.FINANCE.forNumber(0));
		assertEquals("玖", ChineseNumberReader.FINANCE.forNumber(9));
		assertEquals("拾", ChineseNumberReader.FINANCE.forNumber(10));
		assertEquals("拾壹", ChineseNumberReader.FINANCE.forNumber(11));
		assertEquals("拾柒", ChineseNumberReader.FINANCE.forNumber(17));
		assertEquals("贰拾", ChineseNumberReader.FINANCE.forNumber(20));
		assertEquals("玖拾玖", ChineseNumberReader.FINANCE.forNumber(99));
		assertEquals("壹佰", ChineseNumberReader.FINANCE.forNumber(100));
		assertEquals("壹佰零叁", ChineseNumberReader.FINANCE.forNumber(103));
		assertEquals("壹佰叁拾", ChineseNumberReader.FINANCE.forNumber(130));
		assertEquals("壹佰叁拾捌", ChineseNumberReader.FINANCE.forNumber(138));
		assertEquals("贰佰", ChineseNumberReader.FINANCE.forNumber(200));
		assertEquals("玖佰玖拾玖", ChineseNumberReader.FINANCE.forNumber(999));
		assertEquals("壹仟", ChineseNumberReader.FINANCE.forNumber(1000));
		assertEquals("壹仟零壹", ChineseNumberReader.FINANCE.forNumber(1001));
		assertEquals("壹仟零壹拾", ChineseNumberReader.FINANCE.forNumber(1010));
		assertEquals("伍仟壹佰", ChineseNumberReader.FINANCE.forNumber(5100));
		assertEquals("伍仟壹佰零壹", ChineseNumberReader.FINANCE.forNumber(5101));
		assertEquals("伍仟壹佰壹拾壹", ChineseNumberReader.FINANCE.forNumber(5111));
		assertEquals("壹万", ChineseNumberReader.FINANCE.forNumber(10000));
		assertEquals("壹万零壹", ChineseNumberReader.FINANCE.forNumber(10001));
		assertEquals("壹万零壹佰零壹", ChineseNumberReader.FINANCE.forNumber(10101));
		assertEquals("壹万零壹佰壹拾壹", ChineseNumberReader.FINANCE.forNumber(10111));
		assertEquals("壹万壹仟壹佰零壹", ChineseNumberReader.FINANCE.forNumber(11101));
		assertEquals("壹万壹仟壹佰壹拾壹", ChineseNumberReader.FINANCE.forNumber(11111));
		assertEquals("拾贰万叁仟肆佰伍拾陆", ChineseNumberReader.FINANCE.forNumber(123456));
		assertEquals("壹亿贰仟叁佰肆拾万", ChineseNumberReader.FINANCE.forNumber(1_2340_0000)); 
		assertEquals("壹亿贰仟叁佰肆拾伍万", ChineseNumberReader.FINANCE.forNumber(1_2345_0000));
		assertEquals("拾亿", ChineseNumberReader.FINANCE.forNumber(1000000000));
		assertEquals("拾亿零壹", ChineseNumberReader.FINANCE.forNumber(1000000001));
		assertEquals("拾亿零伍仟", ChineseNumberReader.FINANCE.forNumber(1000005000));
		assertEquals("拾亿零伍仟零壹", ChineseNumberReader.FINANCE.forNumber(1000005001));
		assertEquals("拾亿零叁仟肆佰零陆万柒仟捌佰玖拾贰", ChineseNumberReader.FINANCE.forNumber(1034067892));
		assertEquals("拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾", ChineseNumberReader.FINANCE.forNumber(1234567890));
		assertEquals("拾万零叁仟叁佰肆拾亿零玖万", ChineseNumberReader.FINANCE.forNumber(10_3340_0009_0000L));
		assertEquals("壹万亿", ChineseNumberReader.FINANCE.forNumber(1_0000_0000_0000L));
		assertEquals("拾万亿", ChineseNumberReader.FINANCE.forNumber(10_0000_0000_0000L));
		assertEquals("零", ChineseNumberReader.FINANCE.forDouble(0d));
		assertEquals("拾点叁捌", ChineseNumberReader.FINANCE.forDouble(10.38));
		assertEquals("壹佰点叁捌", ChineseNumberReader.FINANCE.forDouble(100.380));
		assertEquals("壹佰点叁捌壹", ChineseNumberReader.FINANCE.forDouble(100.381));
		assertEquals("壹佰点叁捌壹贰叁壹", ChineseNumberReader.FINANCE.forDouble(100.381231));
		assertEquals("拾点叁捌", ChineseNumberReader.FINANCE.forDouble("10.38"));
		assertEquals("壹佰点叁捌", ChineseNumberReader.FINANCE.forDouble("100.380"));
		assertEquals("壹佰点叁捌壹", ChineseNumberReader.FINANCE.forDouble("100.381"));
		assertEquals("壹佰点叁捌壹贰叁壹", ChineseNumberReader.FINANCE.forDouble("100.381231"));
	}
	
	@Test
	public void testCurrency() {
		assertEquals("负壹元整", ChineseNumberReader.CURRENCE.forNumber(-1));
		assertEquals("负玖拾玖万玖仟玖佰玖拾玖元整", ChineseNumberReader.CURRENCE.forNumber(-999999));
		assertEquals("零元整", ChineseNumberReader.CURRENCE.forNumber(0));
		assertEquals("玖元整", ChineseNumberReader.CURRENCE.forNumber(9));
		assertEquals("拾元整", ChineseNumberReader.CURRENCE.forNumber(10));
		assertEquals("拾壹元整", ChineseNumberReader.CURRENCE.forNumber(11));
		assertEquals("拾柒元整", ChineseNumberReader.CURRENCE.forNumber(17));
		assertEquals("贰拾元整", ChineseNumberReader.CURRENCE.forNumber(20));
		assertEquals("玖拾玖元整", ChineseNumberReader.CURRENCE.forNumber(99));
		assertEquals("壹佰元整", ChineseNumberReader.CURRENCE.forNumber(100));
		assertEquals("壹佰零叁元整", ChineseNumberReader.CURRENCE.forNumber(103));
		assertEquals("壹佰叁拾元整", ChineseNumberReader.CURRENCE.forNumber(130));
		assertEquals("壹佰叁拾捌元整", ChineseNumberReader.CURRENCE.forNumber(138));
		assertEquals("贰佰元整", ChineseNumberReader.CURRENCE.forNumber(200));
		assertEquals("玖佰玖拾玖元整", ChineseNumberReader.CURRENCE.forNumber(999));
		assertEquals("壹仟元整", ChineseNumberReader.CURRENCE.forNumber(1000));
		assertEquals("壹仟零壹元整", ChineseNumberReader.CURRENCE.forNumber(1001));
		assertEquals("壹仟零壹拾元整", ChineseNumberReader.CURRENCE.forNumber(1010));
		assertEquals("伍仟壹佰元整", ChineseNumberReader.CURRENCE.forNumber(5100));
		assertEquals("伍仟壹佰零壹元整", ChineseNumberReader.CURRENCE.forNumber(5101));
		assertEquals("伍仟壹佰壹拾壹元整", ChineseNumberReader.CURRENCE.forNumber(5111));
		assertEquals("壹万元整", ChineseNumberReader.CURRENCE.forNumber(10000));
		assertEquals("壹万零壹元整", ChineseNumberReader.CURRENCE.forNumber(10001));
		assertEquals("壹万零壹佰零壹元整", ChineseNumberReader.CURRENCE.forNumber(10101));
		assertEquals("壹万零壹佰壹拾壹元整", ChineseNumberReader.CURRENCE.forNumber(10111));
		assertEquals("壹万壹仟壹佰零壹元整", ChineseNumberReader.CURRENCE.forNumber(11101));
		assertEquals("壹万壹仟壹佰壹拾壹元整", ChineseNumberReader.CURRENCE.forNumber(11111));
		assertEquals("拾贰万叁仟肆佰伍拾陆元整", ChineseNumberReader.CURRENCE.forNumber(123456));
		assertEquals("壹亿贰仟叁佰肆拾万元整", ChineseNumberReader.CURRENCE.forNumber(1_2340_0000)); 
		assertEquals("壹亿贰仟叁佰肆拾伍万元整", ChineseNumberReader.CURRENCE.forNumber(1_2345_0000));
		assertEquals("拾亿元整", ChineseNumberReader.CURRENCE.forNumber(1000000000));
		assertEquals("拾亿零壹元整", ChineseNumberReader.CURRENCE.forNumber(1000000001));
		assertEquals("拾亿零伍仟元整", ChineseNumberReader.CURRENCE.forNumber(1000005000));
		assertEquals("拾亿零伍仟零壹元整", ChineseNumberReader.CURRENCE.forNumber(1000005001));
		assertEquals("拾亿零叁仟肆佰零陆万柒仟捌佰玖拾贰元整", ChineseNumberReader.CURRENCE.forNumber(1034067892));
		assertEquals("拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾元整", ChineseNumberReader.CURRENCE.forNumber(1234567890));
		assertEquals("拾万零叁仟叁佰肆拾亿零玖万元整", ChineseNumberReader.CURRENCE.forNumber(10_3340_0009_0000L));
		assertEquals("壹万亿元整", ChineseNumberReader.CURRENCE.forNumber(1_0000_0000_0000L));
		assertEquals("拾万亿元整", ChineseNumberReader.CURRENCE.forNumber(10_0000_0000_0000L));
		assertEquals("零元整", ChineseNumberReader.CURRENCE.forDouble(0d));
		assertEquals("拾元叁角捌分", ChineseNumberReader.CURRENCE.forDouble(10.38));
		assertEquals("壹佰元叁角捌分", ChineseNumberReader.CURRENCE.forDouble(100.380));
		assertEquals("壹佰元叁角捌分壹厘", ChineseNumberReader.CURRENCE.forDouble(100.381));
		assertEquals("壹佰元叁角捌分壹厘", ChineseNumberReader.CURRENCE.forDouble(100.381231));
		assertEquals("拾元叁角捌分", ChineseNumberReader.CURRENCE.forDouble("10.38"));
		assertEquals("壹佰元叁角捌分", ChineseNumberReader.CURRENCE.forDouble("100.380"));
		assertEquals("壹佰元叁角捌分壹厘", ChineseNumberReader.CURRENCE.forDouble("100.381"));
		assertEquals("壹佰元叁角捌分壹厘", ChineseNumberReader.CURRENCE.forDouble("100.381231"));
	}
	
	@Test
	public void testMilitary() {
		assertEquals("负幺", ChineseNumberReader.MILITARY.forDigits("-1"));
		assertEquals("负勾勾勾勾勾勾", ChineseNumberReader.MILITARY.forDigits("-999999"));
		assertEquals("洞", ChineseNumberReader.MILITARY.forDigits("0"));
		assertEquals("勾", ChineseNumberReader.MILITARY.forDigits("9"));
		assertEquals("幺洞", ChineseNumberReader.MILITARY.forDigits("10"));
		assertEquals("幺幺", ChineseNumberReader.MILITARY.forDigits("11"));
		assertEquals("幺拐", ChineseNumberReader.MILITARY.forDigits("17"));
		assertEquals("两洞", ChineseNumberReader.MILITARY.forDigits("20"));
		assertEquals("勾勾", ChineseNumberReader.MILITARY.forDigits("99"));
		assertEquals("幺洞洞", ChineseNumberReader.MILITARY.forDigits("100"));
		assertEquals("幺洞三", ChineseNumberReader.MILITARY.forDigits("103"));
		assertEquals("幺三洞", ChineseNumberReader.MILITARY.forDigits("130"));
		assertEquals("幺三八", ChineseNumberReader.MILITARY.forDigits("138"));
		assertEquals("两洞洞", ChineseNumberReader.MILITARY.forDigits("200"));
		assertEquals("勾勾勾", ChineseNumberReader.MILITARY.forDigits("999"));
		assertEquals("幺两三四五六拐八勾洞", ChineseNumberReader.MILITARY.forDigits("1234567890"));
		assertEquals("洞", ChineseNumberReader.MILITARY.forDigits("0"));
		assertEquals("幺洞点三八", ChineseNumberReader.MILITARY.forDigits("10.38"));
		assertEquals("幺洞洞点三八洞", ChineseNumberReader.MILITARY.forDigits("100.380"));
		assertEquals("幺洞洞点三八幺", ChineseNumberReader.MILITARY.forDigits("100.381"));
		assertEquals("幺洞洞点三八幺两三幺", ChineseNumberReader.MILITARY.forDigits("100.381231"));
		assertEquals("洞洞拐", ChineseNumberReader.MILITARY.forDigits("007"));
	}
}
