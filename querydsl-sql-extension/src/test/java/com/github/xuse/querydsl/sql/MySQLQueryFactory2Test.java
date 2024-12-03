package com.github.xuse.querydsl.sql;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.Aaa;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.mock.MockedTestBase;
import com.github.xuse.querydsl.sql.dml.SQLDeleteClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLInsertClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLMergeClauseAlter;
import com.github.xuse.querydsl.sql.dml.SQLUpdateClauseAlter;
import com.querydsl.core.QueryFlag.Position;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.mysql.MySQLQuery;
import com.querydsl.sql.mysql.MySQLReplaceClause;

public class MySQLQueryFactory2Test extends MockedTestBase{

    private MySQLQueryFactory2 mySQLQueryFactory2;

    @BeforeEach
    public void setUp() {
        mySQLQueryFactory2 = new MySQLQueryFactory2(factory.getConfiguration(),()->factory.getConnection());
    }

    @Test
    public void testInsertIgnore() {
        QAaa aaa=QAaa.aaa;
        SQLInsertClauseAlter insertClause = mySQLQueryFactory2.insertIgnore(aaa);

        assertNotNull(insertClause);
        insertClause.addFlag(Position.START_OVERRIDE, "insert ignore into ");
    }

    @Test
    public void testInsertOnDuplicateKeyUpdateWithStringClause() {
        RelationalPath<?> entity = QAaa.aaa;
        String clause = "col1 = col1 + 1";
        SQLInsertClauseAlter insertClause = mySQLQueryFactory2.insertOnDuplicateKeyUpdate(entity, clause);

        assertNotNull(insertClause);
        insertClause.addFlag(Position.END, " on duplicate key update " + clause);
    }

    @Test
    public void testInsertOnDuplicateKeyUpdateWithExpressionClause() {
    	RelationalPath<?> entity = QAaa.aaa;
        Expression<?> clause = Expressions.constant("col1 = col1 + 1");
        SQLInsertClauseAlter insertClause = mySQLQueryFactory2.insertOnDuplicateKeyUpdate(entity, clause);

        assertNotNull(insertClause);
        insertClause.addFlag(Position.END, ExpressionUtils.template(String.class, " on duplicate key update {0}", clause));
    }

    @Test
    public void testInsertOnDuplicateKeyUpdateWithMultipleExpressionClauses() {
    	RelationalPath<?> entity = QAaa.aaa;
        Expression<?> clause1 = Expressions.constant("col1 = col1 + 1");
        Expression<?> clause2 = Expressions.constant("col2 = col2 + 2");
        SQLInsertClauseAlter insertClause = mySQLQueryFactory2.insertOnDuplicateKeyUpdate(entity, clause1, clause2);

        assertNotNull(insertClause);
        insertClause.addFlag(Position.END, ExpressionUtils.template(String.class, " on duplicate key update {0}, {1}", clause1, clause2));
    }

    @Test
    public void testInsert() {
    	RelationalPath<?> entity = QAaa.aaa;
        SQLInsertClauseAlter insertClause = mySQLQueryFactory2.insert(entity);

        assertNotNull(insertClause);
    }

    @Test
    public void testQuery() {
        MySQLQuery<?> query = mySQLQueryFactory2.query();

        assertNotNull(query);
    }

    @Test
    public void testReplace() {
        QAaa aaa=QAaa.aaa;
        MySQLReplaceClause replaceClause = mySQLQueryFactory2.replace(aaa);

        assertNotNull(replaceClause);
    }

    @Test
    public void testSelect() {
        Expression<Integer> expr = Expressions.numberPath(Integer.class,"value");
        MySQLQuery<Integer> query = mySQLQueryFactory2.select(expr);

        assertNotNull(query);
    }

    @Test
    public void testSelectDistinct() {
    	Expression<Integer> expr = Expressions.numberPath(Integer.class,"value");
        MySQLQuery<Integer> query = mySQLQueryFactory2.selectDistinct(expr);

        assertNotNull(query);
    }

    @Test
    public void testSelectFrom() {
    	QAaa entity = QAaa.aaa;
        MySQLQuery<Aaa> query = mySQLQueryFactory2.selectFrom(entity);
        assertNotNull(query);
    }

    @Test
    public void testSelectZero() {
        MySQLQuery<Integer> query = mySQLQueryFactory2.selectZero();

        assertNotNull(query);
    }

    @Test
    public void testSelectOne() {
        MySQLQuery<Integer> query = mySQLQueryFactory2.selectOne();

        assertNotNull(query);
    }

    @Test
    public void testUpdate() {
    	RelationalPath<?> entity = QAaa.aaa;
        SQLUpdateClauseAlter updateClause = mySQLQueryFactory2.update(entity);
        assertNotNull(updateClause);
    }

    @Test
    public void testMerge() {
    	RelationalPath<?> entity = QAaa.aaa;
    	SQLMergeClauseAlter updateClause = mySQLQueryFactory2.merge(entity);
        assertNotNull(updateClause);
    }
    
    @Test
    public void testDelete() {
    	RelationalPath<?> entity = QAaa.aaa;
    	SQLDeleteClauseAlter delClause = mySQLQueryFactory2.delete(entity);
        assertNotNull(delClause);
    }
    
    @Test
    public void select2() {
    	QAaa entity = QAaa.aaa;
    	MySQLQuery<Tuple> delClause = mySQLQueryFactory2.select(entity.dataBigint,entity.dataDecimal).from(entity);
    	assertNotNull(delClause);
    }
    
    @Test
    public void testselectDistinct() {
    	QAaa entity = QAaa.aaa;
    	MySQLQuery<Tuple> delClause = mySQLQueryFactory2.selectDistinct(entity.dataBigint,entity.dataDecimal).from(entity);
        assertNotNull(delClause);
    }
}