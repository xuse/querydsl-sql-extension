package com.github.xuse.querydsl.sql.dialect;

/**
 * This is a JDBC driver feature, not a RDBMS feature.
 * 
 *
 */
public enum SchemaPolicy {
	/**
	 * Oracle,Derby，Postgresql and etc...
	 */
	SCHEMA_ONLY{
		public String asCatalog(String namespace) {
			return null;
		}
		public String toNamespace(String catalog, String schema) {
			return schema;
		}
		@Override
		public String asSchema(String namespace) {
			return namespace;
		}
	},
	/**
	 * Known driver is mysql /mariadb
	 */
	CATALOG_ONLY{
		public String asSchema(String namespaces) {
			return null;
		}
		public String toNamespace(String catalog, String schema) {
			return catalog;
		}
		@Override
		public String asCatalog(String namespace) {
			return namespace;
		}
	},
	
	/**
	 * In SQL92 standard, but no RDBMS Driver implements.
	 */
	CATALOG_AND_SCHEMA;

	public String asCatalog(String namespace) {
		int index=namespace.indexOf('.');
		return namespace.substring(0,index);
	}
	
	public String asSchema(String namespace) {
		int index=namespace.indexOf('.');
		return namespace.substring(index+1);
	} 
	
	public String toNamespace(String catalog, String schema) {
		if(catalog==null) {
			catalog="";
		}
		if(schema==null) {
			schema="";
		}
		return catalog + "." + schema;
	}
}
