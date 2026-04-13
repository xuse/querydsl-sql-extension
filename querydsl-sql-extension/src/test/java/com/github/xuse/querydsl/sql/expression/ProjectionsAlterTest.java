package com.github.xuse.querydsl.sql.expression;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.xuse.querydsl.entity.QTableDataTypes;
import com.github.xuse.querydsl.entity.TableDataTypes;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Tests for ProjectionsAlter, JavaTimes, LongExpressions, SQLFunctions.
 */
class ProjectionsAlterTest {

	/** Test bean projection from RelationalPath. */
	@Test
	void testBeanFromRelationalPath() {
		QTableDataTypes t = QTableDataTypes.aaa;
		QBeanEx<TableDataTypes> proj = ProjectionsAlter.bean(TableDataTypes.class, t);
		assertNotNull(proj);
		assertEquals(TableDataTypes.class, proj.getType());
	}

	/** Test bean projection from expressions. */
	@Test
	void testBeanFromExpressions() {
		QTableDataTypes t = QTableDataTypes.aaa;
		QBeanEx<TableDataTypes> proj = ProjectionsAlter.bean(TableDataTypes.class, t.id, t.name);
		assertNotNull(proj);
		assertEquals(2, proj.getArgs().size());
	}

	/** Test bean projection from Path type. */
	@Test
	void testBeanFromPath() {
		QTableDataTypes t = QTableDataTypes.aaa;
		QBeanEx<TableDataTypes> proj = ProjectionsAlter.bean(t, t.id, t.name);
		assertNotNull(proj);
	}

	/** Test bean projection from bindings map with Path key. */
	@Test
	void testBeanFromPathBindings() {
		QTableDataTypes t = QTableDataTypes.aaa;
		Map<String, Expression<?>> bindings = new HashMap<>();
		bindings.put("id", t.id);
		bindings.put("name", t.name);
		QBeanEx<TableDataTypes> proj = ProjectionsAlter.bean(t, bindings);
		assertNotNull(proj);
	}

	/** Test bean projection from bindings map with Class key. */
	@Test
	void testBeanFromClassBindings() {
		QTableDataTypes t = QTableDataTypes.aaa;
		Map<String, Expression<?>> bindings = new HashMap<>();
		bindings.put("id", t.id);
		QBeanEx<TableDataTypes> proj = ProjectionsAlter.bean(TableDataTypes.class, bindings);
		assertNotNull(proj);
	}

	/** Test createProjection for a RelationalPath. */
	@Test
	void testCreateProjection() {
		QTableDataTypes t = QTableDataTypes.aaa;
		FactoryExpression<TableDataTypes> proj = ProjectionsAlter.createProjection(t);
		assertNotNull(proj);
	}

	/** Test createBeanProjection. */
	@Test
	void testCreateBeanProjection() {
		QTableDataTypes t = QTableDataTypes.aaa;
		QBeanEx<TableDataTypes> proj = ProjectionsAlter.createBeanProjection(t);
		assertNotNull(proj);
		assertFalse(proj.getArgs().isEmpty());
	}

	/** Test array projection. */
	@Test
	void testArrayProjection() {
		StringPath name = Expressions.stringPath("name");
		NumberPath<Integer> id = Expressions.numberPath(Integer.class, "id");
		assertNotNull(ProjectionsAlter.array(name, id));
	}

	/** Test stringMap projection. */
	@Test
	void testStringMapProjection() {
		StringPath name = Expressions.stringPath("name");
		QStringObjMap map = ProjectionsAlter.stringMap(name);
		assertNotNull(map);
	}

	/** Test list projection. */
	@Test
	void testListProjection() {
		StringPath name = Expressions.stringPath("name");
		assertNotNull(ProjectionsAlter.list(name));
	}

	/** Test beans projection. */
	@Test
	void testBeansProjection() {
		QTableDataTypes t = QTableDataTypes.aaa;
		assertNotNull(ProjectionsAlter.beans(t));
	}

	/** Test pair projection. */
	@Test
	void testPairProjection() {
		NumberPath<Integer> id = Expressions.numberPath(Integer.class, "id");
		StringPath name = Expressions.stringPath("name");
		assertNotNull(ProjectionsAlter.pair(id, name));
	}

	/** Test constructor projection. */
	@Test
	void testConstructorProjection() {
		StringPath name = Expressions.stringPath("name");
		assertNotNull(ProjectionsAlter.constructor(String.class, name));
	}

	/** Test JavaTimes.currentTimestamp(). */
	@Test
	void testJavaTimesCurrentTimestamp() {
		Expression<?> expr = JavaTimes.currentTimestamp();
		assertNotNull(expr);
	}

	/** Test LongExpressions constants. */
	@Test
	void testLongExpressions() {
		assertNotNull(LongExpressions.ONE);
		assertNotNull(LongExpressions.TWO);
		assertNotNull(LongExpressions.ZERO);
	}

	/** Test SQLFunctions.ifnull(). */
	@Test
	void testSQLFunctionsIfnull() {
		StringPath name = Expressions.stringPath("name");
		assertNotNull(SQLFunctions.ifnull(name, "default"));
		assertNotNull(SQLFunctions.ifnull(name, Expressions.constant("alt")));
	}
}