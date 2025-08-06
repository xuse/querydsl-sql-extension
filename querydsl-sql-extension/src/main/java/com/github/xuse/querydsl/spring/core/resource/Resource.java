package com.github.xuse.querydsl.spring.core.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import com.github.xuse.querydsl.asm.ClassReader;
import com.github.xuse.querydsl.util.IOUtils;

public interface Resource {

	InputStream getInputStream() throws IOException;

	boolean exists();

	boolean isReadable();

	boolean isOpen();

	URL getURL();

	URI getURI() throws IOException;

	File getFile() throws IOException;

	long contentLength() throws IOException;

	long lastModified() throws IOException;

	Resource createRelative(String relativePath) throws IOException;

	String getFilename();

	String getDescription();

	boolean isFile();
	
	default ClassReader toClassReader() {
		try(InputStream in=getInputStream()){
			ClassReader cl = new ClassReader(IOUtils.toByteArray(in));
			return cl;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
