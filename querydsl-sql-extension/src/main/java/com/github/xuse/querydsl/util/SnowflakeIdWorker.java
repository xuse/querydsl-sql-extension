package com.github.xuse.querydsl.util;

/**
 * Twitter_Snowflake<br>
 * The structure of SnowFlake is as follows (each part is separated by -):<br>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 -
 * 000000000000 <br>
 * 1 bit sign, since the long basic type in Java is signed, the highest bit is
 * the sign bit, positive numbers are 0, negative numbers are 1, so the ID is
 * generally positive, and the highest bit is 0<br>
 * 41 bits for the timestamp (millisecond level). Note that the 41-bit timestamp
 * does not store the current time's timestamp, but stores the difference in the
 * timestamp (current timestamp - start timestamp) which is the start timestamp
 * when our ID generator starts using time specified by our program (such as the
 * startTime attribute of the IdWorker class below). 41 bits of the timestamp
 * can be used for 69 years. The year T = (1L << 41) / (1000L * 60 * 60 * 24 *
 * 365) = 69<br>
 * 10 bits for machine data, which can be deployed in 1024 nodes, including 5
 * bits for datacenterId and 5 bits for workerId<br>
 * 12 bits for sequence, which is the count within the millisecond. The 12-bit
 * counting sequence number supports generating 4096 ID sequences per
 * millisecond per node (same machine, same timestamp)<br>
 * This adds up exactly to 64 bits, which is a Long type.
 * <p>
 * The advantages of SnowFlake are that it is incrementally ordered overall, and
 * there will be no ID collisions in the distributed system (distinguished by
 * datacenterId and workerId), and it is highly efficient. Tests have shown that
 * SnowFlake can generate approximately 260,000 IDs per second.
 * 
 * @author Joey
 */
public final class SnowflakeIdWorker implements SnowFlakeParams {

	/**  worker ID(0~255) */
	private int workerId;

	/** Data Center ID(0~3) */
	private int datacenterId;

	/** sequence in same mills(0~4095) */
	private long sequence = 0L;

	/** The time stamp of last generation */
	private long lastTimestamp = -1L;

	/**
	 * @param workerId worker ID (0~255)
	 */
	public SnowflakeIdWorker(int workerId) {
		this(workerId, 0);
	}

	/**
	 * @param workerId     worker ID (0~255)
	 * @param datacenterId data center ID (0~3)
	 */
	public SnowflakeIdWorker(int workerId, int datacenterId) {
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(
					String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
		}
		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(
					String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
		}
		this.workerId = workerId;
		this.datacenterId = datacenterId;
	}

	// ==============================Methods==========================================
	/**
	 * Get the next ID (This method is thread-safe)
	 * 
	 * @return SnowflakeId
	 */
	public synchronized long nextId() {
		long timestamp = timeGen();

		// If the current time is less than the timestamp of the previous ID generation,
		// it indicates that the system clock has moved backward, and an exception
		// should be thrown at this time.
		if (timestamp < lastTimestamp) {
			throw new RuntimeException(String.format(
					"Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
		}
		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0) {
				// When the sequence is exhausted, block until the next millisecond to get a new timestamp.
				timestamp = tilNextMillis(lastTimestamp);
			}
		}else {
			sequence = 0L;
		}
		lastTimestamp = timestamp;
		return ((timestamp - twepoch) << timestampLeftShift) //
				| (datacenterId << datacenterIdShift) //
				| (workerId << workerIdShift) //
				| sequence;
	}

	/**
	 * Spin block until the next millisecond, until a new timestamp is obtained.
	 * @param lastTimestamp the last time stamp.
	 * @return the time stamp.
	 */
	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * Return the current time in milliseconds.
	 * @return  time stamp in milliseconds
	 */
	private long timeGen() {
		return System.currentTimeMillis();
	}
}