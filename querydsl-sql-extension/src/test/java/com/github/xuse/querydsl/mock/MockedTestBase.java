package com.github.xuse.querydsl.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.init.DataInitBehavior;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dbmeta.InformationSchemaReader;
import com.github.xuse.querydsl.sql.dbmeta.PartitionInfo;
import com.github.xuse.querydsl.sql.dbmeta.SchemaReader;
import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.dialect.MySQLWithJSONTemplates;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.sql.support.SimpleDataSource;
import com.github.xuse.querydsl.sql.support.UpdateDeleteProtectListener;
import com.github.xuse.querydsl.types.EnumByCodeType;
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

	@BeforeAll
	public static void doInit() {
		SQLTemplates templates = new MySQLWithJSONTemplates() {
			@Override
			public SchemaReader getSchemaAccessor() {
				return new InformationSchemaReader(0) {
					public List<PartitionInfo> getPartitions(String catalog, String schema, String table, ConnectionWrapper conn) {
						return mysqlPartitions(catalog, schema, table, conn);
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
