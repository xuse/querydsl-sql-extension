package com.github.xuse.querydsl.init.csv;

public final class ReaderSettings {
	/**
	 * Check the Qualifier character or not.
	 */
	public boolean useTextQualifier;
	
	/**
	 * Qualifier char. 
	 */
	public char textQualifier;
	
	/**
	 * Trim values or not.
	 */
	public boolean trimWhitespace;

	/**
	 * Delimiter between two fields.
	 */
	public char delimiter;

	/**
	 * Enable the comment parse.
	 */
	public boolean useComments;

	/**
	 * Comment delimiter char.
	 */
	public char comment;

	/**
	 * If the qualifier character contains in field value. how to escape.
	 */
	public EscapeMode escapeMode;

	/**
	 * Prepare value for 
	 */
	public boolean captureRawRecord;

	public ReaderSettings() {
		trimWhitespace = true;
		delimiter = Characters.COMMA;
		
		useTextQualifier = true;
		textQualifier = Characters.QUOTE;
		
		useComments = false;
		comment = Characters.POUND;
		
		escapeMode = EscapeMode.DOUBLED;
		captureRawRecord = true;
	}
}