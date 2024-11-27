package com.github.xuse.querydsl.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.datatype.util.Threads;

public class ThreadPoolTest {
	/**
	 * 正压线程池测试。
	 * 正压的意思是使用了一个对线程池扩容动作进行正压的队列。
	 * 当达到压力阈值时，线程池即开始扩容。此时队列还有剩余容量，可以避免大量突发性流量触发Reject动作。
	 */
	@Test
	public void testThreadPool() {
		Runnable task=()-> {
			Threads.doSleep(1000);
		};
		ThreadPoolExecutor pool = Threads.newPoolBuilder()
		.coreSize(1)
		.maximumSize(5)
		.queueSize(5)
		.queuePressureSize(3)
		.onReject(new ThreadPoolExecutor.AbortPolicy())
		.build();
		
		System.out.println("0=="+pool.getPoolSize()+"|"+pool.getQueue().size());
		pool.submit(task);
		pool.submit(task);
		pool.submit(task);
		System.out.println("3=="+pool.getPoolSize()+"|"+pool.getQueue().size());
		pool.submit(task);
		//当第4个任务加入后，线程1个，队列3个
		System.out.println("4=="+pool.getPoolSize()+"|"+pool.getQueue().size());
		pool.submit(task);
		//当第5个任务加入后，线程池开始扩容，线程2个，队列3个
		assertTrue(pool.getPoolSize()>1);
		System.out.println("5=="+pool.getPoolSize()+"|"+pool.getQueue().size());
		pool.submit(task);
		pool.submit(task);
		pool.submit(task);
		pool.submit(task);
		//当第8个任务加入后，线程池已经扩满到5。第9个任务加入到队列中。
		assertEquals(4,pool.getQueue().size());
		pool.submit(task);
		assertEquals(5,pool.getQueue().size());
		
		RejectedExecutionException ex = null;
		try {
			pool.submit(task);
		}catch(RejectedExecutionException e) {
			ex=e;
		}
		//第11个任务，因为队列满，触发RejectedExecutionException。
		assertNotNull(ex);
	}
	
	
	
	
}
