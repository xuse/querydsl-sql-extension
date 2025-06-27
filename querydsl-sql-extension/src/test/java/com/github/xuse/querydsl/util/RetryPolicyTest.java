package com.github.xuse.querydsl.util;

import java.time.Duration;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.RetryPolicy.RetryDelayCalculator;

public class RetryPolicyTest {
	@Test
	public void retryPolicyTest() {
		RetryPolicy policy=RetryPolicy.newBuilder().backoff(Duration.ofSeconds(1))
				.maxAttempts(3)
				.withDefaultJitter()
				.build();
		
		policy.execute((Runnable)()->{
			System.out.println(DateFormats.DATE_TIME_SHORT_14.format(new Date()));
			throw new RuntimeException();
		});
		
	}
	
	
	
	@Test
	public void retryPolicyDelayTest() {
		RetryPolicy policy=RetryPolicy.newBuilder().backoff(Duration.ofSeconds(1))
				.withDefaultJitter()
				.build();
		RetryDelayCalculator c=policy.delay;
		System.out.println(c.getDelay(0));
		System.out.println(c.getDelay(1));
		System.out.println(c.getDelay(1));
		System.out.println(c.getDelay(1));
		System.out.println(c.getDelay(2));
		System.out.println(c.getDelay(2));
		System.out.println(c.getDelay(2));
		System.out.println(c.getDelay(3));
		System.out.println(c.getDelay(3));
		System.out.println(c.getDelay(3));
		System.out.println("===================");
		RetryPolicy policy2=RetryPolicy.newBuilder().fixedDelay(Duration.ofSeconds(2))
				.withDefaultJitter()
				.retryForException(Exception.class)
				.build();
		c=policy2.delay;
		System.out.println(c.getDelay(0));
		System.out.println(c.getDelay(1));
		System.out.println(c.getDelay(1));
		System.out.println(c.getDelay(1));
		System.out.println(c.getDelay(2));
		System.out.println(c.getDelay(2));
		System.out.println(c.getDelay(2));
		System.out.println(c.getDelay(3));
		System.out.println(c.getDelay(3));
		System.out.println(c.getDelay(3));
	}
	
}
