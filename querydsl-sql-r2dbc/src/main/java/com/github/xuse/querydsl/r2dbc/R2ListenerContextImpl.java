package com.github.xuse.querydsl.r2dbc;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.querydsl.core.QueryMetadata;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLBindings;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;

/**
 * A mutable implementation of SQL listener context.
 * <p>
 * INTERNAL USE ONLY - {@link com.querydsl.sql.SQLDetailedListener} implementations are not expected to use this
 * class directly
 */
public class R2ListenerContextImpl implements R2ListenerContext {
    private final Map<String, Object> contextMap;

    private final QueryMetadata md;

    private final List<SQLBindings> sqlStatements;

    private final List<Statement> statements;

    private RelationalPath<?> entity;

    private Connection connection;

    private Exception exception;

    public R2ListenerContextImpl(final QueryMetadata metadata, final Connection connection, final RelationalPath<?> entity) {
        this.contextMap = new HashMap<>();
        this.statements = new ArrayList<>();
        this.sqlStatements = new ArrayList<>();
        this.md = metadata;
        this.connection = connection;
        this.entity = entity;
    }

    public R2ListenerContextImpl(final QueryMetadata metadata, final Connection connection) {
        this(metadata, connection, null);
    }

    public R2ListenerContextImpl(final QueryMetadata metadata) {
        this(metadata, null, null);
    }

    public void addSQL(final SQLBindings sql) {
        this.sqlStatements.add(sql);
    }

    public void setEntity(final RelationalPath<?> entity) {
        this.entity = entity;
    }

    public void setConnection(final Connection connection) {
        this.connection = connection;
    }

    public void setException(final Exception exception) {
        this.exception = exception;
    }

    public void addStatement(final Statement preparedStatement) {
        this.statements.add(preparedStatement);
    }

    @Override
    public QueryMetadata getMetadata() {
        return md;
    }

    @Override
    public RelationalPath<?> getEntity() {
        return entity;
    }

    @Override
    public String getSQL() {
        return sqlStatements.isEmpty() ? null : sqlStatements.get(0).getSQL();
    }

    @Override
    public SQLBindings getSQLBindings() {
        return sqlStatements.isEmpty() ? null : sqlStatements.get(0);
    }

    @Override
    public Collection<String> getSQLStatements() {
        return sqlStatements.stream().map(SQLBindings::getSQL).collect(Collectors.toList());
    }

    @Override
    public Collection<SQLBindings> getAllSQLBindings() {
        return sqlStatements;
    }

    @Override
    public Exception getException() {
        return exception;
    }

	@Override
	public Connection getR2Connection() {
		return connection;
	}

	@Override
	public Statement getR2Statement() {
		if(statements.isEmpty()) {
			return null;
		}
		return statements.get(0);
	}


    @Override
    public Object getData(final String dataKey) {
        return contextMap.get(dataKey);
    }

    @Override
    public void setData(final String dataKey, final Object value) {
        contextMap.put(dataKey, value);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append(" sql:").append(nicerSql(getSQL()))
                .append(" connection:").append(connection == null ? "not connected" : "connected")
                .append(" entity:").append(entity)
                .append(" exception:").append(exception);

        for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
            sb.append(" [").append(entry.getKey()).append(":").append(entry.getValue()).append("]");
        }
        return sb.toString();
    }

    private String nicerSql(final String sql) {
        return "'" + (sql == null ? null : sql.replace('\n', ' ')) + "'";
    }
}