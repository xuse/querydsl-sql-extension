/*
 * Copyright 2015, The Querydsl Team (http://www.querydsl.com/team)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.xuse.querydsl.sql.spring.UnmanagedConnection;
import com.querydsl.core.QueryException;

public final class UnmanagedConnectionCloseListener extends SQLBaseListener {
	private static Logger log = LoggerFactory.getLogger("Non-Transactional closer");
	public static final UnmanagedConnectionCloseListener DEFAULT = new UnmanagedConnectionCloseListener();

	private UnmanagedConnectionCloseListener() {
	}

	@Override
	public void end(SQLListenerContext context) {
		Connection connection = context.getConnection();
		if (connection != null && context.getData(AbstractSQLQuery.PARENT_CONTEXT) == null) {
			if (connection instanceof UnmanagedConnection) {
				try {
					log.info("close connection:{}", connection);
					connection.close();
				} catch (SQLException e) {
					throw new QueryException(e);
				}
			}
		}
	}

}
