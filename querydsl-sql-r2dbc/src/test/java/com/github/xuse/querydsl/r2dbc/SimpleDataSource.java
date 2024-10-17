package com.github.xuse.querydsl.r2dbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * 只有SimpleDataSource才支持带properties参数的getConnection()方法。从而获得Oracle的列注释
 * 
 * @author Administrator
 * 
 */
@Slf4j
@Setter
@Getter
public final class SimpleDataSource  implements DataSource {
	private String driverClass;
	
	private String url;

	private String username;

	private String password;

	@Override
	public String toString() {
		return url+":"+username;
	}

	public SimpleDataSource() {
	};

	public SimpleDataSource(String url,String user,String password) {
		this.url=url;
		this.username=user;
		this.password=password;
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return getConnectionFromDriver(new Properties());
	}
	
	public Connection getConnection(String username, String password) throws SQLException {
		initDriver();
		return getConnectionFromDriver(new Properties());
	}

	public Connection getConnectionFromDriver(Properties props) throws SQLException {
		initDriver();
		if(username!=null) {
			props.put("user", username);
		}
		if(password!=null) {
			props.put("password", password);
		}
		Connection conn= DriverManager.getConnection(url, props);
		return conn;
	}
	

	public void setUrl(String url) {
		this.url=url;
	}

	@SneakyThrows
	private void initDriver() {
		Class.forName(driverClass);
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClassName(String driverClass) {
		this.driverClass = driverClass;
	}

	
	private PrintWriter logWriter;

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return logWriter;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.logWriter=out;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

}
