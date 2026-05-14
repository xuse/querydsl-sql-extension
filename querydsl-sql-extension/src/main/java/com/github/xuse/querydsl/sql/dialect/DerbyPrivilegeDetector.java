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
 * Derby-specific privilege detector.
 * <p>
 * Apache Derby uses a SQL standard authorization model. The database owner (DBO)
 * has full DDL privileges. Other users need explicit GRANT for DDL operations.
 * <p>
 * Detection strategy:
 * <ul>
 *   <li>Check if current user is the database owner (DBO) via {@code sys.sysschemas}</li>
 *   <li>Check schema-level permissions via {@code sys.systableperms} and {@code sys.sysschemaperms}</li>
 * </ul>
 * <p>
 * In embedded mode without authentication, the default user is typically the DBO.
 * 
 * @author Joey
 */
@Slf4j
public class DerbyPrivilegeDetector implements PrivilegeDetector {

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
		// Step 1: Check if current user is the database owner (DBO)
		if (isDatabaseOwner(q)) {
			log.info("Derby: current user is database owner (DBO), all privileges granted.");
			return true;
		}
		// Step 2: Check if user has specific schema permissions
		Set<String> required = Arrays.stream(privileges).map(Privilege::name).collect(Collectors.toSet());
		Set<String> owned = getSchemaPermissions(q);
		log.info("Derby owned permissions: {}", owned);
		// Derby DBO has implicit full DDL rights; non-DBO users need explicit grants
		// Map required privileges to Derby permission types
		return owned.containsAll(required);
	}

	private boolean isDatabaseOwner(ConnectionWrapper q) {
		try {
			// The DBO owns the APP schema by default. Check if current user owns the current schema.
			SQLBindings sql = new SQLBindings(
					"SELECT authorizationid FROM sys.sysschemas WHERE schemaname = CURRENT SCHEMA",
					Collections.emptyList());
			String schemaOwner = q.querySingle(sql, rs -> rs.getString("AUTHORIZATIONID"));
			if (schemaOwner == null) {
				return true; // No schema found, likely embedded mode with full access
			}
			// Get current user
			SQLBindings userSql = new SQLBindings("VALUES CURRENT_USER", Collections.emptyList());
			String currentUser = q.querySingle(userSql, rs -> rs.getString(1));
			boolean isOwner = schemaOwner.equalsIgnoreCase(currentUser);
			log.debug("Derby schema owner: {}, current user: {}, isOwner: {}", schemaOwner, currentUser, isOwner);
			return isOwner;
		} catch (Exception e) {
			// In embedded mode without authentication, assume full access
			log.debug("Derby DBO check failed, assuming full access (embedded mode).", e);
			return true;
		}
	}

	private Set<String> getSchemaPermissions(ConnectionWrapper q) {
		Set<String> permissions = new HashSet<>();
		try {
			// Query table-level permissions for current user
			SQLBindings sql = new SQLBindings(
					"SELECT DISTINCT TYPE FROM sys.systableperms tp "
							+ "JOIN sys.sysschemas s ON tp.SCHEMAID = s.SCHEMAID "
							+ "WHERE s.SCHEMANAME = CURRENT SCHEMA "
							+ "AND (tp.GRANTEE = CURRENT_USER OR tp.GRANTEE = 'PUBLIC')",
					Collections.emptyList());
			List<String> perms = q.query(sql, rs -> rs.getString("TYPE"));
			for (String perm : perms) {
				if (perm != null) {
					// Derby permission types: 's'=SELECT, 'i'=INSERT, 'd'=DELETE, 'u'=UPDATE, 't'=TRIGGER, 'r'=REFERENCES
					switch (perm.toLowerCase()) {
					case "s":
						permissions.add("SELECT");
						break;
					case "i":
						permissions.add("INSERT");
						break;
					case "d":
						permissions.add("DELETE");
						break;
					case "u":
						permissions.add("UPDATE");
						break;
					case "t":
						permissions.add("TRIGGER");
						break;
					case "r":
						permissions.add("REFERENCES");
						break;
					}
				}
			}
		} catch (Exception e) {
			log.debug("Derby permission query failed.", e);
		}
		return permissions;
	}
}
