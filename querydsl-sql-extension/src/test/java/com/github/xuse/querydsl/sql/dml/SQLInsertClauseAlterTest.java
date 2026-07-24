package com.github.xuse.querydsl.sql.dml;

import static org.mockito.MockitoAnnotations.openMocks;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Types;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.xuse.querydsl.config.ConfigurationEx;
import com.github.xuse.querydsl.sql.DynamicRelationalPath;
import com.github.xuse.querydsl.sql.SQLQueryFactory;
import com.github.xuse.querydsl.sql.column.ColumnPathHandler;
import com.github.xuse.querydsl.sql.integration.AbstractTestBase;
import com.github.xuse.querydsl.util.SnowflakeIdWorker;
import com.github.xuse.querydsl.util.Util;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.sql.ColumnMetadata;

@ExtendWith(MockitoExtension.class)
public class SQLInsertClauseAlterTest {

    @Mock
    private Connection connection;

    @Mock
    private List<Path<?>> columnsList;

    @Mock
    private List<Expression<?>> valuesList;

    @Mock
    private SnowflakeIdWorker snowflakeWorker;

    private SQLInsertClauseAlter clause;
    
    private Path<Integer> path;

    private ColumnPathHandler<Integer,Path<Integer>> pathBuilder;
    
    @BeforeEach
    public void setup() {
        openMocks(this);
        
        DynamicRelationalPath entity = new DynamicRelationalPath("t1", null, "table_name");
        // 初始化实体的列集合
        pathBuilder=entity.addColumn(Integer.class, ColumnMetadata.named("a").withSize(1).ofType(Types.INTEGER));
        path=pathBuilder.build();
        
        // 初始化父类的 columns 和 values 列表
        ConfigurationEx configuration= AbstractTestBase.querydslConfiguration(SQLQueryFactory.calcSQLTemplate(AbstractTestBase.getEffectiveDs().getUrl()));
        clause = new SQLInsertClauseAlter(connection, configuration, entity);
        // 使用反射设置私有字段
        setField(clause, "columns", columnsList);
        setField(clause, "values", valuesList);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
        	Field field=Util.findField(target.getClass(), fieldName);
        	field.setAccessible(true);
        	field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}