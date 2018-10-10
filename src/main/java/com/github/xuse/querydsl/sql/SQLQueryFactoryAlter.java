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
import javax.sql.DataSource;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLTemplates;

/**
 * Factory class for query and DML clause creation
 *
 * @author Joey
 *
 */
public class SQLQueryFactoryAlter extends SQLQueryFactory {

    public SQLQueryFactoryAlter(Configuration configuration, DataSource dataSource, boolean release) {
		super(configuration, dataSource, release);
	}

	public SQLQueryFactoryAlter(Configuration configuration, DataSource dataSource) {
		super(configuration, dataSource);
	}

	public SQLQueryFactoryAlter(Configuration configuration, Provider<Connection> connProvider) {
		super(configuration, connProvider);
	}

	public SQLQueryFactoryAlter(SQLTemplates templates, Provider<Connection> connection) {
		super(templates, connection);
	}

	@Override
    public SQLQueryAlter<?> query() {
        return new SQLQueryAlter<Void>(connection, configuration);
    }
	
	
	
	
}
