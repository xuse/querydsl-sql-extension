package com.github.xuse.querydsl.util;

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

import io.github.xuse.querydsl.sql.extension.BenchmarkRunner;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1)
@Measurement(iterations = 2)
@RunWith(BenchmarkRunner.class)
public class DigitPerformance {
	@Benchmark
	public void testD64(Blackhole b) {
		for (int i = 90000; i < 100000; i++) {
			b.consume(Radix.D64.encode(i));
		}
	}
	
	@Benchmark
	public void testD62(Blackhole b) {
		for (int i = 90000; i < 100000; i++) {
			b.consume(Radix.D62.encode(i));
		}
	}
	
	@Benchmark
	public void testD84(Blackhole b) {
		for (int i = 90000; i < 100000; i++) {
			b.consume(Radix.D84.encode(i));
		}
	}
	
	@Benchmark
	public void testD3(Blackhole b) {
		for (int i = 90000; i < 100000; i++) {
			b.consume(Radix.D3.encode(i));
		}
	}
	
	@Benchmark
	public void testD16(Blackhole b) {
		for (int i = 90000; i < 100000; i++) {
			b.consume(Radix.D16.encode(i));
		}
	}
	
	@Benchmark
	public void testJDKHex(Blackhole b) {
		for (int i = 90000; i < 100000; i++) {
			b.consume(Integer.toHexString(i));
		}
	}
}
