package com.github.xuse.querydsl.init.csv;

final class ReaderSettings {
	public static final int MAX_BUFFER_SIZE = 1024;

	public static final int MAX_FILE_BUFFER_SIZE = 4 * 1024;

	public static final int INITIAL_COLUMN_COUNT = 10;

	public static final int INITIAL_COLUMN_BUFFER_SIZE = 50;
	

		// having these as publicly accessible members will prevent
		public boolean caseSensitive;

		public char textQualifier;

		public boolean trimWhitespace;
		
		public boolean trimHeaders;

		public boolean useTextQualifier;

		public char delimiter;

		public char recordDelimiter;

		public boolean useComments;
		
		public char comment;

		public int escapeMode;

		public boolean safetySwitch;

		public boolean skipEmptyRecords;

		public boolean captureRawRecord;

		public ReaderSettings() {
			caseSensitive = true;
			textQualifier = Characters.QUOTE;
			trimWhitespace = true;
            trimHeaders = true;
			useTextQualifier = true;
			delimiter = Characters.COMMA;
			recordDelimiter = Characters.NULL;
			comment = Characters.POUND;
			useComments = false;
			escapeMode = CsvFileReader.ESCAPE_MODE_DOUBLED;
			safetySwitch = true;
			skipEmptyRecords = true;
			captureRawRecord = true;
		}
	}