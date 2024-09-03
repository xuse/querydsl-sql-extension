package com.github.xuse.querydsl.sql;



import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import com.github.xuse.querydsl.annotation.InitializeData;
import com.github.xuse.querydsl.entity.QAaa;
import com.github.xuse.querydsl.entity.QSeataStateMachineInst;
import com.github.xuse.querydsl.init.InitDataExporter;
import com.github.xuse.querydsl.sql.routing.TableRouting;
import com.querydsl.core.types.dsl.Expressions;

public class TestDataInitialize extends AbstractTestBase{
	/**
	 * 导出数据
	 */
	@Test
	@Ignore
	public void testExport() {
		new InitDataExporter(factory).writeNullString()
				.targetDirectory(new File(System.getProperty("user.dir"),"src/test/resources"))
				.export(QSeataStateMachineInst.class)
				.export(QAaa.class);
	}
	
	@Test
	public void testDataInitDefault() {
		factory.getMetadataFactory();
		factory.getMetadataFactory();
		
		
		QAaa t=QAaa.aaa;
		factory.initializeTable(t)
			.applyConfig(t.getInitializeData())
			.updateNulls(true)
			.execute();
	}
	
	/**
	 */
	@Test
	public void testDataInitNew() {
		QSeataStateMachineInst t=QSeataStateMachineInst.seataStateMachineInst;
		TableRouting routing = TableRouting.suffix("_m1");
		
		factory.getMetadataFactory().dropTable(t).withRouting(routing).execute();
		factory.getMetadataFactory().createTable(t).ifExists().withRouting(routing).execute();
		InitializeData anno=t.getInitializeData();
		if(anno==null) {
			return;
		}
		factory.initializeTable(t).applyConfig(anno).withRouting(routing).isNewTable(true).execute();
	}
	
	@Test
	public void testDataMerge() {
		QSeataStateMachineInst t=QSeataStateMachineInst.seataStateMachineInst;
		TableRouting routing = TableRouting.suffix("_m1");
		factory.update(t).set(t.isRunning, 1).where(Expressions.TRUE).execute();
		factory.initializeTable(t).applyConfig(t.getInitializeData()).withRouting(routing).execute();
	}
}
