package com.github.xuse.querydsl.sql.dialect;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collections;
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

@Slf4j
public class MySQLPrivilegeDetector implements PrivilegeDetector{
	@Override
	public boolean check(SQLQueryFactory connection, Privilege... privileges) {
		MetadataQuerySupport metadataQuery = new MetadataQuerySupport() {
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
				if(provider==null) {
					throw new IllegalStateException("There is no distributed-lock provider available.");
				}
				return provider.getLock(lockName, 3);
			}
		};
		String[] privilegeOwn = metadataQuery.doSQLQuery(this::getOwn, "getPrivileges");
		Set<String> set=Arrays.stream(privilegeOwn).map(String::trim).collect(Collectors.toSet());
		log.info("Own privilege:{}",set);
		return set.contains("ALL PRIVILEGES")|| set.contains("ALL") ||set.containsAll(Arrays.stream(privileges).map(this::name).collect(Collectors.toList()));
	}
	
	private String name(Privilege p) {
		return p.name().replace('_', ' ');
	}
	
	private String[] getOwn(ConnectionWrapper q) {
		SQLBindings sql=new SQLBindings("SHOW GRANTS", Collections.emptyList());
		String s=q.querySingle(sql, e->e.getString(1));
		s= StringUtils.substringBetween(s, "GRANT ", " ON ");
		return StringUtils.split(s,",");
	}

}
