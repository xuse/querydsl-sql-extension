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
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.AbstractSQLInsertClause;
import com.querydsl.sql.dml.SQLInsertClause;

/**
 * SQLInsertClause defines an INSERT INTO clause
 * If you need to subtype this, use {@link AbstractSQLInsertClause} instead.
 *
 * @author tiwe
 *
 */
public class SQLInsertClauseAlter extends SQLInsertClause {
    public SQLInsertClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity) {
        this(connection, new Configuration(templates), entity);
    }

    public SQLInsertClauseAlter(Connection connection, SQLTemplates templates, RelationalPath<?> entity, SQLQuery<?> subQuery) {
        this(connection, new Configuration(templates), entity, subQuery);
    }

    public SQLInsertClauseAlter(Connection connection, Configuration configuration, RelationalPath<?> entity, SQLQuery<?> subQuery) {
        super(connection, configuration, entity, subQuery);
    }

    public SQLInsertClauseAlter(Connection connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }

    public SQLInsertClauseAlter(Provider<Connection> connection, Configuration configuration, RelationalPath<?> entity, SQLQuery<?> subQuery) {
        super(connection, configuration, entity, subQuery);
    }

    public SQLInsertClauseAlter(Provider<Connection> connection, Configuration configuration, RelationalPath<?> entity) {
        super(connection, configuration, entity);
    }
}
