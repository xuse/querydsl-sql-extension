package com.github.xuse.querydsl.sql.dialect;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.dbmeta.MetadataQuerySupport;
import com.github.xuse.querydsl.sql.ddl.ConnectionWrapper;
import com.github.xuse.querydsl.sql.support.DbDistributedLockProvider;
import com.github.xuse.querydsl.sql.support.DistributedLock;
import com.github.xuse.querydsl.sql.support.DistributedLockProvider;
import com.github.xuse.querydsl.util.StringUtils;
import com.querydsl.sql.SQLBindings;

import lombok.extern.slf4j.Slf4j;

/**
 * MySQL-specific privilege detector using {@code SHOW GRANTS} command.
 * <p>
 * Parses all grant rows returned by MySQL to determine if the current user
 * has the required DDL privileges (CREATE, ALTER, DROP, INDEX, etc.).
 * 
 * @author Joey
 */
@Slf4j
public class MySQLPrivilegeDetector implements PrivilegeDetector {

	@Override
	public boolean check(SQLQueryFactory connection, Privilege... privileges) {
		ConfigurationEx config = connection.getConfiguration();
		MetadataQuerySupport metadataQuery = new MetadataQuerySupport(config) {
			@Override
			protected ConfigurationEx getConfiguration() {
				return connection.getConfiguration();
			}

			@Override
			public Connection getConnection() {
				return connection.getConnection();
			}

			@Override
			public DistributedLock getLock(String lockName) {
				DistributedLockProvider provider = connection.getConfiguration()
						.computeLockProvider(() -> DbDistributedLockProvider.create(connection));
				if (provider == null) {
					throw new IllegalStateException("There is no distributed-lock provider available.");
				}
				return provider.getLock(lockName, 3);
			}
		};
		Set<String> ownedPrivileges = metadataQuery.doSQLQuery(this::getAllPrivileges, "getPrivileges");
		log.info("Own privileges: {}", ownedPrivileges);
		if (ownedPrivileges.contains("ALL PRIVILEGES") || ownedPrivileges.contains("ALL")) {
			return true;
		}
		Set<String> required = Arrays.stream(privileges).map(this::toMySQLName).collect(Collectors.toSet());
		return ownedPrivileges.containsAll(required);
	}

	private String toMySQLName(Privilege p) {
		return p.name().replace('_', ' ');
	}

	/**
	 * Parse all rows from SHOW GRANTS and collect all privilege names.
	 * Each row has format: GRANT priv1, priv2, ... ON scope TO user
	 */
	private Set<String> getAllPrivileges(ConnectionWrapper q) {
		SQLBindings sql = new SQLBindings("SHOW GRANTS", Collections.emptyList());
		List<String> grantRows = q.query(sql, rs -> rs.getString(1));
		Set<String> privileges = new HashSet<>();
		for (String row : grantRows) {
			if (row == null) {
				continue;
			}
			String privPart = StringUtils.substringBetween(row, "GRANT ", " ON ");
			if (StringUtils.isEmpty(privPart)) {
				continue;
			}
			String[] parts = StringUtils.split(privPart, ",");
			if (parts != null) {
				for (String part : parts) {
					String trimmed = part.trim();
					if (StringUtils.isNotEmpty(trimmed)) {
						privileges.add(trimmed);
					}
				}
			}
		}
		return privileges;
	}
}
