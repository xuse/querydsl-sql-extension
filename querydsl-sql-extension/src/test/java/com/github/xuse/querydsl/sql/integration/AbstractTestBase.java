package com.github.xuse.querydsl.sql.integration;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.CustomAnnotation;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.init.DataInitBehavior;
import com.github.xuse.querydsl.sql.RelationalPathEx;
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
		dsMySQL.setUrl(appendUrlParams("jdbc:mysql://10.86.16.12:3306/test?useSSL=false"));
		dsMySQL.setUsername(System.getProperty("mysql.user"));
		dsMySQL.setPassword(System.getProperty("mysql.password"));
		
		String host="localhost";
		dsMySQL8.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dsMySQL8.setUrl(appendUrlParams("jdbc:mysql://"+host+":3306/mysql?allowPublicKeyRetrieval=true"));
		dsMySQL8.setUsername("root");
		dsMySQL8.setPassword(testPws.replace("_", ""));
		
		
		dsPg14.setDriverClassName("org.postgresql.Driver");
		dsPg14.setUrl("jdbc:postgresql://"+host+":5432/test");
		dsPg14.setUsername("postgres");
		dsPg14.setPassword(testPws.replace("_", ""));
		
		dsH2.setDriverClass("org.h2.Driver");
		dsH2.setUrl("jdbc:h2:~/h2test");
	}

	/**
	 * The active datasource, resolved dynamically from the {@code test.db} system property so the
	 * suite can target different databases at runtime, e.g. {@code -Dtest.db=mysql8}. Defaults to
	 * H2 to keep the out-of-the-box behavior unchanged.
	 * <p>
	 * 通过 {@code test.db} 系统属性动态选择数据源，从而在运行时用参数切换目标数据库，如
	 * {@code -Dtest.db=mysql8}。缺省为 H2，保持默认行为不变。
	 */
	private static final SimpleDataSource effectiveDs = resolveEffectiveDs();

	private static SimpleDataSource resolveEffectiveDs() {
		String db = System.getProperty("test.db", "h2").trim().toLowerCase();
		switch (db) {
		case "mysql":
			return dsMySQL;
		case "mysql8":
			return dsMySQL8;
		case "pg":
		case "postgres":
		case "postgresql":
			return dsPg14;
		case "derby":
			return dsDerby;
		case "h2":
			return dsH2;
		default:
			throw new IllegalArgumentException("Unknown test.db value: " + db
					+ ". Supported: h2, mysql, mysql8, pg, derby");
		}
	}

	/**
	 * Append extra JDBC URL parameters supplied via the {@code test.db.urlParams} system property,
	 * e.g. {@code -Dtest.db.urlParams=rewriteBatchedStatements=true&useServerPrepStmts=false}. This
	 * lets tests toggle driver behavior (such as batch statement rewriting) without code changes.
	 * <p>
	 * 通过 {@code test.db.urlParams} 系统属性追加额外的 JDBC URL 参数，例如
	 * {@code -Dtest.db.urlParams=rewriteBatchedStatements=true}，从而在不改代码的情况下切换驱动行为
	 * （如批量语句改写）。
	 */
	private static String appendUrlParams(String url) {
		String extra = System.getProperty("test.db.urlParams");
		if (extra == null || extra.trim().isEmpty()) {
			return url;
		}
		String sep = url.indexOf('?') >= 0 ? "&" : "?";
		return url + sep + extra.trim();
	}

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

	/**
	 * Ensure the given entity table exists before a test uses it, then truncate it.
	 * <p>
	 * The test suite shares a single static {@link #factory} across all test classes, and some
	 * DDL-oriented tests drop shared tables (e.g. {@code ca_foo}) without recreating them. Tests
	 * that only {@code truncate} a table therefore fail with "table not found" depending on
	 * execution order. Creating with {@code IF NOT EXISTS} first makes such tests order-independent.
	 * <p>
	 * 测试套件共享同一个静态 factory，部分 DDL 测试会 drop 共享表且不重建，导致仅 truncate 的测试
	 * 因执行顺序不同而报“表不存在”。此方法先 CREATE TABLE IF NOT EXISTS 再 truncate，消除顺序依赖。
	 *
	 * @param table the entity table to ensure and truncate
	 */
	protected static void ensureAndTruncate(com.github.xuse.querydsl.lambda.LambdaTable<?> table) {
		factory.getMetadataFactory().createTable(table).ifExists().execute();
		factory.getMetadataFactory().truncate(table).execute();
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
			.addListener(AbstractTestBase::onScaned)
			.setDataInitBehavior(DataInitBehavior.NONE);
		configuration.scanPackages("com.github.xuse.querydsl.entity");
		return configuration;
	}
	
	private static void onScaned(RelationalPathEx<?> entityPath) {
		System.out.println("[REG LISTENER]:"+entityPath.getType());
	}
}
