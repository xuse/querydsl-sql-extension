package com.github.xuse.querydsl.init;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.github.xuse.querydsl.sql.RelationalPathBaseEx;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.column.ColumnMapping;
import com.github.xuse.querydsl.sql.dml.SQLMergeClauseAlter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataInitializer {
	private SQLQueryFactory session;
	private volatile boolean enable = true;

	private int tableInit;
	private int recordInit;
	private Charset globalCharset = StandardCharsets.UTF_8;

	public DataInitializer(SQLQueryFactory session, Charset charset) {
		this.session = session;
		if (charset != null)
			this.globalCharset = charset;
	}

	private List<Object> readData(RelationalPathBaseEx<?> meta, URL url, String charset)
			throws UnsupportedEncodingException, IOException {
		CsvReader reader = new CsvReader(new InputStreamReader(url.openStream(), charset));
		try {
			// 根据Header分析Property
			List<ColumnMapping> props = new ArrayList<ColumnMapping>();
			if (reader.readHeaders()) {
				for (String header : reader.getHeaders()) {
					if (header.charAt(0) == '[') {
						header = header.substring(1, header.length() - 1);
					}
					ColumnMapping field = meta.getColumnMetadata(meta.getColumn(header));
					if (field == null) {
						throw new IllegalArgumentException(
								String.format("The field [%s] in CSV file doesn't exsts in the entity [%s] metadata.",
										header, meta.getTableName()));
					}
					props.add(field);
				}
			}
			// 根据预先分析好的Property读取属性
			List<Object> result = new ArrayList<Object>();
			while (reader.readRecord()) {
				Object[] values = new Object[props.size()];
				for (int i = 0; i < props.size(); i++) {
					ColumnMapping prop = props.get(i);
					if (prop.getCustomType() != null) {
						log.info("有customType:{}", prop.getCustomType());
					}
					values[i] = Codecs.fromString(reader.get(i), prop.getType());
				}
				Object obj = meta.getBeanCodec().newInstance(values);
				result.add(obj);
			}
			return result;
		} finally {
			reader.close();
		}
	}

	private int initData0(RelationalPathBaseEx<?> meta, URL url, String charset, boolean manualSequence)
			throws IOException {
		int count = 0;
		List<Object> data = readData(meta, url, charset);
		for (int i = 0; i < data.size(); i += 500) {
			int batchIndex = Math.min(i + 500, data.size());
//			try {
				doBatchInsert(meta,session, data.subList(i, batchIndex));
				count += (batchIndex - i);
//			} catch (SQLIntegrityConstraintViolationException e1) {
//				// 主键冲突，改为逐条插入
//				count += insertOnebyone(data.subList(i, batchIndex));
//			} catch (SQLException e1) {
//				throw Exceptions.toRuntime(e1);
//			}
		}
		return count;
	}

	private void doBatchInsert(RelationalPathBaseEx<?> meta, SQLQueryFactory session, List<Object> subList) {
		SQLMergeClauseAlter query = session.merge(meta);
		for(Object o:subList) {
			query.populate(o).addBatch();
		}
		long count=query.execute();
		log.info("execute batch size={}: return {} ",subList.size(),count);
	}

	private int insertOnebyone(List<Object> data, RelationalPathBaseEx<?> meta) {
		int count = 0;
		for (Object e : data) {
			session.insert(meta).populate(e).execute();
			count++;
		}
		return count;
	}

//	private int mergeData0(RelationalPathBaseEx<?> meta, URL url, String charset, boolean manualSequence,
//			String[] mergeKey) throws IOException {
//		int count = 0;
//		boolean valueBackup = ORMConfig.getInstance().isManualSequence();
//		// 如果manualSequence和默认配置不同，那么修改后再初始化，完成后改回来
//		if (valueBackup != manualSequence)
//			ORMConfig.getInstance().setManualSequence(manualSequence);
//		try {
//			for (Object e : readData(meta, url, charset)) {
//				try {
//					Object result = session.merge(e, mergeKey);
//					if (result == null || result != e) {
//						count++;
//					}
//				} catch (SQLException e1) {
//					log.error("Insert error:{}", e, e1);
//				}
//			}
//		} finally {
//			if (valueBackup != manualSequence) {
//				ORMConfig.getInstance().setManualSequence(valueBackup);
//			}
//		}
//		return count;
//	}

	public final boolean isEnable() {
		return enable;
	}

	/**
	 * 对外暴露。初始化制定表的数据
	 * 
	 * @param meta  表结构元数据
	 * @param isNew 表是否刚刚创建
	 */
//	public final void initData(RelationalPathBaseEx<?> meta, boolean isNew) {
//		URL res=meta.getClass().getResource("/")
//		
////		
////		String csvResouce = initRoot + meta.getThisType().getName() + extension;
////		boolean ensureResourceExists = false;
////		String charset = this.globalCharset;
////		String tableName = meta.getTableName(false);
////		boolean manualSequence = false;
////		String sqlResouce = "";
////		String[] mergeKeys = null;
//		InitializeData config = meta.getClass().getAnnotation(InitializeData.class);
//		if (config != null) {
//			if (!config.enable()) {
//				log.info("Table [{}] was's disabled on DataInitilalize feature by Annotation @InitializeData", tableName);
//				return;
//			}
//			if (StringUtils.isNotEmpty(config.value())) {
//				csvResouce = config.value();
//			}
//			if (StringUtils.isNotEmpty(config.charset())) {
//				charset = config.charset();
//			}
//			if (config.mergeKeys().length > 0) {
//				mergeKeys = config.mergeKeys();
//			}
//			ensureResourceExists = config.ensureFileExists();
//			manualSequence = config.manualSequence();
//			sqlResouce = config.sqlFile();
//		}
//		if (StringUtils.isEmpty(sqlResouce)) {
//			initCSVData(meta, isNew, csvResouce, manualSequence, ensureResourceExists, charset, mergeKeys);
//		} else {
//			initSqlData(meta, isNew, sqlResouce, ensureResourceExists, charset);
//		}
//	}
//
//	private void initSqlData(RelationalPathBaseEx<?> meta, boolean isNew, String resName, boolean ensureResourceExists,
//			String charset) {
//		String tableName = meta.getTableName(false);
//		URL url = meta.getThisType().getResource(resName);
//		if (url == null) {
//			if (ensureResourceExists) {
//				throw new IllegalStateException("Resource of table [" + tableName + "] was not found:" + resName);
//			}
//			return;
//		}
//		try {
//			session.getMetaData(null).executeScriptFile(url);
//		} catch (SQLException e) {
//			throw DbUtils.toRuntimeException(e);
//		}
//	}
//
//	private void initCSVData(RelationalPathBaseEx<?> meta, boolean isNew, String resName, boolean manualSequence,
//			boolean ensureResourceExists, String charset, String[] mergeKey) {
//		String tableName = meta.getTableName(false);
//		URL url = meta.getThisType().getResource(resName);
//		if (url != null) {
//			try {
//				if (isNew) {
//					log.info("Table [{}] was created just now, begin insert data into database.", tableName);
//					int n = initData0(meta, url, charset, manualSequence);
//					recordInit += n;
//					log.info("Table [{}] dataInit completed. {} records inserted.", tableName, n);
//				} else {
//					log.info("Table [{}] already exists, begin merge data into database.", tableName);
//					int n = mergeData0(meta, url, charset, manualSequence, mergeKey);
//					recordInit += n;
//					log.info("Table [{}] dataInit completed. {} records saved.", tableName, n);
//				}
//				tableInit++;
//
//			} catch (RuntimeException e) {
//				ex = e;
//				throw e;
//			} catch (IOException e) {
//				ex = e;
//				throw new IllegalStateException(e);
//			}
//		} else if (ensureResourceExists) {
//			throw new IllegalStateException("Resource of table [" + tableName + "] was not found:" + resName);
//		} else {
//			log.debug("Data file was not found:{}", resName);
//		}
//
//	}
//
//	/**
//	 * 记录初始化任务结果
//	 */
//	private void recordResult(String message) {
//		if (recordInit > 0) {
//			try {
//				AllowDataInitialize record = session.load(QB.create(AllowDataInitialize.class), false);
//				record.getQuery().setAllRecordsCondition();
//				record.getQuery().prepareUpdate(AllowDataInitialize.Field.doInit, false);
//				record.getQuery().prepareUpdate(AllowDataInitialize.Field.lastDataInitTime, new Date());
//				record.getQuery().prepareUpdate(AllowDataInitialize.Field.lastDataInitUser,
//						ProcessUtil.getPid() + "@" + ProcessUtil.getHostname() + "(" + ProcessUtil.getLocalIp()
//								+ ") OS:" + ProcessUtil.getOSName());
//				record.getQuery().prepareUpdate(AllowDataInitialize.Field.lastDataInitResult,
//						StringUtils.truncate(message, 300));
//				session.update(record);
//			} catch (SQLException e) {
//				log.error("Record DataInitilizer Table failure! please check.", e);
//			}
//		}
//	}
//
//	private Exception ex;
//
//	public void finish() {
//		String message;
//		if (ex == null) {
//			message = "success. Tables init = " + tableInit + ", records = " + recordInit;
//		} else {
//			message = ex.toString();
//		}
//		if (useTable) {
//			recordResult(message);
//		} else {
//			log.info(message);
//		}
//	}
//
//	public String getCharset() {
//		return globalCharset;
//	}
//
//	public void setCharset(String charset) {
//		this.globalCharset = charset;
//	}
}
