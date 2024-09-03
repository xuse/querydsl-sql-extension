package com.github.xuse.querydsl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JDBCTest {

	private static Connection conn;

	@BeforeClass
	public static void open() {
		try {
			conn = AbstractTestBase.getEffectiveDs().getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void close() throws SQLException {
		if (conn != null) {
			conn.close();
		}
	}

}
