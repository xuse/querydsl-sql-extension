package com.github.xuse.querydsl.init.csv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.HashMap;

import lombok.SneakyThrows;

public class CsvFileReader implements Closeable {

	private Reader reader = null;

	private File fileName = null;

	private final ReaderSettings config = new ReaderSettings();

	private Charset charset = null;

	private boolean useCustomRecordDelimiter = false;

	private final Buffer dataBuffer = new Buffer();

	private final BufferDataField fieldBuffer = new BufferDataField();

	private final BufferDataRawRecord rawBuffer = new BufferDataRawRecord();

	private boolean[] isQualified;

	private String rawRecord = "";

	private final HeadersHolder headersHolder = new HeadersHolder();

	private boolean fieldStarted = false;

	private boolean startedWithQualifier = false;

	private boolean hasMoreData = true;

	private char lastLetter = '\0';

	private boolean hasReadNextLine = false;

	private int columnsCount = 0;

	private long currentRecord = 0;

	private String[] values = new String[ReaderSettings.INITIAL_COLUMN_COUNT];

	private boolean initialized = false;

	private boolean closed = false;

	public static final int ESCAPE_MODE_DOUBLED = 1;

	public static final int ESCAPE_MODE_BACKSLASH = 2;

	public CsvFileReader(File fileName, char delimiter, Charset charset) {
		if (fileName == null) {
			throw new IllegalArgumentException("Parameter fileName can not be null.");
		}
		if (charset == null) {
			throw new IllegalArgumentException("Parameter charset can not be null.");
		}
		if (!fileName.exists()) {
			throw new IllegalArgumentException("File " + fileName + " does not exist.");
		}
		this.fileName = fileName;
		this.config.delimiter = delimiter;
		this.charset = charset;
		isQualified = new boolean[values.length];
	}

	public CsvFileReader(File fileName, Charset charset) {
		this(fileName, Characters.COMMA, charset);
	}

	public CsvFileReader(String fileName) {
		this(new File(fileName), StandardCharsets.UTF_8);
	}

	public CsvFileReader(Reader inputStream, char delimiter) {
		if (inputStream == null) {
			throw new IllegalArgumentException("Parameter inputStream can not be null.");
		}
		this.reader = inputStream;
		this.config.delimiter = delimiter;
		initialized = true;
		isQualified = new boolean[values.length];
	}

	public CsvFileReader(Reader inputStream) {
		this(inputStream, Characters.COMMA);
	}

	public CsvFileReader(InputStream inputStream, char delimiter, Charset charset) {
		this(new InputStreamReader(inputStream, charset), delimiter);
	}

	public CsvFileReader(InputStream inputStream, Charset charset) {
		this(new InputStreamReader(inputStream, charset));
	}

	public boolean getCaptureRawRecord() {
		return config.captureRawRecord;
	}

	public void setCaptureRawRecord(boolean captureRawRecord) {
		config.captureRawRecord = captureRawRecord;
	}

	public String getRawRecord() {
		return rawRecord;
	}

	public boolean getTrimWhitespace() {
		return config.trimWhitespace;
	}

	public void setTrimWhitespace(boolean trimWhitespace) {
		config.trimWhitespace = trimWhitespace;
	}

	public char getDelimiter() {
		return config.delimiter;
	}

	public void setDelimiter(char delimiter) {
		config.delimiter = delimiter;
	}

	public char getRecordDelimiter() {
		return config.recordDelimiter;
	}

	public void setRecordDelimiter(char recordDelimiter) {
		useCustomRecordDelimiter = true;
		config.recordDelimiter = recordDelimiter;
	}

	public char getTextQualifier() {
		return config.textQualifier;
	}

	public void setTextQualifier(char textQualifier) {
		config.textQualifier = textQualifier;
	}

	public boolean getUseTextQualifier() {
		return config.useTextQualifier;
	}

	public void setUseTextQualifier(boolean useTextQualifier) {
		config.useTextQualifier = useTextQualifier;
	}

	public char getComment() {
		return config.comment;
	}

	public void setComment(char comment) {
		config.comment = comment;
	}

	public boolean getUseComments() {
		return config.useComments;
	}

	public void setUseComments(boolean useComments) {
		config.useComments = useComments;
	}

	public int getEscapeMode() {
		return config.escapeMode;
	}

	public void setEscapeMode(int escapeMode) throws IllegalArgumentException {
		if (escapeMode != ESCAPE_MODE_DOUBLED && escapeMode != ESCAPE_MODE_BACKSLASH) {
			throw new IllegalArgumentException("Parameter escapeMode must be a valid value.");
		}
		config.escapeMode = escapeMode;
	}

	public boolean getSkipEmptyRecords() {
		return config.skipEmptyRecords;
	}

	public void setSkipEmptyRecords(boolean skipEmptyRecords) {
		config.skipEmptyRecords = skipEmptyRecords;
	}

	public boolean getSafetySwitch() {
		return config.safetySwitch;
	}

	public void setSafetySwitch(boolean safetySwitch) {
		config.safetySwitch = safetySwitch;
	}

	public int getColumnCount() {
		return columnsCount;
	}

	public long getCurrentRecord() {
		return currentRecord - 1;
	}

	public int getHeaderCount() {
		return headersHolder.Length;
	}

	public String[] getHeaders(){
		checkClosed();
		if (headersHolder.Headers == null) {
			return null;
		} else {
			// use clone here to prevent the outside code from
			// setting values on the array directly, which would
			// throw off the index lookup based on header name
			String[] clone = new String[headersHolder.Length];
			System.arraycopy(headersHolder.Headers, 0, clone, 0, headersHolder.Length);
			return clone;
		}
	}

	public void setHeaders(String[] headers) {
		headersHolder.Headers = headers;
		headersHolder.IndexByName.clear();
		if (headers != null) {
			headersHolder.Length = headers.length;
		} else {
			headersHolder.Length = 0;
		}
		// use headersHolder.Length here in case headers is null
		for (int i = 0; i < headersHolder.Length; i++) {
			headersHolder.IndexByName.put(headers[i], Integer.valueOf(i));
		}
	}

	public String[] getValues(){
		checkClosed();
		// need to return a clone, and can't use clone because values.Length
		// might be greater than columnsCount
		String[] clone = new String[columnsCount];
		System.arraycopy(values, 0, clone, 0, columnsCount);
		return clone;
	}

	public String get(int columnIndex){
		checkClosed();
		if (columnIndex > -1 && columnIndex < columnsCount) {
			return values[columnIndex];
		} else {
			return "";
		}
	}

	public String get(String headerName) {
		checkClosed();
		return get(getIndex(headerName));
	}

	public static CsvFileReader parse(String data) {
		if (data == null) {
			throw new IllegalArgumentException("Parameter data can not be null.");
		}
		return new CsvFileReader(new StringReader(data));
	}

	public boolean readRecord(){
		checkClosed();
		columnsCount = 0;
		rawBuffer.index = 0;
		dataBuffer.recordStart = dataBuffer.index;
		hasReadNextLine = false;
		if (hasMoreData) {
			do {
				if (dataBuffer.index == dataBuffer.Count) {
					checkDataLength();
				} else {
					startedWithQualifier = false;
					char currentLetter = dataBuffer.buffer[dataBuffer.index];
					if (config.useTextQualifier && currentLetter == config.textQualifier) {
						lastLetter = currentLetter;
						fieldStarted = true;
						dataBuffer.fieldStart = dataBuffer.index + 1;
						startedWithQualifier = true;
						boolean lastLetterWasQualifier = false;
						char escapeChar = config.textQualifier;
						if (config.escapeMode == ESCAPE_MODE_BACKSLASH) {
							escapeChar = Characters.BACKSLASH;
						}
						boolean eatingTrailingJunk = false;
						boolean lastLetterWasEscape = false;
						boolean readingComplexEscape = false;
						ComplexEscape escape = ComplexEscape.UNICODE;
						int escapeLength = 0;
						char escapeValue = (char) 0;
						dataBuffer.index++;
						do {
							if (dataBuffer.index == dataBuffer.Count) {
								checkDataLength();
							} else {
								// grab the current letter as a char
								currentLetter = dataBuffer.buffer[dataBuffer.index];
								if (eatingTrailingJunk) {
									dataBuffer.fieldStart = dataBuffer.index + 1;
									if (currentLetter == config.delimiter) {
										endField();
									} else if ((!useCustomRecordDelimiter && (currentLetter == Characters.CR || currentLetter == Characters.LF)) || (useCustomRecordDelimiter && currentLetter == config.recordDelimiter)) {
										endField();
										endRecord();
									}
								} else if (readingComplexEscape) {
									escapeLength++;
									switch(escape) {
										case UNICODE:
											escapeValue *= (char) 16;
											escapeValue += hexToDec(currentLetter);
											if (escapeLength == 4) {
												readingComplexEscape = false;
											}
											break;
										case OCTAL:
											escapeValue *= (char) 8;
											escapeValue += (char) (currentLetter - '0');
											if (escapeLength == 3) {
												readingComplexEscape = false;
											}
											break;
										case DECIMAL:
											escapeValue *= (char) 10;
											escapeValue += (char) (currentLetter - '0');
											if (escapeLength == 3) {
												readingComplexEscape = false;
											}
											break;
										case HEX:
											escapeValue *= (char) 16;
											escapeValue += hexToDec(currentLetter);
											if (escapeLength == 2) {
												readingComplexEscape = false;
											}
											break;
									}
									if (!readingComplexEscape) {
										appendLetter(escapeValue);
									} else {
										dataBuffer.fieldStart = dataBuffer.index + 1;
									}
								} else if (currentLetter == config.textQualifier) {
									if (lastLetterWasEscape) {
										lastLetterWasEscape = false;
										lastLetterWasQualifier = false;
									} else {
										updateCurrentValue();
										if (config.escapeMode == ESCAPE_MODE_DOUBLED) {
											lastLetterWasEscape = true;
										}
										lastLetterWasQualifier = true;
									}
								} else if (config.escapeMode == ESCAPE_MODE_BACKSLASH && lastLetterWasEscape) {
									switch(currentLetter) {
										case 'n':
											appendLetter(Characters.LF);
											break;
										case 'r':
											appendLetter(Characters.CR);
											break;
										case 't':
											appendLetter(Characters.TAB);
											break;
										case 'b':
											appendLetter(Characters.BACKSPACE);
											break;
										case 'f':
											appendLetter(Characters.FORM_FEED);
											break;
										case 'e':
											appendLetter(Characters.ESCAPE);
											break;
										case 'v':
											appendLetter(Characters.VERTICAL_TAB);
											break;
										case 'a':
											appendLetter(Characters.ALERT);
											break;
										case '0':
										case '1':
										case '2':
										case '3':
										case '4':
										case '5':
										case '6':
										case '7':
											escape = ComplexEscape.OCTAL;
											readingComplexEscape = true;
											escapeLength = 1;
											escapeValue = (char) (currentLetter - '0');
											dataBuffer.fieldStart = dataBuffer.index + 1;
											break;
										case 'u':
										case 'x':
										case 'o':
										case 'd':
										case 'U':
										case 'X':
										case 'O':
										case 'D':
											switch(currentLetter) {
												case 'u':
												case 'U':
													escape = ComplexEscape.UNICODE;
													break;
												case 'x':
												case 'X':
													escape = ComplexEscape.HEX;
													break;
												case 'o':
												case 'O':
													escape = ComplexEscape.OCTAL;
													break;
												case 'd':
												case 'D':
													escape = ComplexEscape.DECIMAL;
													break;
											}
											readingComplexEscape = true;
											escapeLength = 0;
											escapeValue = (char) 0;
											dataBuffer.fieldStart = dataBuffer.index + 1;
											break;
										default:
											break;
									}
									lastLetterWasEscape = false;
								// can only happen for ESCAPE_MODE_BACKSLASH
								} else if (currentLetter == escapeChar) {
									updateCurrentValue();
									lastLetterWasEscape = true;
								} else {
									if (lastLetterWasQualifier) {
										if (currentLetter == config.delimiter) {
											endField();
										} else if ((!useCustomRecordDelimiter && (currentLetter == Characters.CR || currentLetter == Characters.LF)) || (useCustomRecordDelimiter && currentLetter == config.recordDelimiter)) {
											endField();
											endRecord();
										} else {
											dataBuffer.fieldStart = dataBuffer.index + 1;
											eatingTrailingJunk = true;
										}
										// make sure to clear the flag for next
										// run of the loop
										lastLetterWasQualifier = false;
									}
								}
								// keep track of the last letter because we need
								// it for several key decisions
								lastLetter = currentLetter;
								if (fieldStarted) {
									dataBuffer.index++;
									if (config.safetySwitch && dataBuffer.index - dataBuffer.fieldStart + fieldBuffer.index > 100000) {
										close();
										throw new IllegalStateException("Maximum column length of 100,000 exceeded in column " + NumberFormat.getIntegerInstance().format(columnsCount) + " in record " + NumberFormat.getIntegerInstance().format(currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting column lengths greater than 100,000 characters to" + " avoid this error.");
									}
								}
							}
						} while (hasMoreData && fieldStarted);
					} else if (currentLetter == config.delimiter) {
						lastLetter = currentLetter;
						endField();
					} else if (useCustomRecordDelimiter && currentLetter == config.recordDelimiter) {
						if (fieldStarted || columnsCount > 0 || !config.skipEmptyRecords) {
							endField();
							endRecord();
						} else {
							dataBuffer.recordStart = dataBuffer.index + 1;
						}
						lastLetter = currentLetter;
					} else if (!useCustomRecordDelimiter && (currentLetter == Characters.CR || currentLetter == Characters.LF)) {
						// this will skip blank lines
						if (fieldStarted || columnsCount > 0 || (!config.skipEmptyRecords && (currentLetter == Characters.CR || lastLetter != Characters.CR))) {
							endField();
							endRecord();
						} else {
							dataBuffer.recordStart = dataBuffer.index + 1;
						}
						lastLetter = currentLetter;
					} else if (config.useComments && columnsCount == 0 && currentLetter == config.comment) {
						lastLetter = currentLetter;
						skipLine();
					} else if (config.trimWhitespace && (currentLetter == Characters.SPACE || currentLetter == Characters.TAB)) {
						fieldStarted = true;
						dataBuffer.fieldStart = dataBuffer.index + 1;
					} else {
						fieldStarted = true;
						dataBuffer.fieldStart = dataBuffer.index;
						boolean lastLetterWasBackslash = false;
						boolean readingComplexEscape = false;
						ComplexEscape escape = ComplexEscape.UNICODE;
						int escapeLength = 0;
						char escapeValue = (char) 0;
						boolean firstLoop = true;
						do {
							if (!firstLoop && dataBuffer.index == dataBuffer.Count) {
								checkDataLength();
							} else {
								if (!firstLoop) {
									// grab the current letter as a char
									currentLetter = dataBuffer.buffer[dataBuffer.index];
								}
								if (!config.useTextQualifier && config.escapeMode == ESCAPE_MODE_BACKSLASH && currentLetter == Characters.BACKSLASH) {
									if (lastLetterWasBackslash) {
										lastLetterWasBackslash = false;
									} else {
										updateCurrentValue();
										lastLetterWasBackslash = true;
									}
								} else if (readingComplexEscape) {
									escapeLength++;
									switch(escape) {
										case UNICODE:
											escapeValue *= (char) 16;
											escapeValue += hexToDec(currentLetter);
											if (escapeLength == 4) {
												readingComplexEscape = false;
											}
											break;
										case OCTAL:
											escapeValue *= (char) 8;
											escapeValue += (char) (currentLetter - '0');
											if (escapeLength == 3) {
												readingComplexEscape = false;
											}
											break;
										case DECIMAL:
											escapeValue *= (char) 10;
											escapeValue += (char) (currentLetter - '0');
											if (escapeLength == 3) {
												readingComplexEscape = false;
											}
											break;
										case HEX:
											escapeValue *= (char) 16;
											escapeValue += hexToDec(currentLetter);
											if (escapeLength == 2) {
												readingComplexEscape = false;
											}
											break;
									}
									if (!readingComplexEscape) {
										appendLetter(escapeValue);
									} else {
										dataBuffer.fieldStart = dataBuffer.index + 1;
									}
								} else if (config.escapeMode == ESCAPE_MODE_BACKSLASH && lastLetterWasBackslash) {
									switch(currentLetter) {
										case 'n':
											appendLetter(Characters.LF);
											break;
										case 'r':
											appendLetter(Characters.CR);
											break;
										case 't':
											appendLetter(Characters.TAB);
											break;
										case 'b':
											appendLetter(Characters.BACKSPACE);
											break;
										case 'f':
											appendLetter(Characters.FORM_FEED);
											break;
										case 'e':
											appendLetter(Characters.ESCAPE);
											break;
										case 'v':
											appendLetter(Characters.VERTICAL_TAB);
											break;
										case 'a':
											appendLetter(Characters.ALERT);
											break;
										case '0':
										case '1':
										case '2':
										case '3':
										case '4':
										case '5':
										case '6':
										case '7':
											escape = ComplexEscape.OCTAL;
											readingComplexEscape = true;
											escapeLength = 1;
											escapeValue = (char) (currentLetter - '0');
											dataBuffer.fieldStart = dataBuffer.index + 1;
											break;
										case 'u':
										case 'x':
										case 'o':
										case 'd':
										case 'U':
										case 'X':
										case 'O':
										case 'D':
											switch(currentLetter) {
												case 'u':
												case 'U':
													escape = ComplexEscape.UNICODE;
													break;
												case 'x':
												case 'X':
													escape = ComplexEscape.HEX;
													break;
												case 'o':
												case 'O':
													escape = ComplexEscape.OCTAL;
													break;
												case 'd':
												case 'D':
													escape = ComplexEscape.DECIMAL;
													break;
											}
											readingComplexEscape = true;
											escapeLength = 0;
											escapeValue = (char) 0;
											dataBuffer.fieldStart = dataBuffer.index + 1;
											break;
										default:
											break;
									}
									lastLetterWasBackslash = false;
								} else {
									if (currentLetter == config.delimiter) {
										endField();
									} else if ((!useCustomRecordDelimiter && (currentLetter == Characters.CR || currentLetter == Characters.LF)) || (useCustomRecordDelimiter && currentLetter == config.recordDelimiter)) {
										endField();
										endRecord();
									}
								}
								// keep track of the last letter because we need
								// it for several key decisions
								lastLetter = currentLetter;
								firstLoop = false;
								if (fieldStarted) {
									dataBuffer.index++;
									if (config.safetySwitch && dataBuffer.index - dataBuffer.fieldStart + fieldBuffer.index > 100000) {
										close();
										throw new IllegalStateException("Maximum column length of 100,000 exceeded in column " + NumberFormat.getIntegerInstance().format(columnsCount) + " in record " + NumberFormat.getIntegerInstance().format(currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting column lengths greater than 100,000 characters to" + " avoid this error.");
									}
								}
							}
						} while (hasMoreData && fieldStarted);
					}
					if (hasMoreData) {
						dataBuffer.index++;
					}
				}
			} while (hasMoreData && !hasReadNextLine);
			if (fieldStarted || lastLetter == config.delimiter) {
				endField();
				endRecord();
			}
		}
		if (config.captureRawRecord) {
			if (hasMoreData) {
				if (rawBuffer.index == 0) {
					rawRecord = new String(dataBuffer.buffer, dataBuffer.recordStart, dataBuffer.index - dataBuffer.recordStart - 1);
				} else {
					rawRecord = new String(rawBuffer.buffer, 0, rawBuffer.index) + new String(dataBuffer.buffer, dataBuffer.recordStart, dataBuffer.index - dataBuffer.recordStart - 1);
				}
			} else {
				rawRecord = new String(rawBuffer.buffer, 0, rawBuffer.index);
			}
		} else {
			rawRecord = "";
		}
		return hasReadNextLine;
	}

	@SneakyThrows
	private void checkDataLength(){
		if (!initialized) {
			if (fileName != null) {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), charset), ReaderSettings.MAX_FILE_BUFFER_SIZE);
			}
			charset = null;
			initialized = true;
		}
		updateCurrentValue();
		if (config.captureRawRecord && dataBuffer.Count > 0) {
			if (rawBuffer.buffer.length - rawBuffer.index < dataBuffer.Count - dataBuffer.recordStart) {
				int newLength = rawBuffer.buffer.length + Math.max(dataBuffer.Count - dataBuffer.recordStart, rawBuffer.buffer.length);
				char[] holder = new char[newLength];
				System.arraycopy(rawBuffer.buffer, 0, holder, 0, rawBuffer.index);
				rawBuffer.buffer = holder;
			}
			System.arraycopy(dataBuffer.buffer, dataBuffer.recordStart, rawBuffer.buffer, rawBuffer.index, dataBuffer.Count - dataBuffer.recordStart);
			rawBuffer.index += dataBuffer.Count - dataBuffer.recordStart;
		}
		try {
			dataBuffer.Count = reader.read(dataBuffer.buffer, 0, dataBuffer.buffer.length);
		} catch (IOException ex) {
			close();
			throw ex;
		}
		if (dataBuffer.Count == -1) {
			hasMoreData = false;
		}
		dataBuffer.index = 0;
		dataBuffer.recordStart = 0;
		dataBuffer.fieldStart = 0;
	}

	public boolean readHeaders(){
		boolean result = readRecord();
		headersHolder.Length = columnsCount;
		headersHolder.Headers = new String[columnsCount];
		for (int i = 0; i < headersHolder.Length; i++) {
			String columnValue = get(i);
			if(config.trimHeaders)columnValue = columnValue.trim();
			headersHolder.Headers[i] = columnValue;
			headersHolder.IndexByName.put(columnValue, Integer.valueOf(i));
		}
		if (result) {
			currentRecord--;
		}
		columnsCount = 0;
		return result;
	}

	public String getHeader(int columnIndex) throws IOException {
		checkClosed();
		if (columnIndex > -1 && columnIndex < headersHolder.Length) {
			return headersHolder.Headers[columnIndex];
		} else {
			return "";
		}
	}

	public boolean isQualified(int columnIndex){
		checkClosed();
		if (columnIndex < columnsCount && columnIndex > -1) {
			return isQualified[columnIndex];
		} else {
			return false;
		}
	}

	private void endField() {
		String currentValue = "";
		if (fieldStarted) {
			if (fieldBuffer.index == 0) {
				if (dataBuffer.fieldStart < dataBuffer.index) {
					int lastLetter = dataBuffer.index - 1;
					if (config.trimWhitespace && !startedWithQualifier) {
						while (lastLetter >= dataBuffer.fieldStart && (dataBuffer.buffer[lastLetter] == Characters.SPACE || dataBuffer.buffer[lastLetter] == Characters.TAB)) {
							lastLetter--;
						}
					}
					currentValue = new String(dataBuffer.buffer, dataBuffer.fieldStart, lastLetter - dataBuffer.fieldStart + 1);
				}
			} else {
				updateCurrentValue();
				int lastLetter = fieldBuffer.index - 1;
				if (config.trimWhitespace && !startedWithQualifier) {
					while (lastLetter >= 0 && (fieldBuffer.buffer[lastLetter] == Characters.SPACE || fieldBuffer.buffer[lastLetter] == Characters.TAB)) {
						lastLetter--;
					}
				}
				currentValue = new String(fieldBuffer.buffer, 0, lastLetter + 1);
			}
		}
		fieldBuffer.index = 0;
		fieldStarted = false;
		if (columnsCount >= 100000 && config.safetySwitch) {
			close();
			throw new IllegalStateException("Maximum column count of 100,000 exceeded in record " + NumberFormat.getIntegerInstance().format(currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting more than 100,000 columns per record to" + " avoid this error.");
		}
		if (columnsCount == values.length) {
			// holder array needs to grow to be able to hold another column
			int newLength = values.length * 2;
			String[] holder = new String[newLength];
			System.arraycopy(values, 0, holder, 0, values.length);
			values = holder;
			boolean[] qualifiedHolder = new boolean[newLength];
			System.arraycopy(isQualified, 0, qualifiedHolder, 0, isQualified.length);
			isQualified = qualifiedHolder;
		}
		values[columnsCount] = currentValue;
		isQualified[columnsCount] = startedWithQualifier;
		columnsCount++;
	}

	private void appendLetter(char letter) {
		if (fieldBuffer.index == fieldBuffer.buffer.length) {
			int newLength = fieldBuffer.buffer.length * 2;
			char[] holder = new char[newLength];
			System.arraycopy(fieldBuffer.buffer, 0, holder, 0, fieldBuffer.index);
			fieldBuffer.buffer = holder;
		}
		fieldBuffer.buffer[fieldBuffer.index++] = letter;
		dataBuffer.fieldStart = dataBuffer.index + 1;
	}

	private void updateCurrentValue() {
		if (fieldStarted && dataBuffer.fieldStart < dataBuffer.index) {
			if (fieldBuffer.buffer.length - fieldBuffer.index < dataBuffer.index - dataBuffer.fieldStart) {
				int newLength = fieldBuffer.buffer.length + Math.max(dataBuffer.index - dataBuffer.fieldStart, fieldBuffer.buffer.length);
				char[] holder = new char[newLength];
				System.arraycopy(fieldBuffer.buffer, 0, holder, 0, fieldBuffer.index);
				fieldBuffer.buffer = holder;
			}
			System.arraycopy(dataBuffer.buffer, dataBuffer.fieldStart, fieldBuffer.buffer, fieldBuffer.index, dataBuffer.index - dataBuffer.fieldStart);
			fieldBuffer.index += dataBuffer.index - dataBuffer.fieldStart;
		}
		dataBuffer.fieldStart = dataBuffer.index + 1;
	}

	private void endRecord()  {
		hasReadNextLine = true;
		currentRecord++;
	}

	public int getIndex(String headerName) {
		checkClosed();
		Object indexValue = headersHolder.IndexByName.get(headerName);
		if (indexValue != null) {
			return ((Integer) indexValue).intValue();
		} else {
			return -1;
		}
	}

	public boolean skipRecord(){
		checkClosed();
		boolean recordRead = false;
		if (hasMoreData) {
			recordRead = readRecord();
			if (recordRead) {
				currentRecord--;
			}
		}
		return recordRead;
	}

	public boolean skipLine() {
		checkClosed();
		// clear public column values for current line
		columnsCount = 0;
		boolean skippedLine = false;
		if (hasMoreData) {
			boolean foundEol = false;
			do {
				if (dataBuffer.index == dataBuffer.Count) {
					checkDataLength();
				} else {
					skippedLine = true;
					char currentLetter = dataBuffer.buffer[dataBuffer.index];
					if (currentLetter == Characters.CR || currentLetter == Characters.LF) {
						foundEol = true;
					}
					lastLetter = currentLetter;
					if (!foundEol) {
						dataBuffer.index++;
					}
				}
			} while (hasMoreData && !foundEol);
			fieldBuffer.index = 0;
			dataBuffer.recordStart = dataBuffer.index + 1;
		}
		rawBuffer.index = 0;
		rawRecord = "";
		return skippedLine;
	}

	public void close() {
		if (!closed) {
			close(true);
			closed = true;
		}
	}

	private void close(boolean closing) {
		if (!closed) {
			if (closing) {
				charset = null;
				headersHolder.Headers = null;
				headersHolder.IndexByName = null;
				dataBuffer.buffer = null;
				fieldBuffer.buffer = null;
				rawBuffer.buffer = null;
			}
			try {
				if (initialized) {
					reader.close();
				}
			} catch (Exception e) {
			// just eat the exception
			}
			reader = null;
			closed = true;
		}
	}

	private void checkClosed() {
		if (closed) {
			throw new IllegalStateException("This instance of the CsvReader class has already been closed.");
		}
	}

	protected void finalize() {
		close(false);
	}

	private enum ComplexEscape {
		UNICODE,
		OCTAL,
		DECIMAL,
		HEX
	}

	private static char hexToDec(char hex) {
		char result;
		if (hex >= 'a') {
			result = (char) (hex - 'a' + 10);
		} else if (hex >= 'A') {
			result = (char) (hex - 'A' + 10);
		} else {
			result = (char) (hex - '0');
		}
		return result;
	}

	private static class Buffer {

		public char[] buffer;

		public int index;

		public int Count;

		public int fieldStart;

		public int recordStart;

		public Buffer() {
			buffer = new char[ReaderSettings.MAX_BUFFER_SIZE];
			index = 0;
			Count = 0;
			fieldStart = 0;
			recordStart = 0;
		}
	}

	private static class BufferDataField {

		public char[] buffer;

		public int index;

		public BufferDataField() {
			buffer = new char[ReaderSettings.INITIAL_COLUMN_BUFFER_SIZE];
			index = 0;
		}
	}

	private static class BufferDataRawRecord {

		public char[] buffer;

		public int index;

		public BufferDataRawRecord() {
			buffer = new char[ReaderSettings.INITIAL_COLUMN_BUFFER_SIZE * ReaderSettings.INITIAL_COLUMN_COUNT];
			index = 0;
		}
	}
	

	private static class HeadersHolder {
		public String[] Headers;

		public int Length;

		public HashMap<String, Integer> IndexByName;

		public HeadersHolder() {
			Headers = null;
			Length = 0;
			IndexByName = new HashMap<String, Integer>();
		}
	}

}
