package com.github.xuse.querydsl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.util.RetryPolicy.RetryDelayCalculator;

public class RetryDelayCalcuatorTest {

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
        calculator.jitter = Duration.ofSeconds(1);
        long jitteredValue = calculator.jitter(1000);
        assertTrue(jitteredValue >= 999 && jitteredValue <= 1001);
    }
}