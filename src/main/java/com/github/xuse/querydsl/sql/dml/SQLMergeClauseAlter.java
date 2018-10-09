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
package com.github.xuse.querydsl.sql.dml;

import java.sql.Connection;

import javax.inject.Provider;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLMergeClause;



/**
 * {@code SQLMergeClause} defines an MERGE INTO clause
 *
 * @author tiwe
 *
 */
public class SQLMergeClauseAlter extends SQLMergeClause{
	public SQLMergeClauseAlter(Connection connection, Configuration configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}

	public SQLMergeClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity) {
		super(connection, templates, entity);
	}

	public SQLMergeClauseAlter(Provider<Connection> connection, Configuration configuration, RelationalPath<?> entity) {
		super(connection, configuration, entity);
	}
}
