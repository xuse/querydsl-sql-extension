package com.github.xuse.querydsl.init.csv;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

import com.github.xuse.querydsl.util.IOUtils;

import lombok.SneakyThrows;

public class CsvFileReader implements Closeable {

	private Reader reader = null;

	private final ReaderSettings config = new ReaderSettings();

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

	private String[] values = new String[INITIAL_COLUMN_COUNT];

	@SneakyThrows
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
		this.reader=new InputStreamReader(new FileInputStream(fileName),charset);
		this.config.delimiter = delimiter;
		isQualified = new boolean[values.length];
	}

	public CsvFileReader(File fileName, Charset charset) {
		this(fileName, Characters.COMMA, charset);
	}

	@SneakyThrows
	public CsvFileReader(URL url, Charset charset) {
		if (url == null) {
			throw new IllegalArgumentException("Parameter url can not be null.");
		}
		this.reader = new InputStreamReader(url.openStream(),charset);
		isQualified = new boolean[values.length];
	}

	public CsvFileReader(Reader inputStream) {
		if (inputStream == null) {
			throw new IllegalArgumentException("Parameter reader can not be null.");
		}
		this.reader = inputStream;
		isQualified = new boolean[values.length];
	}
	
	public ReaderSettings getSettings() {
		return config;
	}

	public String getRawRecord() {
		return rawRecord;
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

	public String[] getHeaders() throws IOException {
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

	public String[] getValues() throws IOException {
		checkClosed();
		// need to return a clone, and can't use clone because values.Length
		// might be greater than columnsCount
		String[] clone = new String[columnsCount];
		System.arraycopy(values, 0, clone, 0, columnsCount);
		return clone;
	}

	public String get(int columnIndex) throws IOException {
		checkClosed();
		if (columnIndex > -1 && columnIndex < columnsCount) {
			return values[columnIndex];
		} else {
			return "";
		}
	}

	public String get(String headerName) throws IOException {
		checkClosed();
		return get(getIndex(headerName));
	}

	public static CsvFileReader parse(String data) {
		if (data == null) {
			throw new IllegalArgumentException("Parameter data can not be null.");
		}
		return new CsvFileReader(new StringReader(data));
	}

	public boolean readRecord() throws IOException {
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
						if (config.escapeMode == EscapeMode.BACKSLASH) {
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
									} else if ((currentLetter == Characters.CR || currentLetter == Characters.LF)) {
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
										if (config.escapeMode == EscapeMode.DOUBLED) {
											lastLetterWasEscape = true;
										}
										lastLetterWasQualifier = true;
									}
								} else if (config.escapeMode == EscapeMode.BACKSLASH && lastLetterWasEscape) {
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
										} else if ((currentLetter == Characters.CR || currentLetter == Characters.LF)) {
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
								}
							}
						} while (hasMoreData && fieldStarted);
					} else if (currentLetter == config.delimiter) {
						lastLetter = currentLetter;
						endField();
					} else if (currentLetter == Characters.CR || currentLetter == Characters.LF) {
						// this will skip blank lines
						if (fieldStarted || columnsCount > 0) {
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
								if (!config.useTextQualifier && config.escapeMode == EscapeMode.BACKSLASH && currentLetter == Characters.BACKSLASH) {
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
								} else if (config.escapeMode == EscapeMode.BACKSLASH && lastLetterWasBackslash) {
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
									} else if (currentLetter == Characters.CR || currentLetter == Characters.LF) {
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

	private void checkDataLength() throws IOException {
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

	public boolean readHeaders() throws IOException {
		boolean result = readRecord();
		headersHolder.Length = columnsCount;
		headersHolder.Headers = new String[columnsCount];
		for (int i = 0; i < headersHolder.Length; i++) {
			String columnValue = get(i);
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

	public boolean isQualified(int columnIndex) throws IOException {
		checkClosed();
		if (columnIndex < columnsCount && columnIndex > -1) {
			return isQualified[columnIndex];
		} else {
			return false;
		}
	}

	private void endField() throws IOException {
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

	public int getIndex(String headerName) throws IOException {
		checkClosed();
		Object indexValue = headersHolder.IndexByName.get(headerName);
		if (indexValue != null) {
			return ((Integer) indexValue).intValue();
		} else {
			return -1;
		}
	}

	public boolean skipRecord() throws IOException {
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

	public boolean skipLine() throws IOException {
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
		if(reader!=null) {
			IOUtils.closeQuietly(reader);
			reader=null;
			headersHolder.Headers = null;
			headersHolder.IndexByName = null;
			dataBuffer.buffer = null;
			fieldBuffer.buffer = null;
			rawBuffer.buffer = null;
		}
	}

	private void checkClosed() throws IOException {
		if (reader==null) {
			throw new IOException("This instance of the CsvReader class has already been closed.");
		}
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
			buffer = new char[2048];
			index = 0;
			Count = 0;
			fieldStart = 0;
			recordStart = 0;
		}
	}


	public static final int INITIAL_COLUMN_COUNT = 10;

	public static final int INITIAL_COLUMN_BUFFER_SIZE = 50;
	private static class BufferDataField {

		public char[] buffer;

		public int index;

		public BufferDataField() {
			buffer = new char[INITIAL_COLUMN_BUFFER_SIZE];
			index = 0;
		}
	}

	private static class BufferDataRawRecord {

		public char[] buffer;

		public int index;

		public BufferDataRawRecord() {
			buffer = new char[INITIAL_COLUMN_BUFFER_SIZE * INITIAL_COLUMN_COUNT];
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
