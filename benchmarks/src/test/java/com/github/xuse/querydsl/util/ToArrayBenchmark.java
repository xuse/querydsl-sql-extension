package com.github.xuse.querydsl.util;

import java.util.ArrayList;
import java.util.List;
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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
//Scope.Thread：默认的State，每个测试线程分配一个实例；
//Scope.Benchmark：所有测试线程共享一个实例，用于测试有状态实例在多线程共享下的性能；
//Scope.Group：每个线程组共享一个实例；
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@RunWith(BenchmarkRunner.class)
public class ToArrayBenchmark {
	private final List<String> list = new ArrayList<>();
	public volatile Object[] data;
	
	@Setup(Level.Trial)	
	public void prepare() {
		List<String> list=this.list;
		for (int i = 0; i < 500; i++) {
			list.add("aaa" + String.valueOf(i));
		}
	}

	@TearDown(Level.Trial) 
	public void ends() {
		list.clear();
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
}
