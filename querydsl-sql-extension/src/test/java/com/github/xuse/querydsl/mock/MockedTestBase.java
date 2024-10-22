package com.github.xuse.querydsl.mock;

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.init.DataInitBehavior;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.Integration.SimpleDataSource;
import com.github.xuse.querydsl.sql.dbmeta.InformationSchemaReader;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.dbmeta.SchemaReader;
import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.ddl.DDLOps.PartitionMethod;
import com.github.xuse.querydsl.sql.dialect.MySQLWithJSONTemplates;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.sql.support.UpdateDeleteProtectListener;
import com.github.xuse.querydsl.types.EnumByCodeType;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.SQLBindings;
import com.querydsl.sql.SQLTemplates;

public class MockedTestBase {
	private static SimpleDataSource dsMock = new SimpleDataSource();
	
	static {
		MockMySQLDriver.prepare("jdbc:h2:~/test2;MODE=MYSQL;DATABASE_TO_LOWER=TRUE", null, null, "org.h2.Driver");
		dsMock.setDriverClassName("com.github.xuse.querydsl.mock.MockMySQLDriver");
		dsMock.setUrl("jdbc:mysql://localhost:3306/test?useSSL=false");
		dsMock.setUsername("mock");
		dsMock.setPassword("mock");
	}


	private static final SimpleDataSource effectiveDs = dsMock;

	public static SimpleDataSource getEffectiveDs() {
		return effectiveDs;
	}

	
	protected static SQLQueryFactory factory;

	@BeforeClass
	public static void doInit() {
		SQLTemplates templates = new MySQLWithJSONTemplates() {
			@Override
			public SchemaReader getSchemaAccessor() {
				return new InformationSchemaReader(0) {
					@Override
					public List<PartitionInfo> getPartitions(String catalog, String schema, String tableName, ConnectionWrapper conn) {
						schema = mergeSchema(catalog,schema);
						if (StringUtils.isEmpty(schema)) {
							schema = "%";
						}
						SQLBindings sql = new SQLBindings(
								"SELECT * FROM information_schema.partitions WHERE table_name=? AND TABLE_SCHEMA LIKE ? ORDER BY PARTITION_ORDINAL_POSITION ASC",
								Arrays.asList(tableName, schema));
						List<PartitionInfo> partitions = conn.query(sql, rs -> {
							PartitionInfo c = new PartitionInfo();
							c.setTableCat(rs.getString("TABLE_CATALOG"));
							c.setTableSchema(rs.getString("TABLE_SCHEMA"));
							c.setTableName(rs.getString("TABLE_NAME"));
							c.setName(rs.getString("PARTITION_NAME"));
							c.setMethod(PartitionMethod.parse(rs.getString("PARTITION_METHOD")));
							c.setCreateTime(rs.getTimestamp("CREATE_TIME"));
							c.setPartitionExpression(rs.getString("PARTITION_EXPRESSION"));
							c.setPartitionOrdinal(rs.getInt("PARTITION_ORDINAL_POSITION"));
							c.setPartitionDescription(rs.getString("PARTITION_DESCRIPTION"));
							return c;
						});
						return partitions;
					}
				};
			}
		};
		factory = new SQLQueryFactory(querydslConfiguration(templates), effectiveDs, true);
	}

	public static ConfigurationEx querydslConfiguration(SQLTemplates templates) {
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.setSlowSqlWarnMillis(4000);
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		configuration.addListener(new UpdateDeleteProtectListener());
		configuration.register(new EnumByCodeType<>(Gender.class));
		configuration.register(new EnumByCodeType<>(TaskStatus.class));
		// configuration.setExceptionTranslator(new SpringExceptionTranslator());
		// 如果使用了自定义映射，需要提前注册，或者扫描指定包
		configuration.allowTableDropAndCreate();
		configuration.getScanOptions()
		
		.setCreateMissingTable(false)
		.setAlterExistTable(false).setDataInitBehavior(DataInitBehavior.NONE);
		configuration.scanPackages("com.github.xuse.querydsl.entity");
		return configuration;
	}
}
