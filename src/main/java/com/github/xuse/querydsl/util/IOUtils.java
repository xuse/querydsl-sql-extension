package com.github.xuse.querydsl.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
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
	 * 将内存数据块写入文件
	 * 
	 * @param file
	 * @param data
	 * @throws IOException
	 */
	public static void saveAsFile(File file, byte[] data) throws IOException {
		saveAsFile(file, false, data);
	}

	private static final int DEFAULT_BUFFER_SIZE = 4096;

	/**
	 * 从流中读取指定的字节，第三个版本，性能再度提升 参考数据，从120M文件中读取前60M，此方法耗时125ms,v2耗时156ms
	 * 
	 * @param in
	 * @param length 要读取的字节数，-1表示不限制。（注意实际处理中-1的情况下最多读取2G数据，超过2G不会读取）
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream in, int length) throws IOException {
		ByteArrayOutputStream out;
		if (length > 0) {
			out = new ByteArrayOutputStream(length);
		} else {
			out = new ByteArrayOutputStream(1024);
		}
		int buf = DEFAULT_BUFFER_SIZE;
		byte[] pBuffer = new byte[buf];
		int left = (length > 0) ? length : Integer.MAX_VALUE;// 剩余字节数
		while (left >= buf) {
			int n = in.read(pBuffer);
			if (n == -1) {
				left = 0;
				break;
			}
			left -= n;
			out.write(pBuffer, 0, n);
		}
		while (left > 0) {
			int n = in.read(pBuffer, 0, left);
			if (n == -1) {
				break;
			}
			left -= n;
			out.write(pBuffer, 0, n);
		}
		out.close();// ByteArrayOut其实是不需要close的，这里close是为了防止一些代码检查工具提出警告
		byte[] message = out.toByteArray();
		return message;
	}

	/**
	 * 读取流数据到内存。注意这个方法会将数据流全部读入到内存中，因此不适用于很大的数据对象
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(InputStream in) throws IOException {
		byte[] msg = toByteArray(in, -1);
		return msg;

	}

}
