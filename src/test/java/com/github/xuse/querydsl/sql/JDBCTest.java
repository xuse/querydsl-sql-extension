package com.github.xuse.querydsl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.xuse.querydsl.sql.support.SQLTypeUtils;

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

	@Test
	public void test1() throws SQLException {
		try(PreparedStatement st = conn.prepareStatement("insert into test1 (name) values (?)",
				PreparedStatement.RETURN_GENERATED_KEYS)){
			st.setString(1, "bbb");
			st.addBatch();
			st.setString(1, "ccc");
			st.addBatch();
			st.setString(1, "ddd");
			st.addBatch();
			int[] ints=st.executeBatch();
			System.out.println(Arrays.toString(ints));
			
			try(ResultSet rs=st.getGeneratedKeys()){
				String s=SQLTypeUtils.toString(rs);
				System.out.println(s);
			}
		};
	}
	
	@Test
	public void test2() throws SQLException{
		String sql="insert into aaa (name, created, gender, task_status, version, gender2, c_int, c_float, c_double, c_short, c_bigint, c_decimal, c_bool, c_bit, c_date, c_time, c_timestamp, c_text, c_longtext, c_bin, c_varbin)\r\n"
				+ "values (?, ?, DEFAULT, ?, ?, ?, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT, ?, ?, DEFAULT, DEFAULT, DEFAULT, ?, ?, ?, ?)";
		try(PreparedStatement st = conn.prepareStatement(sql,
				PreparedStatement.RETURN_GENERATED_KEYS)){
			st.setString(1, "王五");
			st.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
			st.setInt(3, 1);
			st.setInt(4, 13);
			st.setNull(5, Types.CHAR);
			st.setBoolean(6, false);
			st.setBoolean(7, false);
			st.setNull(8, Types.VARCHAR);
			st.setNull(9, Types.CLOB);
			st.setNull(10, Types.BINARY);
			st.setNull(11, Types.BINARY);
			st.addBatch();
			
			
			st.setString(1, "dcfsdsf");
			st.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
			st.setInt(3, 1);
			st.setInt(4, 14);
			st.setNull(5, Types.CHAR);
			st.setBoolean(6, false);
			st.setBoolean(7, false);
			st.setNull(8, Types.VARCHAR);
			st.setNull(9, Types.CLOB);
			st.setNull(10, Types.BINARY);
			st.setNull(11, Types.BINARY);
			st.addBatch();
			
			st.setString(1, "ccccccc");
			st.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
			st.setInt(3, 1);
			st.setInt(4, 15);
			st.setNull(5, Types.CHAR);
			st.setBoolean(6, false);
			st.setBoolean(7, false);
			st.setNull(8, Types.VARCHAR);
			st.setNull(9, Types.CLOB);
			st.setNull(10, Types.BINARY);
			st.setNull(11, Types.BINARY);
			st.addBatch();
			
			System.out.println("-----");
			
			int[] ints=st.executeBatch();
			System.out.println(Arrays.toString(ints));
			try(ResultSet rs=st.getGeneratedKeys()){
				String s=SQLTypeUtils.toString(rs);
				System.out.println(s);
			}
		};
	
	
	}
	

	@AfterClass
	public static void close() throws SQLException {
		if (conn != null) {
			conn.close();
		}
	}

}
