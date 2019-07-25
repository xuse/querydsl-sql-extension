package com.github.xuse.querydsl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
}
