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

import io.github.xuse.querydsl.sql.extension.BenchmarkRunner;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@RunWith(BenchmarkRunner.class)
public class WriteMapBenchmark{	
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
	Map<String, String> map3 = new NoReadLockHashMap<>();

	@Benchmark
	public void writeLinkedHashMap() {
		doWriteTest(map0);
	}

	@Benchmark
	public void writeFastHashtable() {
		doWriteTest(map1);
	}

	@Benchmark
	public void writeNoReadLockHashMap() {
		doWriteTest(map3);
	}
	
	private void doWriteTest(Map<String,String> map) {
		map.clear();
		for (int i = 0; i < list.size(); i++) {
			final String str = String.valueOf(i);
			map.computeIfAbsent(list.get(i), (e) -> str);
		}
	}
}