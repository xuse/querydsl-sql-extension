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

import com.github.xuse.querydsl.annotation.InitializeData;
import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.init.csv.Codecs;
import com.github.xuse.querydsl.init.csv.CsvFileReader;
import com.github.xuse.querydsl.sql.CloseableSQLQueryFactory;
import com.github.xuse.querydsl.sql.Mappers;
import com.github.xuse.querydsl.sql.RelationalPathEx;
import com.github.xuse.querydsl.sql.RelationalPathExImpl;
import com.github.xuse.querydsl.sql.SQLQueryAlter;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.github.xuse.querydsl.sql.routing.RoutingStrategy;
import com.github.xuse.querydsl.sql.support.SQLTypeUtils;
import com.github.xuse.querydsl.util.Assert;
import com.github.xuse.querydsl.util.Entry;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.collection.CollectionUtils;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.QueryException;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.dml.Mapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableDataInitializer {

	public static final String NULL_STRING_ESCAPE = "@Null@";

	// 文件字符集
	private Charset charset = StandardCharsets.UTF_8;

	// 数据或脚本文件
	private String resource;

	// 基本信息
	private final ConfigurationEx configuration;

	private final SQLQueryFactory factory;

	private final RelationalPathEx<?> table;

	private final Map<String, Path<?>> pathMap;

	// 选项：表名路由
	private RoutingStrategy routing;

	// 提示：是新建的表（跳过数据检测）
	private boolean isNewTable;

	// 选项：如果资源不存在抛出异常
	private boolean ignoreIfResourceFileNotFound;

	// 选项：数据文件是SQL脚本
	private boolean isSql;

	// 选项：MergeKey
	private List<Path<?>> mergeKeys;

	private boolean enable = true;
	
	private boolean forEmptyTableOnly = false;
	
	private int setPrimaryKeys = -1;
	
	private boolean nullsBind;

	private transient Boolean emptyTable;

	
	public TableDataInitializer(SQLQueryFactory session, RelationalPath<?> table) {
		this.factory = session;
		this.configuration = session.getConfiguration();
		this.table = RelationalPathExImpl.toRelationPathEx(table);
		this.pathMap = table.getColumns().stream().collect(Collectors.toMap(e -> e.getMetadata().getName(), e -> e));
		updateNulls(false);
		initMergeKeys();
	}

	public TableDataInitializer from(String resource) {
		this.resource = resource;
		return this;
	}

	public TableDataInitializer withRouting(RoutingStrategy routing) {
		if (routing != null) {
			this.routing = routing;
		}
		return this;
	}

	/**
	 * Treats the data table as a new table. When set to true, the Merge strategy will be abandoned and only data insertion will be attempted.
	 * <p>
	 * 将数据表视作新表。设置true后会放弃Merge策略，仅尝试数据数据插入。
	 * @param isNew isNew
	 * @return this
	 */
	public TableDataInitializer isNewTable(boolean isNew) {
		this.isNewTable = isNew;
		return this;
	}

	/**
	 * Set the character set of the resource file.
	 * <p>
	 * 设置资源文件的字符集
	 * @param charset charset
	 * @return this
	 */
	public TableDataInitializer charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	public TableDataInitializer ignoreResource(boolean flag) {
		ignoreIfResourceFileNotFound = flag;
		return this;
	}

	/**
	 * By default, NULL fields in the data file are not updated into the data table.
	 * Selecting this option ensures that NULL values are also updated into the
	 * database.
	 * <p>
	 * 对于数据文件中的NULL字段，默认是不更新到数据表内的，选择此选项后可以确保null值也更新到数据库中。
	 * 
	 * @param nullsBind true/false
	 * @return this
	 */
	public TableDataInitializer updateNulls(boolean nullsBind) {
		this.nullsBind = nullsBind;
		return this;
	}

	@SuppressWarnings("unchecked")
	private Mapper<Object> getMapper(){
		boolean isTuple = table.getType() == Tuple.class;
		int type=0;
		if(isTuple) {
			type=type|Mappers.TYPE_TUPLE;
		}
		if(nullsBind) {
			type=type|Mappers.NULLS_BIND;
		}
		boolean ignoreKeys=false;
		if(setPrimaryKeys==-1) {
			PrimaryKey<?> pk=table.getPrimaryKey();
			if(pk!=null && pk.getLocalColumns()!=null){
				int pkColumnSize=pk.getLocalColumns().size();
				if(pkColumnSize==1) {
					//如果主键列仅有一个，且是自增列，那么就跳过
					ColumnMapping key=table.getColumnMetadata(pk.getLocalColumns().get(0));
					Assert.notNull(key);
					if(key.isAutoIncreament()) {
						ignoreKeys = true;
					}
				}	
			}
		}else if(setPrimaryKeys==0){
			ignoreKeys = true;
		}
		if(ignoreKeys) {
			type=type | Mappers.PRIMARKKEY_IGNORED;
		}
		return Mappers.get(0,type);
	}
	
	/**
	 * Specify that the resource file is to be executed as an SQL script.
	 * <p>
	 *  指定资源文件是SQL脚本方式执行
	 *  @return this
	 */
	public TableDataInitializer isSqlFile() {
		isSql = true;
		return this;
	}

	/**
	 * Specify the business key for the Merge operation. If not specified, the primary key will be used for data merging.
	 * <p>
	 *  指定Merge的业务键，如果不指定会使用主键进行数据Merge。
	 *  @param keys business keys. / 业务键
	 *  @return this
	 */
	public TableDataInitializer withKeys(String... keys) {
		initMergeKeys(keys);
		return this;
	}

	/**
	 * Specify the business key for the Merge operation. If not specified, the primary key will be used for data merging.
	 * <p>
	 *  指定Merge的业务键。如果不指定会使用主键进行数据Merge.
	 *  @param keys  business keys./业务键
	 *  @return this
	 */
	public TableDataInitializer withKeys(Path<?>... keys) {
		if (keys.length > 0) {
			this.mergeKeys = Arrays.asList(keys);
		}
		return this;
	}

	public final int execute() {
		if (!enable) {
			return 0;
		}
		if(forEmptyTableOnly) {
			if (!isEmptyTable()) {
				return 0;	
			}
		}
		URL url = getResourceURL();
		if (url == null) {
			return 0;
		}
		int count;
		long time = System.currentTimeMillis();
		String tableName = table.getTableName();
		if (isSql) {
			count = importSQLScript(url);
			log.info("Table [{}] dataInit completed. {} statements executed, time={}.", tableName, count, (System.currentTimeMillis() - time));
		} else {
			try (CloseableSQLQueryFactory session = factory.oneConnectionSession(Connection.TRANSACTION_READ_COMMITTED)) {
				if (isNewTable || mergeKeys.isEmpty() || isEmptyTable()) {
					log.info("Table [{}] already exists, begin merge data into database.", tableName);
					count = importWithInsert(session, url);
				} else {
					log.info("Table [{}] already exists, begin merge data into database.", tableName);
					count = importUsingMerge(session, url);
				}
				log.info("Table [{}] dataInit completed. {} records saved, time={}.", tableName, count, (System.currentTimeMillis() - time));
			}
		}
		return count;
	}

	private int importUsingMerge(SQLQueryFactory session, URL url) {
		int count = 0;
		try (CloseableIterator<Object> reader = new CSVObjectReader(url)) {
			while (reader.hasNext()) {
				count += doMerge(session, reader.next());
			}
		}
		return count;
	}

	private int doMerge(SQLQueryFactory session, Object obj) {
		Mapper<Object> mapper = getMapper();
		SQLQueryAlter<?> select = session.selectFrom(table).withRouting(routing);
		Map<Path<?>, Object> values = mapper.createMap(table, obj);
		SQLTypeUtils.setWhere(mergeKeys, select, values);
		List<?> list = select.limit(2).fetch();
		int size = list.size();
		if (size > 1) {
			// Ignore this record
			log.error("Table {}, key={} has more than one record match. Merge process on this record will be ignored.", table, values);
			return 0;
		} else if (size == 0) {
			return (int) session.insert(table).withRouting(routing).populate(obj, mapper).execute();
		}
		SQLUpdateClauseAlter update = session.update(table).withRouting(routing).populateWithCompare(obj, list.get(0), mapper, false);
		SQLTypeUtils.setWhere(mergeKeys, update, values);
		return (int) update.execute();
	}

	private int importWithInsert(SQLQueryFactory session, URL url) {
		int count = 0;
		try (CloseableIterator<Object> reader = new CSVObjectReader(url)) {
			List<Object> batch = new ArrayList<>(500);
			while (reader.hasNext()) {
				batch.add(reader.next());
				if (batch.size() < 500) {
					continue;
				}
				count += insertBatch(session, batch);
			}
			if (!batch.isEmpty()) {
				count += insertBatch(session, batch);
			}
		}
		return count;
	}

	private int insertBatch(SQLQueryFactory session, List<Object> batch) {
		int count;
		try {
			SQLInsertClauseAlter query = session.insert(table).withRouting(routing).populateBatch(batch);
			count = (int) query.execute();
			log.info("execute batch size={}: return {} ", batch.size(), count);
		} catch (QueryException e) {
			if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
				count = insertOneByOne(session, batch, e);
			} else {
				throw e;
			}
		} catch (DataIntegrityViolationException e) {
			count = insertOneByOne(session, batch, e);
		} finally {
			batch.clear();
		}
		return count;
	}

	private int importSQLScript(URL url) {
		return factory.getMetadataFactory().executeScriptFile(url, charset, false, null);
	}
	
	private boolean isEmptyTable() {
		if(emptyTable==null) {
			List<?> obj = factory.selectFrom(table).limit(1).fetch();
			return emptyTable = obj.isEmpty();	
		}
		return emptyTable;
	}

	private URL getResourceURL() {
		String resource = this.resource;
		resource = calcResourceName(resource, table, configuration);
		// get resource file
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
		List<Path<?>> paths = mergeKey == null ? Collections.emptyList() : Arrays.stream(mergeKey).map(e -> Assert.nonNull(pathMap.get(e), "Field {} not found in class {}", e, table)).collect(Collectors.toList());
		if (paths.isEmpty()) {
			PrimaryKey<?> pk = table.getPrimaryKey();
			paths = pk == null ? Collections.emptyList() : CollectionUtils.cast(pk.getLocalColumns());
		}
		this.mergeKeys = paths;
	}

	/*
	 * Use stream processing to prevent high memory usage caused by large files.
	 */
	class CSVObjectReader implements CloseableIterator<Object>, AutoCloseable {

		private final CsvFileReader reader;

		private final List<Entry<Path<?>, Integer>> props = new ArrayList<Entry<Path<?>, Integer>>();

		private Object next = null;

		public CSVObjectReader(URL url) {
			try {
				this.reader = new CsvFileReader(new InputStreamReader(url.openStream(), charset));
				initHeader();
				readNext();
			} catch (IOException e) {
				throw Exceptions.toRuntime(e);
			}
		}

		private void readNext() throws IOException {
			int totalColumns = table.getColumns().size();
			FactoryExpression<?> expr = (FactoryExpression<?>) table.getProjection();
			if (reader.readRecord()) {
				Object[] values = new Object[totalColumns];
				for (int i = 0; i < props.size(); i++) {
					Entry<Path<?>, Integer> entry = props.get(i);
					Path<?> prop = entry.getKey();
					int index = entry.getValue();
					String value = reader.get(i);
					try {
						values[index] = Codecs.fromString(value, prop.getType());
					} catch (RuntimeException e) {
						throw Exceptions.illegalArgument("decode csv value error, value [{}],type:{},raw={}", value, prop.getMetadata().getName(), reader.getRawRecord(), e);
					}
				}
				next = expr.newInstance(values);
			} else {
				next = null;
			}
		}

		private void initHeader() throws IOException {
			List<Path<?>> paths = table.getColumns();
			if (reader.readHeaders()) {
				for (String header : reader.getHeaders()) {
					if (header.charAt(0) == '[') {
						header = header.substring(1, header.length() - 1);
					}
					Path<?> field = pathMap.get(header);
					if (field == null) {
						throw new IllegalArgumentException(String.format("The field [%s] in CSV file doesn't exists in the entity [%s] metadata.", header, table.getTableName()));
					}
					int pathIndex = paths.indexOf(field);
					props.add(new Entry<>(field, pathIndex));
				}
			}
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Object next() {
			Object result = next;
			try {
				readNext();
			} catch (IOException e) {
				throw Exceptions.toRuntime(e);
			}
			return result;
		}

		@Override
		public void close() {
			reader.close();
		}
	}

	/*
	 * 主键或约束冲突，改为逐条插入
	 */
	protected int insertOneByOne(SQLQueryFactory session, List<Object> data, Exception ex) {
		log.warn("Encountering a data constraints conflicts, will try insert one by one.", ex);
		int count = 0;
		for (Object e : data) {
			session.insert(table).withRouting(routing).populate(e).execute();
			count++;
		}
		return count;
	}

	public TableDataInitializer applyConfig(InitializeData anno) {
		Assert.notNull(anno);
		this.enable = anno.enable();
		this.forEmptyTableOnly = anno.forEmptyTableOnly();
		this.resource = anno.value();
		this.setPrimaryKeys = anno.setPrimaryKeys();
		if (StringUtils.isNotEmpty(anno.charset())) {
			this.charset = Charset.forName(anno.charset());
		}
		this.ignoreIfResourceFileNotFound = !anno.ensureFileExists();
		if (anno.mergeKeys() != null && anno.mergeKeys().length > 0) {
			withKeys(anno.mergeKeys());
		}
		if (StringUtils.isNotEmpty(anno.sqlFile())) {
			this.resource = anno.sqlFile();
			this.isSql = true;
		}
		updateNulls(anno.updateNulls());
		return this;
	}
}
