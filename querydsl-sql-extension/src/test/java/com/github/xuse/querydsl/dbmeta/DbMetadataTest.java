package com.github.xuse.querydsl.dbmeta;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.xuse.querydsl.sql.Integration.AbstractTestBase;
import com.github.xuse.querydsl.sql.dbmeta.Constraint;
import com.github.xuse.querydsl.sql.dbmeta.DataType;
import com.github.xuse.querydsl.sql.dbmeta.KeyColumn;
import com.github.xuse.querydsl.sql.dbmeta.ObjectType;
import com.github.xuse.querydsl.sql.dbmeta.SequenceInfo;
import com.github.xuse.querydsl.sql.ddl.SQLMetadataQueryFactory;
import com.github.xuse.querydsl.sql.dialect.DbType;
import com.querydsl.sql.SchemaAndTable;

public class DbMetadataTest extends AbstractTestBase{
	
	@Test
	public void testDataType() {
		SQLMetadataQueryFactory meta=factory.getMetadataFactory();
		List<DataType> datatypes=meta.getDataTypes();
		assertTrue(datatypes.size()>0);
		DataType t=datatypes.get(0);
		for(DataType dt:datatypes) {
			System.out.println(dt.hashCode());
			System.out.println(dt.equals(t));
		}

		String namespace=meta.getDatabaseInfo().getNamespace(); 
		for(String catalog:meta.getCatalogs()) {
			for(String schema:meta.getSchemas(catalog)) {
				List<String> list = meta.getNames(catalog, schema,  ObjectType.TABLE);
				if(namespace.equals(catalog) || namespace.equals(schema)) {
					assertTrue(list.size()>0);
					showTables(catalog,schema, list);
				}
			}
		}
		
		List<SequenceInfo> seqs=meta.getSequenceInfo(namespace, null);
		for(SequenceInfo s:seqs) {
			System.out.println(s.toString());
		}
		
	}
	
	@Test
	public void testExecuteScrip() {
		SQLMetadataQueryFactory meta=factory.getMetadataFactory();
		DbType type=meta.getDatabaseInfo().getDbType();
		
		URL url = this.getClass().getResource("/test_script_" + type.name + ".sql");
		Map<String,RuntimeException> exceptions=new HashMap<>();
		int size=meta.executeScriptFile(url, StandardCharsets.UTF_8, true, exceptions);
		System.out.println(size);
		System.err.println(exceptions);
		assertTrue(exceptions.isEmpty());
		
	}

	
	private void showTables(String catalog, String schema, List<String> list) {
		for(String s:list) {
			SQLMetadataQueryFactory meta=factory.getMetadataFactory();
			Constraint pk=meta.getPrimaryKey(new SchemaAndTable(schema, s));
			System.out.println(pk);
			System.out.println(meta.getForeignKey(new SchemaAndTable(schema, s)));
		}
	}


	@Test
	public void testKeyColumns() {
		KeyColumn k=new KeyColumn();
		
		k.seq=1;
		k.setAscDesc("ASC");
		k.hashCode();
	}
}
