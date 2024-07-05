package com.github.xuse.querydsl.sql.partitions;

import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.querydsl.core.types.Expression;

public interface PartitionAssigned extends PartitionBy {
	Expression<?> defineOne(Partition p, ConfigurationEx configurationEx);
}
