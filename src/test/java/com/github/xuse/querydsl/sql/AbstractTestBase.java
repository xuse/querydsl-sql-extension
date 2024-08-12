package com.github.xuse.querydsl.sql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.init.DataInitBehavior;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.sql.spring.SpringExceptionTranslator;
import com.github.xuse.querydsl.sql.support.UpdateDeleteProtectListener;
import com.github.xuse.querydsl.types.EnumByCodeType;
import com.querydsl.sql.SQLTemplates;

public abstract class AbstractTestBase {

	static String s1 = "r-o-o-t";
	static String s2 = "pp-07-59-12";
	private static DriverManagerDataSource dsDerby = new DriverManagerDataSource();
	private static DriverManagerDataSource dsMySQL = new DriverManagerDataSource();
	

	static {
		System.setProperty("mysql.user", s1.replace("-", ""));
		System.setProperty("mysql.password", s2.replace("-", ""));

		dsDerby.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		dsDerby.setUrl("jdbc:derby:db;create=true");

		dsMySQL.setDriverClassName("com.mysql.jdbc.Driver");
		dsMySQL.setUrl("jdbc:mysql://10.86.15.203:3306/test?useSSL=false");
		//dsMySQL.setUrl("jdbc:mysql://10.86.16.12:3306/test3?useSSL=false");
		
		
		dsMySQL.setUsername(System.getProperty("mysql.user"));
		dsMySQL.setPassword(System.getProperty("mysql.password"));
	}

	private static final DriverManagerDataSource effectiveDs = dsDerby;
	
	
	public static  DriverManagerDataSource getEffectiveDs() {
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
	@AfterClass 
	public static void closeDerby() {
//		// Derby数据库在嵌入模式运行时，最后一定要关闭。
//		try {
//			DriverManager.getConnection("jdbc:derby:;shutdown=true");
//			DriverManager.registerDriver(new org.apache.derby.jdbc.AutoloadedDriver());
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
	
	@BeforeClass
	public static void doInit() {
		try {
			factory = new SQLQueryFactory(querydslConfiguration(SQLQueryFactory.calcSQLTemplate(effectiveDs.getUrl())),
					effectiveDs, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static ConfigurationEx querydslConfiguration(SQLTemplates templates) {
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.setSlowSqlWarnMillis(4000);
		configuration.addListener(new QueryDSLSQLListener(QueryDSLSQLListener.FORMAT_DEBUG));
		configuration.addListener(new UpdateDeleteProtectListener());
		configuration.register(new EnumByCodeType<>(Gender.class));
		configuration.register(new EnumByCodeType<>(TaskStatus.class));
		configuration.setExceptionTranslator(new SpringExceptionTranslator());
		// 如果使用了自定义映射，需要提前注册，或者扫描指定包
		configuration.allowTableDropAndCreate();
		configuration.getScanOptions()
		.setAlterExistTable(false)
		.setDataInitBehavior(DataInitBehavior.NONE);
		configuration.scanPackages("com.github.xuse.querydsl.entity");
		return configuration;
	}

}
