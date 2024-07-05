package com.github.xuse.querydsl.sql.ddl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.annotation.partition.AutoTimePartitions;
import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.github.xuse.querydsl.sql.partitions.RangePartitionBy;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

/**
 * 
 * 注意：DROP PARTITION会清除分区内的表数据，仅用于旧数据清理。
 */
public class DropPartitionQuery extends AbstractDDLClause<DropPartitionQuery>{
	private final RelationalPathEx<?> table;
	
	private List<PartitionInfo> exists;
	
	private final List<String> partitions=new ArrayList<>();
	
	public DropPartitionQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPathEx<?> path) {
		super(connection, configuration, path);
		this.table=path;
	}
	
	public List<PartitionInfo> getCurrentPartitions() {
		if (exists != null) {
			return exists;
		}
		SchemaAndTable acutalTable = connection.asInCurrentSchema(table.getSchemaAndTable());
		if (routing != null) {
			acutalTable = routing.getOverride(acutalTable, configuration);
		}
		return exists = Collections.unmodifiableList(connection.getPartitions(acutalTable));
	}
	
	
	/**
	 * 指定要删除的分区
	 * @param name
	 * @param value
	 * @return this
	 */
	public DropPartitionQuery partitions(Collection<String> names) {
		partitions.addAll(names);
		return this;
	}
	
	/**
	 * 增加一个分区
	 * @param name
	 * @return this
	 */
	public DropPartitionQuery partition(String name) {
		partitions.add(name);
		return this;
	}
	
	/**
	 * 当使用RangePartitionBy并且根据当前时间自动生成了分区后，可以对比出数据库中额外的分区（一般是已经过期的分区）。
	 * 将对比结果加入删除列表
	 * @return this
	 */
	public DropPartitionQuery partitionsOutOfTimeRange() {
		List<PartitionInfo> expired = calcPartitionsOutOfTimeRange();
		for (PartitionInfo p : expired) {
			partitions.add(p.getName());
		}
		return this;
	}
	
	/**
	 * 当使用RangePartitionBy并且根据当前时间自动生成了分区后，可以对比出数据库中额外的分区（一般是已经过期的分区）。
	 * @return 数据库中存在，但table的AutoTimePartitions中不包含的分区。
	 */
	public List<PartitionInfo> calcPartitionsOutOfTimeRange(){
		PartitionBy partitionBy = table.getPartitionBy();
		if (partitionBy instanceof RangePartitionBy) {
			AutoTimePartitions[] auto = ((RangePartitionBy) partitionBy).getAutoPartition();
			if (auto != null && auto.length > 0) {
				List<Partition> autoPartitions = RangePartitionBy.generateAutoPartitions(auto[0]);
				Set<String> effective=autoPartitions.stream().map(Partition::name).collect(Collectors.toSet());
				List<PartitionInfo> result=new ArrayList<>(getCurrentPartitions());
				for(Iterator<PartitionInfo> iter=result.iterator();iter.hasNext();) {
					PartitionInfo info=iter.next();
					if(effective.contains(info.getName())) {
						iter.remove();
					}
				}
				return result;
			}
		}
		throw new UnsupportedOperationException("Table "+table.getSchemaAndTable()+" is not a table with RANGE Partition with AutoTimePartitions.");
	}
	
	/**
	 * 与数据库中存在的分区名称进行对比，过滤掉不存在的分区。
	 * @return this
	 */
	public DropPartitionQuery filterOutInvalidPartitions() {
		Set<String> set=getCurrentPartitions().stream().map(PartitionInfo::getName).map(String::toLowerCase).collect(Collectors.toSet());
		for(Iterator<String> iter=this.partitions.iterator();iter.hasNext();) {
			String name=iter.next();
			if(!set.contains(name.toLowerCase())) {
				iter.remove();
			}
		}
		return this;
	}
	

	@Override
	protected List<String> generateSQLs() {
		if(partitions.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> sqls=new ArrayList<>(partitions.size());
		for(String name:partitions) {
			sqls.add(generateSQL(name));
		}
		return sqls;
	}

	protected String generateSQL(String partition) {
		SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
		serializer.setRouting(routing);
		//,", ALGORITHM=INPLACE, LOCK=NONE" not support
		serializer.serializeAction("ALTER TABLE ",table, " DROP PARTITION ",partition);
		return serializer.toString();
	}

	@Override
	protected String generateSQL() {
		throw new UnsupportedOperationException();
	}
}
