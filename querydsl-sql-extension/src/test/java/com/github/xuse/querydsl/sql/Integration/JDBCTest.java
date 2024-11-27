package com.github.xuse.querydsl.sql.Integration;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class JDBCTest {

	private static Connection conn;

	@BeforeAll
	public static void open() {
		try {
			conn = AbstractTestBase.getEffectiveDs().getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@AfterAll
	public static void close() throws SQLException {
		if (conn != null) {
			conn.close();
		}
	}

}
