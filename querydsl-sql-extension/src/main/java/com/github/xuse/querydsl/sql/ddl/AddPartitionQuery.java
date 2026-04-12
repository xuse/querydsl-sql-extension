package com.github.xuse.querydsl.sql.ddl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.annotation.partition.AutoTimePartitions;
import com.github.xuse.querydsl.annotation.partition.Partition;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.dbmeta.TableInfo;
import com.github.xuse.querydsl.sql.ddl.DDLOps.AlterTablePartitionOps;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionDefineOps;
import com.github.xuse.querydsl.sql.partitions.PartitionAssigned;
import com.github.xuse.querydsl.sql.partitions.PartitionBy;
import com.github.xuse.querydsl.sql.partitions.PartitionDef;
import com.github.xuse.querydsl.sql.partitions.RangePartitionBy;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.FastHashtable;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.SQLTemplatesEx;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLSerializerAlter;
import com.querydsl.sql.SchemaAndTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

public class AddPartitionQuery extends AbstractDDLClause<AddPartitionQuery> {

	private final PartitionAssigned partitionBy;

	private final List<Partition> partitions = new ArrayList<>();

	private List<PartitionInfo> exists;

	public AddPartitionQuery(MetadataQuerySupport connection, ConfigurationEx configuration, RelationalPath<?> t) {
		super(connection, configuration, RelationalPathExImpl.toRelationPathEx(t));
		RelationalPathEx<?> table = super.table;
		if (table.getPartitionBy() == null) {
			throw Exceptions.illegalArgument("There is no partition definitions on Table [{}]", table.getSchemaAndTable());
		}
		this.table = table;
		if (table.getPartitionBy() instanceof PartitionAssigned) {
			this.partitionBy = (PartitionAssigned) table.getPartitionBy();
		} else {
			throw Exceptions.illegalArgument("AddPartitionQuery supports List/Range partition only, to COALESCE or ADD PARTITIONS for HASH/KEY, please use PartitionSizeAdjustQuery. table={}", table.getSchemaAndTable());
		}
	}

	/**
	 * 增加一个分区
	 * @param name name
	 * @param value value
	 * @return this
	 */
	public AddPartitionQuery add(String name, String value) {
		partitions.add(new PartitionDef(name, "",value));
		return this;
	}
	
	/**
	 * 增加一个分区(Range)
	 * @param name name
	 * @param from range from
	 * @param to range to
	 * @return this
	 */
	public AddPartitionQuery add(String name, String from, String to) {
		partitions.add(new PartitionDef(name, from, to));
		return this;
	}
	

	/**
	 *  @return 根据当前时间生成合适的分区。
	 */
	public AddPartitionQuery autoPartitions() {
		PartitionBy partitionBy = table.getPartitionBy();
		if (partitionBy instanceof RangePartitionBy) {
			AutoTimePartitions[] auto = ((RangePartitionBy) partitionBy).getAutoPartition();
			if (auto != null && auto.length > 0) {
				partitions.clear();
				partitions.addAll(RangePartitionBy.generateAutoPartitions(auto[0],null));
			}
		}
		return this;
	}

	public List<Partition> toBeGenerated() {
		List<PartitionInfo> info = getCurrentPartitions();
		Set<String> existsPartitionNames = info.stream().map(PartitionInfo::getName).collect(Collectors.toSet());
		List<Partition> partitions = new ArrayList<>(this.partitions);
		partitions.removeIf(p -> existsPartitionNames.contains(p.name()));
		return partitions;
	}

	public List<PartitionInfo> getCurrentPartitions() {
		if (exists != null) {
			return exists;
		}
		SchemaAndTable actualTable = connection.asInCurrentSchema(table.getSchemaAndTable());
		if (routing != null) {
			actualTable = routing.getOverride(actualTable, configuration);
		}
		List<PartitionInfo> result = exists = connection.getPartitions(actualTable);
		if (result.isEmpty()) {
			TableInfo tableInfo = connection.getTable(actualTable);
			if (tableInfo == null) {
				throw Exceptions.illegalArgument("The table {} is not exist.", table.getSchemaAndTable());
			} else {
				throw Exceptions.illegalArgument("The table {} is not a partitioned table.", table.getSchemaAndTable());
			}
		}
		return result;
	}

	@Override
	protected List<String> generateSQLs() {
		List<Partition> partitions = toBeGenerated();
		if (partitions.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> sqls = new ArrayList<>();
		//收集Reorganize信息，尝试将针对同一个旧分区的重组织合并到SQL语句中
		Map<String,Reorganize> reorganizes = new HashMap<>();
		for (Partition p : partitions) {
			String sql = generateSQL(p,reorganizes);
			if(sql!=null) {
				sqls.add(sql);
			}
		}
		//处理重新组织分区请求
		if(!reorganizes.isEmpty()) {
			for(Reorganize r:reorganizes.values()) {
				SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
				serializer.setRouting(routing);
				serializer.serializeAction(null, null, Collections.singletonList(r.getClause(partitionBy, configuration,table)));
				String sql = serializer.toString();
				if(sql!=null){
					sqls.add(sql);
				}
			}
		}
		return sqls;
	}

	/**
	 * 任何时候，如果新建分区对已有分区产生了重叠，都会涉及REORGANIZE PARTITION。否则无法创建。 分为RANGE，LIST两种情形。
	 * RANGE类分区，可以理解为从负无穷开始的一条线，每个分区是在其中划一个点，小于该点且大于前一点的数据被划入该分区。
	 * 因此每个分区的增加最多是将一个已有线段划分为两段。
	 * LIST分区的情形更复杂，包括拆分已有单个分区，拆分已有多个分区等重组情形。MYSQL要求在重组多个分区时，指定连续的分区进行重组（即便该分区范围没有发生变化，也需要参与重组，这个有点复杂了。）
	 * @param p p
	 * @return String
	 */
	protected String generateSQL(Partition p,Map<String,Reorganize> reorganizesMap) {
		List<PartitionInfo> info = getCurrentPartitions();
		//Prepare Reorganization
		Reorganize reorgnize = null;
		SQLTemplatesEx templates=configuration.getTemplates();
		if(templates.supports(PartitionDefineOps.VALUES_LESS_THAN)
				&& templates.supports(AlterTablePartitionOps.REORGANIZE_PARTITION)
				) {
			if (partitionBy instanceof RangePartitionBy) {
				reorgnize = calcRange(p, info);
			} else {
				reorgnize = calcList(p, info);
			}
			if (reorgnize == DUPLICATE) {
				// A same partition (with different name already exists.)
				return null;
			}
		}
		if(reorgnize!=null) {
			final Reorganize finalReorgnize = reorgnize;
			String key=reorgnize.getSourceKey();
			Reorganize reg = reorganizesMap.computeIfAbsent(key, k-> finalReorgnize);
			if(reg!=reorgnize) {
				reg.merge(reorgnize);
			}
			return null;
		}else {
			SQLSerializerAlter serializer = new SQLSerializerAlter(configuration, true);
			serializer.setRouting(routing);
			// Do not support , ALGORITHM=INPLACE, LOCK=NONE
			Expression<?> exp=DDLExpressions.simple(AlterTablePartitionOps.ADD_PARTITION, partitionBy.defineOnePartition(p, configuration),table);
			serializer.serializeAction(null,null,Collections.singletonList(exp));
			return serializer.toString();
		}
	}

	private Reorganize calcList(Partition p, List<PartitionInfo> infors) {
		FastHashtable<Partition> effected = new FastHashtable<>(infors.size());
		List<String> newList = Arrays.asList(StringUtils.split(p.value(), ','));
		PartitionInfo lastEffected = null;
		// First find all effected partitions in exist.
		for (PartitionInfo info : infors) {
			List<String> ss = new ArrayList<>(Arrays.asList(StringUtils.split(info.getPartitionDescription(), ',')));
			int originalSize = ss.size();
			ss.removeAll(newList);
			if (ss.size() < originalSize) {
				String effectedList = StringUtils.join(ss, ',');
				effected.put(info.getName(), new PartitionDef(info.getName(),null, effectedList));
				lastEffected = info;
			}
		}
		// Calc names and range.
		if (lastEffected==null) {
			return null;
		}
		String firstName = effected.firstKey();
		String lastName = lastEffected.getName();
		List<String> sourcePartitions = new ArrayList<>();
		List<Partition> targetPartitionDefine = new ArrayList<>();
		boolean begin = false;
		for (PartitionInfo info : infors) {
			if (lastName.equals(info.getName())) {
				sourcePartitions.add(info.getName());
				Partition target = effected.get(info.getName());
				targetPartitionDefine.add(target);
				begin = false;
				continue;
			}
			if (firstName.equals(info.getName())) {
				sourcePartitions.add(info.getName());
				Partition target = effected.get(info.getName());
				targetPartitionDefine.add(target);
				begin = true;
				continue;
			}
			if (begin) {
				// MYSQL 要求，REORGANIZE多个分区时，必须指定连续的分区，即便这个分区没有实质改动。因此位于第一个受影响分区和最后一个之间的分区，也被加入调整列表
				sourcePartitions.add(info.getName());
				Partition target = effected.get(info.getName());
				if (target == null) {
					target = new PartitionDef(info.getName(),null, info.getPartitionDescription());
				}
				targetPartitionDefine.add(target);
			}
		}
		targetPartitionDefine.add(p);
		return new Reorganize(sourcePartitions, targetPartitionDefine);
	}

	private Reorganize calcRange(Partition p, List<PartitionInfo> oldPartitions) {
		String add = p.value();
		PartitionInfo effected = null;
		for (PartitionInfo info : oldPartitions) {
			String point = info.getPartitionDescription();
			int v = compare(add, point);
			if (v == 0) {
				return DUPLICATE;
			}
			if (v < 0) {
				effected = info;
				break;
			}
		}
		if (effected != null) {
			String s = effected.getName();
			Partition e = new PartitionDef(effected.getName(), "", effected.getPartitionDescription());
			return new Reorganize(Collections.singletonList(s), Arrays.asList(p, e));
		}
		return null;
	}

	private static int compare(String a, String b) {
		int v = a.compareTo(b);
		if (v == 0) {
			return 0;
		}
		boolean maxA = "MAXVALUE".equalsIgnoreCase(a);
		boolean maxB = "MAXVALUE".equalsIgnoreCase(b);
		if (maxA && maxB) {
			return 0;
		} else if (maxA) {
			return 1;
		} else if (maxB) {
			return -1;
		}
		return v;
	}

	private static final Reorganize DUPLICATE = new Reorganize(Collections.emptyList(), Collections.emptyList());

	/**
	 *  分区重组策略
	 */
	@AllArgsConstructor
	@Data
	static final class Reorganize {
		@NonNull
		List<String> sourcePartition;
		@NonNull
		List<Partition> targetPartitions;
		
		public String getSourceKey() {
			 return StringUtils.join(sourcePartition, ',');
		}

		/*
		 * 将针对同一个分区的再组织请求，合并到一次操作中去. 
		 * Merge的顺序必须严格按照分区从小到大的顺序。
		 */
		public void merge(Reorganize reorgnize) {
			Set<String> names= this.targetPartitions.stream().map(Partition::name).collect(Collectors.toSet());
			List<Partition> result=new ArrayList<>(targetPartitions);
			for(Partition p:reorgnize.getTargetPartitions()) {
				if(!names.contains(p.name())) {
					result.add(p);
				}
			}
			if(result.size()>this.targetPartitions.size()) {
				result.sort((a,b)->AddPartitionQuery.compare(a.value(), b.value()));
				this.targetPartitions = result;
			}
		}

		public Expression<?> getClause(PartitionAssigned partitionBy, ConfigurationEx configuration,RelationalPath<?> table) {
			List<Expression<?>> defines = new ArrayList<>(targetPartitions.size());
			for (Partition p : targetPartitions) {
				defines.add(partitionBy.defineOnePartition(p, configuration));
			}
			return DDLExpressions.simple(AlterTablePartitionOps.REORGANIZE_PARTITION, DDLExpressions.text(getSourceKey()), DDLExpressions.wrapList(defines),table);
		}
	}

	@Override
	protected String generateSQL() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected List<Operator> checkSupports() {
		return Collections.singletonList(AlterTablePartitionOps.ADD_PARTITION);
	}
}
