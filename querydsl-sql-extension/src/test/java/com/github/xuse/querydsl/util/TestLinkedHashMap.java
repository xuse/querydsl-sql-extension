package com.github.xuse.querydsl.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("unused")
public class TestLinkedHashMap {
	private static final int LOOP_TIMES = 100000;

	@Test
	public void testLinkedHasdMap() {
		int SIZE = 256;
		//基线
		Map<String, String> map0 = new LinkedHashMap<>(SIZE);
		//测试目标
		FastHashtable< String> map1 = new FastHashtable<>(SIZE);
		//对照组
		Map<String, String> map3 = new NoReadLockHashMap<>();

		String s;
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < SIZE; i++) {
			list.add(StringUtils.randomString());
		}
		{
			long time = System.currentTimeMillis();
			Map<String, String> map = map0;

			for (int j = 0; j < LOOP_TIMES; j++) {
				map.clear();
				for (int i = 0; i < list.size(); i++) {
					final String str = String.valueOf(i);
					map.computeIfAbsent(list.get(i), (e) -> str);
				}

				for (int i = 0; i < list.size(); i++) {
					s = map.get(list.get(i));
				}
			}
			System.out.println((System.currentTimeMillis() - time) + "ms (LinkedHashMap Write)");
		}
		{
			long time = System.currentTimeMillis();
			Map<String,String> map = map1;

			for (int j = 0; j < LOOP_TIMES; j++) {
				map.clear();
				for (int i = 0; i < list.size(); i++) {
					final String str = String.valueOf(i);
					map.computeIfAbsent(list.get(i), (e) -> str);
				}

				for (int i = 0; i < list.size(); i++) {
					s = map.get(list.get(i));
				}
			}
			System.out.println((System.currentTimeMillis() - time) + "ms (MyLinkedMap Write)");
		}
		{
			long time = System.currentTimeMillis();
			Map<String, String> map = map3;

			for (int j = 0; j < LOOP_TIMES; j++) {
				map.clear();
				for (int i = 0; i < list.size(); i++) {
					final String str = String.valueOf(i);
					map.computeIfAbsent(list.get(i), (e) -> str);
				}

				for (int i = 0; i < list.size(); i++) {
					s = map.get(list.get(i));
				}
			}
			System.out.println((System.currentTimeMillis() - time) + "ms (MyAlterMap Write)");
		}

		{
			Map<String, String> map = map0;

			long time = System.currentTimeMillis();
			for (int j = 0; j < LOOP_TIMES; j++) {
				for (Map.Entry<String, String> e : map.entrySet()) {
					String x = e.getKey();
					s = e.getValue();
				}
			}
			System.out.println((System.currentTimeMillis() - time) + "ms (LinkedHashMap Iterate)");
		}
		{
			Map<String, String> map = map1;

			long time = System.currentTimeMillis();
			for (int j = 0; j < LOOP_TIMES; j++) {
				for (Map.Entry<String, String> e : map.entrySet()) {
					String x = e.getKey();
					s = e.getValue();
				}
			}
			System.out.println((System.currentTimeMillis() - time) + "ms (MyAlterMap Iterate)");
		}
		{
			Map<String, String> map = map3;

			long time = System.currentTimeMillis();
			for (int j = 0; j < LOOP_TIMES; j++) {
				for (Map.Entry<String, String> e : map.entrySet()) {
					String x = e.getKey();
					s = e.getValue();
				}
			}
			System.out.println((System.currentTimeMillis() - time) + "ms (MyAlterMap Iterate)");
		}

		System.out.println(map1.size());
		System.out.println(map0.size());
		if (SIZE < 128) {
			System.out.println(map1);
			System.out.println(map0);
		}
		if (SIZE < 1024) {
			assertEquals(map1.toString(), map0.toString());
		}
		System.out.println(map1.getMaxDepth());
	}

}
