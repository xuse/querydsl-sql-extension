/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.xuse.querydsl.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Generated;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IOUtils {
	/**
	 * Write the memory data to the file.
	 * 
	 * @param file file
	 * @param data data
	 */
	public static void saveAsFile(File file, byte[] data) {
		saveAsFile(file, false, data);
	}

	/**
	 * Close the resource, without throwing exceptions.
	 *
	 * @param resource the resource to close.
	 */
	public static void closeQuietly(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				log.error("IO error", e);
			}
		}
	}

	@SneakyThrows
	public static String toString(Reader reader) {
		if (reader == null)
			return null;
		StringBuilder sb = new StringBuilder(128);
		char[] buf = new char[1024];
		int n;
		while ((n = reader.read(buf)) > -1) {
			sb.append(buf, 0, n);
		}
		return sb.toString();
	}

	/**
	 * 将指定位置的数据读出成为文本
	 *
	 * @param url     资源位置
	 * @param charset 字符编码，可以传入null
	 * @return 读到的文本
	 */
	@SneakyThrows
	public static String toString(URL url, Charset charset) {
		if (url == null)
			return null;
		try (Reader reader = new InputStreamReader(url.openStream(), charset)) {
			return toString(reader);
		}
	}

	@SneakyThrows
	public static String toString(InputStream in, Charset charset) {
		try (Reader reader = new InputStreamReader(in, charset)) {
			return toString(reader);
		}
	}

	/**
	 * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
	 * 
	 * @param bufferSize Size of internal buffer to use.
	 * @param input      Reader
	 * @param output     Writer
	 */
	@SneakyThrows
	public static final void copy(Reader input, Writer output, int bufferSize) {
		if (bufferSize <= 0) {
			bufferSize = 1024;
		}
		char[] buffer = new char[bufferSize];
		int n = 0;
		while ((n = input.read(buffer)) != -1) {
			output.write(buffer, 0, n);
		}
	}

	/**
	 * Copy chars from a <code>InputStream</code> to a <code>OutputStream</code> .
	 * 
	 * @param bufferSize Size of internal buffer to use.
	 * @param input      InputStream
	 * @param output     OutputStream
	 * @return bytes of copied.
	 */
	@SneakyThrows
	public static final int copy(InputStream input, OutputStream output, int bufferSize) {
		byte[] buffer = new byte[bufferSize];
		int total = 0;
		int n = 0;
		while ((n = input.read(buffer)) != -1) {
			total += n;
			output.write(buffer, 0, n);
		}
		return total;
	}

	/**
	 * 文件(目录)重新命名
	 *
	 * @param file      要处理的文件或目录
	 * @param newName   修改后的文件名（不含路径）。
	 * @param overwrite 覆盖模式，如果目标文件已经存在，则删除目标文件后再改名
	 * @return 如果成功改名，返回改名后的file对象，否则返回null。
	 */
	@Generated
	public static File rename(File file, String newName, boolean overwrite) {
		File target = new File(file.getParentFile(), newName);
		if (target.exists()) {
			if (overwrite) {
				if (!target.delete())
					return null;
			} else {
				return null;
			}
		}
		return file.renameTo(target) ? target : null;
	}

	/**
	 * @param file file
	 * @param iss  iss
	 */
	@SneakyThrows
	public static void saveAsFile(File file, InputStream... iss) {
		ensureParentFolder(file);
		try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
			for (InputStream is : iss) {
				copy(is, os, 2048);
			}
		}
	}

	/**
	 * 将文字写入文件
	 * 
	 * @param texts texts
	 * @param file  file
	 */
	public static void saveAsFile(File file, String... texts) {
		saveAsFile(file, Charset.defaultCharset(), texts);
	}

	@SneakyThrows
	public static void saveAsFile(File file, Charset charset, String[] texts) {
		try (BufferedWriter os = getWriter(file, charset == null ? null : charset, false)) {
			for (String text : texts) {
				os.write(text);
			}
		}
	}
	
	@SneakyThrows
	public static BufferedReader getUTF8Reader(URL target) {
		Assert.notNull(target);
		return new BufferedReader(new InputStreamReader(target.openStream(), StandardCharsets.UTF_8));
	}

	@SneakyThrows
	public static BufferedReader getUTF8Reader(File target) {
		Assert.notNull(target);
		return new BufferedReader(new InputStreamReader(new FileInputStream(target), StandardCharsets.UTF_8));
	}

	public static BufferedWriter getUTF8Writer(File target, boolean append) {
		Assert.notNull(target);
		return getWriter(target, StandardCharsets.UTF_8, append);
	}

	@SneakyThrows
	public static BufferedWriter getWriter(File target, Charset charSet, boolean append) {
		ensureParentFolder(target);
		OutputStream os = new FileOutputStream(target, append);
		if (charSet == null)
			charSet = Charset.defaultCharset();
		OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
		return new BufferedWriter(osw);
	}

	/**
	 * 检查/创建文件在所的文件夹。 如果该文件所在的文件夹已存在，什么也不做。 如果该文件所在的文件夹不存在，则创建
	 *
	 * @param file 要检查的路径
	 */
	public static void ensureParentFolder(File file) {
		File f = file.getParentFile();
		if (f != null && !f.exists()) {
			f.mkdirs();
		} else if (f != null && f.isFile()) {
			throw new RuntimeException(f.getAbsolutePath() + " is a exist file, unable to create directory.");
		}
	}

	/**
	 * 给定一个File,确认其不存在于在磁盘上，如果存在就改名以回避 <br>
	 * 这个方法用于向磁盘输出文件时使用。<br>
	 * 比如输出名为 report.txt时，如果发现上一次的report.txt还在那么就会返回 "report(1).txt"。
	 * 如果"report(1).txt"也存在就会返回"report(2).txt"。 以此类推。
	 *
	 * @param file 目标文件
	 * @return 如果目标文件不存在，返回本身。如果目标文件已存在，就返回一个带后缀而磁盘上不存在的文件。
	 */
	public static File escapeExistFile(File file) {
		if (!file.exists())
			return file;
		int pos = file.getName().lastIndexOf(".");
		String path = file.getParent();
		if (StringUtils.isEmpty(path)) {
			path = file.getAbsoluteFile().getParent();
		}
		if (StringUtils.isEmpty(path)) {
			throw new IllegalArgumentException(file.getAbsolutePath() + " has no valid parent folder.");
		}
		String baseFilename = null;
		String extName = null;
		if (pos > -1) {
			baseFilename = file.getName().substring(0, pos);
			extName = file.getName().substring(pos + 1);
		} else {
			baseFilename = file.getName();
		}
		int n = 1;
		while (file.exists()) {
			file = new File(path + "/" + baseFilename + "(" + n + ")" + ((extName == null) ? "" : "." + extName));
			n++;
		}
		return file;
	}

	/**
	 * 将序列化数据还原为对象
	 * 
	 * @param data data
	 * @return Object
	 */
	public static Object deserialize(byte[] data) {
		if (data == null) {
			return null;
		}
		return loadObject(new ByteArrayInputStream(data));
	}

	@SneakyThrows
	public static byte[] serialize(Serializable obj) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			saveObject(obj, out);
			return out.toByteArray();
		}
	}

	/**
	 * 将可序列化的对象存储到流中
	 * 
	 * @param obj    obj
	 * @param output output
	 */
	@SneakyThrows
	public static void saveObject(Serializable obj, OutputStream output) {
		ObjectOutputStream out = new ObjectOutputStream(output);
		out.writeObject(obj);
	}

	/**
	 * 将可序列化的对象保存到磁盘文件
	 * 
	 * @param obj  obj
	 * @param file file
	 */
	@SneakyThrows
	public static void saveObject(Serializable obj, File file) {
		ensureParentFolder(file);
		try (OutputStream os = new FileOutputStream(file)) {
			saveObject(obj, os);
		}
	}

	/**
	 * 从流读取序列化对象
	 * 
	 * @param inn inn
	 * @return Object
	 */
	@SneakyThrows
	public static Object loadObject(InputStream inn) {
		ObjectInputStream in = (inn instanceof ObjectInputStream) ? (ObjectInputStream) inn
				: new ObjectInputStream(inn);
		Object obj = in.readObject();
		return obj;
	}

	/**
	 * Get the file extension (in lowercase, returns an empty string if there is no
	 * extension). If the provided filename contains a path, only the portion after
	 * the last \ or / character will be considered as the filename during parsing.
	 * 
	 * 
	 * @param fileName fileName
	 * @return String
	 */
	public static String getExtName(String fileName) {
		int dashIndex1 = fileName.lastIndexOf('/');
		int dashIndex2 = fileName.lastIndexOf('\\');
		// 获得最后一个斜杠的位置
		int dash = Math.max(dashIndex1, dashIndex2);
		int pos = fileName.lastIndexOf(".");
		if (pos > -1 && pos > dash) {
			return fileName.substring(pos + 1).toLowerCase();
		} else {
			return "";
		}
	}

	/**
	 * Get the part of the filename excluding the extension. If the provided
	 * filename contains a path, only the portion after the last \ or / character
	 * will be considered as the filename during parsing. After removing the
	 * extension, the result will include the path portion.
	 * @param fileName fileName
	 * @return String
	 */
	public static String removeExt(String fileName) {
		int dashIndex1 = fileName.lastIndexOf('/');
		int dashIndex2 = fileName.lastIndexOf('\\');
		// 获得最后一个斜杠的位置
		int dash = Math.max(dashIndex1, dashIndex2);
		int pos = fileName.lastIndexOf('.');
		if (pos > -1 && pos > dash) {
			return fileName.substring(0, pos);
		}
		return fileName;
	}

	@SneakyThrows
	public static byte[] toByteArray(URL url) {
		try (InputStream in = url.openStream()) {
			return toByteArray(in, -1);
		}
	}

	@SneakyThrows
	public static byte[] toByteArray(File file) {
		try (InputStream in = new FileInputStream(file)) {
			return toByteArray(in, -1);
		}
	}

	/**
	 * Read stream data into memory. Note that this method will read the entire data
	 * stream into memory, so it is not suitable for very large data.
	 * 
	 * @param in in
	 * @return byte array of the content.
	 */
	public static byte[] toByteArray(InputStream in) {
		byte[] msg = toByteArray(in, -1);
		return msg;
	}

	/**
	 * Read the specified number of bytes from the stream。
	 * This is the third version, performance improved again. it takes 125ms to read the first 60M from a 120M file, while v2 takes 156ms.
	 * 
	 * @param in     in
	 * @param length 要读取的字节数，-1表示不限制。
	 * @return byte[]
	 */
	@SneakyThrows
	public static byte[] toByteArray(InputStream in, int length) {
		byte[] message;
		int buf = 2048;
		byte[] pBuffer = new byte[buf];
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(length > 0 ? length : 1024)) {
			int n;
			if (length < 0) {
				while ((n = in.read(pBuffer)) >= 0) {
					out.write(pBuffer, 0, n);
				}
			} else {
				int left = length;
				while (left >= buf) {
					if ((n = in.read(pBuffer)) == -1) {
						left = 0;
						break;
					}
					left -= n;
					out.write(pBuffer, 0, n);
				}
				while (left > 0) {
					if ((n = in.read(pBuffer, 0, left)) == -1) {
						break;
					}
					left -= n;
					out.write(pBuffer, 0, n);
				}
			}
			message = out.toByteArray();
		}
		return message;
	}

	private static final File[] EMPTY = new File[0];

	/**
	 * Recursively list files in the directory. An extension can be specified.
	 * 
	 * @param dir      Directory to list.
	 * @param extnames 需要的文件类型（扩展名）。要求小写，无需带'.'符号。
	 * @return 该目录下符合指定类型的所有文件(只搜索一层，不会递归搜索)。<strong>仅列出文件，不会返回目录</strong>
	 */
	public static File[] listFiles(File dir, final String... extnames) {
		boolean isAll = extnames.length == 0;
		File[] r = dir.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isFile() && (isAll || ArrayUtils.contains(extnames, getExtName(f.getName())));
			}
		});
		return r == null ? EMPTY : r;
	}

	/**
	 * List sub directories in the specified directory.
	 *
	 * @param root Specified directory
	 * @return 该目录下的所有文件夹
	 */
	public static File[] listFolders(File root) {
		File[] r = root.listFiles(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		return r == null ? EMPTY : r;
	}

	/**
	 * 将文件移动为指定的新文件
	 * 
	 * @param oldFile oldFile
	 * @param newFile newFile
	 * @return Whether the deletion was successful.
	 */
	public static boolean move(File oldFile, File newFile) {
		Assert.notNull(oldFile, "The source file is null!");
		Assert.notNull(newFile, "The target file is null!");
		Assert.isTrue(oldFile.exists(), "Source file [" + oldFile.getAbsolutePath() + "] doesn't exist.");
		Assert.isFalse(newFile.exists(), "target file already exist!");
		ensureParentFolder(newFile);
		return oldFile.renameTo(newFile);
	}

	/**
	 * Delete the specified directory tree.
	 * 
	 * @param file        file or directory to delete.
	 * @param deleteChild delete children files or not.
	 * @return Whether the deletion was successful
	 */
	public static boolean deleteTree(File file, boolean deleteChild) {
		Assert.notNull(file);
		if (!file.exists())
			return true;
		if (deleteChild && file.isDirectory()) {
			for (File sub : listFolders(file)) {
				if (!deleteTree(sub, true))
					return false;
			}
			for (File sub : listFiles(file)) {
				if (!sub.delete())
					return false;
			}
		}
		return file.delete();
	}

	/**
	 * Save the specified byte array as a file.
	 * 
	 * @param data   data
	 * @param file   file
	 * @param append boolean
	 */
	@SneakyThrows
	public static void saveAsFile(File file, boolean append, byte[] data) {
		ensureParentFolder(file);
		try (OutputStream out = new FileOutputStream(file, append)) {
			out.write(data);
		}
	}

	/**
	 * Save the specified stream as a temporary file.
	 * 
	 * @param is is
	 * @return File
	 */
	@SneakyThrows
	public static File saveAsTempFile(InputStream is) {
		File f = File.createTempFile("~tmp", ".io");
		saveAsFile(f, is);
		return f;
	}

	/**
	 * 获得配置文件的项目。配置文件用= :等分隔对，语法同properties文件
	 * <p>
	 * 使用此方法可以代替使用JDK中的{@link java.util.Properties}工具。
	 * 
	 * @param in 要读取的资源
	 * @return 文件中的键值对信息。
	 */
	public static Map<String, String> loadProperties(URL in) {
		return loadProperties(in, false);
	}

	/**
	 * @implSpec 开启section支持后可以支持ini格式文件，ini文件中通过[section
	 *           name]方式将文件分成多段，每段的下可以有重复的配置项名称。
	 *           为了区分不同section下的相同配置，开启section支持后key的格式为 {@code section|key}。
	 * @param in             URL
	 * @param supportSection true可以开启section支持。
	 * @return 配置项内容
	 */
	@SneakyThrows
	public static Map<String, String> loadProperties(URL in, Boolean supportSection) {
		Assert.notNull(in);
		Map<String, String> result = new LinkedHashMap<String, String>();
		try (LineReader reader = new LineReader(new BufferedReader(new InputStreamReader(in.openStream())))) {
			load0(reader, result, supportSection);
		}
		return result;
	}

	@Generated
	private static void load0(LineReader lr, Map<String, String> map, Boolean supportSection) throws IOException {
		char[] convtBuf = new char[1024];
		int limit;
		int keyLen;
		int valueStart;
		char c;
		boolean hasSep;
		boolean precedingBackslash;
		String currentSection = null;
		while ((limit = lr.readLine()) >= 0) {
			c = 0;
			keyLen = 0;
			valueStart = limit;
			hasSep = false;
			precedingBackslash = false;
			while (keyLen < limit) {
				c = lr.lineBuf[keyLen];
				// need check if escaped.
				if ((c == '=' || c == ':') && !precedingBackslash) {
					valueStart = keyLen + 1;
					hasSep = true;
					break;
				} else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
					valueStart = keyLen + 1;
					break;
				}
				if (c == '\\') {
					precedingBackslash = !precedingBackslash;
				} else {
					precedingBackslash = false;
				}
				keyLen++;
			}
			while (valueStart < limit) {
				c = lr.lineBuf[valueStart];
				if (c != ' ' && c != '\t' && c != '\f') {
					if (!hasSep && (c == '=' || c == ':')) {
						hasSep = true;
					} else {
						break;
					}
				}
				valueStart++;
			}
			String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
			String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
			if (supportSection == null) {
				supportSection = isSection(key);
			}
			if (supportSection && value.length() == 0 && isSection(key)) {
				currentSection = key.length() > 2 ? key.substring(1, key.length() - 1) : null;
			} else {
				if (currentSection == null) {
					map.put(key, value);
				} else {
					map.put(currentSection + "|" + key, value);
				}
			}
		}
	}

	/*
	 * the code was copied from JDK class Properties.
	 */
	@Generated
	static final class LineReader implements AutoCloseable {
		private char[] inCharBuf;

		private char[] lineBuf = new char[1024];

		private int inLimit = 0;

		private int inOff = 0;

		private Reader reader;

		public LineReader(Reader reader) {
			this.reader = reader;
			inCharBuf = new char[8192];
		}

		int readLine() throws IOException {
			int len = 0;
			char c = 0;
			boolean skipWhiteSpace = true;
			boolean isCommentLine = false;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;
			while (true) {
				if (inOff >= inLimit) {
					inLimit = reader.read(inCharBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0 || isCommentLine) {
							return -1;
						}
						return len;
					}
				}
				c = inCharBuf[inOff++];
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
					if (c == '#' || c == '!') {
						isCommentLine = true;
						continue;
					}
				}
				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					// flip the preceding backslash flag
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
				} else {
					// reached EOL
					if (isCommentLine || len == 0) {
						isCommentLine = false;
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = reader.read(inCharBuf);
						inOff = 0;
						if (inLimit <= 0) {
							return len;
						}
					}
					if (precedingBackslash) {
						len -= 1;
						// skip the leading whitespace characters in following
						// line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						return len;
					}
				}
			}
		}

		@Override
		public void close() {
			closeQuietly(reader);
		}
	}

	private static boolean isSection(String key) {
		if (key == null || key.length() < 2) {
			return false;
		}
		return key.charAt(0) == '[' && key.charAt(key.length() - 1) == ']';
	}

	/*
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved chars
	 * to their original forms
	 */
	private static String loadConvert(char[] in, int off, int len, char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;
		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = in[off++];
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = (char) aChar;
			}
		}
		return new String(out, 0, outLen);
	}

	@SneakyThrows
	public static BufferedInputStream getInputStream(File file) {
		return new BufferedInputStream(new FileInputStream(file));
	}

	/**
	 * 将输入流中所有非[0-9a-fA-F]以外的字符全部丢弃，然后作为16进制文本，转换为原始的字节数组。 最长不超过512个字符。
	 * 
	 * @param input input
	 * @throws IOException If encounter IOException
	 * @return byte[] value
	 */
	public static byte[] readHexString(Reader input) throws IOException {
		CharArrayWriter cw = new CharArrayWriter(512);
		int c;
		while ((c = input.read()) > -1) {
			if (c < 48 || (c > 57 && c < 65) || (c > 70 && c < 97) || c > 102) {
				continue;
			}
			cw.append((char) c);
		}
		return StringUtils.fromHex(cw.toCharArray(), false);
	}
	
	/**
	 * 相对路径计算，计算从folder出发，到达file的相对路径
	 * 
	 * @param file
	 * @param folder
	 * @return
	 */
	public static String getRelativepath(String file, String folder) {
		String[] f1 = StringUtils.split(file.replace('/', '\\'), '\\');
		String[] f2 = StringUtils.split(folder.replace('/', '\\'), '\\');
		int breakCount = -1;
		for (int i = 0; i < f1.length; i++) {
			String str = f1[i];
			if (i < f2.length && str.equals(f2[i])) {
				breakCount = i + 1;
			} else {
				break;
			}
		}
		if (breakCount == -1)
			return file;
		StringBuilder sb = new StringBuilder();
		sb.append(StringUtils.repeat(".." + File.separator, f2.length - breakCount));
		for (int i = breakCount; i < f1.length; i++) {
			sb.append(f1[i]).append(File.separatorChar);
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * 相对路径计算，计算从folder出发，到达file的相对路径
	 * 
	 * @param file
	 * @param folder
	 */
	public static String getRelativepath(File file, File folder) {
		String s1 = getCanonicalPath(file);
		String s2 = getCanonicalPath(folder);
		return getRelativepath(s1, s2);
	}
	
	public static String getCanonicalPath(File file) {
		Assert.notNull(file);
		try {
			return file.getCanonicalPath();
		} catch (IOException e) {
			return file.getAbsolutePath();
		}
	}

}
