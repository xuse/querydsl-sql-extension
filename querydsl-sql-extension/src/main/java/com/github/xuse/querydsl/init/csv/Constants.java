package com.github.xuse.querydsl.init.csv;

final class Constants {

	public static final int ESCAPE_MODE_DOUBLED = 1;

	public static final int ESCAPE_MODE_BACKSLASH = 2;
	
	public char textQualifier;

	public boolean useTextQualifier;

	public char delimiter;

	public char recordDelimiter;

	public char comment;

	public int escapeMode;

	public boolean forceQualifier;

	public Constants() {
		textQualifier = Characters.QUOTE;
		useTextQualifier = true;
		delimiter = Characters.COMMA;
		recordDelimiter = Characters.NULL;
		comment = Characters.POUND;
		escapeMode = ESCAPE_MODE_DOUBLED;
		forceQualifier = false;
	}
}