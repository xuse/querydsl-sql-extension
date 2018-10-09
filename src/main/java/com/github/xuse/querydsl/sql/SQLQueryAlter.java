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
package com.github.xuse.querydsl.sql;

import java.sql.Connection;

import javax.inject.Provider;

import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLCommonQuery;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;

/**
 * {@code SQLQuery} is a JDBC based implementation of the {@link SQLCommonQuery}
 * interface
 *
 * @param <T>
 * @author tiwe
 */
public class SQLQueryAlter<T> extends SQLQuery<T> {
	private static final long serialVersionUID = -3451422354253107107L;

	public SQLQueryAlter() {
		super();
	}

	public SQLQueryAlter(Configuration configuration) {
		super(configuration);
	}

	public SQLQueryAlter(Connection conn, Configuration configuration, QueryMetadata metadata) {
		super(conn, configuration, metadata);
	}

	public SQLQueryAlter(Connection conn, Configuration configuration) {
		super(conn, configuration);
	}

	public SQLQueryAlter(Connection conn, SQLTemplates templates, QueryMetadata metadata) {
		super(conn, templates, metadata);
	}

	public SQLQueryAlter(Connection conn, SQLTemplates templates) {
		super(conn, templates);
	}

	public SQLQueryAlter(Provider<Connection> connProvider, Configuration configuration, QueryMetadata metadata) {
		super(connProvider, configuration, metadata);
	}

	public SQLQueryAlter(Provider<Connection> connProvider, Configuration configuration) {
		super(connProvider, configuration);
	}

	public SQLQueryAlter(SQLTemplates templates) {
		super(templates);
	}
}
