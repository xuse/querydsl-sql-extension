package com.github.xuse.querydsl.sql.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.entity.Foo;
import com.github.xuse.querydsl.repository.CRUDRepository;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.integration.AbstractTestBase;
import com.github.xuse.querydsl.sql.support.SimpleDataSource;
import com.querydsl.sql.SQLBaseListener;
import com.querydsl.sql.SQLListenerContext;

/**
 * 回归测试：锁定 executeWithKeys() / executeWithKey() 在执行过程中抛出 RuntimeException 时
 * 仍然释放（归还）数据库连接的行为。
 *
 * <p>背景：自增主键实体的 insert 会走 executeWithKeys 键返回路径。历史 Bug 是该方法只 catch
 * SQLException 且无 finally，一旦执行链上抛出 RuntimeException（例如 Spring 的
 * SpringExceptionTranslator 把约束冲突 SQLException 翻译成 DataIntegrityViolationException
 * 这类 RuntimeException，或 mapper/CustomType/监听器回调抛出 RuntimeException），
 * startContext 借出的连接就永不释放，造成连接池随运行时长单调泄漏。
 *
 * <p>本测试用一个可统计"借出/归还"数量的连接 Supplier 复现该场景：注入一个在插入阶段抛出
 * RuntimeException 的监听器，执行 insert() 后断言 outstanding（未归还）连接数回到 0。
 */
class ExecuteWithKeysLeakTest extends AbstractTestBase {

	/** RuntimeException marker thrown by the injected listener to simulate mapper/translator failures. */
	static class InjectedRuntimeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		InjectedRuntimeException(String m) { super(m); }
	}

	/**
	 * 连接包装：close() 时把 outstanding 计数 -1（幂等，防止重复归还导致计数为负）。
	 */
	static final class CountingConnection extends com.github.xuse.querydsl.sql.ConnectionAdapter {
		private final AtomicInteger outstanding;
		private boolean closed;

		CountingConnection(Connection conn, AtomicInteger outstanding) {
			super(conn);
			this.outstanding = outstanding;
		}

		@Override
		public void close() throws SQLException {
			if (closed) {
				// 重复归还：显式失败，用于捕捉 double-return 回归
				throw new IllegalStateException("connection closed twice (double-return)");
			}
			closed = true;
			try {
				conn.close();
			} finally {
				outstanding.decrementAndGet();
			}
		}
	}

	/**
	 * 统计"当前未归还连接数"。get() 时 +1，被归还的连接 close() 时 -1。
	 * 这模拟了 SpringProvider/UnmanagedConnection 的语义：只有真正 close() 才算归还。
	 */
	static final class CountingConnectionSupplier implements Supplier<Connection> {
		private final DataSource ds;
		final AtomicInteger outstanding = new AtomicInteger();

		CountingConnectionSupplier(DataSource ds) { this.ds = ds; }

		@Override
		public Connection get() {
			try {
				Connection real = ds.getConnection();
				outstanding.incrementAndGet();
				return new CountingConnection(real, outstanding);
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	@BeforeAll
	static void setupTable() {
		doInit();
		try {
			Connection c = factory.getConnection();
			c.createStatement().execute("DROP TABLE IF EXISTS ca_foo");
			c.createStatement().execute(
				"CREATE TABLE IF NOT EXISTS ca_foo (" +
				"id INT AUTO_INCREMENT PRIMARY KEY, " +
				"code VARCHAR(64) NOT NULL DEFAULT '', " +
				"asset_name VARCHAR(128) NOT NULL DEFAULT '', " +
				"content TEXT, created TIMESTAMP, updated TIMESTAMP, gender VARCHAR(16), " +
				"ext VARCHAR(256), map VARCHAR(256), volume INT NOT NULL DEFAULT 0, " +
				"version INT NOT NULL DEFAULT 1, codetype INT NOT NULL DEFAULT 1, " +
				"inday DATE, inday2 TIMESTAMP, inday3 TIMESTAMP(3))");
			c.close();
		} catch (Exception ignore) {
			// table may already exist
		}
	}

	@AfterAll
	static void dropTable() {
		try {
			Connection c = factory.getConnection();
			c.createStatement().execute("DROP TABLE IF EXISTS ca_foo");
			c.close();
		} catch (Exception ignore) {
		}
	}

	/**
	 * 构建一个独立的工厂：使用可统计的连接 Supplier + 一个在 insert 阶段抛 RuntimeException 的监听器，
	 * 并挂上一个"用完即 close"的关闭监听器（模拟 UnmanagedConnectionCloseListener 的释放职责）。
	 */
	private SQLQueryFactory buildFactory(CountingConnectionSupplier supplier, boolean throwOnInsert) {
		SimpleDataSource ds = getEffectiveDs();
		ConfigurationEx configuration = querydslConfiguration(SQLQueryFactory.calcSQLTemplate(ds.getUrl()));
		// 关闭监听器：在 end() 时关闭 CountingConnection —— 即"归还"。
		configuration.addListener(new SQLBaseListener() {
			@Override
			public void end(SQLListenerContext context) {
				Connection conn = context.getConnection();
				if (conn instanceof CountingConnection) {
					try {
						conn.close();
					} catch (SQLException e) {
						throw new IllegalStateException(e);
					}
				}
			}
		});
		if (throwOnInsert) {
			// 注入一个在插入渲染阶段抛 RuntimeException 的监听器，模拟 mapper/translator/自定义监听器异常。
			configuration.addListener(new SQLBaseListener() {
				@Override
				public void notifyInsert(com.querydsl.sql.RelationalPath<?> entity, com.querydsl.core.QueryMetadata md,
						java.util.List<com.querydsl.core.types.Path<?>> columns,
						java.util.List<com.querydsl.core.types.Expression<?>> values,
						com.querydsl.core.types.SubQueryExpression<?> subQuery) {
					throw new InjectedRuntimeException("boom in notifyInsert");
				}
			});
		}
		return new SQLQueryFactory(configuration, supplier);
	}

	@Test
	void insertLeaksNoConnectionWhenRuntimeExceptionThrown() {
		CountingConnectionSupplier supplier = new CountingConnectionSupplier(getEffectiveDs());
		SQLQueryFactory f = buildFactory(supplier, true);
		CRUDRepository<Foo, Integer> repo = f.asRepository(Foo.foo);

		int before = supplier.outstanding.get();
		Foo bean = new Foo();
		bean.setCode("LEAK_RT");
		bean.setName("leak");

		// insert 触发 executeWithKey -> executeWithKeys()，监听器在插入阶段抛 RuntimeException
		assertThrows(RuntimeException.class, () -> repo.insert(bean));

		// 关键断言：即便抛了 RuntimeException，连接也必须被归还（未归还数回到起点）
		assertEquals(before, supplier.outstanding.get(),
				"executeWithKeys 抛 RuntimeException 后连接未归还，存在连接泄漏");
	}

	@Test
	void normalInsertReleasesConnectionExactlyOnce() {
		CountingConnectionSupplier supplier = new CountingConnectionSupplier(getEffectiveDs());
		SQLQueryFactory f = buildFactory(supplier, false);
		CRUDRepository<Foo, Integer> repo = f.asRepository(Foo.foo);

		int before = supplier.outstanding.get();
		Foo bean = new Foo();
		bean.setCode("LEAK_OK_" + System.nanoTime());
		bean.setName("ok");
		repo.insert(bean);

		// 正常路径：连接同样必须归还，且不应出现负数（重复归还）
		assertEquals(before, supplier.outstanding.get(),
				"正常 insert 后连接未归还或重复归还");
		assertTrue(supplier.outstanding.get() >= 0, "连接被重复归还（double-return）");
	}
}
