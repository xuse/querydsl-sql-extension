package com.github.xuse.querydsl.sql.Integration;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.CustomAnnotation;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.init.DataInitBehavior;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.sql.support.SimpleDataSource;
import com.github.xuse.querydsl.sql.support.UpdateDeleteProtectListener;
import com.github.xuse.querydsl.types.EnumByCodeType;
import com.github.xuse.querydsl.util.JefBase64;
import com.querydsl.sql.SQLTemplates;
import com.zaxxer.hikari.HikariDataSource;

public abstract class AbstractTestBase {

	static String s1 = "r-o-o-t";
	static String s2 = "ODgtMDctNTktOTg=";
	static String host="bmJfMy1oel8yMDAxXzczNzc=";
	//static String host="10.25.3.25";
	static String testPws="12_34_5";
	private static SimpleDataSource dsDerby = new SimpleDataSource();
	private static SimpleDataSource dsMySQL = new SimpleDataSource();
	private static SimpleDataSource dsMySQL8 = new SimpleDataSource();
	private static SimpleDataSource dsPg14 = new SimpleDataSource();
	private static SimpleDataSource dsH2 = new SimpleDataSource();
	

	static {
		System.setProperty("mysql.user", s1.replace("-", ""));
		System.setProperty("mysql.password", JefBase64.decodeUTF8(s2).replace("-", ""));

		dsDerby.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		dsDerby.setUrl("jdbc:derby:db;create=true");

		dsMySQL.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dsMySQL.setUrl("jdbc:mysql://10.86.15.203:3306/test?useSSL=false");
		dsMySQL.setUsername(System.getProperty("mysql.user"));
		dsMySQL.setPassword(System.getProperty("mysql.password"));
		
		String host="localhost";
		dsMySQL8.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dsMySQL8.setUrl("jdbc:mysql://"+host+":3306/mysql?allowPublicKeyRetrieval=true");
		dsMySQL8.setUsername("root");
		dsMySQL8.setPassword(testPws.replace("_", ""));
		
		
		dsPg14.setDriverClassName("org.postgresql.Driver");
		dsPg14.setUrl("jdbc:postgresql://"+host+":5432/test");
		dsPg14.setUsername("postgres");
		dsPg14.setPassword(testPws.replace("_", ""));
		
		dsH2.setDriverClass("org.h2.Driver");
		dsH2.setUrl("jdbc:h2:~/h2test");
	}

	private static final SimpleDataSource effectiveDs = dsH2;
	
	
	public static  SimpleDataSource getEffectiveDs() {
		return effectiveDs;
	}
	
	protected static SQLQueryFactory factory;
	
	/**
	 * 关于Derby报错“No suitable driver found for jdbc:derby:db;create=true”的原因
	 * EmbedDriver在类加载（不是实例加载）的时候，会去DriverManager里注册JDBC驱动类。
	 * 在调用jdbc:derby:;shutdown=true时则会去注销驱动类。
	 * 在Junit测试时，每个测试案例都会运行一次创建和注销。但仅在第一次类加载时才会注册驱动类。
	 * 因此，第二个和之后的测试案例就会因无法获得连接而失败。
	 */
	@AfterAll
	public static void closeDerby() {
	}
	
	@BeforeAll
	public static void doInit() {
		if(factory==null) {
			getSqlFactory();			
		}
	}
	
	protected static SQLQueryFactory getSqlFactory() {
		if(factory==null) {
			try {
				return factory = new SQLQueryFactory(querydslConfiguration(SQLQueryFactory.calcSQLTemplate(effectiveDs.getUrl())),
						wrapAsPool(effectiveDs), true);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}	
		}
		return factory;
	}

	private static DataSource wrapAsPool(DataSource ds) {
		HikariDataSource pool = new HikariDataSource();
		pool.setDataSource(ds);
		return pool;
	}
	
	public static ConfigurationEx querydslConfiguration(SQLTemplates templates) {
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.setSlowSqlWarnMillis(4000);
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		configuration.addListener(new UpdateDeleteProtectListener());
		configuration.register(new EnumByCodeType<>(Gender.class));
		configuration.register(new EnumByCodeType<>(TaskStatus.class));
		//configuration.setExceptionTranslator(new SpringExceptionTranslator());
		// 如果使用了自定义映射，需要提前注册，或者扫描指定包
		configuration.allowTableDropAndCreate();
		configuration.getScanOptions()
			.setAlterExistTable(false)
			.allowDrops()
			.withoutAnnotation(CustomAnnotation.class)
			.setDataInitBehavior(DataInitBehavior.NONE);
		configuration.scanPackages("com.github.xuse.querydsl.entity");
		return configuration;
	}
}
