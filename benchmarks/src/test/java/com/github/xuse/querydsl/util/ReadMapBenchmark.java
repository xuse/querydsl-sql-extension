package com.github.xuse.querydsl.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.github.xuse.querydsl.util.collection.CacheMap;
import com.github.xuse.querydsl.util.collection.FastHashtable;

import io.github.xuse.querydsl.sql.extension.BenchmarkRunner;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@RunWith(BenchmarkRunner.class)
public class ReadMapBenchmark  {
	static final int SIZE = 256;
	static List<String> list = new ArrayList<String>();
	
	static {
		setup();
	}
	
	public static void setup() {
		for (int i = 0; i < SIZE; i++) {
			list.add(StringUtils.randomString());
		}
	}
	// 基线
	Map<String, String> map0 = new LinkedHashMap<>(SIZE);
	// 测试目标
	FastHashtable<String> map1 = new FastHashtable<>(SIZE);
	// 对照组
	Map<String, String> map3 = new CacheMap<>(SIZE);

	public ReadMapBenchmark() {
		prepareMap(map0);
		prepareMap(map1);
		prepareMap(map3);
	}

	/**
	 * 定位元素：基础对照组
	 *   1949.816 ns/op
	 * @param bh
	 */
	@Benchmark
	public void locateLinkedHashMap(Blackhole bh) {
		doFind(map0, bh);
	}

	/**
	 * 定位元素：1639.637 ns/op
	 * @param bh
	 */
	@Benchmark
	public void locateFastHashtable(Blackhole bh) {
		doFind(map1, bh);
	}
	
	/**
	 * HashMap的只读版本 定位元素 
	 *   2105.087 ns/op
	 */
	@Benchmark
	public void locateCacheMap(Blackhole bh) {
		doFind(map3, bh);
	}

	/**
	 * 遍历元素：基础对照组
	 *   420.951 ns/op
	 * @param bh
	 */
	@Benchmark
	public void iterateLinkedHash(Blackhole bh) {
		doIter(map0, bh);
	}

	/**
	 * 遍历元素：FastHashMap
	 * @param bh
	 *   428.837 ns/op
	 */
	@Benchmark
	public void iterateFastHash(Blackhole bh) {
		doIter(map1, bh);
	}

	/**
	 *遍历元素：CacheMap
	 * @param bh
	 *   1010.635 ns/op
	 */
	@Benchmark
	public void iterateCacheMap(Blackhole bh) {
		doIter(map3, bh);
	}

	private void doIter(Map<String, String> map, Blackhole bh) {
		for (Map.Entry<String, String> e : map.entrySet()) {
			bh.consume(e.getKey());
			bh.consume(e.getValue());
		}
	}

	private void doFind(Map<String, String> map, Blackhole bh) {
		for (int i = 0; i < list.size(); i++) {
			bh.consume(map.get(list.get(i)));
		}
	}

	private void prepareMap(Map<String, String> map) {
		for (int i = 0; i < list.size(); i++) {
			final String str = String.valueOf(i);
			map.computeIfAbsent(list.get(i), (e) -> str);
		}
		System.out.println(map.size());
	}
}