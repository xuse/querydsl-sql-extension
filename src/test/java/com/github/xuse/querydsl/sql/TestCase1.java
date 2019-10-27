package com.github.xuse.querydsl.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.AvsUserAuthority;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.entity.QAvsUserAuthority;
import com.github.xuse.querydsl.enums.Gender;
import com.github.xuse.querydsl.enums.TaskStatus;
import com.github.xuse.querydsl.sql.expression.Streams;
import com.github.xuse.querydsl.sql.log.QueryDSLDebugListener;
import com.github.xuse.querydsl.sql.log.QueryDSLSQLListener;
import com.github.xuse.querydsl.types.EnumByCodeType;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.UpdateDeleteProtectListener;

public class TestCase1 {

	static String s1 = "r-o-o-t";
	static String s2 = "admin";
	private DriverManagerDataSource dsDerby = new DriverManagerDataSource();
	private DriverManagerDataSource dsMySQL = new DriverManagerDataSource();

	static {
		System.setProperty("mysql.user", s1.replace("-", ""));
		System.setProperty("mysql.password", s2.replace("-", ""));
		System.out.println(System.getProperty("mysql.password"));
	}

	private SQLQueryFactory factory;
	{
		dsDerby.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		dsDerby.setUrl("jdbc:derby:db;create=true");

		dsMySQL.setDriverClassName("com.mysql.jdbc.Driver");
		dsMySQL.setUrl("jdbc:mysql://localhost:3306/test?useSSL=false");
		dsMySQL.setUsername(System.getProperty("mysql.user"));
		dsMySQL.setPassword(System.getProperty("mysql.password"));
//		try {
//			dsMySQL.getConnection().close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
	{
		DriverManagerDataSource ds = dsDerby;
		factory = new SQLQueryFactory(querydslConfiguration(SQLQueryFactory.calcSQLTemplate(ds.getUrl())), ds, true);
	}

	public ConfigurationEx querydslConfiguration(SQLTemplates templates) {
		ConfigurationEx configuration = new ConfigurationEx(templates);
		configuration.setSlowSqlWarnMillis(4000);
		configuration.addListener(new QueryDSLSQLListener());
		configuration.addListener(new UpdateDeleteProtectListener());

		// 枚举自动注册功能
		// JSON自动转换功能
		//
	//	configuration.register(new EnumByCodeType<>(Gender.class));
		configuration.register(new EnumByCodeType<>(TaskStatus.class));

		// 如果使用了自定义映射，需要提前注册
		configuration.registerExType(QAvsUserAuthority.avsUserAuthority);
		return configuration;
	}

	@Test
	public void reCreateTable() throws SQLException {

		Connection conn = factory.getConnection();

		try (Statement st = conn.createStatement()) {
			try {
				st.executeUpdate("drop table aaa");
			} catch (Exception e) {
			}
			try {
				st.executeUpdate("drop table avs_user_authority");
			} catch (Exception e) {
			}

			if (factory.getConfiguration().getTemplates() instanceof MySQLTemplates) {
				st.executeUpdate(
						"create table aaa(id int AUTO_INCREMENT not null, name varchar(64), created timestamp, gender int, task_status int, version int,"
								+ "PRIMARY KEY (id))");
				st.executeUpdate("CREATE TABLE avs_user_authority (\r\n" + " id int not null AUTO_INCREMENT,\r\n"
						+ " user_id varchar(64) NOT NULL DEFAULT '',\r\n" + " dev_id varchar(64) DEFAULT NULL,\r\n"
						+ " channel_no int DEFAULT 0,\r\n" + "  auth_type int,\r\n" + "  auth_content varchar(256),\r\n"
						+ "  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\r\n"
						+ "  update_time timestamp NOT NULL,\r\n" + "  gender varchar(64),\r\n"
						+ "  map_data varchar(512),\r\n" + "  PRIMARY KEY (id)\r\n" + ")");
			} else {
				st.executeUpdate(
						"create table aaa(id int generated by default as identity  ( START WITH 1,INCREMENT BY 1), "
								+ "name varchar(64), " + "created timestamp, " + "gender int," + "version int,"
								+ "task_status int," + "PRIMARY KEY (id))");
				st.executeUpdate("CREATE TABLE avs_user_authority (\r\n"
						+ " id int not null generated by default as identity,\r\n"
						+ " user_id varchar(64) NOT NULL DEFAULT '',\r\n" + " dev_id varchar(64) DEFAULT NULL,\r\n"
						+ " channel_no int DEFAULT 0,\r\n" + "  auth_type int,\r\n" + "  auth_content varchar(256),\r\n"
						+ "  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\r\n"
						+ "  update_time timestamp NOT NULL,\r\n" + "  gender varchar(64),\r\n"
						+ "  map_data varchar(512),\r\n" + "  PRIMARY KEY (id)\r\n" + ")");
			}

		}
		conn.close();
	}

	@Test
	public void testSelect() {
		QAaa t1 = QAaa.aaa;
		List<Aaa> list = factory.selectFrom(t1).fetch();
		for (Aaa aa : list) {
			System.out.println(aa);
		}
	}

	@Test
	public void test1() {
		QAaa t1 = QAaa.aaa;
		Aaa a = new Aaa();
//		a.setId(1);
		a.setName("张三");
		a.setGender(Gender.FEMALE);
		a.setTaskStatus(TaskStatus.INIT);
		a.setCreated(new Timestamp(System.currentTimeMillis()));
		Integer id = factory.insert(t1).populate(a).executeWithKey(Integer.class);

		System.out.println("===========查询t1===========");

		Aaa b = factory.selectFrom(t1).where(t1.id.eq(id)).fetchFirst();
		System.out.println(b);

		System.out.println("===========更新t1===========");
		factory.update(t1).set(t1.taskStatus, TaskStatus.FAIL).set(t1.version, t1.version.add(Expressions.ONE))
				.where(t1.id.eq(id).and(t1.version.eq(b.getVersion()))).execute();

		System.out.println("===========查询t1===========");
		b = factory.selectFrom(t1).where(
				t1.taskStatus.in(Arrays.asList(TaskStatus.FAIL, TaskStatus.INIT)).and(t1.gender.eq(Gender.FEMALE)))
				.fetchFirst();
		System.out.println(b);

		System.out.println("===========插入t2===========");
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		int sid = factory.insert(t2).set(t2.authContent, "abcdefg").set(t2.devId, "123")
				.set(t2.updateTime, "01/12/2019 12:30:21").set(t2.gender, Gender.MALE)
				.executeWithKey(t2.id);
		System.out.println(id);
		long count = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchCount();
		System.out.println("Count:" + count);
		List<String> entity = factory.select(Streams.map(t2.getProjection(), String.class, e -> e.getDevId())).from(t2)
				.where(t2.id.eq(sid)).fetch();

		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchOne();
		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchFirst();
		System.out.println(entity);

		factory.update(t1).set(t1.name, t1.name.concat("Abc123")).where(t1.id.eq(id)).execute();

		factory.update(t2).set(t2.createTime, DateTimeExpression.currentTimestamp(Timestamp.class))
				.where(t2.userId.eq("1")).execute();
		// factory.select(Projections.tuple(exprs))
	}

	@Test
	public void test3() {
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		List<AvsUserAuthority> eee = factory.selectFrom(t2).fetch();
		System.out.println(eee);

		QueryResults<AvsUserAuthority> results = factory.selectFrom(t2).fetchResults();
		System.out.println(results.getLimit() + "," + results.getOffset() + "," + results.getTotal());
		System.out.println(results.getResults());
	}

	@Test
	public void testUpdateSQL() {
		QAaa t1 = QAaa.aaa;
		factory.update(t1).set(t1.created, Expressions.currentTimestamp()).set(t1.name, "李四").where(t1.id.eq(1))
				.execute();
	}

	@Test
	public void testUpdateAll() {
		QAaa t1 = QAaa.aaa;
		factory.update(t1).set(t1.created, Expressions.currentTimestamp()).set(t1.name, "李四").execute();
	}

	@Test
	public void testDeleteAll() {
		QAaa t1 = QAaa.aaa;
		factory.delete(t1).where(Expressions.TRUE).execute();
	}

	@Test
	public void testComplexType() {
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;

		AvsUserAuthority data = new AvsUserAuthority();
		data.setUserId("user-daslfnskfn23");
		data.setDevId("C12345678");
		data.setAuthContent("abcdefg");
		data.setUpdateTime("01/12/2019 13:30:21");
		data.setGender(Gender.MALE);
		data.setMap(new HashMap<>());
		data.getMap().put("attr1", "测试属性");
		data.getMap().put("attr2", "男");
		// 插入
		int sid = factory.insert(t2).populate(data).executeWithKey(t2.id);

		// 查询数量
		long count = factory.selectFrom(t2).where(t2.id.eq(sid)).fetchCount();
		System.out.println("Count:" + count);

		// 查询数据，并支持流操作 (试验性功能，API还需要改进)
		List<String> entity = factory.select(t2.getProjection().map(e -> e.getDevId(), String.class)).from(t2)
				.where(t2.id.eq(sid)).fetch();
		System.out.println(entity);

		// 查询数据，获取一条
		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchOne();

		// 查询数据，获取第一个
		factory.selectFrom(t2).where(t2.id.eq(sid)).fetchFirst();

		// 更新数据
		factory.update(t2).set(t2.createTime, DateTimeExpression.currentTimestamp(Timestamp.class))
				.set(t2.map, Collections.singletonMap("TEST", "UPDATE")).where(t2.userId.eq(data.getUserId()))
				.execute();
	}

	@Test
	public void testFetchAll() {
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;
		List<AvsUserAuthority> list = factory.selectFrom(t2).fetch();
		for (AvsUserAuthority a : list) {
			System.out.println(a);
		}
	}

	@Test
	public void testInsert(){
		QAvsUserAuthority t2 = QAvsUserAuthority.avsUserAuthority;

		AvsUserAuthority data = new AvsUserAuthority();
		data.setUserId("user-daslfnskfn23");
		data.setDevId("C12345678");
		data.setAuthContent("abcdefg");
		data.setUpdateTime("01/12/2019 13:30:21");
		data.setGender(Gender.MALE);
		data.setMap(new HashMap<>());
		data.getMap().put("attr1", "测试属性");
		data.getMap().put("attr2", "女");
		// 插入

		factory.insert(t2).populate(data).execute();
		int sid = factory.insert(t2).populate(data).executeWithKey(t2.id);
		System.out.println(sid);

	}

	@After
	public void closeDerby() {
		// Derby数据库在嵌入模式运行时，最后一定要关闭。
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
		}
	}

	@Test
	public void testMerge() {
		QAaa t1 = QAaa.aaa;
		Aaa a = new Aaa();
//		a.setId(1);
		a.setName("张222");
		a.setGender(Gender.FEMALE);
		a.setCreated(new Timestamp(System.currentTimeMillis()));
		int id = factory.insert(t1).populate(a).executeWithKey(Integer.class);
		System.out.println("id=" + id);

		Integer count = factory.merge(t1).keys(t1.id).columns(t1.id, t1.created, t1.gender)
				.values(999, new Date(), Gender.MALE).executeWithKey(t1.id);
		System.out.println("返回:" + count);

	}

}
