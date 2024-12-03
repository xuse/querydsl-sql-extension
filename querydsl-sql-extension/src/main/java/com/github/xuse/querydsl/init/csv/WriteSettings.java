package com.github.xuse.querydsl.init.csv;

public final class WriteSettings {
	/**
	 * Quot the field value.
	 */
	public char textQualifier;
	/**
	 * delimiter between two fields.
	 */
	public char delimiter;
	/**
	 * For comments.
	 */
	public char comment;
	/**
	 * If the qualifier character contains in field value. how to escape.  
	 */
	public EscapeMode escapeMode;

	/**
	 * If true, all field values are quoted. 
	 */
	public boolean forceQualifier;
	
	public WriteSettings() {
		textQualifier = Characters.QUOTE;
		delimiter = Characters.COMMA;
		comment = Characters.POUND;
		
		forceQualifier = false;
		escapeMode = EscapeMode.DOUBLED;
	}
}