package com.github.xuse.querydsl.sql.ddl;

import java.util.Arrays;
import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTablePartitionOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

/**
 * 仅针对KEY HASH等
 * @author Joey
 *
 */
@Slf4j
public class PartitionSizeAdjustQuery extends AbstractDDLClause<PartitionSizeAdjustQuery> {
	private int currentSize = -1;

	private int toSize;

	public PartitionSizeAdjustQuery(MetadataQuerySupport connection, ConfigurationEx configuration,
			RelationalPath<?> table) {
		super(connection, configuration, RelationalPathExImpl.toRelationPathEx(table));
	}

	private void setToSize(int size) {
		if (size <= 0) {
			throw Exceptions.illegalArgument("can not adjust table {}'s partition size to {}, current is {}",
					table.getSchemaAndTable(), size, currentSize);
		}
		this.toSize = size;
	}

	/**
	 * 指定分区数，该操作会使之前调用的 {@code #toSize(int)} {@link #add(int)} {@link #coalesce(int)}操作失效。
	 * @param size 调整到size个分区。
	 * @return this;
	 */
	public PartitionSizeAdjustQuery toSize(int size) {
		setToSize(size);
		return this;
	}


	/**
	 * 指定增加分区，该操作会使之前调用的 {@link #toSize(int)} {@code #add(int)} {@link #coalesce(int)}操作失效。
	 * @param size 增加size个分区。
	 * @return this;
	 */
	public PartitionSizeAdjustQuery add(int size) {
		setToSize(getCurrentSize() + size);
		return this;
	}

	/**
	 * 指定收缩分区，该操作会使之前调用的 {@link #toSize(int)} {@link #add(int)} {@code #coalesce(int)}操作失效。
	 * @param size 收缩size个分区
	 * @return this;
	 */
	public PartitionSizeAdjustQuery coalesce(int size) {
		setToSize(getCurrentSize() - size);
		return this;
	}

	public int getCurrentSize() {
		if (currentSize > -1) {
			return currentSize;
		}
		SchemaAndTable actualTable = connection.asInCurrentSchema(table.getSchemaAndTable());
		if (routing != null) {
			actualTable = routing.getOverride(actualTable, configuration);
		}
		List<PartitionInfo> info=connection.getPartitions(actualTable);
		if(info.isEmpty()) {
			return currentSize = 0; 
		}

		PartitionInfo i=info.iterator().next();
		if(i.getMethod()==PartitionMethod.HASH || i.getMethod()==PartitionMethod.KEY ||i.getMethod()==PartitionMethod.LINEAR_HASH) {
			return this.currentSize = info.size();		
		}else {
			throw Exceptions.illegalState("the table {}'s partition method is {}, do not support coalesce or expand.",table.getSchemaAndTable(),i.getMethod());
		}
	}

	@Override
	protected String generateSQL() {
		if (toSize <= 0) {
			log.warn(
					"Can't adjust table {}'s partition to 0. please using REMOVE PARTITIONING to remove all partitions",
					table.getSchemaAndTable());
			return null;
		}
		int current = getCurrentSize();
		if (current == 0) {
			log.warn("Can't adjust table {}'s partition to {}. there's no partitions now.", table.getSchemaAndTable(),
					toSize);
			return null;
		}

		if (current == toSize) {
			return null;
		}
		
		DDLMetadataBuilder builder=new DDLMetadataBuilder(configuration, table, routing);
		if (current > toSize) {
			// 收缩
			Expression<?> text=ConstantImpl.create(current - toSize);
			builder.serilizeSimple(AlterTablePartitionOps.COALESCE_PARTITION, table, text);
		} else {
			// 扩张
			Expression<?> text=ConstantImpl.create(toSize - current);
			builder.serilizeSimple(AlterTablePartitionOps.ADD_PARTITION_COUNT, table, text);
		}
		return builder.getSql();
	}

	@Override
	protected List<Operator> checkSupports() {
		return Arrays.asList(AlterTablePartitionOps.ADD_PARTITION_COUNT, AlterTablePartitionOps.COALESCE_PARTITION);
	}
}
