package com.github.xuse.querydsl.mock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterPartitionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableContext;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.mysql.visitor.format.MySQLFormatVisitor;
import org.h2.Driver;

import com.github.xuse.querydsl.sql.SimpleDataSource;

/**
 * This is a virtual JDBC driver designed to support database operations using
 * MySQL syntax, with an underlying in-memory database.
 * 用于单元测试的虚拟JDBC驱动。在执行单元测试时，无需真实的MySQL数据库。
 * @author Joey
 *
 */
public class MockMySQLDriver implements java.sql.Driver, ResultCallback {
	public final static String DEFAULT_PREFIX = "jdbc:mysql:";

	private static SimpleDataSource datasource;
	private String acceptPrefix = DEFAULT_PREFIX;

	private static MockMySQLDriver INSTANCE;

	public Function<String, String> sqlRewriter = SQLMock.getInstance()::process;

	public static MockMySQLDriver getInstance() {
		if (INSTANCE == null) {
			return INSTANCE = new MockMySQLDriver();
		}
		return INSTANCE;
	}

	public static void prepare(String url, String user, String pass, String driver) {
		datasource = new SimpleDataSource();
		datasource.setUrl(url);
		datasource.setUsername(user);
		datasource.setPassword(pass);
		datasource.setDriverClass(driver);
		Driver h2 = Driver.load();
		try {
			java.sql.DriverManager.registerDriver(getInstance());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Connection connect(String url, Properties info) throws SQLException {
		if (!acceptsURL(url)) {
			return null;
		}
		return new MockedConnection(DriverManager.getConnection(datasource.getUrl()), this);
	}

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		if (url == null) {
			return false;
		}
		return url.startsWith(acceptPrefix);
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
		Driver d = Driver.load();
		return d.getPropertyInfo(url, info);
	}

	@Override
	public int getMajorVersion() {
		return 5;
	}

	@Override
	public int getMinorVersion() {
		return 7;
	}

	@Override
	public boolean jdbcCompliant() {
		return true;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

	// "#PartitionEmu#table#schema"
	@Override
	public ResultSet get(String key, Map<String, Object> map) {
		switch (key) {
		case "PartitionEmu":
			return PartitionDataEmu.getInstance().get(map);
		default:
			throw new UnsupportedOperationException();
		}
	}

	static final class SQLMock {
		private static final SQLMock INSTANCE = new SQLMock();

		private Properties propDDL = new Properties();

		protected final Map<Class<?>, Consumer<ParserRuleContext>> map = new HashMap<>();

		final SQLParserEngine parser = new SQLParserEngine("mysql", new CacheOption(100, 100));

		private SQLRewriter v = new SQLRewriter();

		public SQLMock() {
			propDDL.setProperty("parameterized", "false");
			registe(CreateTableContext.class, v::process);
			registe(AlterStatementContext.class, v::process);
//			registe(AlterTableContext.class, v::process);
			// registe(AlterPartitionContext.class, v::process);
		}

		public static SQLMock getInstance() {
			return INSTANCE;
		}

		public String process(String sql) {
			if (sql.startsWith("SELECT * FROM information_schema.partitions")) {
				// 特殊查询
				return "#PartitionEmu#table#schema";
			}
			String rawSql = sql;
			ParseASTNode node = parser.parse(sql, false);
			ParseTree tree = node.getRootNode();
			Consumer<ParserRuleContext> c = map.get(tree.getClass());
			if (c == null) {
				System.out.println("[" + tree.getClass() + "] has no consumer");
				return sql;
			}
			c.accept((ParserRuleContext) tree);
			if (tree.getChildCount() == 0) {
				return show(rawSql, null);
			}
			MySQLFormatVisitor formatter = new MySQLFormatVisitor();
			formatter.init(propDDL);
			return show(rawSql, tree.accept(formatter));
		}

		private String show(String raw, String sql) {
			System.err.println("拦截前SQL");
			System.err.println(raw);
			if (sql != null) {
				System.err.println("拦截后SQL");
				System.err.println(sql);
			} else {
				System.err.println("拦截后: 不再执行");
			}
			return sql;
		}

		@SuppressWarnings("unchecked")
		public <T extends ParserRuleContext> void registe(Class<T> class1, Consumer<T> vvv) {
			map.put(class1, (Consumer<ParserRuleContext>) vvv);
		}
	}

}
