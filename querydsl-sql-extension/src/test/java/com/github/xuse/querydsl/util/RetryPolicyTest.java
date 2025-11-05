package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.github.xuse.querydsl.util.RetryPolicy.RetryDelayCalculator;

public class RetryPolicyTest {
	@Test
	public void retryPolicyTest() {
		Assertions.assertThrows(RuntimeException.class, () -> {
			RetryPolicy policy=RetryPolicy.newBuilder().backoff(Duration.ofMillis(200))
					.maxAttempts(3)
					.withDefaultJitter()
					.build();
			
			policy.execute((Runnable)()->{
				System.out.println(DateFormats.DATE_TIME_SHORT_14.format(new Date()));
				throw new RuntimeException();
			});
		});
	}


    // 测试用例1：正常执行不抛异常
    @Test
    public void testExecuteSuccess() {
        Runnable task = () -> {};
        Callable<String> task2=()->"";
        Function<String,String> task3=(s)->s;
        
        RetryDelayCalculator delay = new RetryDelayCalculator();
        RetryPolicy policy = new RetryPolicy(delay, 3, RuntimeException.class);

        policy.execute(task);
        assertEquals(1, policy.getAttempts());
        
        policy.execute(task2);
        assertEquals(1, policy.getAttempts());

        policy.execute(task3,"");
        assertEquals(1, policy.getAttempts());
    }

    // 测试用例2：达到最大重试次数后抛出异常
    @Test
    public void testMaxAttemptsExceeded() {
        RetryDelayCalculator mockDelay = mock(RetryDelayCalculator.class);
        when(mockDelay.getDelay(1)).thenReturn(100L);
        when(mockDelay.getDelay(2)).thenReturn(200L);

        RetryPolicy policy = new RetryPolicy(mockDelay, 2, RuntimeException.class);
        Runnable task = () -> { throw new RuntimeException(); };

        try (MockedStatic<RetryPolicy> mockedDoSleep = mockStatic(RetryPolicy.class)) {
            // 设置doSleep返回值
            mockedDoSleep.when(() -> RetryPolicy.doSleep(anyLong())).thenReturn(true);

            assertThrows(RuntimeException.class, () -> policy.execute(task));

            // 验证doSleep调用次数
            
            mockedDoSleep.verify(() -> RetryPolicy.doSleep(anyLong()), times(2));
        }

        assertEquals(3, policy.getAttempts());
    }

    // 测试用例3：在重试次数内成功
    @Test
    public void testRetrySuccessOnSecondAttempt() {
        RetryDelayCalculator mockDelay = mock(RetryDelayCalculator.class);
        when(mockDelay.getDelay(1)).thenReturn(100L);

        RetryPolicy policy = new RetryPolicy(mockDelay, 3, RuntimeException.class);

        AtomicInteger count = new AtomicInteger(0);
        Runnable task = () -> {
            if (count.incrementAndGet() == 1) {
                throw new RuntimeException();
            }
        };

        try (MockedStatic<RetryPolicy> mockedDoSleep = mockStatic(RetryPolicy.class)) {
            mockedDoSleep.when(() -> RetryPolicy.doSleep(100L)).thenReturn(true);

            policy.execute(task);
            mockedDoSleep.verify(() -> RetryPolicy.doSleep(100L), times(1));
        }
        assertEquals(2, policy.getAttempts());
    }

    // 测试用例4：验证固定延迟计算
    @Test
    public void testFixedDelay() {
        RetryDelayCalculator mockDelay = mock(RetryDelayCalculator.class);
        when(mockDelay.getDelay(1)).thenReturn(2000L);
        when(mockDelay.getDelay(2)).thenReturn(2000L);

        RetryPolicy policy = new RetryPolicy(mockDelay, 2, RuntimeException.class);
        Runnable task = () -> { throw new RuntimeException(); };

        try (MockedStatic<RetryPolicy> mockedDoSleep = mockStatic(RetryPolicy.class)) {
            mockedDoSleep.when(() -> RetryPolicy.doSleep(2000L)).thenReturn(true);

            assertThrows(RuntimeException.class, () -> policy.execute(task));
            mockedDoSleep.verify(() -> RetryPolicy.doSleep(2000L), times(2));
        }
    }

    // 测试用例5：零延迟直接重试
    @Test
    public void testZeroDelay() {
        RetryDelayCalculator mockDelay = mock(RetryDelayCalculator.class);
        when(mockDelay.getDelay(1)).thenReturn(0L);

        RetryPolicy policy = new RetryPolicy(mockDelay, 1, RuntimeException.class);
        Runnable task = () -> { throw new RuntimeException(); };
        Callable<String> task2=()->{throw new RuntimeException();};
        Function<String,String> task3=(s)->{throw new RuntimeException();};
        
        try (MockedStatic<RetryPolicy> mockedDoSleep = mockStatic(RetryPolicy.class)) {
            Assertions.assertThrows(RuntimeException.class, () -> policy.execute(task));
            mockedDoSleep.verify(() -> RetryPolicy.doSleep(anyLong()), never());
        }
        
        try (MockedStatic<RetryPolicy> mockedDoSleep = mockStatic(RetryPolicy.class)) {
            Assertions.assertThrows(RuntimeException.class, () -> policy.execute(task2));
            mockedDoSleep.verify(() -> RetryPolicy.doSleep(anyLong()), never());
        }
        
        try (MockedStatic<RetryPolicy> mockedDoSleep = mockStatic(RetryPolicy.class)) {
            Assertions.assertThrows(RuntimeException.class, () -> policy.execute(task3,""));
            mockedDoSleep.verify(() -> RetryPolicy.doSleep(anyLong()), never());
        }
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
	

    private RetryDelayCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RetryDelayCalculator();
    }

    @Test
    void powerOfTow_ShouldReturnOne_WhenNumIsZeroOrNegative() {
        assertEquals(1, RetryDelayCalculator.powerOfTwo(0));
        assertEquals(1, RetryDelayCalculator.powerOfTwo(-1));
    }

    @Test
    void powerOfTow_ShouldReturnPowerOfTwo_WhenNumIsPositiveAndLessThanEqualTo32() {
        IntStream.rangeClosed(1, 32).forEach(i -> {
            assertEquals(1L << i, RetryDelayCalculator.powerOfTwo(i));
        });
    }

    @Test
    void powerOfTow_ShouldReturnPowerOfThirtyTwo_WhenNumIsGreaterThan32() {
        assertEquals(1L << 32, RetryDelayCalculator.powerOfTwo(33));
    }

    @Test
    void getDelay_FixedDelayStrategy_ShouldReturnMinDelay() {
        calculator.strategy = RetryPolicy.Strategy.FIXED_DELAY;
        calculator.minDelay = Duration.ofSeconds(1);
        assertEquals(1000, calculator.getDelay(1));
        assertEquals(1000, calculator.getDelay(5));
    }

    @Test
    void getDelay_BackoffStrategy_ShouldReturnExponentialBackoff() {
        calculator.strategy = RetryPolicy.Strategy.BACKOFF;
        calculator.minDelay = Duration.ofSeconds(1);
        calculator.maxDelay = Duration.ofSeconds(10);
        assertTrue(calculator.getDelay(1) >= 1000);
        assertTrue(calculator.getDelay(2) >= 2000);
        assertTrue(calculator.getDelay(3) >= 4000);
    }

    @Test
    void jitter_NoJitter_ShouldReturnInput() {
        calculator.jitter = Duration.ZERO;
        assertEquals(1000, calculator.jitter(1000));
    }

    @Test
    void jitter_WithJitter_ShouldReturnJitteredValue() {
        calculator.jitter = Duration.ofMillis(1);
        long jitteredValue = calculator.jitter(1000);
        assertTrue(jitteredValue >= 999 && jitteredValue <= 1001);
    }
}
