package com.github.xuse.querydsl.sql.dbmeta;


/**
 * The type of database constraints.
 * @author jiyi
 *
 */
public enum ConstraintType {
	C("C", "CHECK"),  //Check on a table  Column
	O("O", "READ ONLY"),  //Read Only on a view  
	P("P", "PRIMARY KEY"),  //Primary Key
	R("R", "FOREIGN KEY"),  //Referential AKA Foreign Key
	U("U", "UNIQUE"),  //Unique Key
	F("F", "REF"), // Constraint that involves a REF column
	H("H", "HASH"), // Hash expression
	S("S", "SUPPLEMENTAL"), // Supplemental logging
	V("V", "VIEW CHECK");  //Check Option on a view 
	
	String typeName;
	String typeFullName;
	
	private ConstraintType(String typeName, String typeFullName){
		this.typeName = typeName;
		this.typeFullName = typeFullName;
	}
	
	public static ConstraintType parseName(String name){
		
		for (ConstraintType a : ConstraintType.values()) {  
            if (a.typeName.equalsIgnoreCase(name)) {  
                return a;  
            }  
        }  
		return null;
	}
	
	public static ConstraintType parseFullName(String name){
		
		for (ConstraintType a : ConstraintType.values()) {  
            if (a.typeFullName.equalsIgnoreCase(name)) {  
                return a;  
            }  
        }  
		return null;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getTypeFullName() {
		return typeFullName;
	}
}
