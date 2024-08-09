package com.github.xuse.querydsl.annotation.partition;

import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;

public enum HashType {
	HASH(PartitionMethod.HASH),
	
	LINEAR_HASH(PartitionMethod.LINEAR_HASH),
	
	KEY(PartitionMethod.KEY);
	
	HashType(PartitionMethod method) {
		this.method=method;
	}
	
	private final PartitionMethod method;

	public PartitionMethod getMethod() {
		return method;
	}
}
