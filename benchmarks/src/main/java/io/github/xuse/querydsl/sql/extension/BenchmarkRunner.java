package io.github.xuse.querydsl.sql.extension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.xuse.querydsl.util.DateFormats;

public class BenchmarkRunner extends Runner implements Filterable {
	private final Class<?> benchmarkClass;
	
	private final List<Method> benchmarkMethods ;

	private List<Method> readyToRunMethods= Collections.emptyList();

	public BenchmarkRunner(Class<?> benchmarkClass) {
		this.benchmarkClass = benchmarkClass;
		benchmarkMethods = Arrays.stream(benchmarkClass.getDeclaredMethods())
				.filter(m -> m.isAnnotationPresent(Benchmark.class)).collect(Collectors.toList());
		readyToRunMethods = new ArrayList<>(benchmarkMethods);
	}

	@Override
	public Description getDescription() {
		Description result = Description.createSuiteDescription(benchmarkClass);
		readyToRunMethods.stream().map(this::getBenchmarkMethodDescription).forEach(result::addChild);
		return result;
	}

	private Description getBenchmarkMethodDescription(Method benchmarkMethod) {
		return Description.createTestDescription(benchmarkClass, benchmarkMethod.getName());
	}

	@Override
	public void run(RunNotifier notifier) {
		for (Method benchmarkMethod : readyToRunMethods) {
			Description testDescription = getBenchmarkMethodDescription(benchmarkMethod);
			try {
				notifier.fireTestStarted(testDescription);
				Options opt = new OptionsBuilder()
						.include(".*" + benchmarkClass.getName() + "." + benchmarkMethod.getName() + ".*")
						.jvmArgsAppend("-Djmh.separateClasspathJAR=true")
						.result(benchmarkClass.getName() + "." + benchmarkMethod.getName() + "_"
								+ DateFormats.DATE_TIME_SHORT_12.format(new Date()) + ".json")
						.resultFormat(ResultFormatType.JSON)
						.build();

				new org.openjdk.jmh.runner.Runner(opt).run();

				notifier.fireTestFinished(testDescription);
			} catch (Exception e) {
				notifier.fireTestFailure(new Failure(testDescription, e));
				return;
			}
		}
	}

	@Override
	public void filter(Filter filter) throws NoTestsRemainException {
		List<Method> filteredMethods = new ArrayList<>();
		for (Method benchmarkMethod : benchmarkMethods) {
			if (filter.shouldRun(getBenchmarkMethodDescription(benchmarkMethod))) {
				filteredMethods.add(benchmarkMethod);
			}
		}
		if (filteredMethods.isEmpty()) {
			throw new NoTestsRemainException();
		}
		this.readyToRunMethods = filteredMethods;
	}
}