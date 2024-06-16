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
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * IO utility methods.
 *
 * <b>Note: Why not use commons-io?</b> While many of these utility methods are
 * also provided by the Apache commons-io library we prefer to our own
 * implementation to, using a external library might cause additional
 * constraints on users embedding FtpServer.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
@Slf4j
public class IOUtils {

	/**
	 * 将内存数据块写入文件
	 * 
	 * @param file
	 * @param data
	 * @throws IOException
	 */
	public static void saveAsFile(File file, byte[] data) throws IOException {
		saveAsFile(file, false, data);
	}

	/**
	 * 将可序列化的对象保存到磁盘文件
	 * 
	 * @param aaa
	 * @param filePath
	 * @return true if saved.
	 */
	public static boolean saveObject(Serializable aaa, String filePath) {
		return saveObject(aaa, new File(filePath));
	}

	/**
	 * 关闭指定的对象，不会抛出异常
	 * 
	 * @param input 需要关闭的资源
	 */
	public static void closeQuietly(Closeable input) {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				log.error("IO error", e);
			}
		}
	}

	/**
	 * Random number generator to make unique file name
	 */
	private final static Random RANDOM_GEN = new Random(System.currentTimeMillis());

	/**
	 * Get a <code>BufferedInputStream</code>.
	 */
	public final static BufferedInputStream getBufferedInputStream(InputStream in) {
		BufferedInputStream bin = null;
		if (in instanceof java.io.BufferedInputStream) {
			bin = (BufferedInputStream) in;
		} else {
			bin = new BufferedInputStream(in);
		}
		return bin;
	}

	/**
	 * Get a <code>BufferedOutputStream</code>.
	 */
	public final static BufferedOutputStream getBufferedOutputStream(OutputStream out) {
		BufferedOutputStream bout = null;
		if (out instanceof java.io.BufferedOutputStream) {
			bout = (BufferedOutputStream) out;
		} else {
			bout = new BufferedOutputStream(out);
		}
		return bout;
	}

	public static String toString(InputStream in, Charset charset) throws IOException {
		try (Reader reader = new InputStreamReader(in, charset)) {
			return toString(reader);
		}
	}

	public static String toString(Reader reader) throws IOException {
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
	 * @throws IOException IO操作异常
	 **/
	public static String toString(URL url, Charset charset) throws IOException {
		if (url == null)
			return null;
		return toString(new InputStreamReader(url.openStream(), charset));
	}

	/**
	 * Get <code>BufferedReader</code>.
	 */
	public final static BufferedReader getBufferedReader(Reader reader) {
		BufferedReader buffered = null;
		if (reader instanceof java.io.BufferedReader) {
			buffered = (BufferedReader) reader;
		} else {
			buffered = new BufferedReader(reader);
		}
		return buffered;
	}

	/**
	 * Get <code>BufferedWriter</code>.
	 */
	public final static BufferedWriter getBufferedWriter(Writer wr) {
		BufferedWriter bw = null;
		if (wr instanceof java.io.BufferedWriter) {
			bw = (BufferedWriter) wr;
		} else {
			bw = new BufferedWriter(wr);
		}
		return bw;
	}

	/**
	 * Get unique file object.
	 */
	public final static File getUniqueFile(File oldFile) {
		File newFile = oldFile;
		while (true) {
			if (!newFile.exists()) {
				break;
			}
			newFile = new File(oldFile.getAbsolutePath() + '.' + Math.abs(RANDOM_GEN.nextLong()));
		}
		return newFile;
	}

	/**
	 * No exception <code>InputStream</code> close method.
	 */
	public final static void close(InputStream is) {
		if (is != null) {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * No exception <code>OutputStream</code> close method.
	 */
	public final static void close(OutputStream os) {
		if (os != null) {
			try {
				os.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * No exception <code>java.io.Reader</code> close method.
	 */
	public final static void close(Reader rd) {
		if (rd != null) {
			try {
				rd.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * No exception <code>java.io.Writer</code> close method.
	 */
	public final static void close(Writer wr) {
		if (wr != null) {
			try {
				wr.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Get exception stack trace.
	 */
	public final static String getStackTrace(Throwable ex) {
		String result = "";
		if (ex != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				ex.printStackTrace(pw);
				pw.close();
				sw.close();
				result = sw.toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Copy chars from a <code>Reader</code> to a <code>Writer</code>.
	 * 
	 * @param bufferSize Size of internal buffer to use.
	 */
	public final static void copy(Reader input, Writer output, int bufferSize) throws IOException {
		char buffer[] = new char[bufferSize];
		int n = 0;
		while ((n = input.read(buffer)) != -1) {
			output.write(buffer, 0, n);
		}
	}

	/**
	 * Copy chars from a <code>InputStream</code> to a <code>OutputStream</code> .
	 * 
	 * @param bufferSize Size of internal buffer to use.
	 */
	public final static int copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
		byte buffer[] = new byte[bufferSize];
		int total = 0;
		int n = 0;
		while ((n = input.read(buffer)) != -1) {
			total += n;
			output.write(buffer, 0, n);
		}
		return total;
	}

	/**
	 * Read fully from reader
	 */
	public final static String readFully(Reader reader) throws IOException {
		StringWriter writer = new StringWriter();
		copy(reader, writer, 1024);
		return writer.toString();
	}

	/**
	 * Read fully from stream
	 * @return content of stream
	 */
	public final static String readFully(InputStream input) throws IOException {
		StringWriter writer = new StringWriter();
		InputStreamReader reader = new InputStreamReader(input);
		copy(reader, writer, 1024);
		return writer.toString();
	}

	/**
	 * 文件(目录)重新命名
	 * 
	 * @param file      要处理的文件或目录
	 * @param newName   修改后的文件名（不含路径）。
	 * @param overwrite 覆盖模式，如果目标文件已经存在，则删除目标文件后再改名
	 * @return 如果成功改名，返回改名后的file对象，否则返回null。
	 */
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

	public static void createFolder(String path) {
		createFolder(new File(path));
	}

	public static void createFolder(File file) {
		if (file.exists() && file.isFile()) {
			throw new RuntimeException("Duplicate name file exist. can't create directory " + file.getPath());
		} else if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * 
	 * @param file
	 * @param iss
	 * @throws IOException
	 */
	public static void saveAsFile(File file, InputStream... iss) throws IOException {
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
	 * @param texts
	 * @param file
	 * @throws IOException
	 */
	public static void saveAsFile(File file, String... texts) throws IOException {
		saveAsFile(file, null, texts);
	}

	public static void saveAsFile(File file, Charset charset, String... texts) throws IOException {
		BufferedWriter os = getWriter(file, charset == null ? null : charset, false);
		try {
			for (String text : texts) {
				os.write(text);
			}
		} finally {
			if (os != null) {
				os.flush();
				os.close();
			}
		}
	}

	public static BufferedWriter getWriter(File target, Charset charSet, boolean append) {
		ensureParentFolder(target);
		try {
			OutputStream os = new FileOutputStream(target, append);
			if (charSet == null)
				charSet = Charset.defaultCharset();
			OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
			return new BufferedWriter(osw);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
			throw new RuntimeException(f.getAbsolutePath() + " is a exist file, can't create directory.");
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
	 * @param data
	 */
	public static Object deserialize(byte[] data) {
		return loadObject(new ByteArrayInputStream(data));
	}



	
	/**
	 * 将可序列化的对象存储到流中
	 * 
	 * @param obj
	 * @param output
	 */
	public static boolean saveObject(Serializable obj, OutputStream output) {
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(output);
			out.writeObject(obj);
			return true;
		} catch (IOException ex) {
			log.error("error", ex);
			return false;
		} finally {
			closeQuietly(out);
		}
	}

	/**
	 * 将可序列化的对象转换到字节数组
	 * 
	 * @param obj
	 */
	public static byte[] saveObject(Serializable obj) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(2048);
		try {
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			out.writeObject(obj);
			closeQuietly(out);
			return bytes.toByteArray();
		} catch (IOException e) {
			throw Exceptions.illegalState("IO error", e);
		}
	}
	

	/**
	 * 将可序列化的对象保存到磁盘文件
	 * 
	 * @param obj
	 * @param file
	 */
	public static boolean saveObject(Serializable obj, File file) {
		try {
			ensureParentFolder(file);
			return saveObject(obj, new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			log.error("IO error", e);
			return false;
		}
	}

	/**
	 * 从流读取序列化对象
	 * 
	 * @param inn
	 * @return Object
	 */
	public static Object loadObject(InputStream inn) {
		try {
			ObjectInputStream in = (inn instanceof ObjectInputStream) ? (ObjectInputStream) inn
					: new ObjectInputStream(inn);
			Object obj = in.readObject();
			return obj;
		} catch (ClassNotFoundException | IOException e) {
			log.error("IO error", e);
		} finally {
			closeQuietly(inn);
		}
		return null;
	}

	public static Object loadObject(byte[] objFile) {
		return loadObject(new ByteArrayInputStream(objFile));
	}

	/**
	 * 得到文件的扩展名（小写如果没有则返回空字符串）。如果传入的文件名包含路径，分析时会考虑最后一个\或/字符后满的部分才作为文件名。
	 * 
	 * @param fileName
	 */
	public static String getExtName(String fileName) {
		int dashIndex1 = fileName.lastIndexOf('/');
		int dashIndex2 = fileName.lastIndexOf('\\');
		int dash = Math.max(dashIndex1, dashIndex2);// 获得最后一个斜杠的位置

		int pos = fileName.lastIndexOf(".");
		if (pos > -1 && pos > dash) {
			return fileName.substring(pos + 1).toLowerCase();
		} else {
			return "";
		}
	}

	/**
	 * 得到文件名除去扩展名的部分。如果传入的文件名包含路径，分析时会考虑最后一个\或/字符后满的部分才作为文件名。 去除扩展名后返回包含路径的部分。
	 * 
	 * @param fileName
	 */
	public static String removeExt(String fileName) {
		int dashIndex1 = fileName.lastIndexOf('/');
		int dashIndex2 = fileName.lastIndexOf('\\');
		int dash = Math.max(dashIndex1, dashIndex2);// 获得最后一个斜杠的位置
		int pos = fileName.lastIndexOf('.');
		if (pos > -1 && pos > dash) {
			return fileName.substring(0, pos);
		}
		return fileName;
	}

	public static byte[] toByteArray(URL url)  {
		try (InputStream in = url.openStream()) {
			return toByteArray(in,-1);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] toByteArray(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			return toByteArray(in,-1);
		}
	}

//	/**
//	 * 读取流数据到内存。注意这个方法会将数据流全部读入到内存中，因此不适用于很大的数据对象
//	 * 
//	 * @param in
//	 * @throws IOException
//	 */
//	public static byte[] toByteArray(InputStream in) throws IOException {
//		try {
//			byte[] msg = toByteArray(in, -1);
//			return msg;
//		} finally {
//			in.close();
//		}
//	}
	
	/**
	 * 读取流数据到内存。注意这个方法会将数据流全部读入到内存中，因此不适用于很大的数据对象
	 * 
	 * @param in
	 * @return byte array of the content. 
	 */
	public static byte[] toByteArray(InputStream in) {
		byte[] msg = toByteArray(in, -1);
		return msg;

	}

	/**
	 * 从流中读取指定的字节数，第三个版本，性能再度提升 参考数据，从120M文件中读取前60M，此方法耗时125ms,v2耗时156ms
	 * @param in
	 * @param length 要读取的字节数，-1表示不限制。
	 */
	public static byte[] toByteArray(InputStream in, int length) {
		byte[] message;
		int buf = 2048;
		byte[] pBuffer = new byte[buf];
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(length > 0 ? length : 1024)) {
			int n;
			if(length<0) {
				while((n=in.read(pBuffer))>=0) {
					out.write(pBuffer, 0, n);
				}
			}else {
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
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return message;
	}

	private static final File[] EMPTY = new File[0];

	/**
	 * 递归列出目录下文件。可以指定扩展名。
	 * 
	 * @param file     要搜索的目录
	 * @param extnames 需要的文件类型（扩展名）。要求小写，无需带'.'符号。
	 * @return 该目录下符合指定类型的所有文件(只搜索一层，不会递归搜索)。<strong>仅列出文件，不会返回目录</strong>
	 */
	public static File[] listFiles(File file, final String... extnames) {
		File[] r = file.listFiles(new FileFilter() {
			public boolean accept(File f) {
				boolean isAll = extnames.length == 0;
				if (f.isFile() && (isAll || ArrayUtils.contains(extnames, getExtName(f.getName())))) {
					return true;
				}
				return false;
			}
		});
		return r == null ? EMPTY : r;
	}

	/**
	 * 列出指定目录下的文件夹
	 * 
	 * @param root 指定目录
	 * @return 该目录下的所有文件夹
	 */
	public static File[] listFolders(File root) {
		File[] r = root.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return false;
			}
		});
		return r == null ? EMPTY : r;
	}

	/**
	 * 将文件移动到指定目录下
	 * 
	 * @param file       文件
	 * @param folder     目标文件夹
	 * @param autoEscape 如果存在同名文件，则自动改名
	 */
	public static boolean moveToFolder(File file, File folder, boolean autoEscape) {
		if (folder.exists() && folder.isFile()) {
			throw new IllegalArgumentException("Target is a file.(" + folder.getAbsolutePath() + ")");
		}
		if (!folder.exists())
			folder.mkdirs();
		File target = new File(folder, file.getName());
		if (target.exists()) {
			if (autoEscape) {
				target = escapeExistFile(target);
			} else {
				return false;
			}
		}
		if (file.equals(target)) {
			return true;
		}
		return move(file, target);
	}

	/**
	 * 将文件移动为指定的新文件
	 * 
	 * @param oldFile
	 * @param newFile
	 */
	public static boolean move(File oldFile, File newFile) {
		Assert.notNull(oldFile, "source file is null!");
		Assert.notNull(newFile, "target file is null!");
		Assert.isTrue(oldFile.exists(), "source file [" + oldFile.getAbsolutePath() + "] doesn't exist.");
		Assert.isFalse(newFile.exists(), "target file already exist!");
		ensureParentFolder(newFile);
		return oldFile.renameTo(newFile);
	}

	/**
	 * 删除整个文件夹树
	 * 
	 * @param f          要删除的文件或文件夹
	 * @param includeSub 如果为false,那么如果目录非空，将不删除。返回false
	 * @return 成功删除返回true,没成功删除返回false。 如果文件夹一开始就不存在，也返回true。
	 */
	public static boolean deleteTree(File f, boolean includeSub) {
		Assert.notNull(f);
		if (!f.exists())
			return true;
		if (includeSub && f.isDirectory()) {
			for (File sub : listFolders(f)) {
				if (!deleteTree(sub, true))
					return false;
			}
			for (File sub : listFiles(f)) {
				if (!sub.delete())
					return false;
			}
		}
		return f.delete();
	}

	/**
	 * 将内存数据块写入文件
	 * 
	 * @param data
	 * @param file
	 * @throws IOException
	 */
	public static void saveAsFile(File file, boolean append, byte[] data) throws IOException {
		ensureParentFolder(file);
		OutputStream out = new FileOutputStream(file, append);
		try {
			out.write(data);
		} finally {
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	/**
	 * 将指定的流保存为临时文件
	 * 
	 * @param is
	 * @throws IOException
	 */
	public static File saveAsTempFile(InputStream is) throws IOException {
		File f = File.createTempFile("~tmp", ".io");
		saveAsFile(f, is);
		return f;
	}

	public static InputStream getInputStream(File file) throws FileNotFoundException {
		return new FileInputStream(file);
	}

	public static OutputStream getOutputStream(File output) throws FileNotFoundException {
		return new FileOutputStream(output);
	}

	/**
	 * 获得配置文件的项目。配置文件用= :等分隔对，语法同properties文件
	 * <p>
	 * 使用此方法可以代替使用JDK中的{@link java.util.Properties}工具。
	 * @param in 要读取的资源
	 * @return 文件中的键值对信息。
	 */
	public static Map<String, String> loadProperties(URL in) {
		return loadProperties(in, false);
	}

	public static Map<String, String> loadProperties(URL in, Boolean supportSection) {
		Assert.notNull(in);
		Map<String, String> result = new LinkedHashMap<String, String>();
		try(BufferedReader reader=new BufferedReader(new InputStreamReader(in.openStream()))){
			loadProperties(reader, result, supportSection);	
		} catch (IOException e) {
			throw Exceptions.toRuntime(e);
		}
		return result;
	}

	private static final void loadProperties(BufferedReader in, Map<String, String> map, Boolean supportSecion) {
		try {
			load0(new LineReader(in), map, supportSecion);
		} catch (Exception e) {
			log.error("load error", e);
		} finally {
			closeQuietly(in);
		}
	}
	
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
	
	static final class LineReader {
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
}
