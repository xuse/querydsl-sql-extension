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
import com.querydsl.sql.SQLBindings;

import lombok.extern.slf4j.Slf4j;

/**
 * PostgreSQL-specific privilege detector.
 * <p>
 * Uses PostgreSQL system functions to check if the current user has DDL privileges:
 * <ul>
 *   <li>{@code pg_has_role(current_user, 'rolsuper')} — checks superuser status</li>
 *   <li>{@code has_schema_privilege()} — checks CREATE privilege on current schema</li>
 *   <li>{@code has_table_privilege()} — checks specific table privileges</li>
 * </ul>
 * <p>
 * For DDL operations, the key privilege is CREATE on the current schema.
 * Superusers and schema owners automatically have all DDL privileges.
 * 
 * @author Joey
 */
@Slf4j
public class PostgreSQLPrivilegeDetector implements PrivilegeDetector {

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
		return metadataQuery.doSQLQuery(q -> doCheck(q, privileges), "checkPrivileges");
	}

	private boolean doCheck(ConnectionWrapper q, Privilege[] privileges) {
		// Step 1: Check if current user is superuser
		if (isSuperUser(q)) {
			log.info("PostgreSQL: current user is superuser, all privileges granted.");
			return true;
		}
		// Step 2: Check if user has CREATE privilege on current schema (required for DDL)
		if (hasSchemaCreatePrivilege(q)) {
			log.info("PostgreSQL: current user has CREATE privilege on current schema.");
			return true;
		}
		// Step 3: Check individual privileges if needed
		Set<String> required = Arrays.stream(privileges).map(Privilege::name).collect(Collectors.toSet());
		Set<String> owned = getOwnedPrivileges(q);
		log.info("PostgreSQL owned privileges: {}", owned);
		return owned.containsAll(required);
	}

	private boolean isSuperUser(ConnectionWrapper q) {
		SQLBindings sql = new SQLBindings(
				"SELECT rolsuper FROM pg_roles WHERE rolname = current_user",
				Collections.emptyList());
		Boolean result = q.querySingle(sql, rs -> rs.getBoolean("rolsuper"));
		return Boolean.TRUE.equals(result);
	}

	private boolean hasSchemaCreatePrivilege(ConnectionWrapper q) {
		SQLBindings sql = new SQLBindings(
				"SELECT has_schema_privilege(current_user, current_schema(), 'CREATE') AS has_create",
				Collections.emptyList());
		Boolean result = q.querySingle(sql, rs -> rs.getBoolean("has_create"));
		return Boolean.TRUE.equals(result);
	}

	private Set<String> getOwnedPrivileges(ConnectionWrapper q) {
		// Check schema-level privileges via information_schema
		SQLBindings sql = new SQLBindings(
				"SELECT privilege_type FROM information_schema.role_table_grants "
						+ "WHERE grantee = current_user "
						+ "UNION "
						+ "SELECT CASE WHEN has_schema_privilege(current_user, current_schema(), 'CREATE') THEN 'CREATE' END "
						+ "UNION "
						+ "SELECT CASE WHEN has_schema_privilege(current_user, current_schema(), 'USAGE') THEN 'USAGE' END",
				Collections.emptyList());
		List<String> privs = q.query(sql, rs -> rs.getString(1));
		Set<String> result = new HashSet<>();
		for (String p : privs) {
			if (p != null && !p.isEmpty()) {
				result.add(p);
			}
		}
		return result;
	}
}
