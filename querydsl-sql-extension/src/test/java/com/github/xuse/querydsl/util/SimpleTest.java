package com.github.xuse.querydsl.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.junit.Ignore;
import org.junit.Test;

public class SimpleTest {
	private final List<String> list = new ArrayList<>();
	public volatile Object[] data;

	@Test
	@Ignore
	public void testPerformanceToArray() {
		List<String> list = prepare();
		{
			long start = System.currentTimeMillis();
			testMethodA(list);
			System.out.println("Cost" + (System.currentTimeMillis() - start));
		}
		{
			long start = System.currentTimeMillis();
			testMethodB(list);
			System.out.println("Cost" + (System.currentTimeMillis() - start));
		}
		{
			long start = System.currentTimeMillis();
			testMethodC(list);
			System.out.println("Cost" + (System.currentTimeMillis() - start));
		}
		{
			long start = System.currentTimeMillis();
			testMethodD(list);
			System.out.println("Cost" + (System.currentTimeMillis() - start));
		}
	}

	// 平均896ms
	private void testMethodA(List<String> list) {
		for (int i = 0; i < 10_000_00; i++) {
			data = list.toArray(new String[0]);
		}
	}

	// 平均827ms
	private void testMethodB(List<String> list) {
		String[] e = new String[0];
		for (int i = 0; i < 10_000_00; i++) {
			data = list.toArray(e);
		}
	}

	// 平均975ms
	private void testMethodC(List<String> list) {
		int size = list.size();
		for (int i = 0; i < 10_000_00; i++) {
			data = list.toArray(new String[size]);
		}
	}
	// 平均980ms

	private void testMethodD(List<String> list) {
		for (int i = 0; i < 10_000_00; i++) {
			data = list.toArray(new String[list.size()]);
		}
	}

	private java.util.List<String> prepare() {
		for (int i = 0; i < 500; i++) {
			list.add("aaa" + String.valueOf(i));
		}
		return list;
	}
	
	
	@Test
	public void testPrimitiveTypeNameDistribution() {
		String[] ss=new String[] {"byte","boolean","char","int","short","long","float","double"};
		
		TreeMap<Integer,String> map=new TreeMap<>();
		for(String s:ss){
			int v = s.length() + s.charAt(0);
			map.put(v, s);
		}
		int min=map.firstKey();		
		int max=map.lastKey();
		if(map.size()<8) {
			System.out.println("发生冲突!");
		}else {
			System.out.println("分布宽度" + (max - min + 1));
		}
		System.out.println(map);
	}
	
    public static int bitCount(int i) {
        i = i - ((i >>> 1) & 0x55555555);  //(0101 0101)
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);//0011 0011
        i = (i + (i >>> 4)) & 0x0f0f0f0f;//0000 1111
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        return i & 0x3f; // 0011 1111
    }
    
	@Test 
	public void timeAccuTest()  {
		BigInteger num=new BigInteger("00010110",2);
		System.out.println(bitCount(num.intValue()));

	}
	
}


