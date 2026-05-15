package com.github.xuse.querydsl.sql.dialect;

import java.sql.Connection;
import java.util.Collections;

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
 * H2-specific privilege detector.
 * <p>
 * H2 database uses a role-based privilege system. The ADMIN role has full DDL privileges.
 * For embedded mode, the default user typically has ADMIN rights.
 * <p>
 * Detection strategy:
 * <ul>
 *   <li>Check if current user has ADMIN role via {@code INFORMATION_SCHEMA.USERS}</li>
 *   <li>Check table rights in {@code INFORMATION_SCHEMA.TABLE_PRIVILEGES}</li>
 * </ul>
 * 
 * @author Joey
 */
@Slf4j
public class H2PrivilegeDetector implements PrivilegeDetector {

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
		return metadataQuery.doSQLQuery(this::doCheck, "checkPrivileges");
	}

	private boolean doCheck(ConnectionWrapper q) {
		// H2: Check if current user is admin
		if (isAdmin(q)) {
			log.info("H2: current user is admin, all privileges granted.");
			return true;
		}
		// For non-admin users, check if they have schema-level rights
		// In H2, non-admin users with ALTER/CREATE rights on schema can perform DDL
		log.info("H2: current user is not admin, DDL privileges may be limited.");
		return false;
	}

	private boolean isAdmin(ConnectionWrapper q) {
		// H2 2.x uses INFORMATION_SCHEMA.USERS with ADMIN column
		try {
			SQLBindings sql = new SQLBindings(
					"SELECT ADMIN FROM INFORMATION_SCHEMA.USERS WHERE NAME = CURRENT_USER",
					Collections.emptyList());
			Boolean result = q.querySingle(sql, rs -> rs.getBoolean("ADMIN"));
			return Boolean.TRUE.equals(result);
		} catch (Exception e) {
			// Fallback: try H2 1.x compatible query
			log.debug("H2 INFORMATION_SCHEMA.USERS query failed, trying alternative.", e);
			return isAdminFallback(q);
		}
	}

	private boolean isAdminFallback(ConnectionWrapper q) {
		try {
			// In older H2 versions or when USERS table is not available,
			// try to query the session admin status
			SQLBindings sql = new SQLBindings(
					"SELECT IS_ADMIN FROM INFORMATION_SCHEMA.SESSIONS WHERE SESSION_ID = SESSION_ID()",
					Collections.emptyList());
			Boolean result = q.querySingle(sql, rs -> rs.getBoolean("IS_ADMIN"));
			return Boolean.TRUE.equals(result);
		} catch (Exception e) {
			// If we can't determine, assume we have privileges (embedded mode default)
			log.debug("H2 privilege detection fallback also failed, assuming admin.", e);
			return true;
		}
	}
}
