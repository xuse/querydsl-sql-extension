package com.github.xuse.querydsl.sql.ddl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.util.Exceptions;
import com.github.xuse.querydsl.util.function.QueryFunction;
import com.querydsl.sql.SQLBindings;

/**
 * A lightweight JDBC query executor that wraps a single {@link Connection} and provides
 * convenience methods for executing queries, accessing metadata, and mapping results.
 * <p>
 * This class translates {@link SQLException} into framework-specific exceptions via
 * {@link ConfigurationEx}.
 *
 * <h3>Thread Safety</h3>
 * <p>This class is <b>NOT thread-safe</b>. It wraps a single JDBC {@link Connection},
 * which itself is not safe for concurrent use. Instances should be confined to a single
 * thread or protected by external synchronization. Typically, a new instance is created
 * per-operation within the scope of a single database interaction.
 *
 * <h3>Connection Lifecycle</h3>
 * <p>This class does <b>not</b> manage the lifecycle of the underlying connection.
 * It does not open or close the connection — the caller is responsible for providing
 * a valid, open connection and closing it when done.
 *
 * <h3>Relationship with MetadataQuerySupport</h3>
 * <p>{@code ConnectionWrapper} provides low-level SQL execution primitives (query, execute,
 * metadata access), while {@link com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport}
 * is a higher-level abstraction that manages connection acquisition and provides
 * schema-aware metadata operations. {@code MetadataQuerySupport} may internally use
 * {@code ConnectionWrapper} for actual JDBC interactions.
 *
 * @author Joey
 */
public class ConnectionWrapper {
	
	private static final Logger log=LoggerFactory.getLogger("SQL");
	
	private static final int MAX_RECORDS = 50000;
	private final Connection connection;
	private final ConfigurationEx configuration;

	public ConnectionWrapper(Connection connection, ConfigurationEx configuration) {
		this.connection = connection;
		this.configuration = configuration;
	}

	public <T> T querySingle(SQLBindings binding, QueryFunction<ResultSet, T> func) {
		List<T> list=query(binding.getSQL(), binding.getNullFriendlyBindings(), func);
		return list.isEmpty()?null:list.get(0);
	}
	
	public <T> List<T> query(SQLBindings binding, QueryFunction<ResultSet, T> func) {
		return query(binding.getSQL(), binding.getNullFriendlyBindings(), func);
	}

	private <T> List<T> query(String sql, List<Object> nullFriendlyBindings, QueryFunction<ResultSet, T> func) {
		if (nullFriendlyBindings == null) {
			nullFriendlyBindings = Collections.emptyList();
		}
		List<T> result = new ArrayList<>();
		log.debug(sql);
		if(!nullFriendlyBindings.isEmpty()) {
			log.debug("{}",nullFriendlyBindings);
		}
		try (PreparedStatement st = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY)) {
			for (int i = 0; i < nullFriendlyBindings.size(); i++) {
				Object param = nullFriendlyBindings.get(i);
				st.setObject(i + 1, param);
			}
			try (ResultSet resultSet = st.executeQuery()) {
				while (resultSet.next()) {
					if (result.size() < MAX_RECORDS) {
						T t = func.apply(resultSet);
						if (t != null) {
							result.add(t);
						}
					} else {
						throw Exceptions.illegalState("Too many records from database. current is {}", result.size());
					}
				}
			}
		} catch (SQLException e) {
			log.error(sql);
			if(!nullFriendlyBindings.isEmpty()) {
				log.error("{}",nullFriendlyBindings);
			}
			throw configuration.get().translate(e);
		}
		return result;
	}
	
	public int executeQuery(String sql, List<Object> nullFriendlyBindings, Consumer<ResultSet> func){
		if (nullFriendlyBindings == null) {
			nullFriendlyBindings = Collections.emptyList();
		}
		log.info(sql);
		if(!nullFriendlyBindings.isEmpty()) {
			log.info("{}",nullFriendlyBindings);
		}
		int count = 0;
		try (PreparedStatement st = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY)) {
			for (int i = 0; i < nullFriendlyBindings.size(); i++) {
				Object param = nullFriendlyBindings.get(i);
				st.setObject(i + 1, param);
			}
			if(st.execute()) {
				if(func!=null) {
					try (ResultSet resultSet = st.executeQuery()) {
						func.accept(resultSet);
					}	
				}else {
					count = 1;
				}	
			}else{
				count = st.getUpdateCount();
			};
		} catch (SQLException e) {
			throw configuration.get().translate(e);
		}
		return count;
	}

	public <T> T metadataAccess(QueryFunction<DatabaseMetaData,T> callback) {
		try {
			return callback.apply(connection.getMetaData());
		}catch(SQLException e) {
			throw configuration.get().translate(e);
		}
	}
	
	public <T> T connectionAccess(QueryFunction<Connection,T> callback) {
		try {
			return callback.apply(connection);
		}catch(SQLException e) {
			throw configuration.get().translate(e);
		}
	}
	
	public <T> List<T> metadataQuery(QueryFunction<DatabaseMetaData, ResultSet> func,
			QueryFunction<ResultSet, T> resultExtractor) {
		try {
			DatabaseMetaData metadata = connection.getMetaData();
			List<T> result = new ArrayList<>();
			try (ResultSet rs = func.apply(metadata)) {
				while (rs.next()) {
					T t = resultExtractor.apply(rs);
					if (t != null) {
						result.add(t);
					}
				}
			}
			return result;
		} catch (SQLException e) {
			throw configuration.get().translate(e);
		}
	}

	public ConfigurationEx getConfiguration() {
		return configuration;
	}
}
