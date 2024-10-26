package com.github.xuse.querydsl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.runner.RunWith;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import io.github.xuse.querydsl.sql.extension.BenchmarkRunner;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@RunWith(BenchmarkRunner.class)
public class ToArrayPerformanceTest {
	private final List<String> list = new ArrayList<>();
	public volatile Object[] data;

//	public void testPerformanceToArray() {
//		prepare();
//		{
//			long start = System.currentTimeMillis();
//			testMethodA();
//			System.out.println("Cost" + (System.currentTimeMillis() - start));
//		}
//		{
//			long start = System.currentTimeMillis();
//			testMethodB();
//			System.out.println("Cost" + (System.currentTimeMillis() - start));
//		}
//		{
//			long start = System.currentTimeMillis();
//			testMethodC();
//			System.out.println("Cost" + (System.currentTimeMillis() - start));
//		}
//		{
//			long start = System.currentTimeMillis();
//			testMethodD();
//			System.out.println("Cost" + (System.currentTimeMillis() - start));
//		}
//	}

	@Setup(Level.Trial)
	public void prepare() {
		List<String> list=this.list;
		for (int i = 0; i < 500; i++) {
			list.add("aaa" + String.valueOf(i));
		}
	}

	@TearDown(Level.Trial) // 结束方法，在全部Benchmark运行之后进行
	public void ends() {
	}

	@Benchmark
	public void testMethodA() {
		List<String> list = this.list;
		for (int i = 0; i < 10_000_00; i++) {
			data = list.toArray(new String[0]);
		}
	}

	// 平均827ms
	@Benchmark
	public void testMethodB() {
		List<String> list = this.list;
		String[] e = new String[0];
		for (int i = 0; i < 10_000_00; i++) {
			data = list.toArray(e);
		}
	}

	// 平均975ms
	@Benchmark
	public void testMethodC() {
		List<String> list = this.list;
		int size = list.size();
		for (int i = 0; i < 10_000_00; i++) {
			data = list.toArray(new String[size]);
		}
	}

	// 平均980ms
	@Benchmark
	public void testMethodD() {
		List<String> list = this.list;
		for (int i = 0; i < 10_000_00; i++) {
			data = list.toArray(new String[list.size()]);
		}
	}

//	public static void main(String[] args) throws RunnerException, IOException {
//		String res="/META-INF/BenchmarkList";
//		Enumeration<URL> urls=ToArrayPerformanceTest.class.getClassLoader().getResources(res);
//		System.out.println(CollectionUtils.toList(urls));
//		
//		
//		Options options = new OptionsBuilder().include(ToArrayPerformanceTest.class.getSimpleName()).build();
//		new Runner(options).run();
//	}
}
