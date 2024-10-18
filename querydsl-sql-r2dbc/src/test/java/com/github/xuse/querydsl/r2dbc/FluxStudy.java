package com.github.xuse.querydsl.r2dbc;

import java.time.Duration;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class FluxStudy {
	@Test
	public void testFlux() {
		Flux.just(1, 2, 3).map((a) -> Mono.just(a*2)).map(e->e.block()).subscribe(System.out::println);
		// FlatMap和Map的区别。map是函数操作是同步的。flatmap是产生一个新的publisher，从而可以实现1对多的展开。
		Flux.just(1, 2, 3).flatMap(a -> Flux.just("f" + a, "sep")).subscribe(System.out::println);
		
		
		Flux.from(Mono.just(1)).flatMap(s->Flux.just("f"+s,"sep")).subscribe(System.out::println);
		Mono.just(1).flatMapMany(s->Flux.just("f"+s,"sep")).subscribe(System.out::println);
		
		Flux.just(1, 2, 3).reduce((a, b) -> a + b).subscribe(System.out::println);

		// 两个流合并后，新的流的每个元素变成Tuple对象。包含了原来的两个流中的元素。
		// 长度不一致时多余的元素将被忽略。
		Flux.zip(Flux.just("a", "b", "c", "d"), Flux.just("d", "e", "f")).subscribe(System.out::println);
		Flux.just("a", "b", "c").zipWith(Flux.just("d", "e", "f", "g")).subscribe(System.out::println);
		// 拼接两个流
		Flux.merge(Flux.just("a", "b"), Flux.just("1", "2", "3")).subscribe(System.out::println);
		Flux.just("a", "b", "c").mergeWith(Flux.just("1", "2")).subscribe(System.out::println);
		System.out.println("------------------------");
		Flux.concat(Flux.just("a", "b", "c"), Flux.just("d", "e", "f")).subscribe(System.out::println);
		Flux.just("a", "b", "c").concatWith(Flux.just("d", "e", "f")).subscribe(System.out::println);

		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).count().subscribe(System.out::println);

		// ALL操作符可以判断流中的所有元素是否都满足某个条件。
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).all(i -> i > 0).subscribe(System.out::println);
		// Any判断流中是否有任意元素满足某个条件。
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).any(i -> i > 5).subscribe(System.out::println);
		// hasElements 操作符可以判断流中是否存在元素。
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).hasElements().subscribe(System.out::println);

		System.out.println("------------------------");
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).take(5).subscribe(System.out::println);
		System.out.println("------------------------");
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).takeLast(5).subscribe(System.out::println);
		System.out.println("------------------------");
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).takeUntil(i -> i > 5).subscribe(System.out::println);
		System.out.println("------------------------");
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).takeWhile(i -> i < 5).subscribe(System.out::println);
		System.out.println("------------------------");

		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5).distinct().subscribe(System.out::println);
		System.out.println("------------------------");
		// distinctUntilChanged 操作符可以去除流中连续重复的元素。
		Flux.just(1, 2, 2, 3, 3, 4, 5, 5, 6, 7, 8, 9, 10, 5).distinctUntilChanged().subscribe(System.out::println);

		Flux.just("a", "b", "c").collectList().subscribe(System.out::println);
		// 输出为：[a, b, c]
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).collectMap(i -> i % 2 == 0 ? "even" : "odd", i -> i)
				.subscribe(System.out::println);
		// 输出为：{odd= 9, even=10}

		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, -1).collectSortedList().subscribe(System.out::println);
		// 会排序
		Flux.just(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).collectMultimap(i -> i % 2 == 0 ? "even" : "odd")
				.subscribe(System.out::println);

		Flux.empty().defaultIfEmpty("default").subscribe(System.out::println);

		// switchIfEmpty 操作符可以在流中没有元素时切换到另一个流。
		// 如果不为空时，另一个流是不生效的。
		Flux.empty().switchIfEmpty(Flux.just(1, 2, 3)).subscribe(System.out::println);
		System.out.println("------------------------");
		Flux.just(10).switchIfEmpty(Flux.just(1, 2, 3)).subscribe(System.out::println);

		Flux.just(1, 2, 3).concatWith(Mono.error(new RuntimeException())).onErrorReturn(0)
				.subscribe(System.out::println);
		System.out.println("------------------------");
		Flux.just(1, 2, 0, 5).map(e -> 10 / e).onErrorReturn(999).subscribe(System.out::println);
		System.out.println("------------------------");
		// onErrorResume 操作符可以在流中发生错误时切换到另一个流。
		Flux.just(1, 2, 3).concatWith(Mono.error(new RuntimeException())).onErrorResume(e -> Flux.just(4, 5, 6))
				.subscribe(System.out::println);
		System.out.println("------------------------");
		// onErrorContinue 操作符可以在流中发生错误时继续发出流中的元素。
		Flux.just(1, 2, 0, 5).map(e -> 10 / e)
				.onErrorContinue((e, o) -> System.out.println("error: " + e.getMessage() + ", object: " + o))
				.subscribe(System.out::println);

		// retry 操作符可以在流中发生错误时重试。
		Flux.just(1, 2, 3).concatWith(Mono.error(new RuntimeException())).retry(1).subscribe(System.out::println);
		// retryWhen 操作符可以在流中发生错误时根据指定的条件重试。
		Flux.just(1, 2, 3).concatWith(Mono.error(new RuntimeException()))
				.retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(1))).subscribe(System.out::println);

		// timeout 操作符可以在流中发生超时时发出一个默认值。
		Flux.just(1, 2, 3).delayElements(Duration.ofSeconds(1)).timeout(Duration.ofMillis(500), Mono.just(0))
				.subscribe(System.out::println);

		// timeoutTo 操作符可以在流中发生超时时切换到另一个流。
		Flux.just(1, 2, 3).delayElements(Duration.ofSeconds(1)).timeout(Duration.ofMillis(500), Flux.just(4, 5, 6))
				.subscribe(System.out::println);
		// 输出为：1 2 3 4 5 6

	}

	@Test
	public void controlFlux() throws InterruptedException {
		// delaySequence 操作符可以在流中的所有元素发出时延迟一段时间。
		Flux.just(1, 2, 3).delayElements(Duration.ofSeconds(1)).subscribe(System.out::println);

		// elapsed 操作符用来计算流中元素的发出时间间隔。
		Flux.interval(Duration.ofMillis(250)).map(input -> {
			if (input < 3)
				return "tick " + input;
			throw new RuntimeException("boom");
		}).elapsed().retry(1).subscribe(System.out::println, System.err::println);

		// delaySubscription 操作符可以在订阅流时延迟一段时间。
		Flux.just(1, 2, 3).delaySubscription(Duration.ofSeconds(1)).subscribe(System.out::println);
		//

		Thread.sleep(2500);
	}

	@Test
	public void testOther() {
		Flux.just(1, 2, 3).repeat(2).subscribe(System.out::println);
		// doOnNext 操作符可以在流中的每个元素发出时执行指定的操作。
		Flux.just(1, 2, 3).doOnNext(s -> System.out.println("doOnNext: " + s)).subscribe(System.out::println);
		// doOnRequest 操作符在request时执行指定的操作。
		Flux.just(1, 2, 3).doOnRequest(s -> System.out.println("doOnRequest: " + s)).subscribe(System.out::println);

		// doOnSubscribe 操作符在subscribe时执行指定的操作。
		Flux.just(1, 2, 3).doOnSubscribe(s -> System.out.println("doOnSubscribe: " + s)).subscribe(System.out::println);
//输出为：doOnSubscribe: reactor.core.publisher.FluxPeekFuseable$PeekFuseableSubscriber@4b1b1c6a 1 2 3

		// doOnComplete
		Flux.just(1, 2, 3).doOnComplete(() -> System.out.println("doOnComplete")).subscribe(System.out::println);

		Flux.just(1, 2, 3).doOnError(e -> System.out.println("doOnError: " + e.getMessage()))
				.subscribe(System.out::println);

		Flux.just(1, 2, 3).doOnTerminate(() -> System.out.println("doOnTerminate")).subscribe(System.out::println);
//输出为：1 2 3 doOnTerminate

		Flux.just(1, 2, 3).doOnCancel(() -> System.out.println("doOnCancel")).subscribe(System.out::println);

		Flux.just(1, 2, 3).doOnDiscard(Object.class, s -> System.out.println("doOnDiscard: " + s))
				.subscribe(System.out::println);

	}

	@Test
	public void testGenerate() {
		Flux.generate(sink -> {
			sink.next("Hello");
			sink.complete();
		});

		Flux.generate(() -> 0, (state, sink) -> {
			sink.next("3 x " + state + " = " + 3 * state);
			if (state == 10)
				sink.complete();
			return state + 1;
		});
	}
}
