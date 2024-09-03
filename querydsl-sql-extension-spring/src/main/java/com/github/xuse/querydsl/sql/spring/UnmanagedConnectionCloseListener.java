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
package com.github.xuse.querydsl.sql.spring;

import java.sql.Connection;
import java.sql.SQLException;

import com.querydsl.core.QueryException;
import com.querydsl.sql.AbstractSQLQuery;
import com.querydsl.sql.SQLBaseListener;
import com.querydsl.sql.SQLListenerContext;

public final class UnmanagedConnectionCloseListener extends SQLBaseListener {
	public static final UnmanagedConnectionCloseListener DEFAULT = new UnmanagedConnectionCloseListener();

	private UnmanagedConnectionCloseListener() {
	}

	/*
	 * Same as com.querydsl.sql.AbstractSQLQuery.PARENT_CONTEXT.
	 * The constant was copied from AbstractSQLQuery since it's package protected.
	 */
	protected static final String PARENT_CONTEXT = AbstractSQLQuery.class.getName() + "#PARENT_CONTEXT";
	
	@Override
	public void end(SQLListenerContext context) {
		Connection connection = context.getConnection();
		if (connection != null && context.getData(PARENT_CONTEXT) == null) {
			if (connection instanceof UnmanagedConnection) {
				try {
					connection.close();
				} catch (SQLException e) {
					throw new QueryException(e);
				}
			}
		}
	}

}
