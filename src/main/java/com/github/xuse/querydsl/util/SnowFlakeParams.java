package com.github.xuse.querydsl.util;

public interface SnowFlakeParams {
    long twepoch = 1644915600000L;

    /** 机器id所占的位数 */
    long workerIdBits = 8L;

    /** 数据标识id所占的位数 */
    long datacenterIdBits = 2L;

    /** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
    long maxWorkerId = -1L ^ (-1L << workerIdBits);

    /** 支持的最大数据标识id，结果是31 */
    long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    /** 序列在id中占的位数 */
    long sequenceBits = 12L;

    /** 机器ID向左移12位 */
    long workerIdShift = sequenceBits;

    /** 数据标识id向左移17位(12+5) */
    long datacenterIdShift = sequenceBits + workerIdBits;

    /** 时间截向左移22位(5+5+12) */
    long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

    /** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
    long sequenceMask = -1L ^ (-1L << sequenceBits);
}
