package com.github.xuse.querydsl.init;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;

import com.github.xuse.querydsl.annotation.init.InitializeData;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.CloseableSQLQueryFactory;
import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.expression.AdvancedMapper;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.querydsl.core.QueryException;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.RoutingStrategy;
import com.querydsl.sql.dml.Mapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataInitializer {
	public static final String NULL_STRING_ESCAPE = "@Null@";
	//文件字符集
	private Charset charset = StandardCharsets.UTF_8;

	//数据或脚本文件
	private String resource;
	
	//基本信息
	private final ConfigurationEx configuration;
	private final SQLQueryFactory factory;
	private final RelationalPath<?> table;
	private final Map<String, Path<?>> pathMap;
	
	@SuppressWarnings("rawtypes")
	private final Mapper mapper;
	
	//选项：允许手动Seq
	private boolean manualSequence;
	
	//选项：表名路由
	private RoutingStrategy routing;
	
	//提示：是新建的表（跳过数据检测）
	private boolean isNewTable;

	//选项：如果资源不存在抛出异常
	private boolean ignoreIfResourceFileNotFound;

	//选项：数据文件是SQL脚本
	private boolean isSql;
	
	//选项：MergeKey
	private List<Path<?>> mergeKeys;
	
	private boolean enable = true;

	public DataInitializer(SQLQueryFactory session,RelationalPath<?> table) {
		this.factory = session;
		this.configuration = session.getConfiguration();
		this.table=table;
		this.pathMap = table.getColumns().stream().collect(Collectors.toMap(e -> e.getMetadata().getName(), e -> e));
		this.mapper = AdvancedMapper.getDefaultMapper(table);
		initMergeKeys();
	}
	
	public DataInitializer from(String resource) {
		this.resource = resource;
		return this;
	}
	
	public DataInitializer withRouting(RoutingStrategy routing) {
		if(routing!=null) {
			this.routing=routing;
		}
		return this;
	}
	
	/**
	 * 将数据表视作新表。设置true后会放弃Merge策略，仅尝试数据数据插入。
	 * @param isNew
	 * @return this
	 */
	public DataInitializer isNewTable(boolean isNew) {
		this.isNewTable=isNew;
		return this;
	}
	
	/**
	 * 设置资源文件的字符集
	 * @param chartset
	 * @return
	 */
	public DataInitializer charset(Charset chartset) {
		this.charset=chartset;
		return this;
	}
	
	
	public DataInitializer ignoreResource(boolean flag) {
		ignoreIfResourceFileNotFound = flag;
		return this;
	}

	/**
	 * 指定资源文件是SQL脚本方式执行
	 * @return this
	 */
	public DataInitializer isSqlFile() {
		isSql = true;
		return this;
	}
	
	/**
	 * 指定Merge的业务键。如果不指定会使用主键进行数据Merge/
	 * @param keys 业务键
	 * @return this
	 */
	public DataInitializer withKeys(String... keys) {
		initMergeKeys(keys);
		return this;
	}
	
	/**
	 * 指定Merge的业务键。如果不指定会使用主键进行数据Merge/
	 * @param keys  业务键
	 * @return this
	 */
	public DataInitializer withKeys(Path<?>... keys) {
		if (keys.length > 0) {
			this.mergeKeys = Arrays.asList(keys);
		}
		return this;
	}

	public final int execute() {
		if(!enable) {
			return 0;
		}
		URL url=getResourceURL();
		if(url==null) {
			return 0;
		}
		int count;
		long time=System.currentTimeMillis();
		String tableName = table.getTableName();
		if(isSql) {
			count=importSQLScript(url);
			log.info("Table [{}] dataInit completed. {} statements executed, time={}.", tableName, count,(System.currentTimeMillis()-time));
		}else {
			try(CloseableSQLQueryFactory session=factory.oneConnectionSession(Connection.TRANSACTION_READ_COMMITTED)){
				if(isNewTable ||  mergeKeys.isEmpty() || isEmptyTable()) {
					log.info("Table [{}] already exists, begin merge data into database.", tableName);
					count=importWithInsert(session, url);
				}else {
					log.info("Table [{}] already exists, begin merge data into database.", tableName);
					count=importUsingMerge(session, url);
				}
				log.info("Table [{}] dataInit completed. {} records saved, time={}.", tableName, count,(System.currentTimeMillis()-time));	
			}
		}
		return count;
	}

	private int importUsingMerge(SQLQueryFactory session, URL url) {
		int count = 0;
		for (Object obj : readData(url)) {
			count+=doMerge(session, obj);
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	private int doMerge(SQLQueryFactory session, Object obj) {
		SQLQueryAlter<?> select = session.selectFrom(table).withRouting(routing);
		Map<Path<?>, Object> values = mapper.createMap(table, obj);
		SQLTypeUtils.setWhere(mergeKeys, select, values);
		List<?> list = select.limit(2).fetch();
		int size = list.size();
		if (size > 1) {
			// Ignore this record
			log.error("Table {}, key={} has more than one record match. Merge process on this record will be ignored.",table,values);
			return 0;
		} else if (size == 0) {
			return (int) session.insert(table).withRouting(routing).populate(obj, mapper).execute();
		}
		SQLUpdateClauseAlter update = session.update(table).withRouting(routing).populateWithCompare(obj, list.get(0));
		SQLTypeUtils.setWhere(mergeKeys, update, values);
		return (int)update.execute();
		
	}

	private int importWithInsert(SQLQueryFactory session, URL url) {
		int count = 0;
		List<Object> data = readData(url);
		for (int i = 0; i < data.size(); i += 500) {
			int batchIndex = Math.min(i + 500, data.size());
			List<Object> currentBatch = data.subList(i, batchIndex);
			try {
				SQLInsertClauseAlter query = session.insert(table).withRouting(routing).populateBatch(currentBatch);
				count += query.execute();
				log.info("execute batch size={}: return {} ", currentBatch.size(), count);
			} catch (QueryException e) {
				if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
					count += insertOnebyOne(session, currentBatch, e);
				} else {
					throw e;
				}
			} catch (DataIntegrityViolationException e) {
				count += insertOnebyOne(session, currentBatch, e);
			}
		}
		return count;
		
	}

	private int importSQLScript(URL url) {
		return factory.getMetadataFactory().executeScriptFile(url, charset, false, null);
	}

	private boolean isEmptyTable() {
		List<?> obj=factory.selectFrom(table).limit(1).fetch();
		return obj.isEmpty();
	}

	private URL getResourceURL() {
		String resource = this.resource;
		resource= calcResourceName(resource,table, configuration);
		
		//get resource file
		URL url = table.getClass().getResource("/" + resource);
		String tableName = table.getTableName();
		if (url == null) {
			if (!ignoreIfResourceFileNotFound) {
				throw new IllegalStateException("Resource of table [" + tableName + "] was not found:" + resource);
			}
			log.warn("Data file was not found:{}", resource);
		}
		return url;
	}

	public static String calcResourceName(String resource, RelationalPath<?> table, ConfigurationEx configuration) {
		if (StringUtils.isEmpty(resource)) {
			resource = table.getType().getName() + configuration.getDataInitFileSuffix();
		}
		return resource;
	}

	private void initMergeKeys(String... mergeKey) {
		List<Path<?>> paths = mergeKey == null ? Collections.emptyList()
				: Arrays.asList(mergeKey).stream()
						.map(e -> Assert.nonNull(pathMap.get(e), "Field {} not found in class {}", e, table))
						.collect(Collectors.toList());

		if (paths.isEmpty()) {
			PrimaryKey<?> pk = table.getPrimaryKey();
			paths = pk == null ? Collections.emptyList() : CollectionUtils.cast(pk.getLocalColumns());
		}
		this.mergeKeys = paths;
	}

	// TODO 优化改造为流式处理，节约内存
	private List<Object> readData(URL url) {
		List<Path<?>> paths = table.getColumns();
		int count = 0;
		try (CsvReader reader = new CsvReader(new InputStreamReader(url.openStream(), charset))) {
			// 根据Header分析Property

			// 按照BeanColumn顺序重新组织序号。。
			List<Entry<Path<?>, Integer>> props = new ArrayList<Entry<Path<?>, Integer>>();
			if (reader.readHeaders()) {
				for (String header : reader.getHeaders()) {
					if (header.charAt(0) == '[') {
						header = header.substring(1, header.length() - 1);
					}
					Path<?> field = pathMap.get(header);
					if (field == null) {
						throw new IllegalArgumentException(
								String.format("The field [%s] in CSV file doesn't exsts in the entity [%s] metadata.",
										header, table.getTableName()));
					}
					int pathIndex = paths.indexOf(field);
					props.add(new Entry<>(field, pathIndex));
				}
			}
			// 根据预先分析好的Property读取属性
			List<Object> result = new ArrayList<Object>();
			int totalColumns = paths.size();
			FactoryExpression<?> exp=(FactoryExpression<?>) table.getProjection();
			while (reader.readRecord()) {
				Object[] values = new Object[totalColumns];
				for (int i = 0; i < props.size(); i++) {
					Entry<Path<?>, Integer> entry = props.get(i);
					Path<?> prop = entry.getKey();
					int index = entry.getValue();
					values[index] = Codecs.fromString(reader.get(i), prop.getType());
				}
				result.add(exp.newInstance(values));
				count++;
				if (count > 100_000) {
					log.error(
							"One table can process up to 100K records, more records will be ignored. current table is {}",
							table.getTableName());
					break;
				}
			}
			return result;
		} catch (IOException e) {
			throw Exceptions.toRuntime(e);
		}
	}

	/*
	 * 主键或约束冲突，改为逐条插入
	 */
	protected int insertOnebyOne(SQLQueryFactory session, List<Object> data, Exception ex) {
		log.warn("Encountering a data constraints conflicts, will try insert one by one.", ex);
		int count = 0;
		for (Object e : data) {
			session.insert(table).withRouting(routing).populate(e).execute();
			count++;
		}
		return count;
	}

	public DataInitializer applyConfig(InitializeData anno) {
		Assert.notNull(anno);
		this.enable=anno.enable();
		this.resource=anno.value();
		if(StringUtils.isNotEmpty(anno.charset())) {
			this.charset=Charset.forName(anno.charset());
		}
		this.ignoreIfResourceFileNotFound = !anno.ensureFileExists();
		this.manualSequence=anno.manualSequence();
		
		if(anno.mergeKeys()!=null && anno.mergeKeys().length>0) {
			withKeys(anno.mergeKeys());
		}
		if(StringUtils.isNotEmpty(anno.sqlFile())) {
			this.resource=anno.sqlFile();
			this.isSql = true;
		}
		return this;
	}
}
