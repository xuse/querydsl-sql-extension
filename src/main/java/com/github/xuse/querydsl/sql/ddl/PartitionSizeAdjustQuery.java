package com.github.xuse.querydsl.sql.ddl;

import java.util.List;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.util.Exceptions;
import com.querydsl.core.types.DDLOps.PartitionMethod;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

import lombok.extern.slf4j.Slf4j;

/**
 * 仅针对KEY HASH等
 * @author jiyi
 *
 */
@Slf4j
public class PartitionSizeAdjustQuery extends AbstractDDLClause<PartitionSizeAdjustQuery> {
	private final RelationalPathEx<?> table;

	private int currentSize = -1;

	private int toSize;

	public PartitionSizeAdjustQuery(MetadataQuerySupport connection, ConfigurationEx configuration,
			RelationalPathEx<?> table) {
		super(connection, configuration, table);
		this.table = table;
	}

	private void setToSize(int size) {
		if (size <= 0) {
			throw Exceptions.illegalArgument("can not adjust table {}'s partition size to {}, current is {}",
					table.getSchemaAndTable(), size, currentSize);
		}
		this.toSize = size;
	}

	/**
	 * 指定分区数，该操作会使之前调用的 {@link #toSize(int)} {@link #add(int)} {@link #coalesce(int)}操作失效。
	 * @param size 调整到size个分区。
	 * @return this;
	 */
	public PartitionSizeAdjustQuery toSize(int size) {
		setToSize(size);
		return this;
	}


	/**
	 * 指定增加分区，该操作会使之前调用的 {@link #toSize(int)} {@link #add(int)} {@link #coalesce(int)}操作失效。
	 * @param size 增加size个分区。
	 * @return this;
	 */
	public PartitionSizeAdjustQuery add(int size) {
		setToSize(getCurrentSize() + size);
		return this;
	}

	/**
	 * 指定收缩分区，该操作会使之前调用的 {@link #toSize(int)} {@link #add(int)} {@link #coalesce(int)}操作失效。
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
		SchemaAndTable acutalTable = connection.asInCurrentSchema(table.getSchemaAndTable());
		if (routing != null) {
			acutalTable = routing.getOverride(acutalTable, configuration);
		}
		List<PartitionInfo> info=connection.getPartitions(acutalTable);
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
					"Cann't adjust table {}'s partition to 0. pleaseing using REMOVE PARTITIONING to remove all partitions",
					table.getSchemaAndTable());
			return null;
		}
		int current = getCurrentSize();
		if (current == 0) {
			log.warn("Cann't adjust table {}'s partition to {}. there's no partitions now.", table.getSchemaAndTable(),
					toSize);
			return null;
		}

		if (current == toSize) {
			return null;
		}
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.setRouting(routing);
		if (current > toSize) {
			// 收缩
			serializer.serializeAction("ALTER TABLE ", table, " COALESCE PARTITION ", String.valueOf(current - toSize));
		} else {
			// 扩张
			serializer.serializeAction("ALTER TABLE ", table, " ADD PARTITION PARTITIONS ",
					String.valueOf(toSize - current));
		}
		return serializer.toString();
	}

}
